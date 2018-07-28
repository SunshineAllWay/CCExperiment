package org.apache.tools.ant.util;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.tools.ant.BuildException;
import java.lang.reflect.Field;
public class ReflectUtil {
    private ReflectUtil() {
    }
    public static Object newInstance(Class ofClass,
                                     Class[] argTypes,
                                     Object[] args) {
        try {
            Constructor con = ofClass.getConstructor(argTypes);
            return con.newInstance(args);
        } catch (Exception t) {
            throwBuildException(t);
            return null; 
        }
    }
    public static Object invoke(Object obj, String methodName) {
        try {
            Method method;
            method = obj.getClass().getMethod(
                        methodName, (Class[]) null);
            return method.invoke(obj, (Object[]) null);
        } catch (Exception t) {
            throwBuildException(t);
            return null; 
        }
    }
    public static Object invokeStatic(Object obj, String methodName) {
        try {
            Method method;
            method = ((Class) obj).getMethod(
                    methodName, (Class[]) null);
            return method.invoke(obj, (Object[]) null);
        }  catch (Exception t) {
            throwBuildException(t);
            return null; 
        }
    }
    public static Object invoke(
        Object obj, String methodName, Class argType, Object arg) {
        try {
            Method method;
            method = obj.getClass().getMethod(
                methodName, new Class[] {argType});
            return method.invoke(obj, new Object[] {arg});
        } catch (Exception t) {
            throwBuildException(t);
            return null; 
        }
    }
    public static Object invoke(
        Object obj, String methodName, Class argType1, Object arg1,
        Class argType2, Object arg2) {
        try {
            Method method;
            method = obj.getClass().getMethod(
                methodName, new Class[] {argType1, argType2});
            return method.invoke(obj, new Object[] {arg1, arg2});
        } catch (Exception t) {
            throwBuildException(t);
            return null; 
        }
    }
    public static Object getField(Object obj, String fieldName)
        throws BuildException {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception t) {
            throwBuildException(t);
            return null; 
        }
    }
    public static void throwBuildException(Exception t)
        throws BuildException {
        throw toBuildException(t);
    }
    public static BuildException toBuildException(Exception t) {
        if (t instanceof InvocationTargetException) {
            Throwable t2 = ((InvocationTargetException) t)
                .getTargetException();
            if (t2 instanceof BuildException) {
                return (BuildException) t2;
            }
            return new BuildException(t2);
        } else {
            return new BuildException(t);
        }
    }
    public static boolean respondsTo(Object o, String methodName)
        throws BuildException {
        try {
            Method[] methods = o.getClass().getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(methodName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception t) {
            throw toBuildException(t);
        }
    }
}