package runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import utils.FileUtils;

public class MainCommandLineTests {
	File workingDir = new File(System.getProperty("user.dir"));
	File listOfFiles1Through4 = new File(workingDir, "TestResources" + File.separatorChar + "ListOfFiles1Through4.txt");
	File listOfFilesAll = new File(workingDir, "TestResources" + File.separatorChar + "ListOfFilesAll.txt");
	File repos = new File(workingDir, "TestResources" + File.separatorChar + "repos.txt");

	@Test
	public void testExplicitFilesTempDir() {
		String args[] = ("--g -u synewmark-resources -r " + repos + " -f " + listOfFiles1Through4
				+ " -b main --m -l java -id 884640278").split(" ");
		MainCommandLine.main(args);
		assertEquals(9, FileUtils.getNumberOfFiles(Enviroment.getRootWorkingStudentFileDir()));
		assertTrue(new File(Enviroment.getRootWorkingStudentFileDir(), "MossRequestResults.htm").exists());
		try {
			FileUtils.deleteDir(Enviroment.getRootWorkingStudentFileDir());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testAllFilesTestDir() {
		File testDir = new File(workingDir, "testDownload");
		testDir.mkdir();
		String args[] = ("--g -u synewmark-resources -r " + repos + " -d " + testDir
				+ " -b main --m -l java -id 884640278").split(" ");
		MainCommandLine.main(args);
		assertEquals(21, FileUtils.getNumberOfFiles(Enviroment.getRootWorkingStudentFileDir()));
		assertTrue(new File(Enviroment.getRootWorkingStudentFileDir(), "MossRequestResults.htm").exists());
		try {
			if (!FileUtils.deleteDir(testDir)) {
				System.err.println("Failed to delete directory, make sure to manually delete before rerunning tests");
			}
		} catch (IOException e) {
			System.err.println("Failed to delete directory, make sure to manually delete before rerunning tests");
		}
	}

	@Test
	public void testExplicitFilesTestDir() {
		File testDir = new File(workingDir, "testDownload");
		testDir.mkdir();
		String args[] = ("--g -u synewmark-resources -r " + repos + " -d " + testDir + " -f " + listOfFiles1Through4
				+ " -b main --m -l java -id 884640278").split(" ");
		MainCommandLine.main(args);
		assertEquals(9, FileUtils.getNumberOfFiles(Enviroment.getRootWorkingStudentFileDir()));
		assertTrue(new File(Enviroment.getRootWorkingStudentFileDir(), "MossRequestResults.htm").exists());
		try {
			if (!FileUtils.deleteDir(testDir)) {
				System.err.println("Failed to delete directory, make sure to manually delete before rerunning tests");
			}
		} catch (IOException e) {
			System.err.println("Failed to delete directory, make sure to manually delete before rerunning tests");
		}
	}

	@Test
	public void testGitAndMossSeperately() {
		String args1[] = ("--g -u synewmark-resources -r " + repos + " -b main --m -l java -id 884640278").split(" ");
		MainCommandLine.main(args1);
		File sfd = Enviroment.getRootWorkingStudentFileDir();
		Enviroment.setRootWorkingStudentFileDir(null);
		String args2[] = ("--m -id 884640278 -l java -sfd " + sfd).split(" ");
		MainCommandLine.main(args2);
		assertEquals(21, FileUtils.getNumberOfFiles(Enviroment.getRootWorkingStudentFileDir()));
		assertTrue(new File(Enviroment.getRootWorkingStudentFileDir(), "MossRequestResults.htm").exists());

	}
}