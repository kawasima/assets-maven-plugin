package net.unit8.maven.plugins.assets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.yaml.snakeyaml.Yaml;

public abstract class AbstractAssetsMojo extends AbstractMojo {
	/**
	 * @parameter expression="${recipeFile}" default-value="recipe.yml"
	 */
	protected File recipeFile;

	/**
	 * @parameter expression="${encoding}" default-value="UTF-8"
	 */
	protected String encoding;

	protected Recipe readRecipe() throws MojoExecutionException {
		Yaml yaml = new Yaml();
		Reader in = null;
		try {
			in = new InputStreamReader(new FileInputStream(recipeFile), encoding);
			Recipe recipe = yaml.loadAs(in, Recipe.class);
			return recipe;
		} catch (IOException e) {
			throw new MojoExecutionException("Can't find recipe file", e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

}
