package org.apache.tools.ant.taskdefs.optional.depend.constantpool;
import java.io.DataInputStream;
import java.io.IOException;
public class Utf8CPInfo extends ConstantPoolEntry {
    private String value;
    public Utf8CPInfo() {
        super(CONSTANT_UTF8, 1);
    }
    public void read(DataInputStream cpStream) throws IOException {
        value = cpStream.readUTF();
    }
    public String toString() {
        return "UTF8 Value = " + value;
    }
    public String getValue() {
        return value;
    }
}
