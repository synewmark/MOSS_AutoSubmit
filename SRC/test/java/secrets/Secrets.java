package secrets;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import utils.FileUtils;

public class Secrets {
	private static String CodequiryAPI;
	private static String GitHubAPI;
	static {
		try {
			File secrets = new File("secret.log");
			setFields(FileUtils.getListOfStringsFromFile(secrets));
		} catch (IOException e) {
			System.err.println("Failed to load API keys proceding without them");
			System.err.println(e);
		}
	}

	private static void setFields(Collection<String> params) {
		for (String string : params) {
			String[] strings = string.split("=");
			switch (strings[0]) {
			case "CodequiryAPI":
				CodequiryAPI = strings[1];
				break;
			case "GitHubAPI":
				GitHubAPI = strings[1];
				break;
			default:
				System.err.println("Key " + strings[0] + " not recognized");
			}
		}
	}

	public static String getCodequiryAPI() {
		return CodequiryAPI;
	}

	public static String getGitHubAPI() {
		return GitHubAPI;
	}

}
