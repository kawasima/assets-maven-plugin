/**
 *
 */
package net.unit8.maven.plugins.assets;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

/**
 * @author kawasima
 *
 */
public class AggregateInHtmlMojoTest extends AggregateInHtmlMojo {

	@Test
	public void test() throws Exception{
		AggregateInHtmlMojo mojo = new AggregateInHtmlMojo();
		mojo.recipeFile = new File("src/test/resources/recipe.yml");
		mojo.sourceDirectory = new File("src/test/resources/html");
		mojo.execute();
	}

}
