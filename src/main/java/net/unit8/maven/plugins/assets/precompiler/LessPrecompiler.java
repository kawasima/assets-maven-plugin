package net.unit8.maven.plugins.assets.precompiler;

import java.io.File;

import net.unit8.maven.plugins.assets.Precompiler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.lesscss.LessCompiler;

public class LessPrecompiler extends Precompiler {
	public String getName() {
		return "less";
	}

	public String getExtension() {
		return "css";
	}
	public boolean canPrecompile(File file) {
		return StringUtils.equals(FilenameUtils.getExtension(file.getName()), "less");
	}

	public File precompile(File source, File outputDir) throws Exception {
		FileUtils.forceMkdir(outputDir);
		File outputFile = new File(outputDir,
				FilenameUtils.getBaseName(source.getName())
				+ "." + getExtension());
		LessCompiler compiler = new LessCompiler();
		compiler.compile(source, outputFile);
		return outputFile;
	}
}
