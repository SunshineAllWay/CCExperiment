package org.apache.tools.ant.taskdefs.optional.depend.constantpool;
import java.io.DataInputStream;
import java.io.IOException;
public class LongCPInfo extends ConstantCPInfo {
    public LongCPInfo() {
        super(CONSTANT_LONG, 2);
    }
    public void read(DataInputStream cpStream) throws IOException {
        setValue(new Long(cpStream.readLong()));
    }
    public String toString() {
        return "Long Constant Pool Entry: " + getValue();
    }
}
