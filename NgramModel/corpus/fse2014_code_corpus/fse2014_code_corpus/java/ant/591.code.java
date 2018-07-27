package org.apache.tools.ant.types;
import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Iterator;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.util.StringUtils;
import org.apache.tools.ant.taskdefs.condition.Os;
public class Commandline implements Cloneable {
    private static final boolean IS_WIN_9X = Os.isFamily("win9x");
    private Vector arguments = new Vector();
    private String executable = null;
    protected static final String DISCLAIMER =
        StringUtils.LINE_SEP
        + "The \' characters around the executable and arguments are"
        + StringUtils.LINE_SEP
        + "not part of the command."
        + StringUtils.LINE_SEP;
    public Commandline(String toProcess) {
        super();
        String[] tmp = translateCommandline(toProcess);
        if (tmp != null && tmp.length > 0) {
            setExecutable(tmp[0]);
            for (int i = 1; i < tmp.length; i++) {
                createArgument().setValue(tmp[i]);
            }
        }
    }
    public Commandline() {
        super();
    }
    public static class Argument extends ProjectComponent {
        private String[] parts;
        private String prefix = "";
        private String suffix = "";
        public void setValue(String value) {
            parts = new String[] {value};
        }
        public void setLine(String line) {
            if (line == null) {
                return;
            }
            parts = translateCommandline(line);
        }
        public void setPath(Path value) {
            parts = new String[] {value.toString()};
        }
        public void setPathref(Reference value) {
            Path p = new Path(getProject());
            p.setRefid(value);
            parts = new String[] {p.toString()};
        }
        public void setFile(File value) {
            parts = new String[] {value.getAbsolutePath()};
        }
        public void setPrefix(String prefix) {
            this.prefix = prefix != null ? prefix : "";
        }
        public void setSuffix(String suffix) {
            this.suffix = suffix != null ? suffix : "";
        }
        public String[] getParts() {
            if (parts == null || parts.length == 0
                || (prefix.length() == 0 && suffix.length() == 0)) {
                return parts;
            }
            String[] fullParts = new String[parts.length];
            for (int i = 0; i < fullParts.length; ++i) {
                fullParts[i] = prefix + parts[i] + suffix;
            }
            return fullParts;
        }
    }
    public class Marker {
        private int position;
        private int realPos = -1;
        private String prefix = "";
        private String suffix = "";
        Marker(int position) {
            this.position = position;
        }
        public int getPosition() {
            if (realPos == -1) {
                realPos = (executable == null ? 0 : 1);
                for (int i = 0; i < position; i++) {
                    Argument arg = (Argument) arguments.elementAt(i);
                    realPos += arg.getParts().length;
                }
            }
            return realPos;
        }
        public void setPrefix(String prefix) {
            this.prefix = prefix != null ? prefix : "";
        }
        public String getPrefix() {
            return prefix;
        }
        public void setSuffix(String suffix) {
            this.suffix = suffix != null ? suffix : "";
        }
        public String getSuffix() {
            return suffix;
        }
    }
    public Argument createArgument() {
        return this.createArgument(false);
    }
    public Argument createArgument(boolean insertAtStart) {
        Argument argument = new Argument();
        if (insertAtStart) {
            arguments.insertElementAt(argument, 0);
        } else {
            arguments.addElement(argument);
        }
        return argument;
    }
    public void setExecutable(String executable) {
        if (executable == null || executable.length() == 0) {
            return;
        }
        this.executable = executable.replace('/', File.separatorChar)
            .replace('\\', File.separatorChar);
    }
    public String getExecutable() {
        return executable;
    }
    public void addArguments(String[] line) {
        for (int i = 0; i < line.length; i++) {
            createArgument().setValue(line[i]);
        }
    }
    public String[] getCommandline() {
        List commands = new LinkedList();
        ListIterator list = commands.listIterator();
        addCommandToList(list);
        final String[] result = new String[commands.size()];
        return (String[]) commands.toArray(result);
    }
    public void addCommandToList(ListIterator list) {
        if (executable != null) {
            list.add(executable);
        }
        addArgumentsToList(list);
    }
    public String[] getArguments() {
        List result = new ArrayList(arguments.size() * 2);
        addArgumentsToList(result.listIterator());
        String [] res = new String[result.size()];
        return (String[]) result.toArray(res);
    }
    public void addArgumentsToList(ListIterator list) {
        for (int i = 0; i < arguments.size(); i++) {
            Argument arg = (Argument) arguments.elementAt(i);
            String[] s = arg.getParts();
            if (s != null) {
                for (int j = 0; j < s.length; j++) {
                    list.add(s[j]);
                }
            }
        }
    }
    public String toString() {
        return toString(getCommandline());
    }
    public static String quoteArgument(String argument) {
        if (argument.indexOf("\"") > -1) {
            if (argument.indexOf("\'") > -1) {
                throw new BuildException("Can\'t handle single and double"
                        + " quotes in same argument");
            } else {
                return '\'' + argument + '\'';
            }
        } else if (argument.indexOf("\'") > -1
                   || argument.indexOf(" ") > -1
                   || (IS_WIN_9X && argument.indexOf(';') != -1)) {
            return '\"' + argument + '\"';
        } else {
            return argument;
        }
    }
    public static String toString(String[] line) {
        if (line == null || line.length == 0) {
            return "";
        }
        final StringBuffer result = new StringBuffer();
        for (int i = 0; i < line.length; i++) {
            if (i > 0) {
                result.append(' ');
            }
            result.append(quoteArgument(line[i]));
        }
        return result.toString();
    }
    public static String[] translateCommandline(String toProcess) {
        if (toProcess == null || toProcess.length() == 0) {
            return new String[0];
        }
        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        int state = normal;
        StringTokenizer tok = new StringTokenizer(toProcess, "\"\' ", true);
        Vector v = new Vector();
        StringBuffer current = new StringBuffer();
        boolean lastTokenHasBeenQuoted = false;
        while (tok.hasMoreTokens()) {
            String nextTok = tok.nextToken();
            switch (state) {
            case inQuote:
                if ("\'".equals(nextTok)) {
                    lastTokenHasBeenQuoted = true;
                    state = normal;
                } else {
                    current.append(nextTok);
                }
                break;
            case inDoubleQuote:
                if ("\"".equals(nextTok)) {
                    lastTokenHasBeenQuoted = true;
                    state = normal;
                } else {
                    current.append(nextTok);
                }
                break;
            default:
                if ("\'".equals(nextTok)) {
                    state = inQuote;
                } else if ("\"".equals(nextTok)) {
                    state = inDoubleQuote;
                } else if (" ".equals(nextTok)) {
                    if (lastTokenHasBeenQuoted || current.length() != 0) {
                        v.addElement(current.toString());
                        current = new StringBuffer();
                    }
                } else {
                    current.append(nextTok);
                }
                lastTokenHasBeenQuoted = false;
                break;
            }
        }
        if (lastTokenHasBeenQuoted || current.length() != 0) {
            v.addElement(current.toString());
        }
        if (state == inQuote || state == inDoubleQuote) {
            throw new BuildException("unbalanced quotes in " + toProcess);
        }
        String[] args = new String[v.size()];
        v.copyInto(args);
        return args;
    }
    public int size() {
        return getCommandline().length;
    }
    public Object clone() {
        try {
            Commandline c = (Commandline) super.clone();
            c.arguments = (Vector) arguments.clone();
            return c;
        } catch (CloneNotSupportedException e) {
            throw new BuildException(e);
        }
    }
    public void clear() {
        executable = null;
        arguments.removeAllElements();
    }
    public void clearArgs() {
        arguments.removeAllElements();
    }
    public Marker createMarker() {
        return new Marker(arguments.size());
    }
    public String describeCommand() {
        return describeCommand(this);
    }
    public String describeArguments() {
        return describeArguments(this);
    }
    public static String describeCommand(Commandline line) {
        return describeCommand(line.getCommandline());
    }
    public static String describeArguments(Commandline line) {
        return describeArguments(line.getArguments());
    }
    public static String describeCommand(String[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuffer buf = new StringBuffer("Executing \'");
        buf.append(args[0]);
        buf.append("\'");
        if (args.length > 1) {
            buf.append(" with ");
            buf.append(describeArguments(args, 1));
        } else {
            buf.append(DISCLAIMER);
        }
        return buf.toString();
    }
    public static String describeArguments(String[] args) {
        return describeArguments(args, 0);
    }
    protected static String describeArguments(String[] args, int offset) {
        if (args == null || args.length <= offset) {
            return "";
        }
        StringBuffer buf = new StringBuffer("argument");
        if (args.length > offset) {
            buf.append("s");
        }
        buf.append(":").append(StringUtils.LINE_SEP);
        for (int i = offset; i < args.length; i++) {
            buf.append("\'").append(args[i]).append("\'")
                .append(StringUtils.LINE_SEP);
        }
        buf.append(DISCLAIMER);
        return buf.toString();
    }
    public Iterator iterator() {
        return arguments.iterator();
    }
}
