package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.PrintStream;
import java.io.OutputStream;
import java.util.Iterator;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.Comparison;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.resources.Resources;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.PropertyOutputStream;
public class Length extends Task implements Condition {
    private static final String ALL = "all";
    private static final String EACH = "each";
    private static final String STRING = "string";
    private static final String LENGTH_REQUIRED
        = "Use of the Length condition requires that the length attribute be set.";
    private String property;
    private String string;
    private Boolean trim;
    private String mode = ALL;
    private Comparison when = Comparison.EQUAL;
    private Long length;
    private Resources resources;
    public synchronized void setProperty(String property) {
        this.property = property;
    }
    public synchronized void setResource(Resource resource) {
        add(resource);
    }
    public synchronized void setFile(File file) {
        add(new FileResource(file));
    }
    public synchronized void add(FileSet fs) {
        add((ResourceCollection) fs);
    }
    public synchronized void add(ResourceCollection c) {
        if (c == null) {
            return;
        }
        resources = (resources == null) ? new Resources() : resources;
        resources.add(c);
    }
    public synchronized void setLength(long ell) {
        length = new Long(ell);
    }
    public synchronized void setWhen(When w) {
        setWhen((Comparison) w);
    }
    public synchronized void setWhen(Comparison c) {
        when = c;
    }
    public synchronized void setMode(FileMode m) {
        this.mode = m.getValue();
    }
    public synchronized void setString(String string) {
        this.string = string;
        this.mode = STRING;
    }
    public synchronized void setTrim(boolean trim) {
        this.trim = trim ? Boolean.TRUE : Boolean.FALSE;
    }
    public boolean getTrim() {
        return trim != null && trim.booleanValue();
    }
    public void execute() {
        validate();
        PrintStream ps = new PrintStream((property != null)
            ? (OutputStream) new PropertyOutputStream(getProject(), property)
            : (OutputStream) new LogOutputStream(this, Project.MSG_INFO));
        if (STRING.equals(mode)) {
            ps.print(getLength(string, getTrim()));
            ps.close();
        } else if (EACH.equals(mode)) {
            handleResources(new EachHandler(ps));
        } else if (ALL.equals(mode)) {
            handleResources(new AllHandler(ps));
        }
    }
    public boolean eval() {
        validate();
        if (length == null) {
            throw new BuildException(LENGTH_REQUIRED);
        }
        Long ell;
        if (STRING.equals(mode)) {
            ell = new Long(getLength(string, getTrim()));
        } else {
            AccumHandler h = new AccumHandler();
            handleResources(h);
            ell = new Long(h.getAccum());
        }
        return when.evaluate(ell.compareTo(length));
    }
    private void validate() {
        if (string != null) {
            if (resources != null) {
                throw new BuildException("the string length function"
                    + " is incompatible with the file/resource length function");
            }
            if (!(STRING.equals(mode))) {
                throw new BuildException("the mode attribute is for use"
                    + " with the file/resource length function");
            }
        } else if (resources != null) {
            if (!(EACH.equals(mode) || ALL.equals(mode))) {
                throw new BuildException("invalid mode setting for"
                    + " file/resource length function: \"" + mode + "\"");
            } else if (trim != null) {
                throw new BuildException("the trim attribute is"
                    + " for use with the string length function only");
            }
        } else {
            throw new BuildException("you must set either the string attribute"
                + " or specify one or more files using the file attribute or"
                + " nested resource collections");
        }
    }
    private void handleResources(Handler h) {
        for (Iterator i = resources.iterator(); i.hasNext();) {
            Resource r = (Resource) i.next();
            if (!r.isExists()) {
                log(r + " does not exist", Project.MSG_WARN);
            }
            if (r.isDirectory()) {
                log(r + " is a directory; length may not be meaningful", Project.MSG_WARN);
            }
            h.handle(r);
        }
        h.complete();
    }
    private static long getLength(String s, boolean t) {
        return (t ? s.trim() : s).length();
    }
    public static class FileMode extends EnumeratedAttribute {
        static final String[] MODES = new String[] {EACH, ALL};
        public String[] getValues() {
            return MODES;
        }
    }
    public static class When extends Comparison {
    }
    private abstract class Handler {
        private PrintStream ps;
        Handler(PrintStream ps) {
            this.ps = ps;
        }
        protected PrintStream getPs() {
            return ps;
        }
        protected abstract void handle(Resource r);
        void complete() {
            FileUtils.close(ps);
        }
    }
    private class EachHandler extends Handler {
        EachHandler(PrintStream ps) {
            super(ps);
        }
        protected void handle(Resource r) {
            getPs().print(r.toString());
            getPs().print(" : ");
            long size = r.getSize();
            if (size == Resource.UNKNOWN_SIZE) {
                getPs().println("unknown");
            } else {
                getPs().println(size);
            }
       }
    }
    private class AccumHandler extends Handler {
        private long accum = 0L;
        AccumHandler() {
            super(null);
        }
        protected AccumHandler(PrintStream ps) {
            super(ps);
        }
        protected long getAccum() {
            return accum;
        }
        protected synchronized void handle(Resource r) {
            long size = r.getSize();
            if (size == Resource.UNKNOWN_SIZE) {
                log("Size unknown for " + r.toString(), Project.MSG_WARN);
            } else {
                accum += size;
            }
        }
    }
    private class AllHandler extends AccumHandler {
        AllHandler(PrintStream ps) {
            super(ps);
        }
        void complete() {
            getPs().print(getAccum());
            super.complete();
        }
    }
}
