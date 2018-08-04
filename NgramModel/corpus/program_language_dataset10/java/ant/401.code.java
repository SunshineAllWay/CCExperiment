package org.apache.tools.ant.taskdefs.optional.depend.constantpool;
import java.io.DataInputStream;
import java.io.IOException;
public class FieldRefCPInfo extends ConstantPoolEntry {
    private String fieldClassName;
    private String fieldName;
    private String fieldType;
    private int classIndex;
    private int nameAndTypeIndex;
    public FieldRefCPInfo() {
        super(CONSTANT_FIELDREF, 1);
    }
    public void read(DataInputStream cpStream) throws IOException {
        classIndex = cpStream.readUnsignedShort();
        nameAndTypeIndex = cpStream.readUnsignedShort();
    }
    public void resolve(ConstantPool constantPool) {
        ClassCPInfo fieldClass
            = (ClassCPInfo) constantPool.getEntry(classIndex);
        fieldClass.resolve(constantPool);
        fieldClassName = fieldClass.getClassName();
        NameAndTypeCPInfo nt
            = (NameAndTypeCPInfo) constantPool.getEntry(nameAndTypeIndex);
        nt.resolve(constantPool);
        fieldName = nt.getName();
        fieldType = nt.getType();
        super.resolve(constantPool);
    }
    public String toString() {
        String value;
        if (isResolved()) {
            value = "Field : Class = " + fieldClassName + ", name = "
                + fieldName + ", type = " + fieldType;
        } else {
            value = "Field : Class index = " + classIndex
                + ", name and type index = " + nameAndTypeIndex;
        }
        return value;
    }
    public String getFieldClassName() {
        return fieldClassName;
    }
    public String getFieldName() {
        return fieldName;
    }
    public String getFieldType() {
        return fieldType;
    }
}