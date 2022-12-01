package backendhandlers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import src.CodequirySDK;
import src.model.Check;
import utils.FileUtils;

public class CodequiryHandler {

	// for some baffling reason the createCheck endpoint doesn't actually accept a
	// language string like the API docs suggest
	// instead it accepts an int which corresponds to a specific language
	// the map translates the language name to the correct integer
	private static final Map<String, Integer> langaugeMap = getPopulatedLanguageMap();

	private String apiKey;
	private String checkName;
	private Integer language;
	private SortedSet<File> setOfDirectoriesToSubmit = new TreeSet<>();

	public CodequiryHandler setLanguage(String language) {
		language = language.toLowerCase();
		if (!langaugeMap.containsKey(language)) {
			throw new IllegalArgumentException("Langauge unsupported");
		}
		this.language = langaugeMap.get(language);
		return this;
	}

	public CodequiryHandler setAPIKey(String apiKey) {
		this.apiKey = apiKey;
		return this;
	}

	public CodequiryHandler setSubmissionName(String checkName) {
		this.checkName = checkName;
		return this;
	}

	public CodequiryHandler addDirectoryToSubmit(File directoryToSubmit) {
		setOfDirectoriesToSubmit.add(directoryToSubmit);
		return this;
	}

	public URL execute() throws IOException {
		prechecks();
		CodequirySDK api = new CodequirySDK(apiKey);
		Check check = api.createCheck(checkName, Integer.toString(language));
		SortedSet<File> zipFilesToUpload = new TreeSet<>();
		for (File directoryToSubmit : setOfDirectoriesToSubmit) {
			File fileToUpload = FileUtils.zipDirectory(directoryToSubmit,
					new File(directoryToSubmit.getParent(), String.format("%sZip_FileCount%d.zip",
							FileUtils.getFileName(directoryToSubmit), FileUtils.getNumberOfFiles(directoryToSubmit))));
			zipFilesToUpload.add(fileToUpload);
			fileToUpload.deleteOnExit();
		}
		for (File uploadFile : zipFilesToUpload) {
			api.upload(check.getId(), uploadFile.toString());
		}
		try {
			return new URL(String.format("https://dashboard.codequiry.com/course/%d/assignment/%d/submission",
					check.getCourse_id(), check.getId()));
		} catch (MalformedURLException e) {
			throw new IOException("Invalid response from the server", e);
		}

	}

	private void prechecks() {
		if (apiKey == null) {
			throw new IllegalArgumentException("API Key not set");
		}

		if (checkName == null) {
			throw new IllegalArgumentException("Check name not set");
		}

		if (language == null) {
			throw new IllegalArgumentException("Language not set");
		}

		if (setOfDirectoriesToSubmit.isEmpty()) {
			throw new IllegalArgumentException("Directories empty");
		}

		List<File> overlap;
		if ((overlap = FileUtils.checkOverlappingDirectories(setOfDirectoriesToSubmit)) != null) {
			throw new IllegalArgumentException(
					String.format("Directory A: %s Overlaps directory B: %s", overlap.get(0), overlap.get(1)));
		}
	}

	private static Map<String, Integer> getPopulatedLanguageMap() {
		// todo: finish manually populating map
		final HashMap<String, Integer> map = new HashMap<>();
		map.put("java", 13);
		map.put("python", 14);
		map.put("c", 17);
		map.put("c++", 17);
		map.put("c#", 18);
		map.put("perl", 20);
		map.put("php", 21);
		map.put("sql", 22);
		return Collections.unmodifiableMap(map);
	}
}
