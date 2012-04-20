package net.unit8.maven.plugins.assets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

		YuiMinifier minifier = new YuiMinifier();

		try {
			for (Rule rule : recipe.getRules()) {
				List<File> files = new ArrayList<File>();
				for (String component : rule.getComponents()) {
					files.add(new File(sourceDirectory, component));
				}
				if (StringUtils.equals(FilenameUtils.getExtension(rule.getTarget()), "js")) {
					minifier.minifyJs(files, new File(targetDirectory, rule.getTarget()));
				} else if (StringUtils.equals(FilenameUtils.getExtension(rule.getTarget()), "css")) {
					minifier.minifyCss(files, new File(targetDirectory, rule.getTarget()));
				} else {
					throw new MojoExecutionException("Unknown target: " + rule.getTarget());
				}
			}
		} catch (IOException e) {
			throw new MojoExecutionException("IOError", e);
		}
	}
}
