package org.apache.tools.ant.taskdefs.optional.depend.constantpool;
import java.io.DataInputStream;
import java.io.IOException;
public class StringCPInfo extends ConstantCPInfo {
    public StringCPInfo() {
        super(CONSTANT_STRING, 1);
    }
    public void read(DataInputStream cpStream) throws IOException {
        index = cpStream.readUnsignedShort();
        setValue("unresolved");
    }
    public String toString() {
        return "String Constant Pool Entry for "
            + getValue() + "[" + index + "]";
    }
    public void resolve(ConstantPool constantPool) {
        setValue(((Utf8CPInfo) constantPool.getEntry(index)).getValue());
        super.resolve(constantPool);
    }
    private int index;
}
