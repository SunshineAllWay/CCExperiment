package org.apache.tools.ant.types.resources;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
public class JavaConstantResource extends AbstractClasspathResource {
    protected InputStream openInputStream(ClassLoader cl) throws IOException {
        Class clazz;
        String constant = getName();
        int index1 = constant.lastIndexOf('.');
        if (index1 < 0) {
            throw new IOException("No class name in " + constant);
        }
        int index = index1;
        String classname = constant.substring(0, index);
        String fieldname = constant.substring(index + 1, constant.length());
        try {
            clazz =
                cl != null
                ? Class.forName(classname, true, cl)
                : Class.forName(classname);
            Field field = clazz.getField(fieldname);
            String value = field.get(null).toString();
            return new ByteArrayInputStream(value.getBytes("UTF-8"));
        } catch (ClassNotFoundException e) {
            throw new IOException("Class not found:" + classname);
        } catch (NoSuchFieldException e) {
            throw new IOException(
                "Field not found:" + fieldname + " in " + classname);
        } catch (IllegalAccessException e) {
            throw new IOException("Illegal access to :" + fieldname + " in " + classname);
        } catch (NullPointerException npe) {
            throw new IOException("Not a static field: " + fieldname + " in " + classname);
        }
    }
}
