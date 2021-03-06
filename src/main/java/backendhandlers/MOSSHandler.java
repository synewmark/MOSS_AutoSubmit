package backendhandlers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import it.zielke.moji.MossException;
import it.zielke.moji.SocketClient;

public class MOSSHandler {
	private static final List<String> supportedLanguages = Collections.unmodifiableList(Arrays.asList("c", "cc", "java",
			"ml", "pascal", "ada", "lisp", "schema", "haskell", "fortran", "ascii", "vhdl", "perl", "matlab", "python",
			"mips", "prolog", "spice", "vb", "csharp", "modula2", "a8086", "javascript", "plsql"));
	private final Collection<File> baseFiles = new ArrayList<>();
	private final Collection<File> submissionFiles = new ArrayList<>();
	private String language;
	private long userId = -1;

	public MOSSHandler addSubmissionFiles(Collection<File> submissionFilesToBeAdded) {
		for (File file : submissionFilesToBeAdded) {
			if (file.isDirectory()) {
				addSubmissionFiles(Arrays.asList(file.listFiles()));
			} else {
				this.submissionFiles.add(file);
			}
		}
		return this;
	}

	public MOSSHandler addSubmissionFiles(File submissionFilesToBeAdded) {
		addSubmissionFiles(Arrays.asList(submissionFilesToBeAdded.listFiles()));
		return this;
	}

	public MOSSHandler addBaseFiles(File baseFilesToBeAdded) {
		addSubmissionFiles(Arrays.asList(baseFilesToBeAdded.listFiles()));
		return this;
	}

	public MOSSHandler addBaseFiles(Collection<File> baseFilesToBeAdded) {
		for (File file : baseFilesToBeAdded) {
			if (file.isDirectory()) {
				addBaseFiles(Arrays.asList(file.listFiles()));
			} else {
				this.baseFiles.add(file);
			}
		}
		return this;
	}

	public MOSSHandler setUserId(long userId) {
		this.userId = userId;
		return this;
	}

	public MOSSHandler setLanguage(String language) {
		if (!supportedLanguages.contains(language.toLowerCase())) {
			throw new IllegalArgumentException(language + " is not supported");
		}
		this.language = language.toLowerCase();
		return this;
	}

	public URL execute() {
		if (userId == -1) {
			throw new IllegalStateException("UserID was not set!");
		}
		if (language == null) {
			throw new IllegalStateException("Language was not set!");
		}
		if (submissionFiles == null || submissionFiles.isEmpty()) {
			throw new IllegalStateException("Submission files not set!");
		}

		SocketClient socketClient = new SocketClient();

		socketClient.setUserID(String.valueOf(userId));
		URL returnUrl = null;
		try {
			socketClient.setLanguage(language);
			socketClient.run();
			if (baseFiles != null) {
				for (File f : baseFiles) {
					socketClient.uploadBaseFile(f);
				}
			}

			for (File f : submissionFiles) {
				socketClient.uploadFile(f);
			}

			socketClient.sendQuery();
			returnUrl = socketClient.getResultURL();
		} catch (MossException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			socketClient.close();
		}
		return returnUrl;
	}

	public static boolean checkLanguageSupported(String language) {
		return supportedLanguages.contains(language.toLowerCase());
	}

	public static List<String> listOfSupportedLanguages() {
		return supportedLanguages;
	}
}
