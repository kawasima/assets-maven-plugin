package net.unit8.maven.plugins.assets.analyzer;

import net.unit8.maven.plugins.assets.Analyzer;
import net.unit8.maven.plugins.assets.precompiler.LessPrecompiler;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An analyzer by JSLint.
 *
 * @author kawasima
 */
public class JSLintAnalyzer extends Analyzer {
    private static final Logger logger = Logger.getLogger(LessPrecompiler.class.getName());
    private Scriptable globalScope;

    public JSLintAnalyzer() {
        try {
            init();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public String getName() {
        return "jslint";
    }

    public String getExtension() {
        return "js";
    }

    public boolean canAnalyze(Path source) {
        return source.toString().endsWith(".js");
    }

    protected List<JSLintError> parseError(NativeArray errors) {
        List<JSLintError> lintErrors = new ArrayList<>();
        for (Object obj : errors) {
            if (obj instanceof NativeObject) {
                NativeObject error = (NativeObject) obj;
                lintErrors.add(new JSLintError(
                        (double)error.get("line"),
                        (double)error.get("character"),
                        (String)error.get("reason"),
                        (String)error.get("evidence")));
            }
        }
        return lintErrors;
    }

    @Override
    public void analyze(Path source) throws Exception {
        String contents = new String(Files.readAllBytes(source), Charset.forName(getEncoding()));
        Context context = Context.enter();
        try {
            Scriptable compileScope = context.newObject(globalScope);
            compileScope.setParentScope(globalScope);
            compileScope.put("contents", compileScope, contents);
            Object result = context.evaluateString(
                    compileScope,
                    "JSLINT(contents, {}) ? null : JSLINT.errors",
                    source.toAbsolutePath().toString(), 0, null);
            if (result != null) {
                List<JSLintError> errors = parseError((NativeArray) result);
                logger.warning(source + ": " + errors.size() + " warnings founds.");
                for(JSLintError error : errors) {
                    logger.warning(error.toString());
                }
            }
        } finally {
            Context.exit();
        }
    }

    private void init() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("com/jslint/jslint.js");
             Reader reader = new InputStreamReader(is)) {
            Context context = Context.enter();
            context.setOptimizationLevel(-1); // Without this, Rhino
            // hits a 64K bytecode
            // limit and fails
            try {
                globalScope = context.initStandardObjects();
                context.evaluateReader(globalScope, reader,
                        "jslint.js", 0, null);
            } finally {
                Context.exit();
            }
        }
    }

}
