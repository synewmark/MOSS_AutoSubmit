package runner;

import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

public class MainCommandLine {
	static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		String[][] parsedArgs = parser(args);
		for (String[] stringArray : parsedArgs) {
			switch (stringArray[0].toLowerCase()) {

			case "--m":
				System.out.println("Executing MOSS request...");
				System.out.println();
				String resultsURL;
				if (stringArray.length > 1) {
					resultsURL = new MOSSRunner(Arrays.copyOfRange(stringArray, 1, stringArray.length)).execute();
				} else {
					resultsURL = new MOSSRunner().execute();
				}
				System.out.println();
				System.out.println("MOSS request completed");
				System.out.println("You can view your results here: " + resultsURL);
				System.out.println();
				break;

			case "--g":
				System.out.println("Executing Git request...");
				System.out.println();
				File downloadLocation;
				if (stringArray.length > 1) {
					downloadLocation = new GitRunner(Arrays.copyOfRange(stringArray, 1, stringArray.length)).execute();
				} else {
					downloadLocation = new GitRunner().execute();
				}
				System.out.println("Git download completed you can find your files at this directory: ");
				System.out.println(downloadLocation);
				System.out.println();
				break;

			default:
				throw new IllegalStateException(stringArray[0] + " is not a recognized command");
			}
		}
	}

	private static String[][] parser(String[] inputs) {
		int size = 0;
		for (String string : inputs) {
			if (string.startsWith("--")) {
				size++;
			}
		}

		String[][] returnArray = new String[size][];
		int[] indices = new int[size + 1];
		int index = 0;
		for (int i = 0; i < inputs.length; i++) {
			if (inputs[i].startsWith("--")) {
				indices[index++] = i;
			}
		}

		indices[size] = inputs.length;

		for (int i = 0; i < indices.length - 1; i++) {
			returnArray[i] = Arrays.copyOfRange(inputs, indices[i], indices[i + 1]);
		}
		return returnArray;
	}

}
