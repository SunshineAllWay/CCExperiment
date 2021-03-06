package org.apache.batik.script.jacl;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Locale;
import org.apache.batik.script.InterpreterException;
import tcl.lang.Interp;
import tcl.lang.ReflectObject;
import tcl.lang.TclException;
public class JaclInterpreter implements org.apache.batik.script.Interpreter {
    private Interp interpreter = null;
    public JaclInterpreter() {
        interpreter = new Interp();
        try {
            interpreter.eval("package require java", 0);
        } catch (TclException e) {
        }
    }
    public String[] getMimeTypes() {
        return JaclInterpreterFactory.JACL_MIMETYPES;
    }
    public Object evaluate(Reader scriptreader) throws IOException {
        return evaluate(scriptreader, "");
    }
    public Object evaluate(Reader scriptreader, String description)
        throws IOException {
        StringBuffer sbuffer = new StringBuffer();
        char[] buffer = new char[1024];
        int val = 0;
        while ((val = scriptreader.read(buffer)) != -1) {
            sbuffer.append(buffer, 0, val);
        }
        String str = sbuffer.toString();
        return evaluate(str);
    }
    public Object evaluate(String script) {
        try {
            interpreter.eval(script, 0);
        } catch (TclException e) {
            throw new InterpreterException(e, e.getMessage(), -1, -1);
        } catch (RuntimeException re) {
            throw new InterpreterException(re, re.getMessage(), -1, -1);
        }
        return interpreter.getResult();
    }
    public void dispose() {
        interpreter.dispose();
    }
    public void bindObject(String name, Object object) {
        try {
            interpreter.
                setVar(name,
                       ReflectObject.
                       newInstance(interpreter, object.getClass(), object),
                       0);
        } catch (TclException e) {
        }
    }
    public void setOut(Writer out) {
    }
    public Locale getLocale() {
        return Locale.getDefault();
    }
    public void setLocale(Locale locale) {
    }
    public String formatMessage(String key, Object[] args) {
        return null;
    }
}
