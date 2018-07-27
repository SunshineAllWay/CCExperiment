package org.apache.tools.ant;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.apache.tools.ant.taskdefs.Typedef;
import org.apache.tools.ant.taskdefs.Definer;
import org.apache.tools.ant.launch.Launcher;
import org.apache.tools.ant.util.FileUtils;
public class ComponentHelper  {
    private Map          restrictedDefinitions = new HashMap();
    private AntTypeTable antTypeTable;
    private final Hashtable taskClassDefinitions = new Hashtable();
    private boolean rebuildTaskClassDefinitions = true;
    private final Hashtable typeClassDefinitions = new Hashtable();
    private boolean rebuildTypeClassDefinitions = true;
    private final HashSet checkedNamespaces = new HashSet();
    private Stack antLibStack = new Stack();
    private String antLibCurrentUri = null;
    private ComponentHelper next;
    private Project project;
    private static final String ERROR_NO_TASK_LIST_LOAD = "Can't load default task list";
    private static final String ERROR_NO_TYPE_LIST_LOAD = "Can't load default type list";
    public static final String COMPONENT_HELPER_REFERENCE = "ant.ComponentHelper";
    private static final String BUILD_SYSCLASSPATH_ONLY = "only";
    private static final String ANT_PROPERTY_TASK = "property";
    private static Properties[] defaultDefinitions = new Properties[2];
     public Project getProject() {
         return project;
     }
    public static ComponentHelper getComponentHelper(Project project) {
        if (project == null) {
            return null;
        }
        ComponentHelper ph = (ComponentHelper) project.getReference(COMPONENT_HELPER_REFERENCE);
        if (ph != null) {
            return ph;
        }
        ph = new ComponentHelper();
        ph.setProject(project);
        project.addReference(COMPONENT_HELPER_REFERENCE, ph);
        return ph;
    }
    protected ComponentHelper() {
    }
    public void setNext(ComponentHelper next) {
        this.next = next;
    }
    public ComponentHelper getNext() {
        return next;
    }
    public void setProject(Project project) {
        this.project = project;
        antTypeTable = new AntTypeTable(project);
    }
    private synchronized Set getCheckedNamespace() {
        return (Set) checkedNamespaces.clone();
    }
    private Map getRestrictedDefinition() {
        Map result = new HashMap();
        synchronized (restrictedDefinitions) {
            for(Iterator i = restrictedDefinitions.entrySet().iterator();
                         i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                List entryVal = (List) entry.getValue();
                synchronized (entryVal) {
                    entryVal = new ArrayList(entryVal);
                }
                Object entryKey = entry.getKey();                                    
                result.put(entryKey, entryVal);
            }
        }
        return result;
    }
    public void initSubProject(ComponentHelper helper) {
        AntTypeTable typeTable = (AntTypeTable) helper.antTypeTable.clone();
        synchronized (antTypeTable) { 
            for (Iterator i = typeTable.values().iterator(); i.hasNext();) {
                AntTypeDefinition def = (AntTypeDefinition) i.next();
                antTypeTable.put(def.getName(), def);
            }
        }
        Set inheritedCheckedNamespace = helper.getCheckedNamespace();
        synchronized (this) {
            checkedNamespaces.addAll(inheritedCheckedNamespace);
        }
        Map inheritedRestrictedDef = helper.getRestrictedDefinition();
        synchronized (restrictedDefinitions) {
            restrictedDefinitions.putAll(inheritedRestrictedDef);
        }
    }
    public Object createComponent(UnknownElement ue, String ns, String componentType)
            throws BuildException {
        Object component = createComponent(componentType);
        if (component instanceof Task) {
            Task task = (Task) component;
            task.setLocation(ue.getLocation());
            task.setTaskType(componentType);
            task.setTaskName(ue.getTaskName());
            task.setOwningTarget(ue.getOwningTarget());
            task.init();
        }
        return component;
    }
    public Object createComponent(String componentName) {
        AntTypeDefinition def = getDefinition(componentName);
        return def == null ? null : def.create(project);
    }
    public Class getComponentClass(String componentName) {
        AntTypeDefinition def = getDefinition(componentName);
        return def == null ? null : def.getExposedClass(project);
    }
    public AntTypeDefinition getDefinition(String componentName) {
        checkNamespace(componentName);
        return antTypeTable.getDefinition(componentName);
    }
    public void initDefaultDefinitions() {
        initTasks();
        initTypes();
    }
    public void addTaskDefinition(String taskName, Class taskClass) {
        checkTaskClass(taskClass);
        AntTypeDefinition def = new AntTypeDefinition();
        def.setName(taskName);
        def.setClassLoader(taskClass.getClassLoader());
        def.setClass(taskClass);
        def.setAdapterClass(TaskAdapter.class);
        def.setClassName(taskClass.getName());
        def.setAdaptToClass(Task.class);
        updateDataTypeDefinition(def);
    }
    public void checkTaskClass(final Class taskClass) throws BuildException {
        if (!Modifier.isPublic(taskClass.getModifiers())) {
            final String message = taskClass + " is not public";
            project.log(message, Project.MSG_ERR);
            throw new BuildException(message);
        }
        if (Modifier.isAbstract(taskClass.getModifiers())) {
            final String message = taskClass + " is abstract";
            project.log(message, Project.MSG_ERR);
            throw new BuildException(message);
        }
        try {
            taskClass.getConstructor((Class[]) null);
        } catch (NoSuchMethodException e) {
            final String message = "No public no-arg constructor in " + taskClass;
            project.log(message, Project.MSG_ERR);
            throw new BuildException(message);
        }
        if (!Task.class.isAssignableFrom(taskClass)) {
            TaskAdapter.checkTaskClass(taskClass, project);
        }
    }
    public Hashtable getTaskDefinitions() {
        synchronized (taskClassDefinitions) {
            synchronized (antTypeTable) {
                if (rebuildTaskClassDefinitions) {
                    taskClassDefinitions.clear();
                    for (Iterator i = antTypeTable.keySet().iterator(); i.hasNext();) {
                        String name = (String) i.next();
                        Class clazz = antTypeTable.getExposedClass(name);
                        if (clazz == null) {
                            continue;
                        }
                        if (Task.class.isAssignableFrom(clazz)) {
                            taskClassDefinitions.put(name, antTypeTable.getTypeClass(name));
                        }
                    }
                    rebuildTaskClassDefinitions = false;
                }
            }
        }
        return taskClassDefinitions;
    }
    public Hashtable getDataTypeDefinitions() {
        synchronized (typeClassDefinitions) {
            synchronized (antTypeTable) {
                if (rebuildTypeClassDefinitions) {
                    typeClassDefinitions.clear();
                    for (Iterator i = antTypeTable.keySet().iterator(); i.hasNext();) {
                        String name = (String) i.next();
                        Class clazz = antTypeTable.getExposedClass(name);
                        if (clazz == null) {
                            continue;
                        }
                        if (!(Task.class.isAssignableFrom(clazz))) {
                            typeClassDefinitions.put(name, antTypeTable.getTypeClass(name));
                        }
                    }
                    rebuildTypeClassDefinitions = false;
                }
            }
        }
        return typeClassDefinitions;
    }
    public List getRestrictedDefinitions(String componentName) {
        synchronized (restrictedDefinitions) {
            return (List) restrictedDefinitions.get(componentName);
        }
    }
    public void addDataTypeDefinition(String typeName, Class typeClass) {
        AntTypeDefinition def = new AntTypeDefinition();
        def.setName(typeName);
        def.setClass(typeClass);
        updateDataTypeDefinition(def);
        project.log(" +User datatype: " + typeName + "     " + typeClass.getName(),
                Project.MSG_DEBUG);
    }
    public void addDataTypeDefinition(AntTypeDefinition def) {
        if (!def.isRestrict()) {
           updateDataTypeDefinition(def);
        } else {
            updateRestrictedDefinition(def);
        }
    }
    public Hashtable getAntTypeTable() {
        return antTypeTable;
    }
    public Task createTask(String taskType) throws BuildException {
        Task task = createNewTask(taskType);
        if (task == null && taskType.equals(ANT_PROPERTY_TASK)) {
            addTaskDefinition(ANT_PROPERTY_TASK, org.apache.tools.ant.taskdefs.Property.class);
            task = createNewTask(taskType);
        }
        return task;
    }
    private Task createNewTask(String taskType) throws BuildException {
        Class c = getComponentClass(taskType);
        if (c == null || !(Task.class.isAssignableFrom(c))) {
            return null;
        }
        Object obj = createComponent(taskType);
        if (obj == null) {
            return null;
        }
        if (!(obj instanceof Task)) {
            throw new BuildException("Expected a Task from '" + taskType
                    + "' but got an instance of " + obj.getClass().getName() + " instead");
        }
        Task task = (Task) obj;
        task.setTaskType(taskType);
        task.setTaskName(taskType);
        project.log("   +Task: " + taskType, Project.MSG_DEBUG);
        return task;
    }
    public Object createDataType(String typeName) throws BuildException {
        return createComponent(typeName);
    }
    public String getElementName(Object element) {
        return getElementName(element, false);
    }
    public String getElementName(Object o, boolean brief) {
        Class elementClass = o.getClass();
        String elementClassname = elementClass.getName();
        synchronized (antTypeTable) {
            for (Iterator i = antTypeTable.values().iterator(); i.hasNext();) {
                AntTypeDefinition def = (AntTypeDefinition) i.next();
                if (elementClassname.equals(def.getClassName())
                        && (elementClass == def.getExposedClass(project))) {
                    String name = def.getName();
                    return brief ? name : "The <" + name + "> type";
                }
            }
        }
        return getUnmappedElementName(o.getClass(), brief);
    }
    public static String getElementName(Project p, Object o, boolean brief) {
        if (p == null) {
            p = Project.getProject(o);
        }
        return p == null ? getUnmappedElementName(o.getClass(), brief) : getComponentHelper(p)
                .getElementName(o, brief);
    }
    private static String getUnmappedElementName(Class c, boolean brief) {
        if (brief) {
            String name = c.getName();
            return name.substring(name.lastIndexOf('.') + 1);
        }
        return c.toString();
    }
    private boolean validDefinition(AntTypeDefinition def) {
        return !(def.getTypeClass(project) == null || def.getExposedClass(project) == null);
    }
    private boolean sameDefinition(AntTypeDefinition def, AntTypeDefinition old) {
        boolean defValid = validDefinition(def);
        boolean sameValidity = (defValid == validDefinition(old));
        return sameValidity && (!defValid || def.sameDefinition(old, project));
    }
    private void updateRestrictedDefinition(AntTypeDefinition def) {
        String name = def.getName();
        List list = null;
        synchronized (restrictedDefinitions) {
            list = (List) restrictedDefinitions.get(name);
            if (list == null) {
                list = new ArrayList();
                restrictedDefinitions.put(name, list);
            }
        }
        synchronized (list) {
            for (Iterator i = list.iterator(); i.hasNext();) {
                AntTypeDefinition current = (AntTypeDefinition) i.next();
                if (current.getClassName().equals(def.getClassName())) {
                    i.remove();
                    break;
                }
            }
            list.add(def);
        }
    }
    private void updateDataTypeDefinition(AntTypeDefinition def) {
        String name = def.getName();
        synchronized (antTypeTable) {
            rebuildTaskClassDefinitions = true;
            rebuildTypeClassDefinitions = true;
            AntTypeDefinition old = antTypeTable.getDefinition(name);
            if (old != null) {
                if (sameDefinition(def, old)) {
                    return;
                }
                Class oldClass = antTypeTable.getExposedClass(name);
                boolean isTask = oldClass != null && Task.class.isAssignableFrom(oldClass);
                project.log("Trying to override old definition of "
                        + (isTask ? "task " : "datatype ") + name, (def.similarDefinition(old,
                        project)) ? Project.MSG_VERBOSE : Project.MSG_WARN);
            }
            project.log(" +Datatype " + name + " " + def.getClassName(), Project.MSG_DEBUG);
            antTypeTable.put(name, def);
        }
    }
    public void enterAntLib(String uri) {
        antLibCurrentUri = uri;
        antLibStack.push(uri);
    }
    public String getCurrentAntlibUri() {
        return antLibCurrentUri;
    }
    public void exitAntLib() {
        antLibStack.pop();
        antLibCurrentUri = (antLibStack.size() == 0) ? null : (String) antLibStack.peek();
    }
    private void initTasks() {
        ClassLoader classLoader = getClassLoader(null);
        Properties props = getDefaultDefinitions(false);
        Enumeration e = props.propertyNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            String className = props.getProperty(name);
            AntTypeDefinition def = new AntTypeDefinition();
            def.setName(name);
            def.setClassName(className);
            def.setClassLoader(classLoader);
            def.setAdaptToClass(Task.class);
            def.setAdapterClass(TaskAdapter.class);
            antTypeTable.put(name, def);
        }
    }
    private ClassLoader getClassLoader(ClassLoader classLoader) {
        String buildSysclasspath = project.getProperty(MagicNames.BUILD_SYSCLASSPATH);
        if (project.getCoreLoader() != null
            && !(BUILD_SYSCLASSPATH_ONLY.equals(buildSysclasspath))) {
            classLoader = project.getCoreLoader();
        }
        return classLoader;
    }
    private static synchronized Properties getDefaultDefinitions(boolean type)
            throws BuildException {
        int idx = type ? 1 : 0;
        if (defaultDefinitions[idx] == null) {
            String resource = type ? MagicNames.TYPEDEFS_PROPERTIES_RESOURCE
                    : MagicNames.TASKDEF_PROPERTIES_RESOURCE;
            String errorString = type ? ERROR_NO_TYPE_LIST_LOAD : ERROR_NO_TASK_LIST_LOAD;
            InputStream in = null;
            try {
                in = ComponentHelper.class.getResourceAsStream(resource);
                if (in == null) {
                    throw new BuildException(errorString);
                }
                Properties p = new Properties();
                p.load(in);
                defaultDefinitions[idx] = p;
            } catch (IOException e) {
                throw new BuildException(errorString, e);
            } finally {
                FileUtils.close(in);
            }
        }
        return defaultDefinitions[idx];
    }
    private void initTypes() {
        ClassLoader classLoader = getClassLoader(null);
        Properties props = getDefaultDefinitions(true);
        Enumeration e = props.propertyNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            String className = props.getProperty(name);
            AntTypeDefinition def = new AntTypeDefinition();
            def.setName(name);
            def.setClassName(className);
            def.setClassLoader(classLoader);
            antTypeTable.put(name, def);
        }
    }
    private synchronized void checkNamespace(String componentName) {
        String uri = ProjectHelper.extractUriFromComponentName(componentName);
        if ("".equals(uri)) {
            uri = ProjectHelper.ANT_CORE_URI;
        }
        if (!uri.startsWith(ProjectHelper.ANTLIB_URI)) {
            return; 
        }
        if (checkedNamespaces.contains(uri)) {
            return; 
        }
        checkedNamespaces.add(uri);
        Typedef definer = new Typedef();
        definer.setProject(project);
        definer.init();
        definer.setURI(uri);
        definer.setTaskName(uri);
        definer.setResource(Definer.makeResourceFromURI(uri));
        definer.setOnError(new Typedef.OnError(Typedef.OnError.POLICY_IGNORE));
        definer.execute();
    }
    public String diagnoseCreationFailure(String componentName, String type) {
        StringWriter errorText = new StringWriter();
        PrintWriter out = new PrintWriter(errorText);
        out.println("Problem: failed to create " + type + " " + componentName);
        boolean lowlevel = false;
        boolean jars = false;
        boolean definitions = false;
        boolean antTask;
        String home = System.getProperty(Launcher.USER_HOMEDIR);
        File libDir = new File(home, Launcher.USER_LIBDIR);
        String antHomeLib;
        boolean probablyIDE = false;
        String anthome = System.getProperty(MagicNames.ANT_HOME);
        if (anthome != null) {
            File antHomeLibDir = new File(anthome, "lib");
            antHomeLib = antHomeLibDir.getAbsolutePath();
        } else {
            probablyIDE = true;
            antHomeLib = "ANT_HOME" + File.separatorChar + "lib";
        }
        StringBuffer dirListingText = new StringBuffer();
        final String tab = "        -";
        dirListingText.append(tab);
        dirListingText.append(antHomeLib);
        dirListingText.append('\n');
        if (probablyIDE) {
            dirListingText.append(tab);
            dirListingText.append("the IDE Ant configuration dialogs");
        } else {
            dirListingText.append(tab);
            dirListingText.append(libDir);
            dirListingText.append('\n');
            dirListingText.append(tab);
            dirListingText.append("a directory added on the command line with the -lib argument");
        }
        String dirListing = dirListingText.toString();
        AntTypeDefinition def = getDefinition(componentName);
        if (def == null) {
            printUnknownDefinition(out, componentName, dirListing);
            definitions = true;
        } else {
            final String classname = def.getClassName();
            antTask = classname.startsWith("org.apache.tools.ant.");
            boolean optional = classname.startsWith("org.apache.tools.ant.taskdefs.optional");
            optional |= classname.startsWith("org.apache.tools.ant.types.optional");
            Class clazz = null;
            try {
                clazz = def.innerGetTypeClass();
            } catch (ClassNotFoundException e) {
                jars = true;
                if (!optional) {
                    definitions = true;
                }
                printClassNotFound(out, classname, optional, dirListing);
            } catch (NoClassDefFoundError ncdfe) {
                jars = true;
                printNotLoadDependentClass(out, optional, ncdfe, dirListing);
            }
            if (clazz != null) {
                try {
                    def.innerCreateAndSet(clazz, project);
                    out.println("The component could be instantiated.");
                } catch (NoSuchMethodException e) {
                    lowlevel = true;
                    out.println("Cause: The class " + classname
                            + " has no compatible constructor.");
                } catch (InstantiationException e) {
                    lowlevel = true;
                    out.println("Cause: The class " + classname
                            + " is abstract and cannot be instantiated.");
                } catch (IllegalAccessException e) {
                    lowlevel = true;
                    out.println("Cause: The constructor for " + classname
                            + " is private and cannot be invoked.");
                } catch (InvocationTargetException ex) {
                    lowlevel = true;
                    Throwable t = ex.getTargetException();
                    out.println("Cause: The constructor threw the exception");
                    out.println(t.toString());
                    t.printStackTrace(out);
                }  catch (NoClassDefFoundError ncdfe) {
                    jars = true;
                    out.println("Cause:  A class needed by class " + classname
                            + " cannot be found: ");
                    out.println("       " + ncdfe.getMessage());
                    out.println("Action: Determine what extra JAR files are"
                            + " needed, and place them in:");
                    out.println(dirListing);
                }
            }
            out.println();
            out.println("Do not panic, this is a common problem.");
            if (definitions) {
                out.println("It may just be a typographical error in the build file "
                        + "or the task/type declaration.");
            }
            if (jars) {
                out.println("The commonest cause is a missing JAR.");
            }
            if (lowlevel) {
                out.println("This is quite a low level problem, which may need "
                        + "consultation with the author of the task.");
                if (antTask) {
                    out.println("This may be the Ant team. Please file a "
                            + "defect or contact the developer team.");
                } else {
                    out.println("This does not appear to be a task bundled with Ant.");
                    out.println("Please take it up with the supplier of the third-party " + type
                            + ".");
                    out.println("If you have written it yourself, you probably have a bug to fix.");
                }
            } else {
                out.println();
                out.println("This is not a bug; it is a configuration problem");
            }
        }
        out.flush();
        out.close();
        return errorText.toString();
    }
    private void printUnknownDefinition(PrintWriter out, String componentName, String dirListing) {
        boolean isAntlib = componentName.indexOf(MagicNames.ANTLIB_PREFIX) == 0;
        String uri = ProjectHelper.extractUriFromComponentName(componentName);
        out.println("Cause: The name is undefined.");
        out.println("Action: Check the spelling.");
        out.println("Action: Check that any custom tasks/types have been declared.");
        out.println("Action: Check that any <presetdef>/<macrodef>"
                + " declarations have taken place.");
        if (uri.length() > 0) {
            List matches = antTypeTable.findMatches(uri);
            if (matches.size() > 0) {
                out.println();
                out.println("The definitions in the namespace " + uri + " are:");
                for (Iterator it = matches.iterator(); it.hasNext();) {
                    AntTypeDefinition def = (AntTypeDefinition) it.next();
                    String local = ProjectHelper.extractNameFromComponentName(def.getName());
                    out.println("    " + local);
                }
            } else {
                out.println("No types or tasks have been defined in this namespace yet");
                if (isAntlib) {
                    out.println();
                    out.println("This appears to be an antlib declaration. ");
                    out.println("Action: Check that the implementing library exists in one of:");
                    out.println(dirListing);
                }
            }
        }
    }
    private void printClassNotFound(PrintWriter out, String classname, boolean optional,
            String dirListing) {
        out.println("Cause: the class " + classname + " was not found.");
        if (optional) {
            out.println("        This looks like one of Ant's optional components.");
            out.println("Action: Check that the appropriate optional JAR exists in");
            out.println(dirListing);
        } else {
            out.println("Action: Check that the component has been correctly declared");
            out.println("        and that the implementing JAR is in one of:");
            out.println(dirListing);
        }
    }
    private void printNotLoadDependentClass(PrintWriter out, boolean optional,
            NoClassDefFoundError ncdfe, String dirListing) {
        out.println("Cause: Could not load a dependent class "
                    +  ncdfe.getMessage());
        if (optional) {
            out.println("       It is not enough to have Ant's optional JARs");
            out.println("       you need the JAR files that the" + " optional tasks depend upon.");
            out.println("       Ant's optional task dependencies are" + " listed in the manual.");
        } else {
            out.println("       This class may be in a separate JAR" + " that is not installed.");
        }
        out.println("Action: Determine what extra JAR files are"
                + " needed, and place them in one of:");
        out.println(dirListing);
    }
    private static class AntTypeTable extends Hashtable {
        private static final long serialVersionUID = -3060442320477772028L;
        private Project project;
        AntTypeTable(Project project) {
            this.project = project;
        }
        AntTypeDefinition getDefinition(String key) {
            return (AntTypeDefinition) (super.get(key));
        }
        public Object get(Object key) {
            return getTypeClass((String) key);
        }
        Class getTypeClass(String name) {
            AntTypeDefinition def = getDefinition(name);
            return (def == null) ? null : def.getTypeClass(project);
        }
        Class getExposedClass(String name) {
            AntTypeDefinition def = getDefinition(name);
            return def == null ? null : def.getExposedClass(project);
        }
        public synchronized boolean contains(Object clazz) {
            boolean found = false;
            if (clazz instanceof Class) {
                for (Iterator i = values().iterator(); i.hasNext() && !found;) {
                    found = (((AntTypeDefinition) (i.next())).getExposedClass(project) == clazz);
                }
            }
            return found;
        }
        public boolean containsValue(Object value) {
            return contains(value);
        }
        public synchronized List findMatches(String prefix) {
            ArrayList matches = new ArrayList();
            for (Iterator i = values().iterator(); i.hasNext();) {
                AntTypeDefinition def = (AntTypeDefinition) (i.next());
                if (def.getName().startsWith(prefix)) {
                    matches.add(def);
                }
            }
            return matches;
        }
    }
}
