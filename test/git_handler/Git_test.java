package git_handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.junit.jupiter.api.Test;

public class Git_test {
	File testDirectory = new File(System.getProperty("user.dir") + "\\testDownload");

	@Test
	public void testGitDownload() {
		Git_downloader_svn.downloadGitRepo("https://github.com/google/gson/tree/master/lib", testDirectory);
		assertEquals(testDirectory.listFiles().length, 2);
		cleanup(testDirectory);
	}

	@Test
	public void testGitDownloadBuilder() {
		new Git_downloader_svn().setOutputFolder(testDirectory)
				.setDownloadSource("https://github.com/google/gson/tree/master/lib").execute();
		assertEquals(testDirectory.listFiles().length, 2);
		cleanup(testDirectory);
	}

	@Test
	public void testGitTempFolderDownload() {
		File tempDirectory = Git_downloader_svn.downloadGitRepo("https://github.com/google/gson/tree/master/lib");
		assertEquals(tempDirectory.listFiles().length, 2);
		cleanup(tempDirectory);
	}

	@Test
	public void testExceptionThrownForIllegalURLs() {
		assertThrows(IllegalArgumentException.class, () -> {
			new Git_downloader_svn().setOutputFolder(testDirectory).setDownloadSource("Clearly not  valid url")
					.execute();
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Git_downloader_svn.downloadGitRepo("Clearly not  valid url", testDirectory);
		});
	}

	public void cleanup(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				cleanup(f);
			}
		}
		file.delete();
	}
}
