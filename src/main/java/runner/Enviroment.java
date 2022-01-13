package runner;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Enviroment {
	private static String workingLanguage;
	private static File rootWorkingStudentFileDir;
	private static SortedSet<File> studentDirectories = new TreeSet<>();
	private static Set<File> unmodifiableStudentDirectories = Collections.unmodifiableSet(studentDirectories);

	public static String getWorkingLanguage() {
		return workingLanguage;
	}

	public static void setWorkingLanguage(String workingLanguage) {
		Enviroment.workingLanguage = workingLanguage;
	}

	public static File getRootWorkingStudentFileDir() {
		return rootWorkingStudentFileDir;
	}

	public static void setRootWorkingStudentFileDir(File rootWorkingStudentFileDir) {
		Enviroment.rootWorkingStudentFileDir = rootWorkingStudentFileDir;
	}

	public static boolean addStudentDirectory(File directory) {
		return studentDirectories.add(directory);
	}

	public static Set<File> getStudentDirectories() {
		return unmodifiableStudentDirectories;
	}

	private Enviroment() {

	}

}
