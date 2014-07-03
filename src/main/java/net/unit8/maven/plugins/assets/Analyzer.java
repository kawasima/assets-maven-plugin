package net.unit8.maven.plugins.assets;

import java.nio.file.Path;

/**
 * @author kawasima
 */
public abstract class Analyzer {
    private String encoding = "UTF-8";

    public abstract String getName();
    public abstract String getExtension();
    public abstract boolean canAnalyze(Path source);
    public abstract void analyze(Path source) throws Exception;

    public String getEncoding() {
        return encoding;
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

}
