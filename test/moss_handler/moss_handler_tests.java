package moss_handler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class moss_handler_tests {
	@Test
	public void testJavaNoBaseFiles() throws IOException {
		createExampleFiles();
		System.out.println(new MOSS_handler().setLanguage("java")
				.addSubmissionFiles(Arrays.asList(new File(File.separator + "studentCode").listFiles()))
				.setUserId(884640278).execute());
		cleanup(new File(File.separator + "studentCode"));
	}

	public void createExampleFiles() throws IOException {
		String[] testCode = { "public", "static", "void", "main(", "String[]", "args", ")", "{\n", "System.out.println",
				"(", "Hello", "World!", ")", ";\n", "}" };
		for (int i = 0; i < 10; i++) {
			File file = new File(File.separator + "studentCode" + File.separator + "student" + i + File.separator
					+ "HelloWorld.java");
			file.getParentFile().mkdirs();
			FileWriter writer = new FileWriter(file);
			for (String s : testCode) {
				writer.write(s + (" ".repeat((int) (Math.random() * 3) + 1)));
			}
			writer.close();
		}
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
