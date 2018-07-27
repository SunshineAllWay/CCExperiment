package org.apache.tools.ant.taskdefs.optional.depend.constantpool;
import java.io.DataInputStream;
import java.io.IOException;
public class NameAndTypeCPInfo extends ConstantPoolEntry {
    public NameAndTypeCPInfo() {
        super(CONSTANT_NAMEANDTYPE, 1);
    }
    public void read(DataInputStream cpStream) throws IOException {
        nameIndex = cpStream.readUnsignedShort();
        descriptorIndex = cpStream.readUnsignedShort();
    }
    public String toString() {
        String value;
        if (isResolved()) {
            value = "Name = " + name + ", type = " + type;
        } else {
            value = "Name index = " + nameIndex
                 + ", descriptor index = " + descriptorIndex;
        }
        return value;
    }
    public void resolve(ConstantPool constantPool) {
        name = ((Utf8CPInfo) constantPool.getEntry(nameIndex)).getValue();
        type = ((Utf8CPInfo) constantPool.getEntry(descriptorIndex)).getValue();
        super.resolve(constantPool);
    }
    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    }
    private String name;
    private String type;
    private int nameIndex;
    private int descriptorIndex;
}
