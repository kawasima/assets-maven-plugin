package net.unit8.maven.plugins.assets;

import java.io.File;

import org.junit.Test;

public class AggregateMojoTest {

	@Test
	public void test() throws Exception {
		AggregateMojo mojo = new AggregateMojo();
		mojo.encoding = "UTF-8";
		mojo.recipeFile = new File("src/test/resources/recipe.yml");
		mojo.workingDirectory = new File("target/working-assets");
		mojo.execute();
	}

}
