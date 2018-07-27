package org.apache.tools.ant.taskdefs.optional.depend.constantpool;
import java.io.DataInputStream;
import java.io.IOException;
public class InterfaceMethodRefCPInfo extends ConstantPoolEntry {
    private String interfaceMethodClassName;
    private String interfaceMethodName;
    private String interfaceMethodType;
    private int classIndex;
    private int nameAndTypeIndex;
    public InterfaceMethodRefCPInfo() {
        super(CONSTANT_INTERFACEMETHODREF, 1);
    }
    public void read(DataInputStream cpStream) throws IOException {
        classIndex = cpStream.readUnsignedShort();
        nameAndTypeIndex = cpStream.readUnsignedShort();
    }
    public void resolve(ConstantPool constantPool) {
        ClassCPInfo interfaceMethodClass
             = (ClassCPInfo) constantPool.getEntry(classIndex);
        interfaceMethodClass.resolve(constantPool);
        interfaceMethodClassName = interfaceMethodClass.getClassName();
        NameAndTypeCPInfo nt
             = (NameAndTypeCPInfo) constantPool.getEntry(nameAndTypeIndex);
        nt.resolve(constantPool);
        interfaceMethodName = nt.getName();
        interfaceMethodType = nt.getType();
        super.resolve(constantPool);
    }
    public String toString() {
        String value;
        if (isResolved()) {
            value = "InterfaceMethod : Class = " + interfaceMethodClassName
                 + ", name = " + interfaceMethodName + ", type = "
                 + interfaceMethodType;
        } else {
            value = "InterfaceMethod : Class index = " + classIndex
                 + ", name and type index = " + nameAndTypeIndex;
        }
        return value;
    }
    public String getInterfaceMethodClassName() {
        return interfaceMethodClassName;
    }
    public String getInterfaceMethodName() {
        return interfaceMethodName;
    }
    public String getInterfaceMethodType() {
        return interfaceMethodType;
    }
}
