package gitHandler;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GitHandlerMiddleManager extends GitHandlerAbstract {
	// urlToDownload, dateToDownload, directoryToDownloadTo, oauthToken,
	// and explicitFilesToDownload params declared in GitHandlerAbstract

	static double currentCost = 0;

	private boolean usingAPI = false;

	@Override
	public void execute() throws IOException {
		GitHandlerAbstract gitDownload = getProperGitAbstractInstance();
		passGitHandlerFieldsTo(gitDownload);
		int currentRequestsRemaining = 0;
		currentRequestsRemaining = GitDownloaderViaAPIRequests.getRateRemaing(oauthToken);
		try {
			gitDownload.execute();
		} catch (IOException e) {
			if (GitDownloaderViaAPIRequests.getRateRemaing(oauthToken) == 0 && explicitFilesToDownload != null) {
				System.out.println("Ran out of API requests mid request: " + urlToDownload.getUsername() + '/'
						+ urlToDownload.getRepoName());
				System.out.println("Wiping directory and retrying with jGitClone");
				deleteDirectoryExcludeExplicitFiles(directoryToDownloadTo, null);
				execute();
			} else {
				throw new IOException(e);
			}
		}

		if (usingAPI) {
			currentCost = currentRequestsRemaining - GitDownloaderViaAPIRequests.getRateRemaing(oauthToken);
		} else if (explicitFilesToDownload != null) {
			Set<File> set = new HashSet<>((int) (explicitFilesToDownload.size() * 0.75) + 1);
			for (String string : explicitFilesToDownload) {
				File fileToAdd = new File(directoryToDownloadTo,
						urlToDownload.getUsername() + File.separator + urlToDownload.getRepoName() + File.separator
								+ urlToDownload.getPath() + File.separator + string);
				set.add(fileToAdd);
			}
			FileFilter filter = (File file) -> !set.contains(file);
			deleteDirectoryExcludeExplicitFiles(
					new File(directoryToDownloadTo, urlToDownload.getUsername() + File.separator + File.separator
							+ urlToDownload.getRepoName() + File.separator + urlToDownload.getPath()),
					filter);
		}

	}

	private GitHandlerAbstract getProperGitAbstractInstance() {
		int rateRemaining = 0;
		try {
			rateRemaining = GitDownloaderViaAPIRequests.getRateRemaing(oauthToken);
		} catch (IOException e) {
			return new GitDownloaderViaJGitClone();
		}
		if (explicitFilesToDownload != null && rateRemaining > 40) {
			usingAPI = true;
			return new GitDownloaderViaAPIRequests();
		} else {
			return new GitDownloaderViaJGitClone();
		}
	}

	private void passGitHandlerFieldsTo(GitHandlerAbstract other) {
		other.setURL(urlToDownload);
		other.setDate(dateToDownload);
		other.setDir(directoryToDownloadTo);
		other.setCredentials(oauthToken);
		other.setFilesToDownload(explicitFilesToDownload);
	}

	private void deleteDirectoryExcludeExplicitFiles(File fileToDelete, FileFilter filter) throws IOException {
		if (fileToDelete.isDirectory()) {
			File[] files = fileToDelete.listFiles(filter);
			if (files != null) {
				for (File file : files) {
					deleteDirectoryExcludeExplicitFiles(file, filter);
				}
			}
		}
		fileToDelete.delete();
	}
}
