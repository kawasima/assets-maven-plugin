package net.unit8.maven.plugins.assets.watcher;

import java.nio.file.Path;

/**
 * A Handler interface for WatcherEvent.
 *
 * @author kawasima
 */
public interface WatcherEventHandler {
    public abstract void handle(Path path);
}
