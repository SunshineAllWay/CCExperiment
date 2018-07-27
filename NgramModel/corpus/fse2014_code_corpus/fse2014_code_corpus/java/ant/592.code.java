package org.apache.tools.ant.types;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.JavaEnvUtils;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Vector;
public class CommandlineJava implements Cloneable {
    private Commandline vmCommand = new Commandline();
    private Commandline javaCommand = new Commandline();
    private SysProperties sysProperties = new SysProperties();
    private Path classpath = null;
    private Path bootclasspath = null;
    private String vmVersion;
    private String maxMemory = null;
    private Assertions assertions = null;
     private boolean executeJar = false;
    private boolean cloneVm = false;
    public static class SysProperties extends Environment implements Cloneable {
        Properties sys = null;
        private Vector propertySets = new Vector();
        public String[] getVariables() throws BuildException {
            List definitions = new LinkedList();
            ListIterator list = definitions.listIterator();
            addDefinitionsToList(list);
            if (definitions.size() == 0) {
                return null;
            } else {
                return (String[]) definitions.toArray(new String[definitions.size()]);
            }
        }
        public void addDefinitionsToList(ListIterator listIt) {
            String[] props = super.getVariables();
            if (props != null) {
                for (int i = 0; i < props.length; i++) {
                    listIt.add("-D" + props[i]);
                }
            }
            Properties propertySetProperties = mergePropertySets();
            for (Enumeration e = propertySetProperties.keys();
                 e.hasMoreElements();) {
                String key = (String) e.nextElement();
                String value = propertySetProperties.getProperty(key);
                listIt.add("-D" + key + "=" + value);
            }
        }
        public int size() {
            Properties p = mergePropertySets();
            return variables.size() + p.size();
        }
        public void setSystem() throws BuildException {
            try {
                sys = System.getProperties();
                Properties p = new Properties();
                for (Enumeration e = sys.propertyNames(); e.hasMoreElements();) {
                    String name = (String) e.nextElement();
                    String value = sys.getProperty(name);
                    if (name != null && value != null) {
                        p.put(name, value);
                    }
                }
                p.putAll(mergePropertySets());
                for (Enumeration e = variables.elements(); e.hasMoreElements();) {
                    Environment.Variable v = (Environment.Variable) e.nextElement();
                    v.validate();
                    p.put(v.getKey(), v.getValue());
                }
                System.setProperties(p);
            } catch (SecurityException e) {
                throw new BuildException("Cannot modify system properties", e);
            }
        }
        public void restoreSystem() throws BuildException {
            if (sys == null) {
                throw new BuildException("Unbalanced nesting of SysProperties");
            }
            try {
                System.setProperties(sys);
                sys = null;
            } catch (SecurityException e) {
                throw new BuildException("Cannot modify system properties", e);
            }
        }
        public Object clone() throws CloneNotSupportedException {
            try {
                SysProperties c = (SysProperties) super.clone();
                c.variables = (Vector) variables.clone();
                c.propertySets = (Vector) propertySets.clone();
                return c;
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }
        public void addSyspropertyset(PropertySet ps) {
            propertySets.addElement(ps);
        }
        public void addSysproperties(SysProperties ps) {
            variables.addAll(ps.variables);
            propertySets.addAll(ps.propertySets);
        }
        private Properties mergePropertySets() {
            Properties p = new Properties();
            for (Enumeration e = propertySets.elements();
                 e.hasMoreElements();) {
                PropertySet ps = (PropertySet) e.nextElement();
                p.putAll(ps.getProperties());
            }
            return p;
        }
    }
    public CommandlineJava() {
        setVm(JavaEnvUtils.getJreExecutable("java"));
        setVmversion(JavaEnvUtils.getJavaVersion());
    }
    public Commandline.Argument createArgument() {
        return javaCommand.createArgument();
    }
    public Commandline.Argument createVmArgument() {
        return vmCommand.createArgument();
    }
    public void addSysproperty(Environment.Variable sysp) {
        sysProperties.addVariable(sysp);
    }
    public void addSyspropertyset(PropertySet sysp) {
        sysProperties.addSyspropertyset(sysp);
    }
    public void addSysproperties(SysProperties sysp) {
        sysProperties.addSysproperties(sysp);
    }
    public void setVm(String vm) {
        vmCommand.setExecutable(vm);
    }
    public void setVmversion(String value) {
        vmVersion = value;
    }
    public void setCloneVm(boolean cloneVm) {
        this.cloneVm = cloneVm;
    }
    public Assertions getAssertions() {
        return assertions;
    }
    public void setAssertions(Assertions assertions) {
        this.assertions = assertions;
    }
    public void setJar(String jarpathname) {
        javaCommand.setExecutable(jarpathname);
        executeJar = true;
    }
    public String getJar() {
        if (executeJar) {
            return javaCommand.getExecutable();
        }
        return null;
    }
    public void setClassname(String classname) {
        javaCommand.setExecutable(classname);
        executeJar = false;
    }
    public String getClassname() {
        if (!executeJar) {
            return javaCommand.getExecutable();
        }
        return null;
    }
    public Path createClasspath(Project p) {
        if (classpath == null) {
            classpath = new Path(p);
        }
        return classpath;
    }
    public Path createBootclasspath(Project p) {
        if (bootclasspath == null) {
            bootclasspath = new Path(p);
        }
        return bootclasspath;
    }
    public String getVmversion() {
        return vmVersion;
    }
    public String[] getCommandline() {
        List commands = new LinkedList();
        final ListIterator listIterator = commands.listIterator();
        addCommandsToList(listIterator);
        return (String[]) commands.toArray(new String[commands.size()]);
    }
    private void addCommandsToList(final ListIterator listIterator) {
        getActualVMCommand().addCommandToList(listIterator);
        sysProperties.addDefinitionsToList(listIterator);
        if (isCloneVm()) {
            SysProperties clonedSysProperties = new SysProperties();
            PropertySet ps = new PropertySet();
            PropertySet.BuiltinPropertySetName sys =
                new PropertySet.BuiltinPropertySetName();
            sys.setValue("system");
            ps.appendBuiltin(sys);
            clonedSysProperties.addSyspropertyset(ps);
            clonedSysProperties.addDefinitionsToList(listIterator);
        }
        Path bcp = calculateBootclasspath(true);
        if (bcp.size() > 0) {
            listIterator.add("-Xbootclasspath:" + bcp.toString());
        }
        if (haveClasspath()) {
            listIterator.add("-classpath");
            listIterator.add(
                    classpath.concatSystemClasspath("ignore").toString());
        }
        if (getAssertions() != null) {
            getAssertions().applyAssertions(listIterator);
        }
        if (executeJar) {
            listIterator.add("-jar");
        }
        javaCommand.addCommandToList(listIterator);
    }
    public void setMaxmemory(String max) {
        this.maxMemory = max;
    }
    public String toString() {
        return Commandline.toString(getCommandline());
    }
    public String describeCommand() {
        return Commandline.describeCommand(getCommandline());
    }
    public String describeJavaCommand() {
        return Commandline.describeCommand(getJavaCommand());
    }
    protected Commandline getActualVMCommand() {
        Commandline actualVMCommand = (Commandline) vmCommand.clone();
        if (maxMemory != null) {
            if (vmVersion.startsWith("1.1")) {
                actualVMCommand.createArgument().setValue("-mx" + maxMemory);
            } else {
                actualVMCommand.createArgument().setValue("-Xmx" + maxMemory);
            }
        }
        return actualVMCommand;
    }
    public int size() {
        int size = getActualVMCommand().size() + javaCommand.size()
            + sysProperties.size();
        if (isCloneVm()) {
            size += System.getProperties().size();
        }
        if (haveClasspath()) {
            size += 2;
        }
        if (calculateBootclasspath(true).size() > 0) {
            size++;
        }
        if (executeJar) {
            size++;
        }
        if (getAssertions() != null) {
            size += getAssertions().size();
        }
        return size;
    }
    public Commandline getJavaCommand() {
        return javaCommand;
    }
    public Commandline getVmCommand() {
        return getActualVMCommand();
    }
    public Path getClasspath() {
        return classpath;
    }
    public Path getBootclasspath() {
        return bootclasspath;
    }
    public void setSystemProperties() throws BuildException {
        sysProperties.setSystem();
    }
    public void restoreSystemProperties() throws BuildException {
        sysProperties.restoreSystem();
    }
    public SysProperties getSystemProperties() {
        return sysProperties;
    }
    public Object clone() throws CloneNotSupportedException {
        try {
            CommandlineJava c = (CommandlineJava) super.clone();
            c.vmCommand = (Commandline) vmCommand.clone();
            c.javaCommand = (Commandline) javaCommand.clone();
            c.sysProperties = (SysProperties) sysProperties.clone();
            if (classpath != null) {
                c.classpath = (Path) classpath.clone();
            }
            if (bootclasspath != null) {
                c.bootclasspath = (Path) bootclasspath.clone();
            }
            if (assertions != null) {
                c.assertions = (Assertions) assertions.clone();
            }
            return c;
        } catch (CloneNotSupportedException e) {
            throw new BuildException(e);
        }
    }
    public void clearJavaArgs() {
        javaCommand.clearArgs();
    }
    public boolean haveClasspath() {
        Path fullClasspath = classpath != null
            ? classpath.concatSystemClasspath("ignore") : null;
        return fullClasspath != null
            && fullClasspath.toString().trim().length() > 0;
    }
    protected boolean haveBootclasspath(boolean log) {
        return calculateBootclasspath(log).size() > 0;
    }
    private Path calculateBootclasspath(boolean log) {
        if (vmVersion.startsWith("1.1")) {
            if (bootclasspath != null && log) {
                bootclasspath.log("Ignoring bootclasspath as "
                                  + "the target VM doesn't support it.");
            }
        } else {
            Path b = bootclasspath;
            if (b == null) {
                b = new Path(null);
            }
            return b.concatSystemBootClasspath(isCloneVm() ? "last" : "ignore");
        }
        return new Path(null);
    }
    private boolean isCloneVm() {
        return cloneVm
            || "true".equals(System.getProperty("ant.build.clonevm"));
    }
}
