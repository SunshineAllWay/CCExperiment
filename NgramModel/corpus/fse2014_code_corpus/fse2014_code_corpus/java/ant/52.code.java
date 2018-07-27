package org.apache.tools.ant;
import java.io.PrintStream;
public interface BuildLogger extends BuildListener {
    void setMessageOutputLevel(int level);
    void setOutputPrintStream(PrintStream output);
    void setEmacsMode(boolean emacsMode);
    void setErrorPrintStream(PrintStream err);
}
