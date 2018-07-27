package org.apache.tools.ant.taskdefs.optional.depend.constantpool;
import java.io.DataInputStream;
import java.io.IOException;
public class FloatCPInfo extends ConstantCPInfo {
    public FloatCPInfo() {
        super(CONSTANT_FLOAT, 1);
    }
    public void read(DataInputStream cpStream) throws IOException {
        setValue(new Float(cpStream.readFloat()));
    }
    public String toString() {
        return "Float Constant Pool Entry: " + getValue();
    }
}
