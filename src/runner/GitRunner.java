package runner;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

import gitHandler.GitHandlerAbstract;
import gitHandler.GitHandlerMiddleManager;
import gitHandler.model.URLbyComponents;

public class GitRunner {
	static Scanner scanner = new Scanner(System.in);

	public static File gitDownload() {
		System.out.println("Please enter the base Git URL to the files (default: https://github.com/) :");
		String stringURL = scanner.nextLine();
		if (stringURL.equals("")) {
			stringURL = "https://github.com/";
		}
		return gitDownload(stringURL);
	}

	private static File gitDownload(String host) {
		System.out.println("Please enter the username hosting the repositories:");
		String username = scanner.nextLine();
		return gitDownload(host, username);
	}

	private static File gitDownload(String host, String username) {
		System.out.println("Please enter the directory to a text document hosting the list of repos :");
		String fileOfRepos = scanner.nextLine();
		File file = new File(fileOfRepos);
		if (!file.exists() || !file.canRead()) {
			System.out.println("Cannot access the file!");
			System.out.println("Please try again");
			return gitDownload(host, username);
		}
		return gitDownload(host, username, file);
	}

	private static File gitDownload(String host, String username, File fileOfRepos) {
		System.out.println("Please enter the subdirectory you wish to download of the git repo: ");
		System.out.println("You may also leave this field blank to download the entire repository");
		String directory = scanner.nextLine();
		return gitDownload(host, username, fileOfRepos, directory);
	}

	private static File gitDownload(String host, String username, File fileOfRepos, String directory) {
		System.out.println("Please enter the branch of the Git Repository you wish to download: ");
		String branch = scanner.nextLine();
		return gitDownload(host, username, fileOfRepos, directory, branch);
	}

	private static File gitDownload(String host, String username, File fileOfRepos, String subDirectory,
			String branch) {
		System.out.println("If this repo is private please enter an OAuth token with valid read permission :");
		System.out.println("You may also wish to enter a token to avoid API limitting");
		char[] oauthToken = null;
		Console console = System.console();
		if (console != null) {
			oauthToken = console.readPassword();
		} else {
			oauthToken = scanner.nextLine().toCharArray();
		}
		return gitDownload(host, username, fileOfRepos, subDirectory, branch, oauthToken);
	}

	private static File gitDownload(String host, String username, File fileOfRepos, String subDirectory, String branch,
			char[] oauthToken) {
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
		if (!checkWriteAccessOfDir(fileDirectory)) {
			System.out.println("Cannot write to " + fileDirectory.toString() + " please try again");
			gitDownload(host, username, fileOfRepos, subDirectory, branch, oauthToken);
		}
		return gitDownload(host, username, fileOfRepos, subDirectory, branch, oauthToken, fileDirectory);
	}

	private static File gitDownload(String host, String username, File fileOfRepos, String subDirectory, String branch,
			char[] oauthToken, File directory) {
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
				return gitDownload(host, username, fileOfRepos, subDirectory, branch, oauthToken, directory);
			}
		}
		return gitDownload(host, username, fileOfRepos, subDirectory, branch, oauthToken, directory, timestamp);
	}

	private static File gitDownload(String host, String username, File fileOfRepos, String subDirectory, String branch,
			char[] oauthToken, File directory, LocalDateTime timestamp) {
		ArrayList<String> listOfRepos = new ArrayList<>();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(fileOfRepos));
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		String input;
		try {
			while ((input = bufferedReader.readLine()) != null) {
				listOfRepos.add(input);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		for (String repo : listOfRepos) {
			URLbyComponents url = null;
			try {
				url = new URLbyComponents(host, username, repo, subDirectory, branch);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			System.out.println("Downloading files from: " + url + " to directory: " + directory);
			try {
				GitHandlerAbstract gitHandler = new GitHandlerMiddleManager().setURL(url).setDir(directory);
				if (oauthToken.length != 0) {
					gitHandler.setCredentials(oauthToken);
				}
				gitHandler.execute();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Enviroment.setWorkingStudentFileDir(directory);
		return directory;
	}

	public static File gitDownload(String[] params) {
		String host = null;
		String username = null;
		File pathToRepoNameFile = null;
		String path = null;
		String branch = null;
		char[] oauthToken = new char[0];
		File directoryToDownloadTo = null;
		LocalDateTime timestamp = null;
		for (int i = 0; i < params.length; i++) {
			switch (params[i].toLowerCase()) {
			case "-h":
			case "-host":
				host = params[++i];
				break;

			case "-u":
			case "-username":
				username = params[++i];
				break;

			case "-r":
			case "-repo":
				pathToRepoNameFile = new File(params[++i]);
				break;

			case "-p":
			case "-path":
				path = params[++i];
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

			default:
				throw new IllegalArgumentException(params[i] + " is not a valid flag");
			}
		}
		if (host == null) {
			host = "https://github.com/";
		}

		if (directoryToDownloadTo == null) {
			try {
				directoryToDownloadTo = Files.createTempDirectory("MOSSAutoSubmit_GitFiles").toFile();
			} catch (IOException e) {
				throw new IllegalStateException(
						"Cannot create temp directory for you, please specificy a valid directory and try again");
			}
		}
		if (timestamp == null) {
			timestamp = LocalDateTime.now();
		}

		return gitDownload(host, username, pathToRepoNameFile, path, branch, oauthToken, directoryToDownloadTo,
				timestamp);
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
