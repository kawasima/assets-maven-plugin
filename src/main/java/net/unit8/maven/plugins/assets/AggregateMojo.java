package net.unit8.maven.plugins.assets;

import net.unit8.maven.plugins.assets.aggregator.ClosureAggregator;
import net.unit8.maven.plugins.assets.aggregator.SimpleAggregator;
import net.unit8.maven.plugins.assets.analyzer.JSLintAnalyzer;
import net.unit8.maven.plugins.assets.precompiler.CoffeePrecompiler;
import net.unit8.maven.plugins.assets.precompiler.LessPrecompiler;
import net.unit8.maven.plugins.assets.watcher.WatcherEventHandler;
import net.unit8.maven.plugins.assets.watcher.WatcherService;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregate asset files.
 *
 * @author kawasima
 */
@Mojo(name = "aggregate", defaultPhase = LifecyclePhase.COMPILE)
public class AggregateMojo extends AbstractAssetsMojo {
    @Parameter
	protected List<Class<? extends Precompiler>> precompilerClasses;

    @Parameter
    protected List<Class<? extends Analyzer>> analyzerClasses;

    @Parameter(defaultValue = "false", property = "assets.auto")
    protected boolean auto;

	protected Map<String, Precompiler> availablePrecompilers = new HashMap<>();
    protected Map<String, Analyzer> availableAnalyzers = new HashMap<>();

	protected void initializePrecompilers() throws MojoExecutionException {
		if (precompilerClasses == null) {
			precompilerClasses = new ArrayList<>();
			precompilerClasses.add(LessPrecompiler.class);
			precompilerClasses.add(CoffeePrecompiler.class);
		}

		for (Class<? extends Precompiler> precompilerClass : precompilerClasses) {
			try {
				Precompiler precompiler = precompilerClass.newInstance();
				if (encoding != null)
					precompiler.setEncoding(encoding);
				availablePrecompilers.put(precompiler.getName(), precompiler);
			} catch (Exception e) {
				throw new MojoExecutionException(precompilerClass + " can't be instantiated.", e);
			}
		}
	}

    protected void initializeAnalyzers() throws MojoExecutionException {
        if (analyzerClasses == null) {
            analyzerClasses = new ArrayList<>();
            analyzerClasses.add(JSLintAnalyzer.class);
        }
        for (Class<? extends Analyzer> analyzerClass : analyzerClasses) {
            try {
                Analyzer analyzer = analyzerClass.newInstance();
                if (encoding != null)
                    analyzer.setEncoding(encoding);
                availableAnalyzers.put(analyzer.getName(), analyzer);
            } catch (Exception e) {
                throw new MojoExecutionException(analyzerClass + " can't be instantiated.", e);
            }
        }
    }
    protected Path precompile(Recipe recipe, Path componentFile) throws MojoExecutionException{
        List<String> precompilers = recipe.getPrecompilers();
        if (precompilers == null)
            return componentFile;

        for (String precompilerName : precompilers) {
            Precompiler precompiler = availablePrecompilers.get(precompilerName);
            if (precompiler == null)
                throw new MojoExecutionException("Can't find precompiler " + precompilerName);
            if (precompiler.canPrecompile(componentFile)) {
                try {
                    Path outputDir = workingDirectory.toPath().resolve(precompiler.getName());
                    return precompiler.precompile(componentFile, outputDir);
                } catch (Exception e) {
                    throw new MojoExecutionException("Precompile error.", e);
                }
            }
        }
        return componentFile;
    }

    protected void analyze(Recipe recipe, Path componentFile) throws MojoExecutionException {
        List<String> analyzers = recipe.getAnalyzers();
        if (analyzers == null)
            return;

        for (String analyzerName : analyzers) {
            Analyzer analyzer = availableAnalyzers.get(analyzerName);
            if (analyzer == null)
                throw new MojoExecutionException("Can't find analyzer " + analyzerName);
            if (analyzer.canAnalyze(componentFile)) {
                try {
                    analyzer.analyze(componentFile);
                } catch (Exception e) {
                    throw new MojoExecutionException("Analyze error.", e);
                }
            }
        }
    }

    protected void build(final Recipe recipe) throws MojoExecutionException {
        final Path sourceDirectory = recipe.getSourceDirectory() != null ?
                Paths.get(recipe.getSourceDirectory()) : Paths.get(".");
        final Path targetDirectory = recipe.getTargetDirectory() != null ?
                Paths.get(recipe.getTargetDirectory()) : Paths.get(".");
        Aggregator simpleAggregator = new SimpleAggregator();
        Aggregator minifyAggregator = new ClosureAggregator();

        try {
            for (Rule rule : recipe.getRules()) {
                Aggregator aggregator = rule.getMinify() ? minifyAggregator : simpleAggregator;
                aggregator.setEncoding(encoding);

                final List<Path> files = new ArrayList<>();
                for (String component : rule.getComponents()) {
                    if (component.contains("*")) {

                        final PathMatcher matcher = FileSystems.getDefault()
                                .getPathMatcher("glob:" + component);
                        Files.walkFileTree(sourceDirectory, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                if (matcher.matches(sourceDirectory.relativize(file))
                                        && !isIgnorePattern(file)) {
                                    try {
                                        analyze(recipe, file);
                                        files.add(precompile(recipe, file));
                                    } catch (MojoExecutionException e) {
                                        throw new IOException(e);
                                    }
                                }
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    } else {
                        analyze(recipe, sourceDirectory.resolve(component));
                        files.add(precompile(recipe, sourceDirectory.resolve(component)));
                    }
                }

                Path targetPath = targetDirectory.resolve(rule.getVersioningTarget());
                Files.deleteIfExists(targetPath);

                if (rule.getTarget().endsWith(".js")) {
                    aggregator.aggregateJs(files, targetPath);

                } else if (rule.getTarget().endsWith(".css")) {
                    aggregator.aggregateCss(files, targetPath);
                } else {
                    throw new MojoExecutionException("Unknown target: " + rule.getTarget());
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("IOError", e);
        }

    }

	public void execute() throws MojoExecutionException, MojoFailureException {
        if (workingDirectory != null && !workingDirectory.exists()) {
            try {
                Files.createDirectories(workingDirectory.toPath());
            } catch(IOException e) {
                throw new MojoExecutionException("Can't create working directory: " + workingDirectory, e);
            }
        }
		initializePrecompilers();
        initializeAnalyzers();
		final Recipe recipe = readRecipe();

        build(recipe);
        if (auto) {
            getLog().info("Auto compile mode.");
            final WatcherService watcherService = new WatcherService();
            watcherService.addWatcher(Paths.get(recipe.getSourceDirectory()));
            watcherService.addHandler(new WatcherEventHandler() {
                @Override
                public void handle(Path path) {
                    if (isIgnorePattern(path))
                        return;
                    try {
                        getLog().info("Found a file change event. Start aggregattion...");
                        build(recipe);
                    } catch(MojoExecutionException e) {
                        getLog().error(e);
                    }
                }
            });
            watcherService.start();
            Runtime.getRuntime().addShutdownHook(new Thread(){
                public void run() {
                    watcherService.stop();
                }
            });

            Object lock = new Object();
            while(true) {
                try {
                    synchronized (lock) {
                        lock.wait();
                    }
                } catch(InterruptedException ignore) {}
            }
        }
	}

    protected boolean isIgnorePattern(Path file) {
        String filename = file.getFileName().getFileName().toString();
        return filename.endsWith("~")
                || filename.startsWith("#")
                || filename.startsWith(".")
                || filename.endsWith(".swp");
    }
}
