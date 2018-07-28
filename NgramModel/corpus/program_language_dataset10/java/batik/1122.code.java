package org.apache.batik.script.jacl;
import java.net.URL;
import org.apache.batik.script.Interpreter;
import org.apache.batik.script.InterpreterFactory;
public class JaclInterpreterFactory implements InterpreterFactory {
    public static final String[] JACL_MIMETYPES = { "text/tcl" };
    public JaclInterpreterFactory() {
    }
    public String[] getMimeTypes() {
        return JACL_MIMETYPES;
    }
    public Interpreter createInterpreter(URL documentURL, boolean svg12) {
        return new JaclInterpreter();
    }
    public Interpreter createInterpreter(URL documentURL, boolean svg12,
                                         ImportInfo imports) {
        return new JaclInterpreter();
    }
}
