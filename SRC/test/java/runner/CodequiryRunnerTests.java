package runner;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backendhandlers.CodequiryHandler;
import secrets.Secrets;
import utils.FileUtils;

class CodequiryRunnerTests {
	static File testDir = new File("TestDir");

	@BeforeAll
	public static void ensureAPIKey() {
		Assumptions.assumeTrue(Secrets.getCodequiryAPI() != null);
	}

	@BeforeEach
	public void createFiles() {
		assumeFalse(testDir.exists());
		try {
			createExampleFiles(testDir);
		} catch (IOException e) {
			Assumptions.assumeTrue(e == null);
		}
	}

	@AfterEach
	public void deleteFiles() throws IOException {
		FileUtils.deleteDir(testDir);
	}

	@Test
	public void createCheck() throws IOException {
		CodequiryHandler codequiryHandler = new CodequiryHandler().setAPIKey(Secrets.getCodequiryAPI())
				.setLanguage("Java").setSubmissionName("TestRun");
		for (int i = 0; i < 10; i++) {
			codequiryHandler.addDirectoryToSubmit(new File(testDir, "studentCode" + File.separator + "student" + i));
		}
		System.out.println(codequiryHandler.execute());
	}

	private void createExampleFiles(File dir) throws IOException {
		String[] testCode = { "public", "static", "void", "main(", "String[]", "args", ")", "{\n", "System.out.println",
				"(", "Hello", "Local", "World!", ")", ";\n", "}" };
		for (int i = 0; i < 10; i++) {
			File file = new File(dir,
					"studentCode" + File.separator + "student" + i + File.separator + "HelloWorld.java");
			file.getParentFile().mkdirs();
			FileWriter writer = new FileWriter(file);
			for (String s : testCode) {
				writer.write(s + (" ".repeat((int) (Math.random() * 3) + 1)));
			}
			writer.close();
		}
	}

}
