package runner;

import java.io.File;

public class Enviroment {
	private static String workingLanguage;
	private static File workingStudentFileDir;

	public static String getWorkingLanguage() {
		return workingLanguage;
	}

	public static void setWorkingLanguage(String workingLanguage) {
		Enviroment.workingLanguage = workingLanguage;
	}

	public static File getWorkingStudentFileDir() {
		return workingStudentFileDir;
	}

	public static void setWorkingStudentFileDir(File workingStudentFileDir) {
		Enviroment.workingStudentFileDir = workingStudentFileDir;
	}

	private Enviroment() {

	}

}
