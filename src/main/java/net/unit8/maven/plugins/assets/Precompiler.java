package net.unit8.maven.plugins.assets;

import java.io.File;

public abstract class Precompiler {
	public abstract String getName();
	public abstract String getExtension();
	public abstract boolean canPrecompile(File source);
	public abstract File precompile(File source, File target) throws Exception;
}
