package runner;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import com.google.devtools.common.options.Converter;
import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionsBase;
import com.google.devtools.common.options.OptionsParsingException;

public class Enviroment extends OptionsBase {
	@Option(name = "git", abbrev = 'g', help = "Github Request Oauth Token", category = "git", defaultValue = "", converter = CharArrayConverter.class)
	public char[] gitAPI;
	@Option(name = "username", abbrev = 'u', help = "Github Username", category = "git", defaultValue = "")
	public String username;
	@Option(name = "repo", abbrev = 'r', help = "Path to repo text file", category = "git", defaultValue = "", converter = FileConverter.class)
	public File repoPath;
	@Option(name = "branch", abbrev = 'b', help = "branch of GitHub repo", category = "git", defaultValue = "")
	public String branch;
	@Option(name = "subdirectory", help = "Explicit subdirectory of files to download", category = "git", defaultValue = "")
	public String subdirectory;
	@Option(name = "files", help = "Text file containing explicit files to download", category = "git", defaultValue = "", converter = FileConverter.class)
	public File filesToDownload;
	@Option(name = "timestamp", help = "ISO 8601 timestamp in the format: YYYY-MM-DDTHH:MM:SSZ", category = "git", defaultValue = "", converter = LDTConverter.class)
	public LocalDateTime timestamp;

	@Option(name = "moss", abbrev = 'm', help = "MOSS Request User ID", category = "MOSS", defaultValue = "-1")
	public long mossID;
	@Option(name = "basefiles", help = "Directory of baseFiles for MOSS", category = "MOSS", defaultValue = "", converter = FileConverter.class)
	public File baseFiles;

	@Option(name = "codequiry", abbrev = 'c', help = "Codequiry Request API key", category = "Codequiry", defaultValue = "")
	public String codequiryAPI;
	@Option(name = "name", help = "Codequiry custom Request name", category = "Codequiry", defaultValue = "")
	public String codequiryRequestName;

	@Option(name = "language", abbrev = 'l', help = "Language of student code", category = "MOSS & Codequiry", defaultValue = "")
	public String language;

	@Option(name = "directory", abbrev = 'd', help = "Working directory to download and upload files from", category = "Git & MOSS & Codequiry", defaultValue = "", converter = FileConverter.class)
	public File directory;

	public static class FileConverter implements Converter<File> {
		@Override
		public File convert(String input) throws OptionsParsingException {
			if (input == null || input.isBlank()) {
				return null;
			}
			return new File(input);
		}

		@Override
		public String getTypeDescription() {
			return "File from String";
		}
	}

	public static class LDTConverter implements Converter<LocalDateTime> {
		@Override
		public LocalDateTime convert(String input) throws OptionsParsingException {
			if (input == null || input.isBlank()) {
				return LocalDateTime.now();
			}
			try {
				return LocalDateTime.parse(input);
			} catch (DateTimeParseException e) {
				throw new OptionsParsingException(
						"Time stamp must be in the format: YYYY-MM-DDTHH:MM:SSZ " + e.getMessage(), e);
			}
		}

		@Override
		public String getTypeDescription() {
			return "Timestamp from String";
		}
	}

	public static class CharArrayConverter implements Converter<char[]> {

		@Override
		public char[] convert(String input) throws OptionsParsingException {
			if (input == null || input.length() == 0) {
				return null;
			}
			if (input.isBlank()) {
				return new char[0];
			}
			return input.toCharArray();
		}

		@Override
		public String getTypeDescription() {
			return "Char array from String";
		}
	}
}
