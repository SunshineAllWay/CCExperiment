package org.apache.batik.script.rhino;
import java.net.URL;
import org.apache.batik.script.ImportInfo;
import org.apache.batik.script.Interpreter;
import org.apache.batik.script.InterpreterFactory;
import org.apache.batik.script.rhino.svg12.SVG12RhinoInterpreter;
public class RhinoInterpreterFactory implements InterpreterFactory {
    public static final String[] RHINO_MIMETYPES = {
        "application/ecmascript",
        "application/javascript",
        "text/ecmascript",
        "text/javascript",
    };
    public RhinoInterpreterFactory() {
    }
    public String[] getMimeTypes() {
        return RHINO_MIMETYPES;
    }
    public Interpreter createInterpreter(URL documentURL, boolean svg12) {
        return createInterpreter(documentURL, svg12, null);
    }
    public Interpreter createInterpreter(URL documentURL, boolean svg12,
                                         ImportInfo imports) {
        if (svg12) {
            return new SVG12RhinoInterpreter(documentURL, imports);
        }
        return new RhinoInterpreter(documentURL, imports);
    }
}
