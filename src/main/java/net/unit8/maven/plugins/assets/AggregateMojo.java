package net.unit8.maven.plugins.assets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.unit8.maven.plugins.assets.aggregator.SimpleAggregator;
import net.unit8.maven.plugins.assets.aggregator.YuiAggregator;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @phase compile
 * @goal aggregate
 * @author kawasima
 */
public class AggregateMojo extends AbstractAssetsMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {
		Recipe recipe = readRecipe();
		File sourceDirectory = (recipe.getSourceDirectory() != null) ? new File(recipe.getSourceDirectory()) : new File(".");
		File targetDirectory = (recipe.getTargetDirectory() != null) ? new File(recipe.getTargetDirectory()) : new File(".");

		Aggregator simpleAggregator = new SimpleAggregator();
		Aggregator minifyAggregator = new YuiAggregator();

		try {
			for (Rule rule : recipe.getRules()) {
				Aggregator aggregator = rule.getMinify() ? minifyAggregator : simpleAggregator;
				aggregator.setEncoding(encoding);

				List<File> files = new ArrayList<File>();
				for (String component : rule.getComponents()) {
					files.add(new File(sourceDirectory, component));
				}
				if (StringUtils.equals(FilenameUtils.getExtension(rule.getTarget()), "js")) {
					aggregator.aggregateJs(files, new File(targetDirectory, rule.getTarget()));
				} else if (StringUtils.equals(FilenameUtils.getExtension(rule.getTarget()), "css")) {
					aggregator.aggregateCss(files, new File(targetDirectory, rule.getTarget()));
				} else {
					throw new MojoExecutionException("Unknown target: " + rule.getTarget());
				}
			}
		} catch (IOException e) {
			throw new MojoExecutionException("IOError", e);
		}
	}
}
