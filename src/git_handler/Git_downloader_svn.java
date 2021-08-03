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
	File directory;
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
		checkout.setSingleTarget(SvnTarget.fromFile(directory));
		this.directory = directory;
		return this;
	}
	
	public void execute() {
		try {
			checkout.run();
			delete(new File(directory.toString()+File.separatorChar+".svn"));
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
		    delete(new File(directory.toString()+File.separatorChar+".svn"));
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
		    delete(new File(tempDirectory.toString()+File.separatorChar+".svn"));
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
		    delete(new File(directory.toString()+File.separatorChar+".svn"));
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
		    delete(new File(tempDirectory.toString()+File.separatorChar+".svn"));
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
