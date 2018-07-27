package org.apache.tools.ant.taskdefs.optional.depend.constantpool;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
public class ConstantPool {
    private Vector entries;
    private Hashtable utf8Indexes;
    public ConstantPool() {
        entries = new Vector();
        entries.addElement(null);
        utf8Indexes = new Hashtable();
    }
    public void read(DataInputStream classStream) throws IOException {
        int numEntries = classStream.readUnsignedShort();
        for (int i = 1; i < numEntries;) {
            ConstantPoolEntry nextEntry
                 = ConstantPoolEntry.readEntry(classStream);
            i += nextEntry.getNumEntries();
            addEntry(nextEntry);
        }
    }
    public int size() {
        return entries.size();
    }
    public int addEntry(ConstantPoolEntry entry) {
        int index = entries.size();
        entries.addElement(entry);
        int numSlots = entry.getNumEntries();
        for (int j = 0; j < numSlots - 1; ++j) {
            entries.addElement(null);
        }
        if (entry instanceof Utf8CPInfo) {
            Utf8CPInfo utf8Info = (Utf8CPInfo) entry;
            utf8Indexes.put(utf8Info.getValue(), new Integer(index));
        }
        return index;
    }
    public void resolve() {
        for (Enumeration i = entries.elements(); i.hasMoreElements();) {
            ConstantPoolEntry poolInfo = (ConstantPoolEntry) i.nextElement();
            if (poolInfo != null && !poolInfo.isResolved()) {
                poolInfo.resolve(this);
            }
        }
    }
    public ConstantPoolEntry getEntry(int index) {
        return (ConstantPoolEntry) entries.elementAt(index);
    }
    public int getUTF8Entry(String value) {
        int index = -1;
        Integer indexInteger = (Integer) utf8Indexes.get(value);
        if (indexInteger != null) {
            index = indexInteger.intValue();
        }
        return index;
    }
    public int getClassEntry(String className) {
        int index = -1;
        for (int i = 0; i < entries.size() && index == -1; ++i) {
            Object element = entries.elementAt(i);
            if (element instanceof ClassCPInfo) {
                ClassCPInfo classinfo = (ClassCPInfo) element;
                if (classinfo.getClassName().equals(className)) {
                    index = i;
                }
            }
        }
        return index;
    }
    public int getConstantEntry(Object constantValue) {
        int index = -1;
        for (int i = 0; i < entries.size() && index == -1; ++i) {
            Object element = entries.elementAt(i);
            if (element instanceof ConstantCPInfo) {
                ConstantCPInfo constantEntry = (ConstantCPInfo) element;
                if (constantEntry.getValue().equals(constantValue)) {
                    index = i;
                }
            }
        }
        return index;
    }
    public int getMethodRefEntry(String methodClassName, String methodName,
                                 String methodType) {
        int index = -1;
        for (int i = 0; i < entries.size() && index == -1; ++i) {
            Object element = entries.elementAt(i);
            if (element instanceof MethodRefCPInfo) {
                MethodRefCPInfo methodRefEntry = (MethodRefCPInfo) element;
                if (methodRefEntry.getMethodClassName().equals(methodClassName)
                     && methodRefEntry.getMethodName().equals(methodName)
                     && methodRefEntry.getMethodType().equals(methodType)) {
                    index = i;
                }
            }
        }
        return index;
    }
    public int getInterfaceMethodRefEntry(String interfaceMethodClassName,
                                          String interfaceMethodName,
                                          String interfaceMethodType) {
        int index = -1;
        for (int i = 0; i < entries.size() && index == -1; ++i) {
            Object element = entries.elementAt(i);
            if (element instanceof InterfaceMethodRefCPInfo) {
                InterfaceMethodRefCPInfo interfaceMethodRefEntry
                     = (InterfaceMethodRefCPInfo) element;
                if (interfaceMethodRefEntry.getInterfaceMethodClassName().equals(
                        interfaceMethodClassName)
                     && interfaceMethodRefEntry.getInterfaceMethodName().equals(
                         interfaceMethodName)
                     && interfaceMethodRefEntry.getInterfaceMethodType().equals(
                         interfaceMethodType)) {
                    index = i;
                }
            }
        }
        return index;
    }
    public int getFieldRefEntry(String fieldClassName, String fieldName,
                                String fieldType) {
        int index = -1;
        for (int i = 0; i < entries.size() && index == -1; ++i) {
            Object element = entries.elementAt(i);
            if (element instanceof FieldRefCPInfo) {
                FieldRefCPInfo fieldRefEntry = (FieldRefCPInfo) element;
                if (fieldRefEntry.getFieldClassName().equals(fieldClassName)
                     && fieldRefEntry.getFieldName().equals(fieldName)
                     && fieldRefEntry.getFieldType().equals(fieldType)) {
                    index = i;
                }
            }
        }
        return index;
    }
    public int getNameAndTypeEntry(String name, String type) {
        int index = -1;
        for (int i = 0; i < entries.size() && index == -1; ++i) {
            Object element = entries.elementAt(i);
            if (element instanceof NameAndTypeCPInfo) {
                NameAndTypeCPInfo nameAndTypeEntry
                    = (NameAndTypeCPInfo) element;
                if (nameAndTypeEntry.getName().equals(name)
                     && nameAndTypeEntry.getType().equals(type)) {
                    index = i;
                }
            }
        }
        return index;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer("\n");
        int size = entries.size();
        for (int i = 0; i < size; ++i) {
            sb.append("[" + i + "] = " + getEntry(i) + "\n");
        }
        return sb.toString();
    }
}
