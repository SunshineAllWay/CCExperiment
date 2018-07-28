package org.apache.batik.script.rhino.svg12;
import java.net.URL;
import org.apache.batik.script.ImportInfo;
import org.apache.batik.script.rhino.RhinoInterpreter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
public class SVG12RhinoInterpreter extends RhinoInterpreter {
    public SVG12RhinoInterpreter(URL documentURL) {
        super(documentURL);
    }
    public SVG12RhinoInterpreter(URL documentURL,
                                 ImportInfo imports) {
        super(documentURL, imports);
    }
    protected void defineGlobalWrapperClass(Scriptable global) {
        try {
            ScriptableObject.defineClass(global, GlobalWrapper.class);
        } catch (Exception ex) {
        }
    }
    protected ScriptableObject createGlobalObject(Context ctx) {
        return new GlobalWrapper(ctx);
    }
}
