package org.apache.tools.ant.taskdefs.optional.junit;
import java.util.Enumeration;
import java.util.NoSuchElementException;
public final class Enumerations {
        private Enumerations() {
        }
        public static Enumeration fromArray(Object[] array) {
                return new ArrayEnumeration(array);
        }
        public static Enumeration fromCompound(Enumeration[] enums) {
                return new CompoundEnumeration(enums);
        }
}
class ArrayEnumeration implements Enumeration {
        private Object[] array;
        private int pos;
        public ArrayEnumeration(Object[] array) {
                this.array = array;
                this.pos = 0;
        }
        public boolean hasMoreElements() {
                return (pos < array.length);
        }
        public Object nextElement() throws NoSuchElementException {
                if (hasMoreElements()) {
                        Object o = array[pos];
                        pos++;
                        return o;
                }
                throw new NoSuchElementException();
        }
}
 class CompoundEnumeration implements Enumeration {
        private Enumeration[] enumArray;
        private int index = 0;
    public CompoundEnumeration(Enumeration[] enumarray) {
                this.enumArray = enumarray;
    }
    public boolean hasMoreElements() {
                while (index < enumArray.length) {
                        if (enumArray[index] != null && enumArray[index].hasMoreElements()) {
                                return true;
                        }
                        index++;
                }
                return false;
    }
    public Object nextElement() throws NoSuchElementException {
                if (hasMoreElements()) {
                        return enumArray[index].nextElement();
                }
                throw new NoSuchElementException();
    }
}
