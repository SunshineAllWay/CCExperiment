package org.apache.batik.script;
import java.net.URL;
public interface InterpreterFactory {
    String[] getMimeTypes();
    Interpreter createInterpreter(URL documentURL, boolean svg12,
                                  ImportInfo imports);
    Interpreter createInterpreter(URL documentURL, boolean svg12);
}
