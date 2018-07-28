package org.apache.tools.ant;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import org.apache.tools.ant.helper.ProjectHelper2;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.util.LoaderUtils;
public class ProjectHelperRepository {
    private static final String DEBUG_PROJECT_HELPER_REPOSITORY =
        "ant.project-helper-repo.debug";
    private static final boolean DEBUG =
        "true".equals(System.getProperty(DEBUG_PROJECT_HELPER_REPOSITORY));
    private static ProjectHelperRepository instance =
        new ProjectHelperRepository();
    private List helpers = new ArrayList();
    private static final Class[] NO_CLASS = new Class[0];
    private static final Object[] NO_OBJECT = new Object[0];
    private static Constructor PROJECTHELPER2_CONSTRUCTOR;
    static {
        try {
            PROJECTHELPER2_CONSTRUCTOR = ProjectHelper2.class
                    .getConstructor(NO_CLASS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static ProjectHelperRepository getInstance() {
        return instance;
    }
    private ProjectHelperRepository() {
        collectProjectHelpers();
    }
    private void collectProjectHelpers() {
        Constructor projectHelper = getProjectHelperBySystemProperty();
        registerProjectHelper(projectHelper);
        try {
            ClassLoader classLoader = LoaderUtils.getContextClassLoader();
            if (classLoader != null) {
                Enumeration resources =
                    classLoader.getResources(ProjectHelper.SERVICE_ID);
                while (resources.hasMoreElements()) {
                    URL resource = (URL) resources.nextElement();
                    projectHelper =
                        getProjectHelperByService(resource.openStream());
                    registerProjectHelper(projectHelper);
                }
            }
            InputStream systemResource =
                ClassLoader.getSystemResourceAsStream(ProjectHelper.SERVICE_ID);
            if (systemResource != null) {
                projectHelper = getProjectHelperByService(systemResource);
                registerProjectHelper(projectHelper);
            }
        } catch (Exception e) {
            System.err.println("Unable to load ProjectHelper from service "
                               + ProjectHelper.SERVICE_ID + " ("
                               + e.getClass().getName()
                               + ": " + e.getMessage() + ")");
            if (DEBUG) {
                e.printStackTrace(System.err);
            }
        }
    }
    public void registerProjectHelper(String helperClassName)
            throws BuildException {
        registerProjectHelper(getHelperConstructor(helperClassName));
    }
    public void registerProjectHelper(Class helperClass) throws BuildException {
        try {
            registerProjectHelper(helperClass.getConstructor(NO_CLASS));
        } catch (NoSuchMethodException e) {
            throw new BuildException("Couldn't find no-arg constructor in "
                    + helperClass.getName());
        }
    }
    private void registerProjectHelper(Constructor helperConstructor) {
        if (helperConstructor == null) {
            return;
        }
        if (DEBUG) {
            System.out.println("ProjectHelper "
                    + helperConstructor.getClass().getName() + " registered.");
        }
        helpers.add(helperConstructor);
    }
    private Constructor getProjectHelperBySystemProperty() {
        String helperClass = System.getProperty(ProjectHelper.HELPER_PROPERTY);
        try {
            if (helperClass != null) {
                return getHelperConstructor(helperClass);
            }
        } catch (SecurityException e) {
            System.err.println("Unable to load ProjectHelper class \""
                               + helperClass + " specified in system property "
                               + ProjectHelper.HELPER_PROPERTY + " ("
                               + e.getMessage() + ")");
            if (DEBUG) {
                e.printStackTrace(System.err);
            }
        }
        return null;
    }
    private Constructor getProjectHelperByService(InputStream is) {
        try {
            InputStreamReader isr;
            try {
                isr = new InputStreamReader(is, "UTF-8");
            } catch (java.io.UnsupportedEncodingException e) {
                isr = new InputStreamReader(is);
            }
            BufferedReader rd = new BufferedReader(isr);
            String helperClassName = rd.readLine();
            rd.close();
            if (helperClassName != null && !"".equals(helperClassName)) {
                return getHelperConstructor(helperClassName);
            }
        } catch (Exception e) {
            System.out.println("Unable to load ProjectHelper from service "
                    + ProjectHelper.SERVICE_ID + " (" + e.getMessage() + ")");
            if (DEBUG) {
                e.printStackTrace(System.err);
            }
        }
        return null;
    }
    private Constructor getHelperConstructor(String helperClass) throws BuildException {
        ClassLoader classLoader = LoaderUtils.getContextClassLoader();
        try {
            Class clazz = null;
            if (classLoader != null) {
                try {
                    clazz = classLoader.loadClass(helperClass);
                } catch (ClassNotFoundException ex) {
                }
            }
            if (clazz == null) {
                clazz = Class.forName(helperClass);
            }
            return clazz.getConstructor(NO_CLASS);
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }
    public ProjectHelper getProjectHelperForBuildFile(Resource buildFile) throws BuildException {
        Iterator it = getHelpers();
        while (it.hasNext()) {
            ProjectHelper helper = (ProjectHelper) it.next();
            if (helper.canParseBuildFile(buildFile)) {
                if (DEBUG) {
                    System.out.println("ProjectHelper "
                                       + helper.getClass().getName()
                                       + " selected for the build file "
                                       + buildFile);
                }
                return helper;
            }
        }
        throw new RuntimeException("BUG: at least the ProjectHelper2 should "
                                   + "have supported the file " + buildFile);
    }
    public ProjectHelper getProjectHelperForAntlib(Resource antlib) throws BuildException {
        Iterator it = getHelpers();
        while (it.hasNext()) {
            ProjectHelper helper = (ProjectHelper) it.next();
            if (helper.canParseAntlibDescriptor(antlib)) {
                if (DEBUG) {
                    System.out.println("ProjectHelper "
                                       + helper.getClass().getName()
                                       + " selected for the antlib "
                                       + antlib);
                }
                return helper;
            }
        }
        throw new RuntimeException("BUG: at least the ProjectHelper2 should "
                                   + "have supported the file " + antlib);
    }
    public Iterator getHelpers() {
        return new ConstructingIterator(helpers.iterator());
    }
    private static class ConstructingIterator implements Iterator {
        private final Iterator nested;
        private boolean empty = false;
        ConstructingIterator(Iterator nested) {
            this.nested = nested;
        }
        public boolean hasNext() {
            return nested.hasNext() || !empty;
        }
        public Object next() {
            Constructor c;
            if (nested.hasNext()) {
                c = (Constructor) nested.next();
            } else {
                empty = true;
                c = PROJECTHELPER2_CONSTRUCTOR;
            }
            try {
                return c.newInstance(NO_OBJECT);
            } catch (Exception e) {
                throw new BuildException("Failed to invoke no-arg constructor"
                                         + " on " + c.getName());
            }
        }
        public void remove() {
            throw new UnsupportedOperationException("remove is not supported");
        }
    }
}
