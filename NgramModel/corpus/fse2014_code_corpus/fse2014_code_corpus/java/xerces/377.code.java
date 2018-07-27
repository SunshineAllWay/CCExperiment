package org.apache.xerces.impl.dv.xs;
import java.util.AbstractList;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidationContext;
import org.apache.xerces.xs.datatypes.ObjectList;
public class ListDV extends TypeValidator{
    public short getAllowedFacets(){
          return (XSSimpleTypeDecl.FACET_LENGTH | XSSimpleTypeDecl.FACET_MINLENGTH | XSSimpleTypeDecl.FACET_MAXLENGTH | XSSimpleTypeDecl.FACET_PATTERN | XSSimpleTypeDecl.FACET_ENUMERATION | XSSimpleTypeDecl.FACET_WHITESPACE );
    }
    public Object getActualValue(String content, ValidationContext context) throws InvalidDatatypeValueException{
        return content;
    }
    public int getDataLength(Object value) {
        return ((ListData)value).getLength();
    }
    final static class ListData extends AbstractList implements ObjectList {
        final Object[] data;
        private String canonical;
        public ListData(Object[] data) {
            this.data = data;
        }
        public synchronized String toString() {
            if (canonical == null) {
                int len = data.length;
                StringBuffer buf = new StringBuffer();
                if (len > 0) {
                    buf.append(data[0].toString());
                }
                for (int i = 1; i < len; i++) {
                    buf.append(' ');
                    buf.append(data[i].toString());
                }
                canonical = buf.toString();
            }
            return canonical;
        }
        public int getLength() {
            return data.length;
        }
        public boolean equals(Object obj) {
            if (!(obj instanceof ListData))
                return false;
            Object[] odata = ((ListData)obj).data;
            int count = data.length;
            if (count != odata.length)
                return false;
            for (int i = 0 ; i < count ; i++) {
                if (!data[i].equals(odata[i]))
                    return false;
            }
            return true;
        }
        public int hashCode() {
            int hash = 0;
            for (int i = 0; i < data.length; ++i) {
                hash ^= data[i].hashCode();
            }
            return hash;
        }
        public boolean contains(Object item) {
            for (int i = 0;i < data.length; i++) {
                if (item == data[i]) {
                    return true;
                }
            }
            return false;
        }
        public Object item(int index) {
            if (index < 0 || index >= data.length) {
                return null;
            }
            return data[index];
        }
        public Object get(int index) {
            if (index >= 0 && index < data.length) {
                return data[index];
            }
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        public int size() {
            return getLength();
        }
    }
} 
