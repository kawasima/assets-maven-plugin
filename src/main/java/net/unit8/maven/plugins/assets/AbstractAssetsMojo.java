package net.unit8.maven.plugins.assets;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.yaml.snakeyaml.Yaml;

public abstract class AbstractAssetsMojo extends AbstractMojo {
	/**
	 * @parameter expression="${recipeFile}" default="recipe.yml"
	 */
	protected File recipeFile;

	protected Recipe readRecipe() throws MojoExecutionException {
		Yaml yaml = new Yaml();
		Reader in = null;
		try {
			in = new FileReader(recipeFile);
			Recipe recipe = yaml.loadAs(in, Recipe.class);
			return recipe;
		} catch (IOException e) {
			throw new MojoExecutionException("Can't find recipe file", e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

}
