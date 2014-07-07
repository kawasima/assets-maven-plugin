package net.unit8.maven.plugins.assets.watcher;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author kawasima
 */
public class WatcherService {
    private static final Logger logger = Logger.getGlobal();
    private static final WatchEvent.Kind<?>[] events = {
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY };
    private final WatchService watchService;
    private ExecutorService executorService;
    private List<WatcherEventHandler> handlers = new ArrayList<>();

    public WatcherService() {
        logger.setLevel(Level.INFO);
        try {
            watchService  = FileSystems.getDefault().newWatchService();
        } catch(IOException e) {
            throw new IllegalStateException(e);
        }
    }
    public void addWatcher(Path dir) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes basicFileAttributes) throws IOException {
                    dir.register(watchService, events);
                    logger.fine("register watcher: " + dir.toAbsolutePath());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch(IOException e) {
            throw new IllegalArgumentException(e);
        }

    }

    public void addHandler(WatcherEventHandler handler) {
        handlers.add(handler);
    }

    public void start() {
        executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable);
            }
        });
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                for(;;) {
                    WatchKey key = null;
                    try {
                        key = watchService.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (event.kind() == StandardWatchEventKinds.OVERFLOW)
                                continue;

                            Path path = ((WatchEvent<Path>)event).context();
                            Watchable watchable = key.watchable();
                            if (watchable instanceof Path) {
                                Path dir = (Path) watchable;
                                for (WatcherEventHandler handler : handlers) {
                                    handler.handle(dir.resolve(path));
                                }
                            }
                        }
                    } catch (InterruptedException ex) {
                        logger.log(Level.WARNING, "", ex);
                    } finally {
                        if (key != null)
                            key.reset();
                    }
                }
            }
        });
    }

    public void stop() {
        if (executorService == null || executorService.isTerminated()) {
            return;
        }

        executorService.shutdown();
    }
}
