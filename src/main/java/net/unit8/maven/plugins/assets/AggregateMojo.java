package net.unit8.maven.plugins.assets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.unit8.maven.plugins.assets.aggregator.SimpleAggregator;
import net.unit8.maven.plugins.assets.aggregator.YuiAggregator;
import net.unit8.maven.plugins.assets.precompiler.CoffeePrecompiler;
import net.unit8.maven.plugins.assets.precompiler.LessPrecompiler;

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
	/**
	 * @parameter
	 */
	protected List<Class<? extends Precompiler>> precompilerClasses;

	protected Map<String, Precompiler> availablePrecompilers = new HashMap<String, Precompiler>();

	public void initializePrecompilers() throws MojoExecutionException {
		if (precompilerClasses == null) {
			precompilerClasses = new ArrayList<Class<? extends Precompiler>>();
			precompilerClasses.add(LessPrecompiler.class);
			precompilerClasses.add(CoffeePrecompiler.class);
		}

		for (Class<? extends Precompiler> precompilerClass : precompilerClasses) {
			try {
				Precompiler precompiler = precompilerClass.newInstance();
				if (encoding != null)
					precompiler.setEncoding(encoding);
				availablePrecompilers.put(precompiler.getName(), precompiler);
			} catch (Exception e) {
				throw new MojoExecutionException(precompilerClass + " can't be instantiated.", e);
			}
		}
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		initializePrecompilers();
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
					File componentFile = new File(sourceDirectory, component);
					for (String precompilerName : recipe.getPrecompilers()) {
						Precompiler precompiler = availablePrecompilers.get(precompilerName);
						if (precompiler == null)
							throw new MojoExecutionException("Can't find precompiler " + precompilerName);
						if (precompiler.canPrecompile(componentFile)) {
							try {
								File outputDir = new File(workingDirectory, precompiler.getName());
								componentFile = precompiler.precompile(componentFile, outputDir);
							} catch (Exception e) {
								throw new MojoExecutionException("Precompile error.", e);
							}
						}
					}
					files.add(componentFile);
				}
				if (StringUtils.equals(FilenameUtils.getExtension(rule.getTarget()), "js")) {
					aggregator.aggregateJs(files, new File(targetDirectory, rule.getVersioningTarget()));
				} else if (StringUtils.equals(FilenameUtils.getExtension(rule.getTarget()), "css")) {
					aggregator.aggregateCss(files, new File(targetDirectory, rule.getVersioningTarget()));
				} else {
					throw new MojoExecutionException("Unknown target: " + rule.getTarget());
				}
			}
		} catch (IOException e) {
			throw new MojoExecutionException("IOError", e);
		}
	}
}
