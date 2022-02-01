package runner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import utils.FileUtils;

public class MainCommandLineTests {
	File workingDir = new File(System.getProperty("user.dir"));
	File listOfFiles1Through4 = new File(workingDir, "TestResources" + File.separatorChar + "ListOfFiles1Through4.txt");
	File listOfFilesAll = new File(workingDir, "TestResources" + File.separatorChar + "ListOfFilesAll.txt");
	File students = new File(workingDir, "TestResources" + File.separatorChar + "Students.txt");
	File stages = new File(workingDir, "TestResources" + File.separatorChar + "Stage1+2.txt");
	File repos = new File(workingDir, "TestResources" + File.separatorChar + "repos.txt");
	File testDir = new File(workingDir, "testDownload");

	@AfterEach
	public void deleteTestDir() {
		try {
			if (testDir.exists() && !FileUtils.deleteDir(testDir)) {
				System.err.println("Failed to delete directory, make sure to manually delete before rerunning tests");
			}
		} catch (IOException e) {
			System.err.println("Failed to delete directory, make sure to manually delete before rerunning tests");
		}
	}

	@Test
	public void testExplicitFilesTempDir() {
		String args[] = ("-g  -u synewmark-resources -r " + repos + " --files " + listOfFiles1Through4
				+ " -b main -m 884640278 -l java").split(" ");
		args[1] = " ";
		MainCommandLine.main(args);
		assertEquals(8, FileUtils.getNumberOfFiles(MainCommandLine.enviroment.directory));
	}

	@Test
	public void testAllFilesTestDir() {
		testDir.mkdir();
		String args[] = ("-g  -u synewmark-resources -r " + repos + " -d " + testDir + " -b main -m 884640278 -l java")
				.split(" ");
		args[1] = " ";
		MainCommandLine.main(args);
		assertEquals(20, FileUtils.getNumberOfFiles(MainCommandLine.enviroment.directory));
	}

	@Test
	public void testExplicitFilesTestDir() {
		testDir.mkdir();
		String args[] = ("-g  -u synewmark-resources -r " + repos + " -d " + testDir + " --files "
				+ listOfFiles1Through4 + " -b main -m 884640278 -l java").split(" ");
		args[1] = " ";
		MainCommandLine.main(args);
		assertEquals(8, FileUtils.getNumberOfFiles(MainCommandLine.enviroment.directory));
	}

	@Test
	public void testGitAndMossSeperately() {
		String args1[] = ("-g  -u synewmark-resources -r " + repos + " -b main -m 884640278 -l java").split(" ");
		args1[1] = " ";
		MainCommandLine.main(args1);
		File sfd = MainCommandLine.enviroment.directory;
		MainCommandLine.enviroment.directory = null;
		String args2[] = ("-m 884640278 -l java -d " + sfd).split(" ");
		MainCommandLine.main(args2);
		assertEquals(20, FileUtils.getNumberOfFiles(MainCommandLine.enviroment.directory));
	}

	@Test
	public void testGitTimeStampNow() {
		String args1[] = ("-g  -u synewmark-resources -b main -r " + students + " --timestamp " + LocalDateTime.now()
				+ " --subdirectory " + "DataStructures-Spring2021-Project/stage2").split(" ");
		args1[1] = " ";
		MainCommandLine.main(args1);
		assertEquals(34, FileUtils.getNumberOfFiles(MainCommandLine.enviroment.directory));
	}

	@Test
	public void testGitTimeStampNowJan30() {
		String args1[] = ("-g  -u synewmark-resources -b main -r " + students
				+ " --timestamp 2022-01-30T00:00 --subdirectory " + "DataStructures-Spring2021-Project/stage1")
						.split(" ");
		args1[1] = " ";
		MainCommandLine.main(args1);
		assertEquals(30, FileUtils.getNumberOfFiles(MainCommandLine.enviroment.directory));
	}

}