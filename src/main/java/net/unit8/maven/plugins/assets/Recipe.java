package net.unit8.maven.plugins.assets;

import java.util.List;

public class Recipe {
	private String targetDirectory;
	private String sourceDirectory;

	private List<String> precompilers;
	private List<Rule> rules;

	public String getTargetDirectory() {
		return targetDirectory;
	}
	public void setTargetDirectory(String targetDirectory) {
		this.targetDirectory = targetDirectory;
	}
	public String getSourceDirectory() {
		return sourceDirectory;
	}
	public void setSourceDirectory(String sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	public List<String> getPrecompilers() {
		return precompilers;
	}

	public void setPrecompilers(List<String> precompilers) {
		this.precompilers = precompilers;
	}

	public List<Rule> getRules() {
		return rules;
	}
	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}
}
