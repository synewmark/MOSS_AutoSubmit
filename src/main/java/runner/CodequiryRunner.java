package runner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import backendhandlers.CodequiryHandler;

public class CodequiryRunner {
	private String apiKey;
	private String language;
	private File directoriesToUpload;
	private String name;

	public CodequiryRunner(Enviroment eviroment) {
		vertifyAndPassParameters(eviroment);
	}

	private void vertifyAndPassParameters(Enviroment enviroment) {
		this.apiKey = enviroment.codequiryAPI;
		this.directoriesToUpload = enviroment.directory;
		this.language = enviroment.language;
		this.name = enviroment.codequiryRequestName;
		checkNullValues();
	}

	private void checkNullValues() {
		if (apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException("Codequiry API not set");
		}

		if (directoriesToUpload == null) {
			throw new IllegalStateException("Student Directory not set");
		}

		if (!directoriesToUpload.canRead()) {
			throw new IllegalStateException("Cannot read from Student directory: " + directoriesToUpload);
		}

		if (!directoriesToUpload.isDirectory() || directoriesToUpload.list().length == 0) {
			throw new IllegalStateException("Student directory is empty or a not a valid directory");
		}

		if (language == null) {
			throw new IllegalStateException("Language not set");
		}
	}

	public URL execute() {
		File[] filesToUpload = directoriesToUpload.listFiles();
		if (name == null || name.isEmpty()) {
			name = String.format("AutosubmitRequest: %s StudentCount: %d",
					new Date(System.currentTimeMillis()).toString(), filesToUpload.length);
		}
		CodequiryHandler codequiryHandler = new CodequiryHandler();
		codequiryHandler.setAPIKey(apiKey).setLanguage(language).setSubmissionName(name);
		for (File file : filesToUpload) {
			codequiryHandler.addDirectoryToSubmit(file);
		}
		try {
			return codequiryHandler.execute();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
