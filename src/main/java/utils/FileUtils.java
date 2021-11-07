package utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class FileUtils {

	public static int getNumberOfFiles(File startDirectory) {
		int count = 0;
		File[] list = startDirectory.listFiles();
		if (list != null) {
			for (File file : list) {
				if (file.isFile()) {
					count++;
				} else if (file.isDirectory()) {
					count += getNumberOfFiles(file);
				}
			}
		}
		return count;
	}

	public static boolean deleteDir(File dirToDelete) throws IOException {
		return deleteDirExclude(dirToDelete, null);
	}

	public static boolean deleteDirExclude(File fileToDelete, FileFilter filterToExclude) throws IOException {
		if (fileToDelete.isDirectory()) {
			File[] files = fileToDelete.listFiles(filterToExclude);
			if (files != null) {
				for (File file : files) {
					deleteDirExclude(file, filterToExclude);
				}
			}
		}
		return fileToDelete.delete();
	}

	// checks if we have write access to the directory or if the directory doesn't
	// exist if we have write access to the parent directory
	// in slightly different terms: returns true if we have write access to passed
	// directory currently or if such a directory can be created and we'll have
	// write permission to it
	public static boolean checkWriteAccessOfDir(File dir) {
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
