package net.unit8.maven.plugins.assets;

import net.unit8.maven.plugins.assets.aggregator.SimpleAggregator;
import net.unit8.maven.plugins.assets.aggregator.YuiAggregator;
import net.unit8.maven.plugins.assets.precompiler.CoffeePrecompiler;
import net.unit8.maven.plugins.assets.precompiler.LessPrecompiler;
import net.unit8.maven.plugins.assets.watcher.WatcherEventHandler;
import net.unit8.maven.plugins.assets.watcher.WatcherService;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @phase compile
 * @goal aggregate
 * @author kawasima
 */
public class AggregateMojo extends AbstractAssetsMojo {
	/**
	 * @parameter
	 */
	protected List<Class<? extends Precompiler>> precompilerClasses;

    protected boolean auto;

	protected Map<String, Precompiler> availablePrecompilers = new HashMap<>();

	public void initializePrecompilers() throws MojoExecutionException {
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

    protected Path precompile(Recipe recipe, Path componentFile) throws MojoExecutionException{
        for (String precompilerName : recipe.getPrecompilers()) {
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
    protected void build(final Recipe recipe) throws MojoExecutionException {
        Path sourceDirectory = (recipe.getSourceDirectory() != null) ? Paths.get(recipe.getSourceDirectory()) : Paths.get(".");
        Path targetDirectory = (recipe.getTargetDirectory() != null) ? Paths.get(recipe.getTargetDirectory()) : Paths.get(".");
        Aggregator simpleAggregator = new SimpleAggregator();
        Aggregator minifyAggregator = new YuiAggregator();

        try {
            for (Rule rule : recipe.getRules()) {
                Aggregator aggregator = rule.getMinify() ? minifyAggregator : simpleAggregator;
                aggregator.setEncoding(encoding);

                final List<Path> files = new ArrayList<>();
                for (String component : rule.getComponents()) {
                    if (component.contains("*")) {
                        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + sourceDirectory.toString() + "/" + component);
                        getLog().info("glob:" + sourceDirectory.toString() + "/" + component);
                        Files.walkFileTree(sourceDirectory, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                getLog().info(file.toString() + matcher.matches(file));
                                if (matcher.matches(file)) {
                                    try {
                                        files.add(precompile(recipe, file));
                                    } catch(MojoExecutionException e) {
                                        throw new IOException(e);
                                    }
                                }
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    } else {
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
		final Recipe recipe = readRecipe();

        build(recipe);
        if (auto) {
            final WatcherService watcherService = new WatcherService();
            watcherService.addWatcher(Paths.get(recipe.getSourceDirectory()));
            watcherService.addHandler(new WatcherEventHandler() {
                @Override
                public void handle(Path path) {
                    try {
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
}
