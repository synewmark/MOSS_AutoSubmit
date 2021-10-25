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
		currentRequestsRemaining = GitHandlerAPI.getRateRemaing(oauthToken);
		gitDownload.execute();
		if (usingAPI) {
			currentCost = currentRequestsRemaining - GitHandlerAPI.getRateRemaing(oauthToken);
		}

	}

	private GitHandlerAbstract getProperGitAbstractInstance() {
		int rateRemaining = 0;
		try {
			rateRemaining = GitHandlerAPI.getRateRemaing(oauthToken);
		} catch (IOException e) {
			return new GitHandlerClone();
		}
		if (explicitFilesToDownload != null && rateRemaining > 40 && rateRemaining * 2 > currentCost) {
			usingAPI = true;
			return new GitHandlerAPI();
		} else {
			return new GitHandlerClone();
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
