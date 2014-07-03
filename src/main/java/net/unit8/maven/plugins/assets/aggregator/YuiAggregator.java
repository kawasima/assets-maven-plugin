package net.unit8.maven.plugins.assets.aggregator;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import net.unit8.maven.plugins.assets.Aggregator;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class YuiAggregator extends Aggregator {
	private String minifyJsSingle(final Path file) throws IOException {
		try (Reader in = Files.newBufferedReader(file, Charset.forName(getEncoding()))){
			JavaScriptCompressor compressor = new JavaScriptCompressor(in,
					new ErrorReporter() {
						public void warning(String message, String sourceName,
								int line, String lineSource, int lineOffset) {
							if (line < 0) {
								System.err.println("[WARNING] " + file.getFileName() + ":" + message);
							} else {
								System.err.println("[WARNING] " + file.getFileName() + ":" + line + ':'
										+ lineOffset + ':' + message);
							}
						}

						public void error(String message, String sourceName,
								int line, String lineSource, int lineOffset) {
							if (line < 0) {
								System.err.println("[ERROR] " + file.getFileName() + ":" + message);
							} else {
								System.err.println("[ERROR] " + file.getFileName() + ":" + line + ':'
										+ lineOffset + ':' + message);
							}
						}

						public EvaluatorException runtimeError(String message,
								String sourceName, int line, String lineSource,
								int lineOffset) {
							error(message, sourceName, line, lineSource,
									lineOffset);
							return new EvaluatorException(message);
						}
					});

			StringWriter writer = new StringWriter();
			compressor.compress(writer, -1, false, false, false, false);
			return writer.toString();
		}
	}

	private String minifyCssSingle(Reader in) throws IOException {
		CssCompressor compressor = new CssCompressor(in);
		StringWriter writer = new StringWriter();
		compressor.compress(writer, -1);
		return writer.toString();
	}

	/**
	 * Minify javascript files.
	 *
	 */
	public void aggregateJs(List<Path> files, Path outputFile) throws IOException {
		if (Files.notExists(outputFile.getParent()))
            Files.createDirectories(outputFile.getParent());

		try (Writer out = Files.newBufferedWriter(outputFile, Charset.forName(getEncoding()))) {
			for (Path file : files) {
				out.write(minifyJsSingle(file));
			}
		}
	}

	/**
	 * Minify css files.
	 *
	 */
	public void aggregateCss(List<Path> files, Path outputFile) throws IOException {
		if (Files.notExists(outputFile.getParent()))
			Files.createDirectories(outputFile.getParent());

		try (Writer out = Files.newBufferedWriter(outputFile, Charset.forName(getEncoding()))){
			for (Path file : files) {
                try (Reader in = Files.newBufferedReader(file, Charset.forName(getEncoding()))) {
					out.write(minifyCssSingle(in));
				}
			}
		}
	}
}
