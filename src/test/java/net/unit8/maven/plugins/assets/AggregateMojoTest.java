package net.unit8.maven.plugins.assets;

import java.io.File;

import org.junit.Test;

public class AggregateMojoTest extends AggregateMojo {

	@Test
	public void test() throws Exception {
		AggregateMojo mojo = new AggregateMojo();
		mojo.recipeFile = new File("src/test/resources/recipe.yml");
		mojo.execute();
	}

}
