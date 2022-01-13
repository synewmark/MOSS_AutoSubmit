package runner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Scanner;

import backendhandlers.MOSSHandler;

public class MOSSRunner {
	private static final Scanner scanner = new Scanner(System.in);
	private String language;
	private File studentFileDirectory;
	private File baseFileDirectory;
	private long mossID;

	public MOSSRunner(String[] params) {
		setFields(params);
		getUnsetFields(false);
	}

	public MOSSRunner() {
		getUnsetFields(true);
	}

	private void getUnsetFields(boolean getOptionalFields) {
		if (language == null) {
			language = getLanguage();
		}
		if (studentFileDirectory == null) {
			studentFileDirectory = getStudentFileDirectory();
		}
		if (mossID == 0) {
			mossID = getMossID();
		}
		if (getOptionalFields) {
			if (baseFileDirectory == null) {
				baseFileDirectory = getBaseFileDirectory();
			}
		}
	}

	private static String getLanguage() {
		System.out.println("Please enter the language of the files to be checked");
		System.out.println("The folowing languages are supported: " + MOSSHandler.listOfSupportedLanguages());
		String language = scanner.nextLine();
		if (!MOSSHandler.checkLanguageSupported(language)) {
			System.out.println(language + " not supported");
			System.out.println("Only the following languages are supported: " + MOSSHandler.listOfSupportedLanguages());
			return getLanguage();
		}
		return language;
	}

	private static File getStudentFileDirectory() {
		// cache dir for performance and to avoid value being altered in concurrent
		// environment
		// that being said, code is largely not designed for concurrency requries
		// further alterations if pushed in that direction
		File cacheLocalWorkingStudentFileDir = Enviroment.getRootWorkingStudentFileDir();
		if (cacheLocalWorkingStudentFileDir != null) {
			System.out.println("Found working directory " + cacheLocalWorkingStudentFileDir);
			System.out.println("Would you like to continue using that directory y/n");
			if (scanner.nextLine().toLowerCase().equals("y")) {
				return cacheLocalWorkingStudentFileDir;
			}
		}
		System.out.println("Please enter the directory to the student files");
		String stringDirectory = scanner.nextLine();
		File fileDirectory = new File(stringDirectory);
		if (!fileDirectory.canRead()) {
			System.out.println("Cannot read from " + fileDirectory
					+ " make sure you entered the directory properly and that you have read permission");
			return getStudentFileDirectory();
		}
		return fileDirectory;
	}

	private static File getBaseFileDirectory() {
		System.out.println("Please enter the directory to the base files");
		System.out.println("You can also leave this field blank if no base files need to be uploaded");
		String stringDirectory = scanner.nextLine();
		File fileDirectory = new File(stringDirectory);
		if (!stringDirectory.isEmpty() && !fileDirectory.canRead()) {
			System.out.println("Cannot read from " + fileDirectory
					+ " make sure you entered the directory properly and that you have read permission");
			return getBaseFileDirectory();
		}
		return fileDirectory;
	}

	private static long getMossID() {
		System.out.println("Please enter your MOSS ID:");
		String mossIDString = scanner.nextLine();
		long mossIDNumeric;
		try {
			mossIDNumeric = Long.parseLong(mossIDString);
		} catch (NumberFormatException e) {
			System.out.println(mossIDString + " is not a valid MOSS ID, please try again");
			return getMossID();
		}
		return mossIDNumeric;
	}

	private void setFields(String[] params) {
		for (int i = 0; i < params.length; i++) {
			switch (params[i].toLowerCase()) {
			case "-language":
			case "-l":
				language = params[++i];
				break;
			case "-studentfiledirectory":
			case "-sfd":
				studentFileDirectory = new File(params[++i]);
				break;
			case "-basefiledirectory":
			case "-bfd":
				baseFileDirectory = new File(params[++i]);
				break;
			case "-i":
			case "-id":
				mossID = Long.parseLong(params[++i]);
				break;
			default:
				throw new IllegalArgumentException(params[i] + " is not a valid flag for Moss Upload");
			}
		}
		File cacheLocalWorkingStudentFileDir = Enviroment.getRootWorkingStudentFileDir();
		if (studentFileDirectory == null && cacheLocalWorkingStudentFileDir != null) {
			System.out.println(
					"Student File Directory not supplied. Using working directory: " + cacheLocalWorkingStudentFileDir);
			System.out.println();
			studentFileDirectory = cacheLocalWorkingStudentFileDir;
		}
	}

	public String execute() {
		MOSSHandler mossHandler = new MOSSHandler();
		mossHandler.setLanguage(language).addSubmissionFiles(studentFileDirectory).setUserId(mossID);
		if (baseFileDirectory != null && !baseFileDirectory.toString().isEmpty()) {
			mossHandler.addBaseFiles(baseFileDirectory);
		}
		Enviroment.setWorkingLanguage(language);
		Enviroment.setRootWorkingStudentFileDir(studentFileDirectory);
		URL resultsURL = mossHandler.execute();
		File mossResultsFile = new File(studentFileDirectory, "MossRequestResults.htm");
		try {
			downloadFromURL(resultsURL, mossResultsFile, Integer.MAX_VALUE);
			System.out.println();
			System.out.println("Local results saved to: " + mossResultsFile);
		} catch (IOException e) {
			System.err.println();
			System.err.println("Could not automatically download MOSS results!");
			System.err.println("Make sure to save the results independently");
		}
		return resultsURL.toString();
	}

	private void downloadFromURL(URL urlToDownloadFrom, File fileToDownloadTo, int fileSize) throws IOException {
		ReadableByteChannel readableByteChannel = Channels.newChannel(urlToDownloadFrom.openStream());
		FileOutputStream fileOutputStream = new FileOutputStream(fileToDownloadTo);
		fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, fileSize);
		readableByteChannel.close();
		fileOutputStream.close();
	}
}
