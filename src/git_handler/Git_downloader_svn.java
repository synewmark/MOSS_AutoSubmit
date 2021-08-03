package git_handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnCheckout;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;

public class Git_downloader_svn {
	// instantiate directory file on Object to perform cleanup
	private File directory;
	private final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
	private final SvnCheckout checkout = svnOperationFactory.createCheckout();

	public Git_downloader_svn setCredentials(char[] oauthToken) {
		// despite potential appearances, SvnCheckout does remain linked to the relevant
		// instance of SvnOperationFactory
		// setAuthenticationManager can be called after createCheckout without issue
		svnOperationFactory
				.setAuthenticationManager(SVNWCUtil.createDefaultAuthenticationManager("${token}", oauthToken));
		return this;
	}

	public Git_downloader_svn setDownloadSource(String urlToDownload) {
		try {
			checkout.setSource(SvnTarget.fromURL(SVNURL.parseURIEncoded(gitURLtoSVN(urlToDownload))));
		} catch (SVNException e) {
			throw new IllegalArgumentException("URL " + urlToDownload + " is malformed!", e);
		}
		return this;
	}

	public Git_downloader_svn setOutputFolder(File directory) {
		if (!directory.mkdirs() || !directory.canWrite()) {
			throw new IllegalArgumentException("Cannot write to " + directory);
		}
		checkout.setSingleTarget(SvnTarget.fromFile(directory));
		this.directory = directory;
		return this;
	}

	public void execute() {
		try {
			checkout.run();
			delete(new File(directory.toString() + File.separatorChar + ".svn"));
		} catch (SVNException e) {
			// TODO: instance check exceptions and rewrap solvable ones into other
			// exceptions with more detailed error messages
			e.printStackTrace();
		}
	}

	public static void downloadGitRepo(String urlToDownload, File directory) {
		new Git_downloader_svn().setOutputFolder(directory).setDownloadSource(urlToDownload).execute();
	}

	public static File downloadGitRepo(String urlToDownload) {
		File tempDirectory = null;
		try {
			tempDirectory = Files.createTempDirectory("").toFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		new Git_downloader_svn().setOutputFolder(tempDirectory).setDownloadSource(urlToDownload).execute();
		return tempDirectory;
	}

	public static void downloadGitRepo(String urlToDownload, File directory, char[] oauthToken) {
		new Git_downloader_svn().setCredentials(oauthToken).setOutputFolder(directory).setDownloadSource(urlToDownload)
				.execute();
	}

	public static File downloadGitRepo(String urlToDownload, char[] oauthToken) {
		File tempDirectory = null;
		try {
			tempDirectory = Files.createTempDirectory("").toFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		new Git_downloader_svn().setCredentials(oauthToken).setOutputFolder(tempDirectory)
				.setDownloadSource(urlToDownload).execute();
		return tempDirectory;
	}

	private static String gitURLtoSVN(String url) {
		// pattern matches any substring starting with "tree/" until and including the
		// next '/' and replaces it with "trunk"
		// see
		// https://stackoverflow.com/questions/7106012/download-a-single-folder-or-directory-from-a-github-repo
		return url.replaceFirst("tree/.*/", "trunk/");
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
