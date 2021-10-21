package backendhandlers;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnCheckout;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import gitHandler.model.URLbyComponents;

public class SVNGitHandler {
	// instantiate directory file as global to perform cleanup
	private final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
	private final SvnCheckout checkout = svnOperationFactory.createCheckout();

	private File directory;
	private URLbyComponents urlToDownload;
	private char[] oauthToken;
	private LocalDateTime date;

	public SVNGitHandler setCredentials(char[] oauthToken) {
		// despite potential appearances, SvnCheckout does remain linked to the relevant
		// instance of SvnOperationFactory so setAuthenticationManager can be called
		// after createCheckout without issue
		this.oauthToken = oauthToken;
		return this;
	}

	public SVNGitHandler setDownloadSource(URLbyComponents urlToDownload) {
		this.urlToDownload = urlToDownload;
		return this;
	}

	public SVNGitHandler setDate(LocalDateTime date) {
		this.date = date;
		return this;
	}

	public SVNGitHandler setOutputFolder(File directory) {
		directory.mkdirs();
		if (!directory.canWrite()) {
			throw new IllegalArgumentException("Cannot write to: " + directory);
		}
		this.directory = directory;
		return this;
	}

	public File execute() {
		passFieldsToSVN();
		try {
			checkout.run();
			// SVNKit auto creates a .svn folder within the directory which has to be
			// deleted
			// will look into possibility of preventing that folder from being created in
			// the first place
			delete(new File(directory.toString() + File.separatorChar + ".svn"));
		} catch (SVNAuthenticationException e) {
			throw new SecurityException(
					"Authentication failed, make sure the URL and token were entered correctly and that the token's permissions cover the requested action",
					e);
		} catch (SVNException e) {
			// TODO: instance check exceptions and re-wrap solvable ones into other
			// exceptions with more detailed error messages
			e.printStackTrace();
		} finally {
			svnOperationFactory.dispose();
		}
		return directory;
	}

	private void passFieldsToSVN() {
		checkout.setSingleTarget(SvnTarget.fromFile(directory));
		svnOperationFactory
				.setAuthenticationManager(SVNWCUtil.createDefaultAuthenticationManager("${token}", oauthToken));
		try {
			checkout.setSource(SvnTarget.fromURL(SVNURL.parseURIEncoded(gitURLtoSVN(urlToDownload, date, oauthToken))));
		} catch (SVNException e) {
			throw new IllegalArgumentException("URL: " + urlToDownload + " is malformed!", e);
		}

	}

	private String gitURLtoSVN(URLbyComponents urlToDownload, LocalDateTime date, char[] oauthToken) {
		// pattern matches any substring starting with "tree/" until and including the
		// next '/' and replaces it with "trunk/"
		// see
		// https://stackoverflow.com/questions/7106012/download-a-single-folder-or-directory-from-a-github-repo
		if (date == null) {
			return urlToDownload.toString().replaceFirst("tree/.*?/", "trunk/");
		} else {
			String gitCommitHash = null;
			try {
				gitCommitHash = getGitCommitForDate(urlToDownload, date, oauthToken);
				System.out.println(gitCommitHash);
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println(urlToDownload);
			System.out.println(urlToDownload.toString().replaceFirst("tree/.*?/", "trunk/"));
			return urlToDownload.toString().replaceFirst("tree/.*?/", "trunk/");
		}
	}

	private static File createTempDirectory() {
		try {
			return Files.createTempDirectory("MOSSAutoSubmit_GitFiles").toFile();
		} catch (IOException e) {
			throw new IllegalStateException();
		}
	}

	private static void delete(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				delete(f);
			}
		}
		file.delete();
	}

	private static String getGitCommitForDate(URLbyComponents urlish, LocalDateTime date, char[] oauthToken)
			throws IOException {
		URL url = getGitHubURLForRequest(urlish, date);
		String shaHash = null;
		HttpURLConnection http;
		try {
			http = (HttpURLConnection) url.openConnection();

			http.setRequestProperty("Accept", "application/vnd.github.v3+json");
			if (oauthToken != null) {
				// what's even the point of ensuring the token stays as a char array if I need
				// to create a string to pass it as an HTTP property?!
				// there should be a way to pass sensitive values as a bitstream which gets
				// consumed
				http.addRequestProperty("${token}", new String(oauthToken));
			}
			String string = new String(url.openStream().readAllBytes());
			JSONTokener tokener = new JSONTokener(string);
			JSONArray jsonArray = new JSONArray(tokener);
			JSONObject jsonObj = jsonArray.getJSONObject(0);
			shaHash = jsonObj.getString("sha");
			http.disconnect();
		} catch (JSONException e) {
			throw new IllegalStateException(
					"Likely API change. Check https://docs.github.com/en/rest/reference/repos#commits");
		}
		return shaHash;
	}

	private static URL getGitHubURLForRequest(URLbyComponents urlish, LocalDateTime date) {
		try {
			return new URL("https://api.github.com/repos/" + urlish.getUsername() + '/' + urlish.getRepoName()
					+ "/commits" + "?per_page=1" + "&until=" + date.toString());
		} catch (MalformedURLException e) {
			throw new IllegalStateException(
					"URL from this operation should *always* be valid if the URLbyComponents was validly constructed");
		}
	}

	public static void downloadGitRepo(URLbyComponents urlToDownload, File directory) {
		new SVNGitHandler().setOutputFolder(directory).setDownloadSource(urlToDownload).execute();
	}

	public static File downloadGitRepo(URLbyComponents urlToDownload) {
		File tempDirectory = createTempDirectory();
		new SVNGitHandler().setOutputFolder(tempDirectory).setDownloadSource(urlToDownload).execute();
		return tempDirectory;
	}

	public static void downloadGitRepo(URLbyComponents urlToDownload, File directory, char[] oauthToken) {
		new SVNGitHandler().setCredentials(oauthToken).setOutputFolder(directory).setDownloadSource(urlToDownload)
				.execute();
	}

	public static File downloadGitRepo(URLbyComponents urlToDownload, char[] oauthToken) {
		File tempDirectory = createTempDirectory();
		new SVNGitHandler().setCredentials(oauthToken).setOutputFolder(tempDirectory).setDownloadSource(urlToDownload)
				.execute();
		return tempDirectory;
	}
}
