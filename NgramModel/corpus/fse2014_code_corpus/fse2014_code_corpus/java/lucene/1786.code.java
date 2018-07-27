package org.apache.lucene.util;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
public abstract class AttributeImpl implements Cloneable, Serializable, Attribute {  
  public abstract void clear();
  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    Class<?> clazz = this.getClass();
    Field[] fields = clazz.getDeclaredFields();
    try {
      for (int i = 0; i < fields.length; i++) {
        Field f = fields[i];
        if (Modifier.isStatic(f.getModifiers())) continue;
        f.setAccessible(true);
        Object value = f.get(this);
        if (buffer.length()>0) {
          buffer.append(',');
        }
        if (value == null) {
          buffer.append(f.getName() + "=null");
        } else {
          buffer.append(f.getName() + "=" + value);
        }
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    return buffer.toString();
  }
  @Override
  public abstract int hashCode();
  @Override
  public abstract boolean equals(Object other);
  public abstract void copyTo(AttributeImpl target);
  @Override
  public Object clone() {
    Object clone = null;
    try {
      clone = super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);  
    }
    return clone;
  }
}
