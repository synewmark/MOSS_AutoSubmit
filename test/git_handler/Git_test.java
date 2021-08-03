package git_handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;

public class Git_test {
	@Test
	public void testGitDownload() {
		File testDirectory = new File("\\test");
		Git_downloader_svn.downloadGitRepo("https://github.com/google/gson/tree/master/lib", testDirectory);
		assertEquals(testDirectory.listFiles().length, 2);
		cleanup(testDirectory);
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
