package runner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import backendhandlers.MOSSHandler;

public class MOSSRunner {
	private long mossID;
	private String language;
	private File studentFileDirectory;
	private File baseFileDirectory;

	public MOSSRunner(Enviroment enviroment) {
		vertifyAndPassParameters(enviroment);
	}

	private void vertifyAndPassParameters(Enviroment enviroment) {
		this.mossID = enviroment.mossID;
		this.studentFileDirectory = enviroment.directory;
		this.baseFileDirectory = enviroment.baseFiles;
		this.language = enviroment.language;
		checkNullValues();
	}

	private void checkNullValues() {
		if (mossID < 0) {
			throw new IllegalStateException("MOSS id not set");
		}

		if (studentFileDirectory == null) {
			throw new IllegalStateException("Student File Directory not set");
		}

		if (!studentFileDirectory.canRead()) {
			throw new IllegalStateException("Cannot read from Student directory: " + studentFileDirectory);
		}

		if (baseFileDirectory != null && !baseFileDirectory.canRead()) {
			throw new IllegalStateException("Cannot read from Base File directory: " + baseFileDirectory);
		}

		if (language == null) {
			throw new IllegalStateException("Language not set");
		}
	}

	public String execute() {
		MOSSHandler mossHandler = new MOSSHandler();
		mossHandler.setLanguage(language).addSubmissionFiles(studentFileDirectory).setUserId(mossID);
		if (baseFileDirectory != null) {
			mossHandler.addBaseFiles(baseFileDirectory);
		}
		URL resultsURL = mossHandler.execute();
		File mossResultsFile = new File(studentFileDirectory.getParentFile(),
				"MossRequestResults_" + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) + ".htm");
		try {
			downloadFromURL(resultsURL, mossResultsFile, Integer.MAX_VALUE);
			System.out.println();
			System.out.println("Local results saved to: " + mossResultsFile);
		} catch (IOException e) {
			System.err.println();
			System.err.println("Could not automatically download MOSS results!");
			System.err.println("Make sure to save the results independently");
		}
		return resultsURL.toString();
	}

	private void downloadFromURL(URL urlToDownloadFrom, File fileToDownloadTo, int fileSize) throws IOException {
		ReadableByteChannel readableByteChannel = Channels.newChannel(urlToDownloadFrom.openStream());
		FileOutputStream fileOutputStream = new FileOutputStream(fileToDownloadTo);
		fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, fileSize);
		readableByteChannel.close();
		fileOutputStream.close();
	}
}
