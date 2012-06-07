package net.unit8.maven.plugins.assets.aggregator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import net.unit8.maven.plugins.assets.Aggregator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

public class YuiAggregator extends Aggregator {
	private String minifyJsSingle(final File file) throws IOException {
		Reader in = null;
		try {
			in = new InputStreamReader(new FileInputStream(file), getEncoding());
			JavaScriptCompressor compressor = new JavaScriptCompressor(in,
					new ErrorReporter() {
						public void warning(String message, String sourceName,
								int line, String lineSource, int lineOffset) {
							if (line < 0) {
								System.err.println("[WARNING] " + file.getName() + ":" + message);
							} else {
								System.err.println("[WARNING] " + file.getName() + ":" + line + ':'
										+ lineOffset + ':' + message);
							}
						}

						public void error(String message, String sourceName,
								int line, String lineSource, int lineOffset) {
							if (line < 0) {
								System.err.println("[ERROR] " + file.getName() + ":" + message);
							} else {
								System.err.println("[ERROR] " + file.getName() + ":" + line + ':'
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
		} finally {
			IOUtils.closeQuietly(in);
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
	 * @see net.unit8.maven.plugins.assets.minifier.Aggregator#aggregateJs(java.util.List, java.io.File)
	 */
	public void aggregateJs(List<File> files, File outputFile) throws IOException {
		if (!outputFile.getParentFile().exists())
			FileUtils.forceMkdir(outputFile.getParentFile());
		Writer out = null;
		try {
			out = new OutputStreamWriter(new FileOutputStream(outputFile), getEncoding());
			for (File file : files) {
				out.write(minifyJsSingle(file));
			}
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * Minify css files.
	 *
	 * @see net.unit8.maven.plugins.assets.minifier.Aggregator#aggregateCss(java.util.List, java.io.File)
	 */
	public void aggregateCss(List<File> files, File outputFile) throws IOException {
		if (!outputFile.getParentFile().exists())
			FileUtils.forceMkdir(outputFile.getParentFile());
		Writer out = null;
		try {
			out = new OutputStreamWriter(new FileOutputStream(outputFile), getEncoding());
			for (File file : files) {
				Reader in = null;
				try {
					in = new InputStreamReader(new FileInputStream(file), getEncoding());
					out.write(minifyCssSingle(in));
				} finally {
					IOUtils.closeQuietly(in);
				}
			}
		} finally {
			IOUtils.closeQuietly(out);
		}
	}
}
