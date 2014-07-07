package net.unit8.maven.plugins.assets;

import java.nio.file.Path;

/**
 * Precompiler class.
 *
 * @author kawasima
 */
public abstract class Precompiler {
	private String encoding = "UTF-8";

	public abstract String getName();
	public abstract String getExtension();
	public abstract boolean canPrecompile(Path source);
	public abstract Path precompile(Path source, Path target) throws Exception;

	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}


}
