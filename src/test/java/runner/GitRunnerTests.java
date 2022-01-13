package runner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;

import utils.FileUtils;

public class GitRunnerTests {
	File workingDir = new File(System.getProperty("user.dir"));
	File listOfFiles1Through4 = new File(workingDir, "TestResources" + File.separatorChar + "ListOfFiles1Through4.txt");
	File listOfFilesAll = new File(workingDir, "TestResources" + File.separatorChar + "ListOfFilesAll.txt");
	File repos = new File(workingDir, "TestResources" + File.separatorChar + "repos.txt");

	@Test
	public void testGitDownloadExplcitFilesTempDir() {
		String[] args = ("-u synewmark-resources -r " + repos + " -b main -f " + listOfFiles1Through4).split(" ");
		File directory = new GitRunner(args).execute();
		assertEquals(FileUtils.getNumberOfFiles(directory), 8);
	}

	@Test
	public void testGitDownloadAllFilesTempDir() {
		String[] args = ("-u synewmark-resources -r " + repos + " -b main").split(" ");
		File directory = new GitRunner(args).execute();
		assertEquals(FileUtils.getNumberOfFiles(directory), 20);
	}

	@Test
	public void testGitDownloadSubdirTempDir() {
		String[] args = ("-u synewmark-resources -r " + repos + " -b main -sd studentCode").split(" ");
		File directory = new GitRunner(args).execute();
		assertEquals(FileUtils.getNumberOfFiles(directory), 20);
	}

	@Test
	public void testGitDownloadSubdirStudent9TempDir() {
		String[] args = ("-u synewmark-resources -r " + repos + " -b main -sd studentCode/student9").split(" ");
		File directory = new GitRunner(args).execute();
		assertEquals(FileUtils.getNumberOfFiles(directory), 2);
	}

}
