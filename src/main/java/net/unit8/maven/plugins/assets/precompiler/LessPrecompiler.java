package net.unit8.maven.plugins.assets.precompiler;

import net.unit8.maven.plugins.assets.Precompiler;
import org.apache.commons.io.FilenameUtils;
import org.lesscss.LessCompiler;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class LessPrecompiler extends Precompiler {
    private static final Logger logger = Logger.getLogger(LessPrecompiler.class.getName());

    public String getName() {
		return "less";
	}

	public String getExtension() {
		return "css";
	}

    @Override
	public boolean canPrecompile(Path file) {
		return file.toString().endsWith(".less");
	}

    @Override
	public Path precompile(Path source, Path outputDir) throws Exception {
        Files.createDirectories(outputDir);

		Path outputFile = outputDir.resolve(
				FilenameUtils.getBaseName(source.getFileName().toString())
				+ "." + getExtension());
		LessCompiler compiler = new LessCompiler();
		compiler.setEncoding(getEncoding());

		String lessCode = new String(Files.readAllBytes(source), getEncoding());
        Files.write(outputFile, compiler.compile(lessCode).getBytes(Charset.forName(getEncoding())));
        logger.fine("precompiled less(" + source +") to java(" + outputFile + ". " );
		return outputFile;
	}
}
