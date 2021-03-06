package net.unit8.maven.plugins.assets.aggregator;

import net.unit8.maven.plugins.assets.Aggregator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class SimpleAggregator extends Aggregator {
    private static final Logger logger = Logger.getLogger(SimpleAggregator.class.getName());
    @Override
	public void aggregateJs(List<Path> files, Path outputFile) throws IOException {
		if (Files.notExists(outputFile.getParent()))
            Files.createDirectories(outputFile.getParent());

        for (Path file : files) {
            Files.write(outputFile, Files.readAllLines(file, Charset.forName(getEncoding())),
                    Charset.forName(getEncoding()),
                    CREATE ,APPEND);
        }
        logger.fine("Aggregated javascripts to " + outputFile + ".");
    }

    @Override
	public void aggregateCss(List<Path> files, Path outputFile) throws IOException {
        if (Files.notExists(outputFile.getParent()))
            Files.createDirectories(outputFile.getParent());

        for (Path file : files) {
            Files.write(outputFile, Files.readAllLines(file, Charset.forName(getEncoding())),
                    Charset.forName(getEncoding()),
                    CREATE, APPEND);
        }
        logger.fine("Aggregated stylesheets to " + outputFile + ".");
	}

}
