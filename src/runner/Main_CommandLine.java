package runner;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Scanner;

import git_handler.GitDownloader;
import moss_handler.MOSSHandler;

public class Main_CommandLine {
	static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
	}

	private static File GitDownload() {
		System.out.println("Please enter the complete Git URL to the files: \n");
		String stringURL = scanner.nextLine();
		try {
			return GitDownload(new URL(stringURL));
		} catch (MalformedURLException e) {
			System.out.println(stringURL + " is not a valid URL please try again \n");
			return GitDownload();
		}
	}

	private static File GitDownload(URL url) {
		System.out.println(
				"If this repo is private please enter an OAuth token with valid read permission, otherwise leave this field blank");
		char[] oauthToken = null;
		Console console = System.console();
		if (console != null) {
			oauthToken = console.readPassword();
		} else {
			oauthToken = scanner.nextLine().toCharArray();
		}
		return GitDownload(url, oauthToken);
	}

	private static File GitDownload(URL url, char[] oauthToken) {
		System.out.println("Please enter the directory in which you'd like these files to be downloaded");
		System.out.println("You can also leave this field blank and a temp directory will be assigned to you");
		String stringDirectory = scanner.nextLine();
		File fileDirectory = null;
		if (stringDirectory.length() > 0) {
			fileDirectory = new File(stringDirectory);
		} else {
			try {
				fileDirectory = Files.createTempDirectory("MOSSAutoSubmit_GitFiles").toFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!checkWriteAccessOfDir(fileDirectory)) {
			System.out.println("Cannot write to " + fileDirectory.toString() + " please try again");
			GitDownload(url, oauthToken);
		}
		return GitDownload(url, oauthToken, fileDirectory);
	}

	private static File GitDownload(URL url, char[] oauthToken, File directory) {
		System.out.println("Downloading files from: " + url + " to directory: " + directory);
		GitDownloader GitDownloader = new GitDownloader().setDownloadSource(url.toString()).setOutputFolder(directory);
		if (oauthToken.length > 0) {
			GitDownloader.setCredentials(oauthToken);
		}
		GitDownloader.execute();
		return directory;
	}

	private static String MossUpload() {
		System.out.println("Please enter the language of the files to be checked");
		System.out.println("The folowing languages are supported: " + MOSSHandler.listOfSupportedLanguages());
		String language = scanner.nextLine();
		if (!MOSSHandler.checkLanguageSupported(language)) {
			System.out.println(language + " not supported");
			System.out.println("Only the following languages are supported: " + MOSSHandler.listOfSupportedLanguages());
			return MossUpload();
		}
		return MossUpload(language);
	}

	private static String MossUpload(String language) {
		System.out.println("Please enter the directory to the student files");
		String stringDirectory = scanner.nextLine();
		File fileDirectory = new File(stringDirectory);
		if (!fileDirectory.canRead()) {
			System.out.println("Cannot read from " + fileDirectory
					+ " make sure you entered the directory properly and that you have read permission");
			MossUpload(language);
		}
		return MossUpload(language, fileDirectory);
	}

	private static String MossUpload(String language, File studentFiles) {
		System.out.println("Please enter the directory to the base files");
		System.out.println("You can also leave this field blank if no base files need to be uploaded");
		String stringDirectory = scanner.nextLine();
		File fileDirectory = new File(stringDirectory);
		if (!stringDirectory.isEmpty() && !fileDirectory.canRead()) {
			System.out.println("Cannot read from " + fileDirectory
					+ " make sure you entered the directory properly and that you have read permission");
			MossUpload(language);
		}
		return MossUpload(language, studentFiles, fileDirectory);
	}

	private static String MossUpload(String language, File studentFileDirectory, File baseFilesDirectory) {
		System.out.println("Please enter your MOSS ID:");
		String mossIDString = scanner.nextLine();
		long mossIDNumeric;
		try {
			mossIDNumeric = Long.parseLong(mossIDString);
		} catch (NumberFormatException e) {
			System.out.println(mossIDString + " is not a valid MOSS ID, please try again");
			return MossUpload(language, studentFileDirectory, baseFilesDirectory);
		}
		return MossUpload(language, studentFileDirectory, baseFilesDirectory, mossIDNumeric);
	}

	private static String MossUpload(String language, File studentFileDirectory, File baseFileDirectory, long mossID) {
		MOSSHandler mossHandler = new MOSSHandler();
		mossHandler.setLanguage(language).addSubmissionFiles(studentFileDirectory).setUserId(mossID);
		if (!baseFileDirectory.toString().isEmpty()) {
			mossHandler.addBaseFiles(baseFileDirectory);
		}
		return mossHandler.execute().toString();
	}

	// checks if we have write access to the directory or if the directory doesn't
	// exist if we have write access to the parent directory
	// in slightly different terms: returns true if we have write access to passed
	// directory currently or if such a directory can be created and we'll have
	// write permission to it
	private static boolean checkWriteAccessOfDir(File dir) {
		// short circuit and return false early if we're at the top of the directory
		if (dir.equals(dir.getParentFile())) {
			return false;
		}
		if (!dir.exists()) {
			return checkWriteAccessOfDir(dir.getParentFile());
		}
		return dir.canWrite();
	}

}
