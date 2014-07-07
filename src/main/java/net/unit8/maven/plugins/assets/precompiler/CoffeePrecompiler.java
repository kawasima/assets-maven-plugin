package net.unit8.maven.plugins.assets.precompiler;

import net.unit8.maven.plugins.assets.Precompiler;
import org.apache.commons.io.FilenameUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.logging.Logger;

public class CoffeePrecompiler extends Precompiler {
    private static final Logger logger = Logger.getLogger(CoffeePrecompiler.class.getName());

    private Scriptable globalScope;

	public CoffeePrecompiler() {
        try {
            initCoffeescriptCompiler();
        } catch (IOException e) {
            throw new IOError(e);
        }
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
	public boolean canPrecompile(Path source) {
        return source.toString().endsWith(".coffee");
	}

	@Override
	public Path precompile(Path source, Path target) throws Exception {
        String coffeeScriptSource = new String(Files.readAllBytes(source), Charset.forName(getEncoding()));
		Context context = Context.enter();
		try {
			Scriptable compileScope = context.newObject(globalScope);
			compileScope.setParentScope(globalScope);
			compileScope.put("coffeeScriptSource", compileScope, coffeeScriptSource);
			String compiledStr = (String) context.evaluateString(
					compileScope,
					"CoffeeScript.compile(coffeeScriptSource, {});",
					source.toAbsolutePath().toString(), 0, null);
            if (Files.notExists(target))
                Files.createDirectories(target);
			Path outputFile = target.resolve(
					FilenameUtils.getBaseName(source.getFileName().toString()) + "." + getExtension());
            Files.write(outputFile, Arrays.asList(compiledStr),
                    Charset.forName(getEncoding()),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            logger.fine("precompiled coffee(" + source +") to java(" + outputFile + ". " );
			return outputFile;
		} finally {
			Context.exit();
		}
	}

	private void initCoffeescriptCompiler() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("org/jcoffeescript/coffee-script.js");
             Reader reader = new InputStreamReader(is)) {
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
        }
	}
}
