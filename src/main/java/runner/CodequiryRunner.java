package runner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import backendhandlers.CodequiryHandler;
import utils.FileUtils;

public class CodequiryRunner {
	private static final Scanner scanner = new Scanner(System.in);
	private String apiKey;
	private String language;
	private Collection<File> directoriesToUpload;
	private String name;

	public CodequiryRunner(String[] params) {
		setFields(params);
		getUnsetFields(false);
	}

	public CodequiryRunner() {
		getUnsetFields(true);
	}

	private void getUnsetFields(boolean getOptionalFields) {
		if (apiKey == null) {
			apiKey = getApiKey();
		}

		if (language == null) {
			language = getLanguage();
		}

		if (directoriesToUpload == null) {
			directoriesToUpload = getDirectoriesToUpload();
		}
		if (getOptionalFields) {
			if (name == null) {
				name = getName();
			}
		}
	}

	public URL execute() {
		if (name == null) {
			name = String.format("AutosubmitRequest: %s StudentCount: %d",
					new Date(System.currentTimeMillis()).toString(), directoriesToUpload.size());
		}
		CodequiryHandler codequiryHandler = new CodequiryHandler();
		codequiryHandler.setAPIKey(apiKey).setLanguage(language).setSubmissionName(name);
		for (File file : directoriesToUpload) {
			codequiryHandler.addDirectoryToSubmit(file);
		}
		try {
			return codequiryHandler.execute();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private static String getLanguage() {
		if (Enviroment.getWorkingLanguage() != null) {
			System.out.println("Found working language: " + Enviroment.getWorkingLanguage());
			System.out.println("If you would like to use it enter Y");
			String response = scanner.nextLine();
			if (response.equals("Y")) {
				return Enviroment.getWorkingLanguage();
			}
		}
		System.out.println("Please enter the language the code is in");
		String response = scanner.nextLine();
		return response;
	}

	private static String getApiKey() {
		System.out.println("Please enter the API key for Codeqiry");
		String response = scanner.nextLine();
		if (response.isBlank()) {
			System.out.println("Invalid API key, please try again");
			return getApiKey();
		}
		return response;
	}

	private static String getName() {
		System.out.println("Please enter a name for your Codeqiry request");
		System.out.println("You can also leave this blank and one will be assigned to you");
		String response = scanner.nextLine();
		if (response.isBlank()) {
			return null;
		}
		return response;

	}

	private static Collection<File> getDirectoriesToUpload() {
		if (!Enviroment.getStudentDirectories().isEmpty()) {
			System.out.println(String.format("Found %d working student directories from previous Git request",
					Enviroment.getStudentDirectories().size()));
			System.out.println(
					"If you would like to use those enter Y, to review them enter P, otherwise enter any other key");
			while (true) {
				String response = scanner.nextLine();
				if (response.equals("Y")) {
					return Enviroment.getStudentDirectories();
				} else if (!response.equals("P")) {
					break;
				} else {
					int i = 1;
					for (File file : Enviroment.getStudentDirectories()) {
						System.out.println("Directory " + i++ + ": " + file);
					}
				}
			}
		}
		boolean directory;
		System.out.println(
				"Please enter whether the directory about to be passed is to a root directory or a text list of directories");
		while (true) {
			System.out.println("Please enter \"root\" if it's a root and \"txt\" if it's a text file");
			String reponse = scanner.nextLine();
			if (reponse.equals("root")) {
				directory = true;
				break;
			} else if (reponse.equals("txt")) {
				directory = false;
				break;
			}
		}
		System.out.println("Now please enter the path");
		String reponse = scanner.nextLine();
		File file = new File(reponse);
		if (!file.canRead()) {
			System.out.println("Cannot read from file: " + reponse);
			System.out.println("Please try again");
			return getDirectoriesToUpload();
		}
		if (directory) {
			return new TreeSet<File>(List.of(file.listFiles()));

		}
		Collection<File> returnCollection = new TreeSet<>();
		Collection<String> listOfFiles;
		try {
			listOfFiles = FileUtils.getListOfStringsFromFile(file);
		} catch (IOException e) {
			throw new IllegalStateException("Could not read from file: " + file, e);
		}
		for (String string : listOfFiles) {
			returnCollection.add(new File(string));
		}
		return returnCollection;
	}

	private void setFields(String[] params) {
		for (int i = 0; i < params.length; i++) {
			switch (params[i].toLowerCase()) {
			case "-name":
			case "-n":
				name = params[++i];
			case "-language":
			case "-l":
				language = params[++i];
				break;
			case "-key":
			case "-apikey":
				apiKey = params[++i];
				break;
			case "-sd":
			case "studentdirectory":
				File rootDirectory = new File(params[++i]);
				directoriesToUpload = new TreeSet<File>(List.of(rootDirectory.listFiles()));
				break;

			case "-sdf":
			case "-studentdirectoriesfile":
				File fileDirectoriesToUpload = new File(params[++i]);
				Set<File> set = new TreeSet<>();
				Collection<String> listOfFiles;
				try {
					listOfFiles = FileUtils.getListOfStringsFromFile(fileDirectoriesToUpload);
				} catch (IOException e) {
					throw new IllegalStateException("Could not read from file: " + fileDirectoriesToUpload, e);
				}
				for (String string : listOfFiles) {
					set.add(new File(string));
				}
				directoriesToUpload = set;
			default:
				throw new IllegalArgumentException(params[i] + " is not a valid flag for Codequiry Upload");
			}
		}
		String cacheLanguage = Enviroment.getWorkingLanguage();
		if (language == null && cacheLanguage != null) {
			System.out.println("Language not supplied. Using working language: " + cacheLanguage);
			System.out.println();
			language = cacheLanguage;
		}

		Collection<File> cacheSetOfDirectories = Enviroment.getStudentDirectories();
		if (directoriesToUpload == null && cacheSetOfDirectories.size() != 0) {
			System.out.println("Student File Directory not supplied. Using working set: " + cacheSetOfDirectories);
			directoriesToUpload = cacheSetOfDirectories;
		}
	}
}
