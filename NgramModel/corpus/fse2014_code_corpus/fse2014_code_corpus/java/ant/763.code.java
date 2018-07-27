package org.apache.tools.ant.util;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
public class ClasspathUtils {
    public static final String REUSE_LOADER_REF = MagicNames.REFID_CLASSPATH_REUSE_LOADER;
    public static ClassLoader getClassLoaderForPath(Project p, Reference ref) {
        return getClassLoaderForPath(p, ref, false);
    }
    public static ClassLoader getClassLoaderForPath(
        Project p, Reference ref, boolean reverseLoader) {
        String pathId = ref.getRefId();
        Object path = p.getReference(pathId);
        if (!(path instanceof Path)) {
            throw new BuildException("The specified classpathref " + pathId
                    + " does not reference a Path.");
        }
        String loaderId = MagicNames.REFID_CLASSPATH_LOADER_PREFIX + pathId;
        return getClassLoaderForPath(p, (Path) path, loaderId, reverseLoader);
    }
    public static ClassLoader getClassLoaderForPath(Project p, Path path, String loaderId) {
        return getClassLoaderForPath(p, path, loaderId, false);
    }
    public static ClassLoader getClassLoaderForPath(
            Project p, Path path, String loaderId, boolean reverseLoader) {
        return getClassLoaderForPath(p, path, loaderId, reverseLoader, isMagicPropertySet(p));
    }
    public static ClassLoader getClassLoaderForPath(
            Project p, Path path, String loaderId, boolean reverseLoader, boolean reuseLoader) {
        ClassLoader cl = null;
        if (loaderId != null && reuseLoader) {
            Object reusedLoader = p.getReference(loaderId);
            if (reusedLoader != null && !(reusedLoader instanceof ClassLoader)) {
                throw new BuildException("The specified loader id " + loaderId
                        + " does not reference a class loader");
            }
            cl = (ClassLoader) reusedLoader;
        }
        if (cl == null) {
            cl = getUniqueClassLoaderForPath(p, path, reverseLoader);
            if (loaderId != null && reuseLoader) {
                p.addReference(loaderId, cl);
            }
        }
        return cl;
    }
    public static ClassLoader getUniqueClassLoaderForPath(Project p, Path path,
            boolean reverseLoader) {
        AntClassLoader acl = p.createClassLoader(path);
        if (reverseLoader) {
            acl.setParentFirst(false);
            acl.addJavaLibraries();
        }
        return acl;
    }
    public static Object newInstance(String className, ClassLoader userDefinedLoader) {
        return newInstance(className, userDefinedLoader, Object.class);
    }
    public static Object newInstance(String className, ClassLoader userDefinedLoader,
            Class expectedType) {
        try {
            Class clazz = Class.forName(className, true, userDefinedLoader);
            Object o = clazz.newInstance();
            if (!expectedType.isInstance(o)) {
                throw new BuildException("Class of unexpected Type: " + className + " expected :"
                        + expectedType);
            }
            return o;
        } catch (ClassNotFoundException e) {
            throw new BuildException("Class not found: " + className, e);
        } catch (InstantiationException e) {
            throw new BuildException("Could not instantiate " + className
                    + ". Specified class should have a no " + "argument constructor.", e);
        } catch (IllegalAccessException e) {
            throw new BuildException("Could not instantiate " + className
                    + ". Specified class should have a " + "public constructor.", e);
        } catch (LinkageError e) {
            throw new BuildException("Class " + className
                    + " could not be loaded because of an invalid dependency.", e);
        }
    }
    public static Delegate getDelegate(ProjectComponent component) {
        return new Delegate(component);
    }
    private static boolean isMagicPropertySet(Project p) {
        return p.getProperty(REUSE_LOADER_REF) != null;
    }
    public static class Delegate {
        private final ProjectComponent component;
        private Path classpath;
        private String classpathId;
        private String className;
        private String loaderId;
        private boolean reverseLoader = false;
        Delegate(ProjectComponent component) {
            this.component = component;
        }
        public void setClasspath(Path classpath) {
            if (this.classpath == null) {
                this.classpath = classpath;
            } else {
                this.classpath.append(classpath);
            }
        }
        public Path createClasspath() {
            if (this.classpath == null) {
                this.classpath = new Path(component.getProject());
            }
            return this.classpath.createPath();
        }
        public void setClassname(String fcqn) {
            this.className = fcqn;
        }
        public void setClasspathref(Reference r) {
            this.classpathId = r.getRefId();
            createClasspath().setRefid(r);
        }
        public void setReverseLoader(boolean reverseLoader) {
            this.reverseLoader = reverseLoader;
        }
        public void setLoaderRef(Reference r) {
            this.loaderId = r.getRefId();
        }
        public ClassLoader getClassLoader() {
            return getClassLoaderForPath(getContextProject(), classpath, getClassLoadId(),
                    reverseLoader, loaderId != null || isMagicPropertySet(getContextProject()));
        }
        private Project getContextProject() {
            return component.getProject();
        }
        public String getClassLoadId() {
            if (loaderId == null && classpathId != null) {
                return MagicNames.REFID_CLASSPATH_LOADER_PREFIX + classpathId;
            } else {
                return loaderId;
            }
        }
        public Object newInstance() {
            return ClasspathUtils.newInstance(this.className, getClassLoader());
        }
        public Path getClasspath() {
            return classpath;
        }
        public boolean isReverseLoader() {
            return reverseLoader;
        }
    }
}
