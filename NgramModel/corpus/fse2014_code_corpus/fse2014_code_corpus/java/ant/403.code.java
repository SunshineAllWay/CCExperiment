package org.apache.tools.ant.taskdefs.optional.depend.constantpool;
import java.io.DataInputStream;
import java.io.IOException;
public class IntegerCPInfo extends ConstantCPInfo {
    public IntegerCPInfo() {
        super(CONSTANT_INTEGER, 1);
    }
    public void read(DataInputStream cpStream) throws IOException {
        setValue(new Integer(cpStream.readInt()));
    }
    public String toString() {
        return "Integer Constant Pool Entry: " + getValue();
    }
}
