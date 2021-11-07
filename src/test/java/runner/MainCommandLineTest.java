package runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import utils.FileUtils;

class MainCommandLineTest {
	File workingDir = new File(System.getProperty("user.dir"));
	File listOfFiles1Through4 = new File(workingDir, "TestResources" + File.separatorChar + "ListOfFiles1Through4.txt");
	File listOfFilesAll = new File(workingDir, "TestResources" + File.separatorChar + "ListOfFilesAll.txt");
	File repos = new File(workingDir, "TestResources" + File.separatorChar + "repos.txt");

	@Test
	void testExplicitFilesTempDir() {
		String args[] = ("--g -u synewmark -r " + repos + " -f " + listOfFiles1Through4
				+ " -b main --m -l java -id 884640278").split(" ");
		MainCommandLine.main(args);
		assertEquals(9, FileUtils.getNumberOfFiles(Enviroment.getWorkingStudentFileDir()));
		assertTrue(new File(Enviroment.getWorkingStudentFileDir(), "MossRequestResults.htm").exists());
		try {
			FileUtils.deleteDir(Enviroment.getWorkingStudentFileDir());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testAllFilesTestDir() {
		File testDir = new File(workingDir, "testDownload");
		testDir.mkdir();
		String args[] = ("--g -u synewmark -r " + repos + " -d " + testDir + " -b main --m -l java -id 884640278")
				.split(" ");
		MainCommandLine.main(args);
		assertEquals(21, FileUtils.getNumberOfFiles(Enviroment.getWorkingStudentFileDir()));
		assertTrue(new File(Enviroment.getWorkingStudentFileDir(), "MossRequestResults.htm").exists());
		try {
			if (!FileUtils.deleteDir(testDir)) {
				System.err.println("Failed to delete directory, make sure to manually delete before rerunning tests");
			}
		} catch (IOException e) {
			System.err.println("Failed to delete directory, make sure to manually delete before rerunning tests");
		}
	}

	@Test
	void testExplicitFilesTestDir() {
		File testDir = new File(workingDir, "testDownload");
		testDir.mkdir();
		String args[] = ("--g -u synewmark -r " + repos + " -d " + testDir + " -f " + listOfFiles1Through4
				+ " -b main --m -l java -id 884640278").split(" ");
		MainCommandLine.main(args);
		assertEquals(9, FileUtils.getNumberOfFiles(Enviroment.getWorkingStudentFileDir()));
		assertTrue(new File(Enviroment.getWorkingStudentFileDir(), "MossRequestResults.htm").exists());
		try {
			if (!FileUtils.deleteDir(testDir)) {
				System.err.println("Failed to delete directory, make sure to manually delete before rerunning tests");
			}
		} catch (IOException e) {
			System.err.println("Failed to delete directory, make sure to manually delete before rerunning tests");
		}
	}

	@Test
	void testGitAndMossSeperately() {
		String args1[] = ("--g -u synewmark -r " + repos + " -b main --m -l java -id 884640278").split(" ");
		MainCommandLine.main(args1);
		File sfd = Enviroment.getWorkingStudentFileDir();
		Enviroment.setWorkingStudentFileDir(null);
		String args2[] = ("--m -id 884640278 -l java -sfd " + sfd).split(" ");
		MainCommandLine.main(args2);
		assertEquals(21, FileUtils.getNumberOfFiles(Enviroment.getWorkingStudentFileDir()));
		assertTrue(new File(Enviroment.getWorkingStudentFileDir(), "MossRequestResults.htm").exists());
		try {
			FileUtils.deleteDir(Enviroment.getWorkingStudentFileDir());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}