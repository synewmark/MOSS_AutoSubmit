package gitHandler.model;

import java.net.MalformedURLException;
import java.net.URL;

public class URLbyComponents {
	private final String host;
	private final String username;
	private final String repoName;
	private final String path;
	private final String branch;

	private final URL fullURL;

	public URLbyComponents(String[] components) throws MalformedURLException {
		if (components.length != 5) {
			throw new IllegalArgumentException(
					"Passed array must be comprised of a host, Username, RepoName, and path");
		}
		host = components[0];
		username = components[1];
		repoName = components[2];
		path = components[3];
		branch = components[4];
		fullURL = initializeURL();
	}

	public URLbyComponents(String host, String username, String repoName, String path, String branch)
			throws MalformedURLException {
		this.host = host;
		this.username = username;
		this.repoName = repoName;
		this.path = path;
		this.branch = branch;
		fullURL = initializeURL();
	}

	private URL initializeURL() throws MalformedURLException {
		try {
			return new URL(host + username + '/' + repoName + "tree/" + branch + '/' + path);
		} catch (MalformedURLException e) {
			throw new MalformedURLException("host + username + '/' + repoName + tree/ + branch + '/' + path");
		}
	}

	public String getHost() {
		return host;
	}

	public String getUsername() {
		return username;
	}

	public String getRepoName() {
		return repoName;
	}

	public String getPath() {
		return path;
	}

	public String getBranch() {
		return branch;
	}

	public URL getFullURL() {
		return fullURL;
	}

	@Override
	public String toString() {
		return fullURL.toString();
	}

}
