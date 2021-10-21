package gitHandler.model;

public class GitFileRepresentation {

	@Override
	public String toString() {
		return "GitFileRepresentation [type=" + type + ", size=" + size + ", path=" + path + ", sha=" + sha + ", url="
				+ url + ", git_url=" + git_url + ", html_url=" + html_url + ", download_url=" + download_url + "]";
	}

	private BranchType type;
	private int size;
	private String path;
	private String sha;
	private String url;
	private String git_url;
	private String html_url;
	private String download_url;

	public BranchType getType() {
		return type;
	}

	public void setType(BranchType type) {
		this.type = type;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getSha() {
		return sha;
	}

	public void setSha(String sha) {
		this.sha = sha;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getGit_url() {
		return git_url;
	}

	public void setGit_url(String git_url) {
		this.git_url = git_url;
	}

	public String getHtml_url() {
		return html_url;
	}

	public void setHtml_url(String html_url) {
		this.html_url = html_url;
	}

	public String getDownload_url() {
		return download_url;
	}

	public void setDownload_url(String download_url) {
		this.download_url = download_url;
	}

	public enum BranchType {
		file, dir, submodule
	}
}
