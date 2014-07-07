package net.unit8.maven.plugins.assets.analyzer;

/**
 * Error object for JSLint.
 *
 * @author kawasima
 */
public class JSLintError {
    private int line;
    private int character;
    private String reason;
    private String evidence;

    public JSLintError(double line, double character, String reason, String evidence) {
        this.line = (int) line;
        this.character = (int) character;
        this.reason = reason;
        this.evidence = evidence;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("(%d,%d) %s\n       %s\n", line, character, reason, evidence));
        for (int i=0; i < 6 + character; i++)
            sb.append(" ");
        sb.append("^");

        return sb.toString();
    }
}
