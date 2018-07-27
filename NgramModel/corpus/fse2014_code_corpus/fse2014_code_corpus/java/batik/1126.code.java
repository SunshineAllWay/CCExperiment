package org.apache.batik.script.rhino;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;
import org.w3c.dom.events.EventTarget;
class BatikWrapFactory extends WrapFactory {
    private RhinoInterpreter interpreter;
    public BatikWrapFactory(RhinoInterpreter interp) {
        interpreter = interp;
        setJavaPrimitiveWrap(false);
    }
    public Object wrap(Context ctx, Scriptable scope,
                       Object obj, Class staticType) {
        if (obj instanceof EventTarget) {
            return interpreter.buildEventTargetWrapper((EventTarget)obj);
        }
        return super.wrap(ctx, scope, obj, staticType);
    }
}
