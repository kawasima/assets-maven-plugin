package net.unit8.maven.plugins.assets.watcher;

import org.junit.Test;

import java.nio.file.Paths;

/**
 * @author kawasima
 */
public class WatcherServiceTest {
    @Test
    public void test() throws InterruptedException{
        WatcherService service = new WatcherService();
        service.addWatcher(Paths.get("target"));
        service.start();
    }
}
