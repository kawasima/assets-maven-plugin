package net.unit8.maven.plugins.assets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.xerces.parsers.DOMParser;
import org.cyberneko.html.HTMLConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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

	private void rewriteAssets(Document doc, Recipe recipe, File htmlFile, String tagName, String attrName, String optCondition) throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		String xpathStr = "//" + tagName;
		if (optCondition != null) {
			xpathStr += optCondition;
		}
		NodeList scripts = (NodeList)xpath.evaluate(xpathStr, doc, XPathConstants.NODESET);
		Set<AggregatedFile> aggregatedFiles = new HashSet<AggregatedFile>();
		List<Element> originalScripts = new ArrayList<Element>();
		for (int i=0; i<scripts.getLength(); i++) {
			Element script = (Element)scripts.item(i);
			Node srcAttr = script.getAttributes().getNamedItem(attrName);
			if (srcAttr == null)
				continue;
			File srcFile = new File(htmlFile.getParent(), srcAttr.getNodeValue());
			for (Rule rule : recipe.getRules()) {
				for (String component : rule.getComponents()) {
					if (FilenameUtils.equalsNormalized(
						srcFile.getAbsolutePath(),
						new File(recipe.getSourceDirectory(), component).getAbsolutePath())) {
						aggregatedFiles.add(new AggregatedFile(rule.getTarget(), script.getParentNode()));
						originalScripts.add(script);
					}
				}
			}
		}

		for (Element el : originalScripts) {
			el.getParentNode().removeChild(el);
		}
		for (AggregatedFile aggregatedFile : aggregatedFiles) {
			Element script = doc.createElement(tagName);
			script.setAttribute(attrName, aggregatedFile.getName());
			aggregatedFile.getParentNode().appendChild(script);
		}
	}
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (targetDirectory == null)
			targetDirectory = sourceDirectory;
		Recipe recipe = readRecipe();
		HTMLConfiguration config = new HTMLConfiguration();
		config.setProperty("http://cyberneko.org/html/properties/names/elems", "match");
		DOMParser parser = new DOMParser(config);

		Collection<File> htmlFiles = FileUtils.listFiles(sourceDirectory, new String[]{"html"}, true);
		for (File htmlFile : htmlFiles) {
			Reader in = null;
			Document doc = null;
			try {
				in = new FileReader(htmlFile);
				parser.parse(new InputSource(in));
				doc = parser.getDocument();

				// javascript
				rewriteAssets(doc, recipe, htmlFile, "script", "src", null);
				// stylesheet
				rewriteAssets(doc, recipe, htmlFile, "link", "href", "[@rel='stylesheet']");
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
				transformer.transform(new DOMSource(doc), new StreamResult(fos));
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
			if (!(o instanceof AggregatedFile))
				return false;
			return StringUtils.equals(this.name, ((AggregatedFile)o).getName());
		}

		private String name;
		private Node parentNode;
	}
}
