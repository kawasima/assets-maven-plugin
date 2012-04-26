package net.unit8.maven.plugins.assets;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class Aggregator {
	private String encoding;

	public abstract void aggregateJs(List<File> files, File outputFile)
			throws IOException;

	public abstract void aggregateCss(List<File> files, File outputFile)
			throws IOException;

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

}