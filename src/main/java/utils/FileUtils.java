package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

	// returns first overlapped directory or null if all are disjoint
	public static List<File> checkOverlappingDirectories(Collection<File> files) {
		Map<File, File> fileComponents = new HashMap<>();
		for (File file : files) {
			File parent = file.getParentFile();
			while (parent != null) {
				fileComponents.put(parent, file);
				parent = parent.getParentFile();
			}
		}
		for (File file : files) {
			if (fileComponents.containsKey(file)) {
				List<File> returnList = new ArrayList<>();
				returnList.add(file);
				returnList.add(fileComponents.get(file));
				return returnList;
			}
		}
		return null;
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

	public static List<String> getListOfStringsFromFile(File file) throws IOException {
		ArrayList<String> list = new ArrayList<>();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		String input;
		while ((input = bufferedReader.readLine()) != null) {
			list.add(input);
		}
		bufferedReader.close();
		return list;
	}

	public static File zipDirectory(File directoryToZip, File zipFileName) throws IOException {
		if (!directoryToZip.isDirectory()) {
			throw new IllegalArgumentException(directoryToZip + " is not a directory");
		}
		if (!directoryToZip.canRead()) {
			throw new IllegalArgumentException("Cannot read from: " + directoryToZip
					+ " make sure the directory is named correctly and that you have the required permissions");
		}
		zipFileName.getParentFile().mkdirs();
		// use Try despite throw in method to ensure closing of resources
		try (FileOutputStream fileOutput = new FileOutputStream(zipFileName);
				ZipOutputStream zipOutput = new ZipOutputStream(fileOutput);) {
			Queue<File> filesToZip = new ArrayDeque<>();
			filesToZip.add(directoryToZip);
			while (!filesToZip.isEmpty()) {
				File currFile = filesToZip.poll();
				if (currFile.isDirectory()) {
					for (File file : currFile.listFiles()) {
						filesToZip.add(file);
					}
				} else if (currFile.isFile()) {
					zipOutput.putNextEntry(new ZipEntry(getRelativeFile(directoryToZip, currFile).toString()));
					Files.copy(currFile.toPath(), zipOutput);
				}
			}
		}
		return zipFileName;

	}

	private static File getRelativeFile(File parent, File child) {
		return parent.toPath().relativize(child.toPath()).toFile();
	}

	public static String getFileName(File file) {
		return file.toPath().getFileName().toString();
	}

	private FileUtils() {
	}
}
