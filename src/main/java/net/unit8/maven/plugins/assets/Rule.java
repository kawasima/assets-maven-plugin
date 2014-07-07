package net.unit8.maven.plugins.assets;

import org.apache.commons.io.FilenameUtils;

import java.util.List;

public class Rule {
	private String target;
	private String version;
	private Boolean minify = false;
	private List<String> components;

	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}

	public String getVersioningTarget() {
		if (version != null) {
			return FilenameUtils.concat(
				FilenameUtils.getPath(target),
				FilenameUtils.getBaseName(target)
					+ "-" + version
					+ "." + FilenameUtils.getExtension(target)
			);
		} else {
			return target;
		}
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public Boolean getMinify() {
		return minify;
	}
	public void setMinify(Boolean minify) {
		this.minify = minify;
	}

	public List<String> getComponents() {
		return components;
	}
	public void setComponents(List<String> components) {
		this.components = components;
	}
}
