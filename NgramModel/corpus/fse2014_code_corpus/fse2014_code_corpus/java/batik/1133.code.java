package org.apache.batik.script.rhino;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.batik.script.Window;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Location;
public class WindowWrapper extends ImporterTopLevel {
    private static final Object[] EMPTY_ARGUMENTS = new Object[0];
    protected RhinoInterpreter interpreter;
    protected Window window;
    public WindowWrapper(Context context) {
        super(context);
        String[] names = { "setInterval", "setTimeout", "clearInterval",
                           "clearTimeout", "parseXML", "printNode", "getURL",
                           "postURL", "alert", "confirm", "prompt" };
        this.defineFunctionProperties(names, WindowWrapper.class,
                                      ScriptableObject.DONTENUM);
        this.defineProperty("location", WindowWrapper.class,
                            ScriptableObject.PERMANENT);
    }
    public String getClassName() {
        return "Window";
    }
    public String toString() {
        return "[object Window]";
    }
    public static Object setInterval(Context cx,
                                     Scriptable thisObj,
                                     Object[] args,
                                     Function funObj) {
        int len = args.length;
        WindowWrapper ww = (WindowWrapper)thisObj;
        Window window = ww.window;
        if (len < 2) {
            throw Context.reportRuntimeError("invalid argument count");
        }
        long to = ((Long)Context.jsToJava(args[1], Long.TYPE)).longValue();
        if (args[0] instanceof Function) {
            RhinoInterpreter interp =
                (RhinoInterpreter)window.getInterpreter();
            FunctionWrapper fw;
            fw = new FunctionWrapper(interp, (Function)args[0],
                                     EMPTY_ARGUMENTS);
            return Context.toObject(window.setInterval(fw, to), thisObj);
        }
        String script =
            (String)Context.jsToJava(args[0], String.class);
        return Context.toObject(window.setInterval(script, to), thisObj);
    }
    public static Object setTimeout(Context cx,
                                    Scriptable thisObj,
                                    Object[] args,
                                    Function funObj) {
        int len = args.length;
        WindowWrapper ww = (WindowWrapper)thisObj;
        Window window = ww.window;
        if (len < 2) {
            throw Context.reportRuntimeError("invalid argument count");
        }
        long to = ((Long)Context.jsToJava(args[1], Long.TYPE)).longValue();
        if (args[0] instanceof Function) {
            RhinoInterpreter interp =
                (RhinoInterpreter)window.getInterpreter();
            FunctionWrapper fw;
            fw = new FunctionWrapper(interp, (Function)args[0],
                                     EMPTY_ARGUMENTS);
            return Context.toObject(window.setTimeout(fw, to), thisObj);
        }
        String script =
            (String)Context.jsToJava(args[0], String.class);
        return Context.toObject(window.setTimeout(script, to), thisObj);
    }
    public static void clearInterval(Context cx,
                                     Scriptable thisObj,
                                     Object[] args,
                                     Function funObj) {
        int len = args.length;
        WindowWrapper ww = (WindowWrapper)thisObj;
        Window window = ww.window;
        if (len >= 1) {
            window.clearInterval(Context.jsToJava(args[0], Object.class));
        }
    }
    public static void clearTimeout(Context cx,
                                    Scriptable thisObj,
                                    Object[] args,
                                    Function funObj) {
        int len = args.length;
        WindowWrapper ww = (WindowWrapper)thisObj;
        Window window = ww.window;
        if (len >= 1) {
            window.clearTimeout(Context.jsToJava(args[0], Object.class));
        }
    }
    public static Object parseXML(Context cx,
                                  Scriptable thisObj,
                                  final Object[] args,
                                  Function funObj) {
        int len = args.length;
        WindowWrapper ww = (WindowWrapper)thisObj;
        final Window window = ww.window;
        if (len < 2) {
            throw Context.reportRuntimeError("invalid argument count");
        }
        RhinoInterpreter     interp = (RhinoInterpreter)window.getInterpreter();
        AccessControlContext acc    = interp.getAccessControlContext();
        PrivilegedAction pa = new PrivilegedAction() {
                public Object run() {
                    return window.parseXML
                        ((String)Context.jsToJava(args[0], String.class),
                         (Document)Context.jsToJava(args[1], Document.class));
                }
            };
        Object ret;
        if (acc != null) ret = AccessController.doPrivileged(pa , acc);
        else             ret = AccessController.doPrivileged(pa);
        return Context.toObject(ret, thisObj);
    }
    public static Object printNode(Context cx,
                                   Scriptable thisObj,
                                   final Object[] args,
                                   Function funObj) {
        if (args.length != 1) {
            throw Context.reportRuntimeError("invalid argument count");
        }
        WindowWrapper ww = (WindowWrapper)thisObj;
        final Window window = ww.window;
        AccessControlContext acc =
            ((RhinoInterpreter)window.getInterpreter()).getAccessControlContext();
        Object ret = AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return window.printNode
                        ((Node) Context.jsToJava(args[0], Node.class));
                }
            }, acc);
        return Context.toString(ret);
    }
    public static void getURL(Context cx,
                              Scriptable thisObj,
                              final Object[] args,
                              Function funObj) {
        int len = args.length;
        WindowWrapper ww = (WindowWrapper)thisObj;
        final Window window = ww.window;
        if (len < 2) {
            throw Context.reportRuntimeError("invalid argument count");
        }
        RhinoInterpreter interp =
            (RhinoInterpreter)window.getInterpreter();
        final String uri = (String)Context.jsToJava(args[0], String.class);
        Window.URLResponseHandler urlHandler = null;
        if (args[1] instanceof Function) {
            urlHandler = new GetURLFunctionWrapper
                (interp, (Function)args[1], ww);
        } else {
            urlHandler = new GetURLObjectWrapper
                (interp, (NativeObject)args[1], ww);
        }
        final Window.URLResponseHandler fw = urlHandler;
        AccessControlContext acc =
            ((RhinoInterpreter)window.getInterpreter()).getAccessControlContext();
        if (len == 2) {
            AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run(){
                        window.getURL(uri, fw);
                        return null;
                    }
                }, acc);
        } else {
            AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        window.getURL
                            (uri, fw,
                             (String)Context.jsToJava(args[2], String.class));
                        return null;
                    }
                }, acc);
        }
    }
    public static void postURL(Context cx,
                               Scriptable thisObj,
                               final Object[] args,
                               Function funObj) {
        int len = args.length;
        WindowWrapper ww = (WindowWrapper)thisObj;
        final Window window = ww.window;
        if (len < 3) {
            throw Context.reportRuntimeError("invalid argument count");
        }
        RhinoInterpreter interp =
            (RhinoInterpreter)window.getInterpreter();
        final String uri     = (String)Context.jsToJava(args[0], String.class);
        final String content = (String)Context.jsToJava(args[1], String.class);
        Window.URLResponseHandler urlHandler = null;
        if (args[2] instanceof Function) {
            urlHandler = new GetURLFunctionWrapper
                (interp, (Function)args[2], ww);
        } else {
            urlHandler = new GetURLObjectWrapper
                (interp, (NativeObject)args[2], ww);
        }
        final Window.URLResponseHandler fw = urlHandler;
        AccessControlContext acc;
        acc = interp.getAccessControlContext();
        switch (len) {
        case 3:
            AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run(){
                        window.postURL(uri, content, fw);
                        return null;
                    }
                }, acc);
            break;
        case 4:
            AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        window.postURL
                            (uri, content, fw,
                             (String)Context.jsToJava(args[3], String.class));
                        return null;
                    }
                }, acc);
            break;
        default:
            AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        window.postURL
                            (uri, content, fw,
                             (String)Context.jsToJava(args[3], String.class),
                             (String)Context.jsToJava(args[4], String.class));
                        return null;
                    }
                }, acc);
        }
    }
    public static void alert(Context cx,
                             Scriptable thisObj,
                             Object[] args,
                             Function funObj) {
        int len = args.length;
        WindowWrapper ww = (WindowWrapper)thisObj;
        Window window = ww.window;
        if (len >= 1) {
            String message =
                (String)Context.jsToJava(args[0], String.class);
            window.alert(message);
        }
    }
    public static Object confirm(Context cx,
                                  Scriptable thisObj,
                                  Object[] args,
                                  Function funObj) {
        int len = args.length;
        WindowWrapper ww = (WindowWrapper)thisObj;
        Window window = ww.window;
        if (len >= 1) {
            String message =
                (String)Context.jsToJava(args[0], String.class);
            if (window.confirm(message))
                return Context.toObject(Boolean.TRUE, thisObj);
            else
                return Context.toObject(Boolean.FALSE, thisObj);
        }
        return Context.toObject(Boolean.FALSE, thisObj);
    }
    public static Object prompt(Context cx,
                                Scriptable thisObj,
                                Object[] args,
                                Function funObj) {
        WindowWrapper ww = (WindowWrapper)thisObj;
        Window window = ww.window;
        Object result;
        switch (args.length) {
            case 0:
                result = "";
                break;
            case 1:
                String message =
                    (String)Context.jsToJava(args[0], String.class);
                result = window.prompt(message);
                break;
            default:
                message =
                    (String)Context.jsToJava(args[0], String.class);
                String defVal =
                    (String)Context.jsToJava(args[1], String.class);
                result = window.prompt(message, defVal);
                break;
        }
        if (result == null) {
            return null;
        }
        return Context.toString(result);
    }
    public Location getLocation() {
        return window.getLocation();
    }
    public void setLocation(Object val) {
        String url = (String)Context.jsToJava(val, String.class);
        window.getLocation().assign(url);
    }
    protected static class FunctionWrapper implements Runnable {
        protected RhinoInterpreter interpreter;
        protected Function function;
        protected Object[] arguments;
        public FunctionWrapper(RhinoInterpreter ri,
                               Function f,
                               Object[] args) {
            interpreter = ri;
            function = f;
            arguments = args;
        }
        public void run() {
            interpreter.callHandler(function, arguments);
        }
    }
    protected static class GetURLFunctionWrapper
        implements Window.URLResponseHandler {
        protected RhinoInterpreter interpreter;
        protected Function function;
        protected WindowWrapper windowWrapper;
        public GetURLFunctionWrapper(RhinoInterpreter ri, Function fct,
                                     WindowWrapper ww) {
            interpreter = ri;
            function = fct;
            windowWrapper = ww;
        }
        public void getURLDone(final boolean success,
                               final String mime,
                               final String content) {
            interpreter.callHandler
                (function,
                 new GetURLDoneArgBuilder(success, mime,
                                          content, windowWrapper));
        }
    }
    private static class GetURLObjectWrapper
        implements Window.URLResponseHandler {
        private RhinoInterpreter interpreter;
        private ScriptableObject object;
        private WindowWrapper windowWrapper;
        private static final String COMPLETE = "operationComplete";
        public GetURLObjectWrapper(RhinoInterpreter ri,
                                   ScriptableObject obj,
                                   WindowWrapper ww) {
            interpreter = ri;
            object = obj;
            windowWrapper = ww;
        }
        public void getURLDone(final boolean success,
                               final String mime,
                               final String content) {
            interpreter.callMethod
                (object, COMPLETE,
                 new GetURLDoneArgBuilder(success, mime,
                                          content, windowWrapper));
        }
    }
    static class GetURLDoneArgBuilder
        implements RhinoInterpreter.ArgumentsBuilder {
        boolean success;
        String mime, content;
        WindowWrapper windowWrapper;
        public GetURLDoneArgBuilder(boolean success,
                                    String mime, String content,
                                    WindowWrapper ww) {
            this.success = success;
            this.mime    = mime;
            this.content = content;
            this.windowWrapper = ww;
        }
        public Object[] buildArguments() {
            ScriptableObject so = new NativeObject();
            so.put("success", so,
                   (success) ? Boolean.TRUE : Boolean.FALSE);
            if (mime != null) {
                so.put("contentType", so,
                       Context.toObject(mime, windowWrapper));
            }
            if (content != null) {
                so.put("content", so,
                       Context.toObject(content, windowWrapper));
            }
            return new Object [] { so };
        }
    }
}
