package net.unit8.maven.plugins.assets;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Aggregator class.
 *
 * @author kawasima
 */
public abstract class Aggregator {
	private String encoding;

	public abstract void aggregateJs(List<Path> files, Path outputFile)
			throws IOException;

	public abstract void aggregateCss(List<Path> files, Path outputFile)
			throws IOException;

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

}