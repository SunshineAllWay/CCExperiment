package org.apache.tools.ant.types;
import org.apache.tools.ant.Project;
public class LogLevel extends EnumeratedAttribute {
    public static final LogLevel ERR = new LogLevel("error");
    public static final LogLevel WARN = new LogLevel("warn");
    public static final LogLevel INFO = new LogLevel("info");
    public static final LogLevel VERBOSE = new LogLevel("verbose");
    public static final LogLevel DEBUG = new LogLevel("debug");
    public LogLevel() {
    }
    private LogLevel(String value) {
        this();
        setValue(value);
    }
    public String[] getValues() {
        return new String[] {
            "error",
            "warn",
            "warning",
            "info",
            "verbose",
            "debug"};
    }
    private static int[] levels = {
        Project.MSG_ERR,
        Project.MSG_WARN,
        Project.MSG_WARN,
        Project.MSG_INFO,
        Project.MSG_VERBOSE,
        Project.MSG_DEBUG
    };
    public int getLevel() {
        return levels[getIndex()];
    }
}
