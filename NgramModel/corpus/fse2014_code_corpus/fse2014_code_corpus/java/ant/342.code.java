package org.apache.tools.ant.taskdefs.cvslib;
import org.apache.tools.ant.util.LineOrientedOutputStream;
class RedirectingOutputStream extends LineOrientedOutputStream {
    private final ChangeLogParser parser;
    public RedirectingOutputStream(final ChangeLogParser parser) {
        this.parser = parser;
    }
    protected void processLine(final String line) {
        parser.stdout(line);
    }
}
