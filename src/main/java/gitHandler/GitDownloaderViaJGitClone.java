package gitHandler;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class GitDownloaderViaJGitClone extends GitHandlerAbstract {
	// urlToDownload, dateToDownload, directoryToDownloadTo, oauthToken,
	// and explicitFilesToDownload params declared in GitHandlerAbstract

	@Override
	public void execute() throws IOException {
		this.directoryToDownloadTo = new File(this.directoryToDownloadTo, this.urlToDownload.getRepoName());

		CloneCommand clone = new CloneCommand().setDirectory(directoryToDownloadTo)
				.setURI(this.urlToDownload.getBareURL().toString());

		clone.setBranch(urlToDownload.getBranch());

		if (this.oauthToken != null) {
			clone.setCredentialsProvider(new UsernamePasswordCredentialsProvider("${token}", oauthToken));
		}
		try (Git git = clone.call();) {
			// this or revwalk...
			if (dateToDownload != null) {
				String hash = GitDownloaderViaAPIRequests.getGitCommitForDate(urlToDownload, dateToDownload,
						oauthToken);
				CheckoutCommand checkout = git.checkout();
				checkout.setName(hash);
				checkout.call();
			}
		} catch (GitAPIException e) {
			throw new IOException(e);
		}
	}
}
