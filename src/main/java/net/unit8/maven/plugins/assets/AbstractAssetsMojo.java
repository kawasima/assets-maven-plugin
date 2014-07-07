package net.unit8.maven.plugins.assets;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

public abstract class AbstractAssetsMojo extends AbstractMojo {
    @Parameter(property = "recipeFile", defaultValue = "recipe.yml")
	protected File recipeFile;

    @Parameter(property = "encoding", defaultValue = "UTF-8")
	protected String encoding;

    @Parameter(property = "workingDirectory", defaultValue = "target/assets-working")
	protected File workingDirectory;

    protected AbstractAssetsMojo() {
        final Logger logger = Logger.getLogger("");
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }
        final Log mavenLogger = getLog();

        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord logRecord) {
                Level lv = logRecord.getLevel();
                if (Arrays.asList(ALL, CONFIG, FINE, FINER, FINEST).contains(lv)) {
                    mavenLogger.debug(logRecord.getMessage());
                } else if (lv.equals(INFO)) {
                    mavenLogger.info(logRecord.getMessage());
                } else if (lv.equals(WARNING)) {
                    Throwable t = logRecord.getThrown();
                    if (t == null)
                        mavenLogger.warn(logRecord.getMessage());
                    else
                        mavenLogger.warn(logRecord.getMessage(), t);
                } else if (lv.equals(SEVERE)) {
                    Throwable t = logRecord.getThrown();
                    if (t == null)
                        mavenLogger.error(logRecord.getMessage());
                    else
                        mavenLogger.error(logRecord.getMessage(), t);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
    }

	protected Recipe readRecipe() throws MojoExecutionException {
		Yaml yaml = new Yaml();
		try (Reader reader = Files.newBufferedReader(recipeFile.toPath(), Charset.forName(encoding))) {
			return yaml.loadAs(reader, Recipe.class);
		} catch (IOException e) {
			throw new MojoExecutionException("Can't find recipe file", e);
		}
	}

}
