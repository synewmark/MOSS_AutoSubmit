package gitHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import gitHandler.model.GitFileRepresentation;
import gitHandler.model.URLbyComponents;

public class GitDownloaderViaAPIRequests extends GitHandlerAbstract {
	// urlToDownload, dateToDownload, directoryToDownloadTo, oauthToken,
	// and explicitFilesToDownload params declared in GitHandlerAbstract
	private static Gson gson = new Gson();

	private Queue<GitFileRepresentation> trees = new ArrayDeque<>();
	private Queue<GitFileRepresentation> files = new ArrayDeque<>();
	private Queue<GitFileRepresentation> directory = new ArrayDeque<>();

	@Override
	public void execute() throws IOException {
		if (urlToDownload == null) {
			throw new IllegalArgumentException("URL must be set");
		}
		if (directoryToDownloadTo == null) {
			throw new IllegalArgumentException("Directory must be set");
		}
		if (dateToDownload == null) {
			dateToDownload = LocalDateTime.now();
		}
		this.directoryToDownloadTo = new File(this.directoryToDownloadTo,
				this.urlToDownload.getUsername() + File.separator + this.urlToDownload.getRepoName());

		if (explicitFilesToDownload != null) {
			downloadGitWithDeclaredFiles(urlToDownload, dateToDownload, directoryToDownloadTo, oauthToken,
					explicitFilesToDownload);
		} else {
			downloadGit(urlToDownload, dateToDownload, directoryToDownloadTo, oauthToken);
		}
	}

	public void downloadGit(URLbyComponents urlish, LocalDateTime date, File directory, char[] oauthToken)
			throws IOException {
		String gitHash = getGitCommitForDate(urlish, date, oauthToken);
		populateDataStructures(urlish, gitHash, oauthToken);
		createDirectoryStructue(directory);
		downloadFiles(directory, oauthToken);
	}

	public void downloadGitWithDeclaredFiles(URLbyComponents urlish, LocalDateTime date, File directory,
			char[] oauthToken, Collection<String> explicitFilesToDownload) throws IOException {
		String gitHash = getGitCommitForDate(urlish, date, oauthToken);
		populateDataStructuresWithExplicitFilesAndCreateDir(urlish, gitHash, directory, explicitFilesToDownload);
		downloadFiles(directory, oauthToken);
	}

	private void createDirectoryStructue(File baseDir) {
		baseDir.mkdirs();
		new File(baseDir, directory.poll().getPath()).mkdirs();
		while (!directory.isEmpty()) {
			new File(baseDir, directory.poll().getPath()).mkdir();
		}
	}

	private void downloadFiles(File baseDir, char[] oauthToken) throws IOException {
		while (!files.isEmpty()) {
			GitFileRepresentation fileToDownload = files.poll();
			downloadFromURL(new URL(fileToDownload.getDownload_url()), new File(baseDir, fileToDownload.getPath()),
					(fileToDownload.getSize() == 0 ? Integer.MAX_VALUE : fileToDownload.getSize()));
		}
	}

	private void downloadFromURL(URL urlToDownloadFrom, File fileToDownloadTo, int fileSize) throws IOException {
		ReadableByteChannel readableByteChannel = Channels.newChannel(urlToDownloadFrom.openStream());
		FileOutputStream fileOutputStream = new FileOutputStream(fileToDownloadTo);
		fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, fileSize);
		readableByteChannel.close();
		fileOutputStream.close();
	}

	private void populateDataStructures(URLbyComponents urlish, String gitHash, char[] oauthToken) throws IOException {
		URL url = getGitHubURLForRootContentRequests(urlish, gitHash);
		GitFileRepresentation gfr = new GitFileRepresentation();
		gfr.setUrl(url.toString());
		gfr.setPath(urlish.getPath());
		trees.add(gfr);
		while (!trees.isEmpty()) {
			GitFileRepresentation tree = trees.poll();
			directory.add(tree);
			manageGitSubDir(tree, oauthToken);
		}
	}

	private void populateDataStructuresWithExplicitFilesAndCreateDir(URLbyComponents urlish, String gitHash,
			File directoryToDownloadTo, Collection<String> explicitFilesToDownload) {

		directoryToDownloadTo.mkdirs();
		for (String file : explicitFilesToDownload) {
			file = file.replace("\\", "/");
			URLbyComponents urlToDownload;
			try {
				urlToDownload = new URLbyComponents(urlish.getHost(), urlish.getUsername(), urlish.getRepoName(),
						urlish.getPath() + '/' + file, urlish.getBranch());
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException("File: " + file + " is invalid");
			}
			URL rawURL = getGitHubRawContentURL(urlToDownload, gitHash);
			GitFileRepresentation gfr = new GitFileRepresentation();
			gfr.setDownload_url(rawURL.toString());
			gfr.setPath(urlish.getPath() + '/' + file);
			files.add(gfr);
			new File(directoryToDownloadTo, urlish.getPath() + '/' + file).getParentFile().mkdirs();
		}

	}

	private void manageGitSubDir(GitFileRepresentation dir, char[] oauthToken) throws IOException {
		URL url = new URL(dir.getUrl());
		JSONTokener tokener = getJsonFromGitGet(url, oauthToken);
		JSONArray treeArray;
		try {
			treeArray = new JSONArray(tokener);
		} catch (JSONException e) {
			throw new IllegalStateException(
					"Likely API change. Check https://docs.github.com/en/rest/reference/repos#commits", e);
		}
		for (int i = 0; i < treeArray.length(); i++) {
			try {
				GitFileRepresentation gfr = gson.fromJson(treeArray.getString(i), GitFileRepresentation.class);
				if (gfr.getType() == GitFileRepresentation.BranchType.file) {
					files.add(gfr);
				} else {
					trees.add(gfr);
				}
			} catch (JsonSyntaxException | JSONException e) {
				throw new IllegalStateException(
						"Likely API change. Check https://docs.github.com/en/rest/reference/repos#commits", e);
			}
		}
	}

	protected static String getGitCommitForDate(URLbyComponents urlish, LocalDateTime date, char[] oauthToken)
			throws IOException {
		URL url = getGitHubURLForRootRequest(urlish, date);
		String shaHash = null;
		JSONTokener tokener = getJsonFromGitGet(url, oauthToken);
		try {
			JSONArray jsonArray = new JSONArray(tokener);
			JSONObject jsonObj = jsonArray.getJSONObject(0);
			shaHash = jsonObj.getString("sha");
			if (jsonArray.length() < 1) {
				throw new IOException("No commits found before timestamp: " + date);
			}
		} catch (JSONException e) {
			throw new IllegalStateException(
					"Likely API change. Check https://docs.github.com/en/rest/reference/repos#commits", e);
		}
		return shaHash;
	}

	private static URL getGitHubURLForRootRequest(URLbyComponents urlish, LocalDateTime date) {
		try {
			return new URL("https://api.github.com/repos/" + urlish.getUsername() + '/' + urlish.getRepoName()
					+ "/commits" + "?sha=" + urlish.getBranch() + "&per_page=1" + "&until=" + date.toString());
		} catch (MalformedURLException e) {
			throw new IllegalStateException(
					"URL from this operation should *always* be valid if the URLbyComponents was validly constructed");
		}
	}

	private static URL getGitHubURLForRootContentRequests(URLbyComponents urlish, String gitName) {
		try {
			return new URL("https://api.github.com/repos/" + urlish.getUsername() + '/' + urlish.getRepoName()
					+ "/contents/" + urlish.getPath() + (gitName == null ? "" : "?=ref" + gitName));
		} catch (MalformedURLException e) {
			throw new IllegalStateException(
					"URL from this operation should *always* be valid if the URLbyComponents was validly constructed");
		}
	}

	private static URL getGitHubRawContentURL(URLbyComponents urlish, String gitHash) {
		try {
			return new URL("https://raw.githubusercontent.com/" + urlish.getUsername() + '/' + urlish.getRepoName()
					+ '/' + (gitHash != null ? gitHash : urlish.getBranch()) + urlish.getPath());
		} catch (MalformedURLException e) {
			throw new IllegalStateException(
					"URL from this operation should *always* be valid if the URLbyComponents was validly constructed");
		}
	}

	private static JSONTokener getJsonFromGitGet(URL url, char[] oauthToken) throws IOException {
		JSONTokener tokener;
		HttpURLConnection http;
		http = (HttpURLConnection) url.openConnection();
		http.setRequestProperty("Accept", "application/vnd.github.v3+json");
		if (oauthToken != null) {
			// what's even the point of ensuring the token stays as a char array if I need
			// to create a string to pass it as an HTTP property?!
			// there should be a way to pass sensitive values as a bitstream which get
			// consumed
			http.addRequestProperty("Authorization", "token " + new String(oauthToken));
		}
		int responseCode = http.getResponseCode();
		if (responseCode < 200 || responseCode >= 300) {
			throw new IOException("Invalid reponse code from the server: " + responseCode + "\n With response message: "
					+ http.getResponseMessage());
		}
		String string = new String(http.getInputStream().readAllBytes());
		tokener = new JSONTokener(string);
		http.disconnect();
		return tokener;
	}

	public static int getRateRemaing(char[] oauthToken) throws IOException {
		URL getRateLimitURL = new URL("https://api.github.com/rate_limit");
		JSONTokener tokenizer = getJsonFromGitGet(getRateLimitURL, oauthToken);

		int remaining = 0;
		try {
			JSONObject resources = new JSONObject(tokenizer);
			JSONObject rate = resources.getJSONObject("rate");
			remaining = rate.getInt("remaining");
		} catch (JSONException e) {
			throw new IllegalStateException(
					"Likely API change. Check https://docs.github.com/en/rest/reference/repos#commits", e);
		}
		return remaining;
	}
}
