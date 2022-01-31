package runner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import com.google.devtools.common.options.OptionsParser;

public class MainCommandLine {
	static Enviroment enviroment;
	static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		System.out.println(Arrays.toString(args));
		OptionsParser parser = OptionsParser.newOptionsParser(Enviroment.class);
		parser.parseAndExitUponError(args);
		enviroment = parser.getOptions(Enviroment.class);
		System.out.println(Arrays.toString(enviroment.gitAPI));
		if (enviroment.gitAPI != null) {
			gitRequest();
		}

		if (enviroment.mossID > -1) {
			mossRequest();
		}

		if (enviroment.codequiryAPI != null && !enviroment.codequiryAPI.isBlank()) {
			codequiryRequest();
		}
	}

	private static void gitRequest() {
		System.out.println("Executing Git request...");
		System.out.println();
		File downloadLocation;
		try {
			downloadLocation = new GitRunner(enviroment).execute();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		enviroment.directory = downloadLocation;
		System.out.println();
		System.out.println("Git download completed you can find your files at this directory: ");
		System.out.println(downloadLocation);
		System.out.println();
	}

	private static void mossRequest() {
		System.out.println("Executing MOSS request...");
		System.out.println();
		String resultsURL;
		resultsURL = new MOSSRunner(enviroment).execute();
		System.out.println();
		System.out.println("MOSS request completed");
		System.out.println("You can view your results here: " + resultsURL);
		System.out.println();

	}

	private static void codequiryRequest() {
		System.out.println("Executing Codequiry request...");
		System.out.println();
		String resultsURL;
		resultsURL = new CodequiryRunner(enviroment).execute().toString();
		System.out.println();
		System.out.println("Codequiry check creation+upload completed");
		System.out.println("You can finish execute your request at the following URL: " + resultsURL);
		System.out.println();
	}
}
