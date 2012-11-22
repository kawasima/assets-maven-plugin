package net.unit8.maven.plugins.assets.precompiler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import net.unit8.maven.plugins.assets.Precompiler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class CoffeePrecompiler extends Precompiler {
	private Scriptable globalScope;

	public CoffeePrecompiler() {
		initCoffeescriptCompiler();
	}

	@Override
	public String getName() {
		return "coffee";
	}

	@Override
	public String getExtension() {
		return "js";
	}

	@Override
	public boolean canPrecompile(File source) {
		return StringUtils.equals(FilenameUtils.getExtension(source.getName()),
				"coffee");
	}

	@Override
	public File precompile(File source, File target) throws Exception {
		String coffeeScriptSource = FileUtils.readFileToString(source);
		Context context = Context.enter();
		try {
			Scriptable compileScope = context.newObject(globalScope);
			compileScope.setParentScope(globalScope);
			compileScope.put("coffeeScriptSource", compileScope, coffeeScriptSource);
			String compiledStr = (String) context.evaluateString(
					compileScope,
					"CoffeeScript.compile(coffeeScriptSource, {});",
					source.getAbsolutePath(), 0, null);
			File outputFile = new File(target,
					FilenameUtils.getBaseName(source.getName()) + "." + getExtension());
			FileUtils.writeStringToFile(outputFile, compiledStr, "UTF-8");
			return outputFile;
		} finally {
			Context.exit();
		}
	}

	private void initCoffeescriptCompiler() throws Error {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader
				.getResourceAsStream("org/jcoffeescript/coffee-script.js");
		try {
			try {
				Reader reader = new InputStreamReader(inputStream, "UTF-8");
				try {
					Context context = Context.enter();
					context.setOptimizationLevel(-1); // Without this, Rhino
														// hits a 64K bytecode
														// limit and fails
					try {
						globalScope = context.initStandardObjects();
						context.evaluateReader(globalScope, reader,
								"coffee-script.js", 0, null);
					} finally {
						Context.exit();
					}
				} finally {
					reader.close();
				}
			} catch (UnsupportedEncodingException e) {
				throw new Error(e); // This should never happen
			} finally {
				inputStream.close();
			}
		} catch (IOException e) {
			throw new Error(e); // This should never happen
		}

	}

}
