package org.apache.tools.ant.util.optional;
import org.apache.tools.ant.BuildException;
import java.util.Iterator;
import org.apache.tools.ant.util.ScriptRunnerBase;
import org.apache.tools.ant.util.ReflectWrapper;
public class JavaxScriptRunner extends ScriptRunnerBase {
    private ReflectWrapper engine;
    public String getManagerName() {
        return "javax";
    }
    public boolean supportsLanguage() {
        if (engine != null) {
            return true;
        }
        checkLanguage();
        ClassLoader origLoader = replaceContextLoader();
        try {
            return createEngine() != null;
        } catch (Exception ex) {
            return false;
        } finally {
            restoreContextLoader(origLoader);
        }
    }
    public void executeScript(String execName) throws BuildException {
        evaluateScript(execName);
    }
    public Object evaluateScript(String execName) throws BuildException {
        checkLanguage();
        ClassLoader origLoader = replaceContextLoader();
        try {
            ReflectWrapper engine = createEngine();
            if (engine == null) {
                throw new BuildException(
                    "Unable to create javax script engine for "
                    + getLanguage());
            }
            for (Iterator i = getBeans().keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();
                Object value = getBeans().get(key);
                if ("FX".equalsIgnoreCase(getLanguage())) {
                    engine.invoke(
                        "put", String.class, key
                        + ":" + value.getClass().getName(),
                        Object.class, value);
                } else {
                    engine.invoke(
                        "put", String.class, key,
                        Object.class, value);
                }
            }
            return engine.invoke("eval", String.class, getScript());
        } catch (BuildException be) {
            throw unwrap(be);
        } catch (Exception be) {
            Throwable t = be;
            Throwable te = be.getCause();
            if (te != null) {
                if  (te instanceof BuildException) {
                    throw (BuildException) te;
                } else {
                    t = te;
                }
            }
            throw new BuildException(t);
        } finally {
            restoreContextLoader(origLoader);
        }
    }
    private ReflectWrapper createEngine() throws Exception {
        if (engine != null) {
            return engine;
        }
        ReflectWrapper manager = new ReflectWrapper(
            getClass().getClassLoader(), "javax.script.ScriptEngineManager");
        Object e = manager.invoke(
            "getEngineByName", String.class, getLanguage());
        if (e == null) {
            return null;
        }
        ReflectWrapper ret = new ReflectWrapper(e);
        if (getKeepEngine()) {
            this.engine = ret;
        }
        return ret;
    }
    private static BuildException unwrap(Throwable t) {
        BuildException deepest =
            t instanceof BuildException ? (BuildException) t : null;
        Throwable current = t;
        while (current.getCause() != null) {
            current = current.getCause();
            if (current instanceof BuildException) {
                deepest = (BuildException) current;
            }
        }
        return deepest;
    }
}
