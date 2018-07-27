package org.apache.tools.ant.util;
import java.lang.reflect.Constructor;
public class ReflectWrapper {
    private Object obj;
    public ReflectWrapper(ClassLoader loader, String name) {
        try {
            Class clazz;
            clazz = Class.forName(name, true, loader);
            Constructor constructor;
            constructor = clazz.getConstructor((Class[]) null);
            obj = constructor.newInstance((Object[]) null);
        } catch (Exception t) {
            ReflectUtil.throwBuildException(t);
        }
    }
    public ReflectWrapper(Object obj) {
        this.obj = obj;
    }
    public Object getObject() {
        return obj;
    }
    public Object invoke(String methodName) {
        return ReflectUtil.invoke(obj, methodName);
    }
    public Object invoke(
        String methodName, Class argType, Object arg) {
        return ReflectUtil.invoke(obj, methodName, argType, arg);
    }
    public Object invoke(
        String methodName, Class argType1, Object arg1,
        Class argType2, Object arg2) {
        return ReflectUtil.invoke(
            obj, methodName, argType1, arg1, argType2, arg2);
    }
}
