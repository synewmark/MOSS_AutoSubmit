package runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

import gitHandler.GitHandlerAbstract;
import gitHandler.GitHandlerMiddleManager;
import gitHandler.model.URLbyComponents;
import utils.FileUtils;

public class GitRunner {

	private String username;
	private File pathToRepoNameFile;
	private String subdirectory = "";
	private String branch;
	private char[] oauthToken;
	private File directoryToDownloadTo;
	private LocalDateTime timestamp = LocalDateTime.now();
	private File pathToListOfFilesToDownload;

	private static final String gitHost = "https://github.com/";

	public GitRunner(Enviroment enviroment) {
		vertifyAndPassParameters(enviroment);
	}

	private void vertifyAndPassParameters(Enviroment enviroment) {
		this.oauthToken = enviroment.gitAPI;
		this.username = enviroment.username;
		this.pathToRepoNameFile = enviroment.repoPath;
		this.branch = enviroment.branch;
		this.subdirectory = enviroment.subdirectory;
		this.pathToListOfFilesToDownload = enviroment.filesToDownload;
		this.timestamp = enviroment.timestamp;
		checkNullValues();
	}

	private void checkNullValues() {

		if (pathToRepoNameFile == null) {
			throw new IllegalArgumentException("Repo name file not set");
		}

		if (!pathToRepoNameFile.canRead()) {
			throw new IllegalArgumentException("Cannot read from repo name file: " + pathToRepoNameFile);
		}

		if (pathToListOfFilesToDownload != null && !pathToListOfFilesToDownload.canRead()) {
			throw new IllegalArgumentException("Cannot read from repo name file: " + pathToRepoNameFile);
		}

		if (username == null || username.isEmpty()) {
			throw new IllegalStateException("Git Username is not set");
		}

		if (pathToRepoNameFile == null) {
			throw new IllegalStateException("Git Repo Path is not set");
		}

		if (branch == null || branch.isEmpty()) {
			throw new IllegalStateException("Git Branch is not set");
		}

		if (directoryToDownloadTo == null) {
			try {
				directoryToDownloadTo = Files.createTempDirectory("MOSSAutoSubmit_GitFiles").toFile();
			} catch (IOException e) {
				throw new IllegalStateException(
						"Cannot create temp directory for you, please specificy a valid directory and try again");
			}
		}
		if (!directoryToDownloadTo.canWrite()) {
			throw new IllegalArgumentException("Cannot write to download directory: " + pathToRepoNameFile);
		}
	}

	public File execute() throws IOException {
		List<String> filesToDownload = null;
		if (pathToListOfFilesToDownload != null) {
			filesToDownload = FileUtils.getListOfStringsFromFile(pathToListOfFilesToDownload);
		}
		List<String> listOfRepos = FileUtils.getListOfStringsFromFile(pathToRepoNameFile);
		boolean success = false;
		for (String repo : listOfRepos) {

			try {
				URLbyComponents url = new URLbyComponents(gitHost, username, repo, subdirectory, branch);

				System.out.println(
						"Downloading files from: " + (url.getPath().equals("") ? url.getBareURL() : url.getFullURL())
								+ " to directory: " + directoryToDownloadTo);
				GitHandlerAbstract gitHandler = new GitHandlerMiddleManager().setURL(url).setDir(directoryToDownloadTo)
						.setDate(timestamp);
				if (oauthToken.length > 0) {
					gitHandler.setCredentials(oauthToken);
				}
				if (filesToDownload != null) {
					gitHandler.setFilesToDownload(filesToDownload);

				}
				gitHandler.execute();
				success = true;
			} catch (FileNotFoundException e) {
				System.err.println("Failed to download repo: " + repo);
				System.err.println("Operation failed with the message: " + e.getMessage());
			}

		}

		if (!success) {
			throw new IOException("0 Git downloads completed succesfully - check error log");
		}

		return directoryToDownloadTo;
	}
}
