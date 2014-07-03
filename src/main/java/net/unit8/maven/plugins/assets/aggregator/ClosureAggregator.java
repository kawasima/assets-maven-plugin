package net.unit8.maven.plugins.assets.aggregator;

import com.google.common.css.JobDescription;
import com.google.common.css.JobDescriptionBuilder;
import com.google.common.css.SourceCode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.GssParser;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.compiler.passes.CompactPrinter;
import com.google.common.css.compiler.passes.PrettyPrinter;
import com.google.javascript.jscomp.*;
import com.google.javascript.jscomp.Compiler;
import net.unit8.maven.plugins.assets.Aggregator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClosureAggregator extends Aggregator {
	/**
	 * Minify javascript files.
	 *
	 */
	public void aggregateJs(List<Path> files, Path outputFile) throws IOException {
		if (Files.notExists(outputFile.getParent()))
            Files.createDirectories(outputFile.getParent());

        List<SourceFile> sourceFiles = new ArrayList<>();
        for (Path file : files) {
            sourceFiles.add(SourceFile.fromFile(file.toFile()));
        }
        List<SourceFile> externFiles = new ArrayList<>();
        Compiler compiler = new Compiler(System.out);
        CompilerOptions options = new CompilerOptions();
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        Result result = compiler.compile(externFiles, sourceFiles, options);
        if (result.success) {
            Files.write(outputFile, Arrays.asList(compiler.toSourceArray()), Charset.forName(getEncoding()));
        }
	}

	/**
	 * Minify css files.
	 *
	 */
	public void aggregateCss(List<Path> files, Path outputFile) throws IOException {
        if (Files.notExists(outputFile.getParent()))
            Files.createDirectories(outputFile.getParent());

        JobDescriptionBuilder builder = new JobDescriptionBuilder();
        for (Path file : files) {
            String contents = new String(Files.readAllBytes(file), getEncoding());
            builder.addInput(new SourceCode(file.getFileName().toString(), contents));
        }
        JobDescription job = builder.getJobDescription();
        GssParser parser = new GssParser(job.inputs);
        StringBuilder result = new StringBuilder();
        try {
            CssTree cssTree = parser.parse();
            if (job.outputFormat == JobDescription.OutputFormat.COMPRESSED) {
                CompactPrinter compactPrinterPass = new CompactPrinter(cssTree);
                compactPrinterPass.runPass();
                result.append(compactPrinterPass.getCompactPrintedString());
            } else {
                PrettyPrinter prettyPrinterPass = new PrettyPrinter(cssTree
                        .getVisitController());
                prettyPrinterPass.runPass();
                result.append(prettyPrinterPass.getPrettyPrintedString());
            }
            Files.write(outputFile, Arrays.asList(result.toString()), Charset.forName(getEncoding()));
        } catch (GssParserException e) {
            throw new IOException(e);
        }
	}
}
