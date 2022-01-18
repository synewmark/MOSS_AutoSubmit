package gitHandler;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import utils.FileUtils;

public class GitHandlerMiddleManager extends GitHandlerAbstract {
	// urlToDownload, dateToDownload, directoryToDownloadTo, oauthToken,
	// and explicitFilesToDownload params declared in GitHandlerAbstract

	private boolean usingAPI = false;

	@Override
	public void execute() throws IOException {
		GitHandlerAbstract gitDownload = getProperGitAbstractInstance();
		passGitHandlerFieldsTo(gitDownload);
		try {
			gitDownload.execute();
		} catch (IOException e) {
			if (GitDownloaderViaAPIRequests.getRateRemaing(oauthToken) == 0 && usingAPI) {
				System.err.println("Ran out of API requests mid request: " + urlToDownload.getUsername() + '/'
						+ urlToDownload.getRepoName());
				System.err.println("Wiping directory and retrying with jGitClone");
				FileUtils.deleteDir(directoryToDownloadTo);
				execute();
			} else {
				throw new IOException(e);
			}
		}

		if (!usingAPI) {
			cleanupDirectory();
		}
	}

	private void cleanupDirectory() throws IOException {
		File directoryToRepo = new File(directoryToDownloadTo,
				this.urlToDownload.getUsername() + File.separator + this.urlToDownload.getRepoName());

		FileUtils.deleteDir(new File(directoryToRepo, ".git"));

		if (!urlToDownload.getPath().isEmpty()) {
			File dirToKeep = new File(directoryToRepo, urlToDownload.getPath());
			FileFilter filter = (File file) -> !dirToKeep.equals(file);
			FileUtils.deleteDirExclude(directoryToRepo, filter);
		}

		if (explicitFilesToDownload != null) {
			Set<File> set = new HashSet<>((int) (explicitFilesToDownload.size() * 0.75) + 1);
			for (String specificFile : explicitFilesToDownload) {
				File relative = new File(urlToDownload.getPath(), specificFile);
				File fileToAdd = new File(directoryToRepo, relative.toString().replaceAll("\\||/", File.separator));
				set.add(fileToAdd);
			}
			FileFilter filter = (File file) -> !set.contains(file);
			FileUtils.deleteDirExclude(new File(directoryToDownloadTo, urlToDownload.getUsername() + File.separator
					+ urlToDownload.getRepoName() + File.separator + urlToDownload.getPath()), filter);
		}

	}

	private GitHandlerAbstract getProperGitAbstractInstance() {
		int rateRemaining = 0;
		try {
			rateRemaining = GitDownloaderViaAPIRequests.getRateRemaing(oauthToken);
		} catch (IOException e) {
			return new GitDownloaderViaJGitClone();
		}
		if ((explicitFilesToDownload != null || !urlToDownload.getPath().isEmpty()) && rateRemaining > 40) {
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

}
