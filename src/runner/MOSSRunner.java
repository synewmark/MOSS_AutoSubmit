package runner;

import java.io.File;
import java.net.URL;
import java.util.Scanner;

import backendhandlers.MOSSHandler;

public class MOSSRunner {
	static Scanner scanner = new Scanner(System.in);

	public static String mossUpload() {
		System.out.println("Please enter the language of the files to be checked");
		System.out.println("The folowing languages are supported: " + MOSSHandler.listOfSupportedLanguages());
		String language = scanner.nextLine();
		if (!MOSSHandler.checkLanguageSupported(language)) {
			System.out.println(language + " not supported");
			System.out.println("Only the following languages are supported: " + MOSSHandler.listOfSupportedLanguages());
			return mossUpload();
		}
		return mossUpload(language);
	}

	private static String mossUpload(String language) {
		// cache dir for performance and to avoid value being altered in concurrent
		// environment
		// that being said, code is largely not designed for concurrency requries
		// further alterations if pushed in that direction
		File cacheLocalWorkingStudentFileDir = Enviroment.getWorkingStudentFileDir();
		if (cacheLocalWorkingStudentFileDir != null) {
			System.out.println("Found working directory " + cacheLocalWorkingStudentFileDir);
			System.out.println("Would you like to continue using that directory y/n");
			if (scanner.nextLine().toLowerCase().equals("y")) {
				return mossUpload(language, cacheLocalWorkingStudentFileDir);
			}
		}
		System.out.println("Please enter the directory to the student files");
		String stringDirectory = scanner.nextLine();
		File fileDirectory = new File(stringDirectory);
		if (!fileDirectory.canRead()) {
			System.out.println("Cannot read from " + fileDirectory
					+ " make sure you entered the directory properly and that you have read permission");
			mossUpload(language);
		}
		return mossUpload(language, fileDirectory);
	}

	private static String mossUpload(String language, File studentFiles) {
		System.out.println("Please enter the directory to the base files");
		System.out.println("You can also leave this field blank if no base files need to be uploaded");
		String stringDirectory = scanner.nextLine();
		File fileDirectory = new File(stringDirectory);
		if (!stringDirectory.isEmpty() && !fileDirectory.canRead()) {
			System.out.println("Cannot read from " + fileDirectory
					+ " make sure you entered the directory properly and that you have read permission");
			mossUpload(language);
		}
		return mossUpload(language, studentFiles, fileDirectory);
	}

	private static String mossUpload(String language, File studentFileDirectory, File baseFilesDirectory) {
		System.out.println("Please enter your MOSS ID:");
		String mossIDString = scanner.nextLine();
		long mossIDNumeric;
		try {
			mossIDNumeric = Long.parseLong(mossIDString);
		} catch (NumberFormatException e) {
			System.out.println(mossIDString + " is not a valid MOSS ID, please try again");
			return mossUpload(language, studentFileDirectory, baseFilesDirectory);
		}
		return mossUpload(language, studentFileDirectory, baseFilesDirectory, mossIDNumeric);
	}

	private static String mossUpload(String language, File studentFileDirectory, File baseFileDirectory, long mossID) {
		MOSSHandler mossHandler = new MOSSHandler();
		mossHandler.setLanguage(language).addSubmissionFiles(studentFileDirectory).setUserId(mossID);
		if (baseFileDirectory != null && !baseFileDirectory.toString().isEmpty()) {
			mossHandler.addBaseFiles(baseFileDirectory);
		}
		Enviroment.setWorkingLanguage(language);
		Enviroment.setWorkingStudentFileDir(studentFileDirectory);
		URL resultsURL = mossHandler.execute();
		return resultsURL.toString();
	}

	public static String mossUpload(String[] params) {
		String language = null;
		File studentFileDirectory = null;
		File baseFileDirectory = null;
		long mossID = -1;
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
			case "i":
			case "-id":
				mossID = Long.parseLong(params[++i]);
				break;
			default:
				throw new IllegalArgumentException(params[i] + " is not a valid flag for Moss Upload");
			}
			File cacheLocalWorkingStudentFileDir = Enviroment.getWorkingStudentFileDir();
			if (studentFileDirectory == null && cacheLocalWorkingStudentFileDir != null) {
				System.out.println("Student File Directory not supplied. Using working directory: "
						+ cacheLocalWorkingStudentFileDir);
				System.out.println();
				studentFileDirectory = cacheLocalWorkingStudentFileDir;
			}
		}
		return mossUpload(language, studentFileDirectory, baseFileDirectory, mossID);
	}
}
