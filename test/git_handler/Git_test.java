package git_handler;

import java.io.File;

import org.junit.jupiter.api.Test;

public class Git_test {
	@Test
	public void testGitDownload() {
		Git_downloader_svn.downloadGitRepo("https://github.com/Yeshiva-University-CS/Newmark_Shmuel_800579209/tree/main/se-practice", 
			new File("C:\\Users\\ahome\\OneDrive\\Desktop\\Java"), "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".toCharArray());
	}
}
