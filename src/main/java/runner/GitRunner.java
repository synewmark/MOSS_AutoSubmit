package runner;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import gitHandler.GitHandlerAbstract;
import gitHandler.GitHandlerMiddleManager;
import gitHandler.model.URLbyComponents;
import utils.FileUtils;

public class GitRunner {
	private static final Scanner scanner = new Scanner(System.in);
	private String host = "https://github.com/";
	private String username;
	private File pathToRepoNameFile;
	private String subdirectory = "";
	private String branch;
	private char[] oauthToken;
	private File directoryToDownloadTo;
	private LocalDateTime timestamp = LocalDateTime.now();
	private File pathToListOfFilesToDownload;

	GitRunner(String[] params) {
		setFields(params);
		setUnsetFields(false);
	}

	GitRunner() {
		setUnsetFields(true);
	}

	private static String getHost() {
		System.out.println("Please enter the base Git URL to the files (default: https://github.com/) :");
		String stringURL = scanner.nextLine();
		if (stringURL.equals("")) {
			stringURL = "https://github.com/";
		}
		return stringURL;
	}

	private static String getUsername() {
		System.out.println("Please enter the username hosting the repositories:");
		String username = scanner.nextLine();
		return username;
	}

	private static File getPathToRepoNameFile() {
		System.out.println("Please enter the directory to a text document hosting the list of repos :");
		String fileOfRepos = scanner.nextLine();
		File file = new File(fileOfRepos);
		if (!file.exists() || !file.canRead()) {
			System.out.println("Cannot access the file!");
			System.out.println("Please try again");
			return getPathToRepoNameFile();
		}
		return file;
	}

	private static String getSubdirectory() {
		System.out.println("Please enter the subdirectory you wish to download of the git repo: ");
		System.out.println("You may also leave this field blank to download the entire repository");
		String directory = scanner.nextLine();
		return directory;
	}

	private static String getBranch() {
		System.out.println("Please enter the branch of the Git Repository you wish to download: ");
		String branch = scanner.nextLine();
		return branch;
	}

	private static char[] getOauthToken() {
		System.out.println("If this repo is private please enter an OAuth token with valid read permission :");
		System.out.println("You may also wish to enter a token to avoid API limitting");
		char[] oauthToken = null;
		Console console = System.console();
		if (console != null) {
			oauthToken = console.readPassword();
		} else {
			oauthToken = scanner.nextLine().toCharArray();
		}
		return oauthToken;
	}

	private static File getDirectoryToDownloadTo() {
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
				System.out.println(
						"Cannot create temp directory for you, please specificy a valid directory and try again");
			}
		}
		if (!FileUtils.checkWriteAccessOfDir(fileDirectory)) {
			System.out.println("Cannot write to " + fileDirectory.toString() + " please try again");
			return getDirectoryToDownloadTo();
		}
		return fileDirectory;
	}

	private static LocalDateTime getTimeStamp() {
		System.out.println(
				"Please enter the ISO-8061 compliant time stamp corresponding to the latest date you wish to be downloaded");
		System.out.println("You can also leave this field blank and the latest repository will be downloaded");
		LocalDateTime timestamp = null;
		String passedValue = scanner.nextLine();
		if (passedValue.equals("")) {
			timestamp = LocalDateTime.MAX;
		} else {
			try {
				timestamp = LocalDateTime.parse(passedValue);
			} catch (DateTimeParseException e) {
				System.out.println(
						"Time stamp invalid, please enter an ISO-8061 compliant time stamp (YYYY-MM-DDTHH:MM:SSZ)");
				return getTimeStamp();
			}
		}
		return timestamp;
	}

	private static File getPathToListOfFilesToDownload() {
		System.out.println("Please enter the path to the list of files to download");
		System.out.println("You can also leave this field blank if you wish to download the entire directory");
		String stringDirectory = scanner.nextLine();
		File fileDirectory = new File(stringDirectory);
		if (!fileDirectory.canRead()) {
			System.out.println("Cannot read from directory: " + fileDirectory);
			System.out.println("Please try again");
			return getPathToListOfFilesToDownload();
		}
		return fileDirectory;
	}

	public File execute() {
		try {
			List<String> listOfRepos = FileUtils.getListOfStringsFromFile(pathToRepoNameFile);
			for (String repo : listOfRepos) {
				URLbyComponents url = null;
				url = new URLbyComponents(host, username, repo, subdirectory, branch);

				System.out.println(
						"Downloading files from: " + (url.getPath().equals("") ? url.getBareURL() : url.getFullURL())
								+ " to directory: " + directoryToDownloadTo);
				GitHandlerAbstract gitHandler = new GitHandlerMiddleManager().setURL(url).setDir(directoryToDownloadTo);
				if (oauthToken != null) {
					gitHandler.setCredentials(oauthToken);
				}
				if (pathToListOfFilesToDownload != null) {
					List<String> filesToDownload = FileUtils.getListOfStringsFromFile(pathToListOfFilesToDownload);
					gitHandler.setFilesToDownload(filesToDownload);
				}
				gitHandler.execute();
				Enviroment.addStudentDirectory(new File(directoryToDownloadTo, username + File.separatorChar + repo));
			}

			Enviroment.setRootWorkingStudentFileDir(directoryToDownloadTo);
			return directoryToDownloadTo;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public void setUnsetFields(boolean getOptionalFields) {
		if (username == null) {
			username = getUsername();
		}
		if (pathToRepoNameFile == null) {
			pathToRepoNameFile = getPathToRepoNameFile();
		}
		if (branch == null) {
			branch = getBranch();
		}
		if (directoryToDownloadTo == null) {
			directoryToDownloadTo = getDirectoryToDownloadTo();
		}
		if (getOptionalFields) {
			if (host == null) {
				host = getHost();
			}
			if (subdirectory == null) {
				subdirectory = getSubdirectory();
			}
			if (oauthToken == null) {
				oauthToken = getOauthToken();
			}
			if (timestamp == null) {
				timestamp = getTimeStamp();
			}
			if (pathToListOfFilesToDownload == null) {
				pathToListOfFilesToDownload = getPathToListOfFilesToDownload();
			}
		}
	}

	public void setFields(String[] params) {
		for (int i = 0; i < params.length; i++) {
			switch (params[i].toLowerCase()) {
			case "-h":
			case "-host":
				host = params[++i];
				break;

			case "-u":
			case "-user":
			case "-username":
				username = params[++i];
				break;

			case "-r":
			case "-repo":
				File pathToRepoName = new File(params[++i]);
				if (!pathToRepoName.canRead()) {
					throw new IllegalStateException("Cannot read from file: " + pathToRepoName);
				}
				pathToRepoNameFile = pathToRepoName;
				break;

			case "-sd":
			case "-subdirectory":
				subdirectory = params[++i];
				break;

			case "-b":
			case "-branch":
				branch = params[++i];
				break;

			case "-o":
			case "-oauth":
			case "-oauthtoken":
				oauthToken = params[++i].toCharArray();
				break;

			case "-d":
			case "-dir":
			case "-directory":
				directoryToDownloadTo = new File(params[++i]);
				break;

			case "-t":
			case "-time":
			case "-timestamp":
				timestamp = LocalDateTime.parse(params[++i]);
				break;
			case "-files":
			case "-f":
				pathToListOfFilesToDownload = new File(params[++i]);
				break;
			default:
				throw new IllegalArgumentException(params[i] + " is not a valid flag");
			}
		}
		if (directoryToDownloadTo == null) {
			try {
				directoryToDownloadTo = Files.createTempDirectory("MOSSAutoSubmit_GitFiles").toFile();
			} catch (IOException e) {
				throw new IllegalStateException(
						"Cannot create temp directory for you, please specificy a valid directory and try again");
			}
		} else {
			if (directoryToDownloadTo.exists() && FileUtils.getNumberOfFiles(directoryToDownloadTo) != 0) {
				throw new IllegalStateException("Directory must be empty " + directoryToDownloadTo);
			}
		}
	}

}
