package net.unit8.maven.plugins.assets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * HTMLで読み込んでいるscript, cssをaggregateしたファイルに置き換えます。
 *
 * @phase compile
 * @goal aggregate-in-html
 * @author kawasima
 *
 */
public class AggregateInHtmlMojo extends AbstractAssetsMojo {
	/**
	 *  @parameter
	 *  @required
	 */
	protected File sourceDirectory;

	/**
	 *  @parameter
	 */
	protected File targetDirectory;

	private void rewriteAssets(Document doc, Recipe recipe, File htmlFile, String tagName, String attrName) throws XPathExpressionException {
		Set<AggregatedFile> aggregatedFiles = new HashSet<>();
		List<Element> originalScripts = new ArrayList<>();
        Elements elements = doc.select(tagName + "[" + attrName + "]");
		for (Element el : elements) {
			File srcFile = new File(htmlFile.getParent(), el.attr("src"));
			for (Rule rule : recipe.getRules()) {
				for (String component : rule.getComponents()) {
					if (FilenameUtils.equalsNormalized(
						srcFile.getAbsolutePath(),
						new File(recipe.getSourceDirectory(), component).getAbsolutePath())) {
						//aggregatedFiles.add(new AggregatedFile(rule.getTarget(), script.getParentNode()));
						//originalScripts.add(script);
					}
				}
			}
		}

		for (AggregatedFile aggregatedFile : aggregatedFiles) {
			Element script = doc.createElement(tagName);
			script.attr(attrName, aggregatedFile.getName());
			//aggregatedFile.getParentNode().appendChild(script);
		}
	}
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (targetDirectory == null)
			targetDirectory = sourceDirectory;
		Recipe recipe = readRecipe();

		Collection<File> htmlFiles = FileUtils.listFiles(sourceDirectory, new String[]{"html"}, true);
		for (File htmlFile : htmlFiles) {
			InputStream in = null;
			Document doc = null;
			try {
				in = new FileInputStream(htmlFile);
                doc = Jsoup.parse(in, encoding, null);

				// javascript
				rewriteAssets(doc, recipe, htmlFile, "script", "src");
				// stylesheet
				rewriteAssets(doc, recipe, htmlFile, "link", "href");
			} catch(Exception e) {
				throw new MojoExecutionException("Error in parsing " + htmlFile, e);
			} finally {
				IOUtils.closeQuietly(in);
			}

			TransformerFactory tf = TransformerFactory.newInstance();
			FileOutputStream fos = null;
			String relativePath = htmlFile.getAbsolutePath().substring(sourceDirectory.getAbsolutePath().length() + 1);
			File outHtmlFile = new File(targetDirectory, relativePath);
			File temp = null;
			try {
				temp = File.createTempFile("assets", ".html");
				Transformer transformer = tf.newTransformer();
				fos = new FileOutputStream(temp);
				//transformer.transform(new DOMSource(doc), new StreamResult(fos));
				FileUtils.deleteQuietly(outHtmlFile);
				if (!outHtmlFile.getParentFile().exists())
					FileUtils.forceMkdir(outHtmlFile.getParentFile());
				FileUtils.copyFile(temp, outHtmlFile);
			} catch(Exception e) {
				throw new MojoExecutionException("Error in writing to " + outHtmlFile, e);
			} finally {
				FileUtils.deleteQuietly(temp);
				IOUtils.closeQuietly(fos);
			}
		}
	}

	static class AggregatedFile {
		public AggregatedFile(String name, Node parentNode) {
			assert(name != null);
			this.name = name;
			this.parentNode = parentNode;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Node getParentNode() {
			return parentNode;
		}
		public void setParentNode(Node parentNode) {
			this.parentNode = parentNode;
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}
		@Override
		public boolean equals(Object o) {
            return o instanceof AggregatedFile && StringUtils.equals(this.name, ((AggregatedFile) o).getName());
        }

		private String name;
		private Node parentNode;
	}
}
