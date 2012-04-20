package net.unit8.maven.plugins.assets;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringWriter;
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
	protected File sourceDirectory;

	public void execute() throws MojoExecutionException, MojoFailureException {
		Recipe recipe = readRecipe();
		HTMLConfiguration config = new HTMLConfiguration();
		config.setProperty("http://cyberneko.org/html/properties/names/elems", "match");
		DOMParser parser = new DOMParser(config);
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		Collection<File> htmlFiles = FileUtils.listFiles(sourceDirectory, new String[]{"html"}, true);
		for (File htmlFile : htmlFiles) {
			Reader in = null;
			try {
				in = new FileReader(htmlFile);
				parser.parse(new InputSource(in));
				Document doc = parser.getDocument();
				NodeList scripts = (NodeList)xpath.evaluate("//script", doc, XPathConstants.NODESET);
				Set<AggregatedFile> aggregatedFiles = new HashSet<AggregatedFile>();
				List<Element> originalScripts = new ArrayList<Element>();
				for (int i=0; i<scripts.getLength(); i++) {
					Element script = (Element)scripts.item(i);
					Node srcAttr = script.getAttributes().getNamedItem("src");
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
					Element script = doc.createElement("script");
					script.setAttribute("src", aggregatedFile.getName());
					aggregatedFile.getParentNode().appendChild(script);
				}
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer transformer = tf.newTransformer();
				StringWriter out = new StringWriter();
				transformer.transform(new DOMSource(doc), new StreamResult(out));
				System.out.println(out.toString());
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				IOUtils.closeQuietly(in);
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
