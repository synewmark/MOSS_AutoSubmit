package gitHandler;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
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

		if (this.oauthToken != null && oauthToken.length > 0) {
			clone.setCredentialsProvider(new UsernamePasswordCredentialsProvider("${token}", oauthToken));
		}
		try (Git git = clone.call();) {
			// this or revwalk...
			if (dateToDownload != null) {
				String hash = getHashForDate(git.getRepository(), dateToDownload);
				CheckoutCommand checkout = git.checkout();
				checkout.setName(hash);
				checkout.call();
			}
		} catch (GitAPIException e) {
			throw new IOException(e);
		}
	}

	private String getHashForDate(Repository repo, LocalDateTime ldt) throws IOException {
		try (RevWalk walk = new RevWalk(repo)) {
			walk.markStart(walk.parseCommit(repo.resolve(Constants.HEAD)));
			walk.sort(RevSort.COMMIT_TIME_DESC);
			for (RevCommit commit : walk) {
				if (commit.getCommitTime() <= ldt.toEpochSecond(ZoneOffset.UTC)) {
					return commit.name();
				}
			}
		}
		throw new IOException("Could not find repo with timestamp");
	}
}
