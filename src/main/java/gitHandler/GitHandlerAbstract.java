package gitHandler;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;

import gitHandler.model.URLbyComponents;
import utils.FileUtils;

public abstract class GitHandlerAbstract {
	URLbyComponents urlToDownload;
	LocalDateTime dateToDownload;
	File directoryToDownloadTo;
	char[] oauthToken;
	Collection<String> explicitFilesToDownload;

	public GitHandlerAbstract setURL(URLbyComponents urlToDownload) {
		this.urlToDownload = urlToDownload;
		return this;
	}

	public GitHandlerAbstract setDate(LocalDateTime dateToDownload) {
		this.dateToDownload = dateToDownload;
		return this;
	}

	public GitHandlerAbstract setDir(File directoryToDownloadTo) {
		if (!FileUtils.checkWriteAccessOfDir(directoryToDownloadTo)) {
			throw new IllegalArgumentException("Cannot write to dir: " + directoryToDownloadTo);
		}

		this.directoryToDownloadTo = directoryToDownloadTo;
		return this;
	}

	public GitHandlerAbstract setCredentials(char[] oauthToken) {
		this.oauthToken = oauthToken;
		return this;
	}

	public GitHandlerAbstract setFilesToDownload(Collection<String> explicitFilesToDownload) {
		this.explicitFilesToDownload = explicitFilesToDownload;
		return this;
	}

	public abstract void execute() throws IOException;
}
