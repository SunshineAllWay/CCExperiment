package org.apache.tools.ant.taskdefs.optional.depend.constantpool;
import java.io.DataInputStream;
import java.io.IOException;
public class DoubleCPInfo extends ConstantCPInfo {
    public DoubleCPInfo() {
        super(CONSTANT_DOUBLE, 2);
    }
    public void read(DataInputStream cpStream) throws IOException {
        setValue(new Double(cpStream.readDouble()));
    }
    public String toString() {
        return "Double Constant Pool Entry: " + getValue();
    }
}
