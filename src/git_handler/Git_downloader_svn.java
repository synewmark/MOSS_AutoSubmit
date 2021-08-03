package git_handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc2.SvnCheckout;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNWCUtil;


public class Git_downloader_svn {
	
	SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
    SvnCheckout checkout = svnOperationFactory.createCheckout();
	
	public Git_downloader_svn setCredentials(char[] oauthToken) {
		svnOperationFactory.setAuthenticationManager(SVNWCUtil.createDefaultAuthenticationManager("${token}", oauthToken));
		return this;
	}
	public Git_downloader_svn setDownloadSource(String urlToDownload) {
		try {
			checkout.setSource(SvnTarget.fromURL(SVNURL.parseURIEncoded(gitURLtoSVN(urlToDownload))));
		} catch (SVNException e) {
			throw new IllegalArgumentException("URL "+ urlToDownload+" is malformed!", e);
		}
		return this;
	}
	
	public Git_downloader_svn setOutputFolder(File directory) {
		if (!directory.canWrite()) {
			throw new IllegalArgumentException("Cannot write to "+directory.toString()+'\n'
					+ "Make sure the directory exists and you have permission to access it");
		}
		checkout.setSingleTarget(SvnTarget.fromFile(directory));
		return this;
	}
	
	public void execute() {
		try {
			checkout.run();
		} catch (SVNException e) {
			e.printStackTrace();
		}
	}
	
	public static void downloadGitRepo(String urlToDownload, File directory) {
		SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
		try {
		    SvnCheckout checkout = svnOperationFactory.createCheckout();
		    checkout.setSource(SvnTarget.fromURL(SVNURL.parseURIEncoded(gitURLtoSVN(urlToDownload))));
		    checkout.setSingleTarget(SvnTarget.fromFile(directory));
		    checkout.run();
		} catch (SVNException e) {
			e.printStackTrace();
		} finally {
		    svnOperationFactory.dispose();
		}
	}

	public static File downloadGitRepo(String urlToDownload) {
		SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
		File tempDirectory = null;
		try {
			tempDirectory = Files.createTempDirectory("random").toFile();
		    SvnCheckout checkout = svnOperationFactory.createCheckout();
		    checkout.setSource(SvnTarget.fromURL(SVNURL.parseURIEncoded(gitURLtoSVN(urlToDownload))));
		    checkout.setSingleTarget(SvnTarget.fromFile(tempDirectory));
		    checkout.run();
		} catch (SVNException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		    svnOperationFactory.dispose();
		}
		return tempDirectory;
	}
	
	public static void downloadGitRepo(String urlToDownload, File directory, char[] oauthToken) {
		SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
		svnOperationFactory.setAuthenticationManager(SVNWCUtil.createDefaultAuthenticationManager("${token}", oauthToken));
		try {
			SvnCheckout checkout = svnOperationFactory.createCheckout();
		    checkout.setSource(SvnTarget.fromURL(SVNURL.parseURIEncoded(gitURLtoSVN(urlToDownload))));
		    checkout.setSingleTarget(SvnTarget.fromFile(directory));
		    checkout.run();
		} catch (SVNException e) {
			e.printStackTrace();
		} finally {
		    svnOperationFactory.dispose();
		}
	}

	public static File downloadGitRepo(String urlToDownload, char[] oauthToken) {
		SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
		svnOperationFactory.setAuthenticationManager(SVNWCUtil.createDefaultAuthenticationManager("${token}", oauthToken));
		File tempDirectory = null;
		try {
			tempDirectory = Files.createTempDirectory("random").toFile();
		    SvnCheckout checkout = svnOperationFactory.createCheckout();
		    checkout.setSource(SvnTarget.fromURL(SVNURL.parseURIEncoded(gitURLtoSVN(urlToDownload))));
		    checkout.setSingleTarget(SvnTarget.fromFile(tempDirectory));
		    checkout.run();
		} catch (SVNException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		    svnOperationFactory.dispose();
		}
		return tempDirectory;
	}
	
	private static String gitURLtoSVN(String url) {
		return url.replaceFirst("tree/main", "trunk");
	}
}
