package org.apache.tools.ant;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
public class AntTypeDefinition {
    private String      name;
    private Class       clazz;
    private Class       adapterClass;
    private Class       adaptToClass;
    private String      className;
    private ClassLoader classLoader;
    private boolean     restrict = false;
     public void setRestrict(boolean restrict) {
         this.restrict = restrict;
     }
    public boolean isRestrict() {
        return restrict;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setClass(Class clazz) {
        this.clazz = clazz;
        if (clazz == null) {
            return;
        }
        this.classLoader = (classLoader == null)
            ? clazz.getClassLoader() : classLoader;
        this.className = (className == null) ? clazz.getName() : className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public String getClassName() {
        return className;
    }
    public void setAdapterClass(Class adapterClass) {
        this.adapterClass = adapterClass;
    }
    public void setAdaptToClass(Class adaptToClass) {
        this.adaptToClass = adaptToClass;
    }
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    public Class getExposedClass(Project project) {
        if (adaptToClass != null) {
            Class z = getTypeClass(project);
            if (z == null || adaptToClass.isAssignableFrom(z)) {
                return z;
            }
        }
        return (adapterClass == null) ? getTypeClass(project) :  adapterClass;
    }
    public Class getTypeClass(Project project) {
        try {
            return innerGetTypeClass();
        } catch (NoClassDefFoundError ncdfe) {
            project.log("Could not load a dependent class ("
                        + ncdfe.getMessage() + ") for type "
                        + name, Project.MSG_DEBUG);
        } catch (ClassNotFoundException cnfe) {
            project.log("Could not load class (" + className
                        + ") for type " + name, Project.MSG_DEBUG);
        }
        return null;
    }
    public Class innerGetTypeClass() throws ClassNotFoundException {
        if (clazz != null) {
            return clazz;
        }
        if (classLoader == null) {
            clazz = Class.forName(className);
        } else {
            clazz = classLoader.loadClass(className);
        }
        return clazz;
    }
    public Object create(Project project) {
        return icreate(project);
    }
    private Object icreate(Project project) {
        Class c = getTypeClass(project);
        if (c == null) {
            return null;
        }
        Object o = createAndSet(project, c);
        if (o == null || adapterClass == null) {
            return o;
        }
        if (adaptToClass != null) {
            if (adaptToClass.isAssignableFrom(o.getClass())) {
                return o;
            }
        }
        TypeAdapter adapterObject = (TypeAdapter) createAndSet(
            project, adapterClass);
        if (adapterObject == null) {
            return null;
        }
        adapterObject.setProxy(o);
        return adapterObject;
    }
    public void checkClass(Project project) {
        if (clazz == null) {
            clazz = getTypeClass(project);
            if (clazz == null) {
                throw new BuildException(
                    "Unable to create class for " + getName());
            }
        }
        if (adapterClass != null && (adaptToClass == null
            || !adaptToClass.isAssignableFrom(clazz))) {
            TypeAdapter adapter = (TypeAdapter) createAndSet(
                project, adapterClass);
            if (adapter == null) {
                throw new BuildException("Unable to create adapter object");
            }
            adapter.checkProxyClass(clazz);
        }
    }
    private Object createAndSet(Project project, Class c) {
        try {
            Object o = innerCreateAndSet(c, project);
            return o;
        } catch (InvocationTargetException ex) {
            Throwable t = ex.getTargetException();
            throw new BuildException(
                "Could not create type " + name + " due to " + t, t);
        } catch (NoClassDefFoundError ncdfe) {
            String msg = "Type " + name + ": A class needed by class "
                + c + " cannot be found: " + ncdfe.getMessage();
            throw new BuildException(msg, ncdfe);
        } catch (NoSuchMethodException nsme) {
            throw new BuildException("Could not create type " + name
                    + " as the class " + c + " has no compatible constructor");
        } catch (InstantiationException nsme) {
            throw new BuildException("Could not create type "
                    + name + " as the class " + c + " is abstract");
        } catch (IllegalAccessException e) {
            throw new BuildException("Could not create type "
                    + name + " as the constructor " + c + " is not accessible");
        } catch (Throwable t) {
            throw new BuildException(
                "Could not create type " + name + " due to " + t, t);
        }
    }
    public Object innerCreateAndSet(Class newclass, Project project)
            throws NoSuchMethodException,
            InstantiationException,
            IllegalAccessException,
            InvocationTargetException {
        Constructor ctor = null;
        boolean noArg = false;
        try {
            ctor = newclass.getConstructor(new Class[0]);
            noArg = true;
        } catch (NoSuchMethodException nse) {
            ctor = newclass.getConstructor(new Class[] {Project.class});
            noArg = false;
        }
        Object o = ctor.newInstance(
            ((noArg) ? new Object[0] : new Object[] {project}));
        project.setProjectReference(o);
        return o;
    }
    public boolean sameDefinition(AntTypeDefinition other, Project project) {
        return (other != null && other.getClass() == getClass()
            && other.getTypeClass(project).equals(getTypeClass(project))
            && other.getExposedClass(project).equals(getExposedClass(project))
            && other.restrict == restrict
            && other.adapterClass == adapterClass
            && other.adaptToClass == adaptToClass);
    }
    public boolean similarDefinition(AntTypeDefinition other, Project project) {
        if (other == null
            || getClass() != other.getClass()
            || !getClassName().equals(other.getClassName())
            || !extractClassname(adapterClass).equals(
            extractClassname(other.adapterClass))
            || !extractClassname(adaptToClass).equals(
            extractClassname(other.adaptToClass))
            || restrict != other.restrict) {
            return false;
        }
        ClassLoader oldLoader = other.getClassLoader();
        ClassLoader newLoader = getClassLoader();
        return oldLoader == newLoader
            || (oldLoader instanceof AntClassLoader
            && newLoader instanceof AntClassLoader
            && ((AntClassLoader) oldLoader).getClasspath()
            .equals(((AntClassLoader) newLoader).getClasspath()));
    }
    private String extractClassname(Class c) {
        return (c == null) ? "<null>" : c.getClass().getName();
    }
}
