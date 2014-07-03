package net.unit8.maven.plugins.assets;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;

public abstract class AbstractAssetsMojo extends AbstractMojo {
	/**
	 * @parameter expression="${recipeFile}" default-value="recipe.yml"
	 */
	protected File recipeFile;

	/**
	 * @parameter expression="${encoding}" default-value="UTF-8"
	 */
	protected String encoding;

	/**
	 * @parameter expression="${workingDirectory}" default-value="target/assets-working"
	 */
	protected File workingDirectory;

	protected Recipe readRecipe() throws MojoExecutionException {
		Yaml yaml = new Yaml();
		try (Reader reader = Files.newBufferedReader(recipeFile.toPath(), Charset.forName(encoding))) {
			return yaml.loadAs(reader, Recipe.class);
		} catch (IOException e) {
			throw new MojoExecutionException("Can't find recipe file", e);
		}
	}

}
