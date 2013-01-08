package net.unit8.maven.plugins.assets.precompiler;

import net.unit8.maven.plugins.assets.Precompiler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.lesscss.LessCompiler;

import java.io.File;

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

		compiler.setEncoding(getEncoding());
        compiler.setCustomJs(getClass().getClassLoader().getResource("envjs-patch.js"));
		String lessCode = FileUtils.readFileToString(source, getEncoding());

		FileUtils.writeStringToFile(outputFile,
				compiler.compile(lessCode),
				getEncoding());
		return outputFile;
	}
}