package gitHandler;

import java.io.IOException;

public class GitHandlerMiddleManager extends GitHandlerAbstract {
	static double currentCost = 0;

	private boolean usingAPI = false;

	@Override
	public void execute() throws IOException {
		GitHandlerAbstract gitDownload = getProperGitAbstractInstance();
		passGitHandlerFieldsTo(gitDownload);
		int currentRequestsRemaining = 0;
		currentRequestsRemaining = GitDownloaderViaAPIRequests.getRateRemaing(oauthToken);
		gitDownload.execute();
		if (usingAPI) {
			currentCost = currentRequestsRemaining - GitDownloaderViaAPIRequests.getRateRemaing(oauthToken);
		}

	}

	private GitHandlerAbstract getProperGitAbstractInstance() {
		int rateRemaining = 0;
		try {
			rateRemaining = GitDownloaderViaAPIRequests.getRateRemaing(oauthToken);
		} catch (IOException e) {
			return new GitHandlerViaJGitClone();
		}
		if (explicitFilesToDownload != null && rateRemaining > 40 && rateRemaining * 2 > currentCost) {
			usingAPI = true;
			return new GitDownloaderViaAPIRequests();
		} else {
			return new GitHandlerViaJGitClone();
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
