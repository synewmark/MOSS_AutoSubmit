package git_handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnCheckout;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;

public class GitDownloader {
	// instantiate directory file as global to perform cleanup
	private File directory;
	private final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
	private final SvnCheckout checkout = svnOperationFactory.createCheckout();

	public GitDownloader setCredentials(char[] oauthToken) {
		// despite potential appearances, SvnCheckout does remain linked to the relevant
		// instance of SvnOperationFactory so setAuthenticationManager can be called
		// after createCheckout without issue
		svnOperationFactory
				.setAuthenticationManager(SVNWCUtil.createDefaultAuthenticationManager("${token}", oauthToken));
		return this;
	}

	public GitDownloader setDownloadSource(String urlToDownload) {
		try {
			checkout.setSource(SvnTarget.fromURL(SVNURL.parseURIEncoded(gitURLtoSVN(urlToDownload))));
		} catch (SVNException e) {
			throw new IllegalArgumentException("URL " + urlToDownload + " is malformed!", e);
		}
		return this;
	}

	public GitDownloader setOutputFolder(File directory) {
		directory.mkdirs();
		if (!directory.canWrite()) {
			throw new IllegalArgumentException("Cannot write to " + directory);
		}
		checkout.setSingleTarget(SvnTarget.fromFile(directory));
		this.directory = directory;
		return this;
	}

	public void execute() {
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
	}

	public static void downloadGitRepo(String urlToDownload, File directory) {
		new GitDownloader().setOutputFolder(directory).setDownloadSource(urlToDownload).execute();
	}

	public static File downloadGitRepo(String urlToDownload) {
		File tempDirectory = createTempDirectory();
		new GitDownloader().setOutputFolder(tempDirectory).setDownloadSource(urlToDownload).execute();
		return tempDirectory;
	}

	public static void downloadGitRepo(String urlToDownload, File directory, char[] oauthToken) {
		new GitDownloader().setCredentials(oauthToken).setOutputFolder(directory).setDownloadSource(urlToDownload)
				.execute();
	}

	public static File downloadGitRepo(String urlToDownload, char[] oauthToken) {
		File tempDirectory = createTempDirectory();
		new GitDownloader().setCredentials(oauthToken).setOutputFolder(tempDirectory).setDownloadSource(urlToDownload)
				.execute();
		return tempDirectory;
	}

	private static String gitURLtoSVN(String url) {
		// pattern matches any substring starting with "tree/" until and including the
		// next '/' and replaces it with "trunk/"
		// see
		// https://stackoverflow.com/questions/7106012/download-a-single-folder-or-directory-from-a-github-repo
		return url.replaceFirst("tree/.*/", "trunk/");
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
}
