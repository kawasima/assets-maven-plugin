package net.unit8.maven.plugins.assets.aggregator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import net.unit8.maven.plugins.assets.Aggregator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class SimpleAggregator extends Aggregator {
	public void aggregateJs(List<File> files, File outputFile) throws IOException {
		if (!outputFile.getParentFile().exists())
			FileUtils.forceMkdir(outputFile.getParentFile());
		Writer out = null;
		try {
			out = new OutputStreamWriter(new FileOutputStream(outputFile), getEncoding());
			for (File file : files) {
				out.write(FileUtils.readFileToString(file, getEncoding()));
			}
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public void aggregateCss(List<File> files, File outputFile) throws IOException {
		if (!outputFile.getParentFile().exists())
			FileUtils.forceMkdir(outputFile.getParentFile());
		Writer out = null;
		try {
			out = new OutputStreamWriter(new FileOutputStream(outputFile), getEncoding());
			for (File file : files) {
				out.write(FileUtils.readFileToString(file, getEncoding()));
			}
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

}
