package org.apache.tools.ant.taskdefs.optional.depend.constantpool;
import java.io.DataInputStream;
import java.io.IOException;
public abstract class ConstantPoolEntry {
    public static final int CONSTANT_UTF8 = 1;
    public static final int CONSTANT_INTEGER = 3;
    public static final int CONSTANT_FLOAT = 4;
    public static final int CONSTANT_LONG = 5;
    public static final int CONSTANT_DOUBLE = 6;
    public static final int CONSTANT_CLASS = 7;
    public static final int CONSTANT_STRING = 8;
    public static final int CONSTANT_FIELDREF = 9;
    public static final int CONSTANT_METHODREF = 10;
    public static final int CONSTANT_INTERFACEMETHODREF = 11;
    public static final int CONSTANT_NAMEANDTYPE = 12;
    private int tag;
    private int numEntries;
    private boolean resolved;
    public ConstantPoolEntry(int tagValue, int entries) {
        tag = tagValue;
        numEntries = entries;
        resolved = false;
    }
    public static ConstantPoolEntry readEntry(DataInputStream cpStream)
         throws IOException {
        ConstantPoolEntry cpInfo = null;
        int cpTag = cpStream.readUnsignedByte();
        switch (cpTag) {
            case CONSTANT_UTF8:
                cpInfo = new Utf8CPInfo();
                break;
            case CONSTANT_INTEGER:
                cpInfo = new IntegerCPInfo();
                break;
            case CONSTANT_FLOAT:
                cpInfo = new FloatCPInfo();
                break;
            case CONSTANT_LONG:
                cpInfo = new LongCPInfo();
                break;
            case CONSTANT_DOUBLE:
                cpInfo = new DoubleCPInfo();
                break;
            case CONSTANT_CLASS:
                cpInfo = new ClassCPInfo();
                break;
            case CONSTANT_STRING:
                cpInfo = new StringCPInfo();
                break;
            case CONSTANT_FIELDREF:
                cpInfo = new FieldRefCPInfo();
                break;
            case CONSTANT_METHODREF:
                cpInfo = new MethodRefCPInfo();
                break;
            case CONSTANT_INTERFACEMETHODREF:
                cpInfo = new InterfaceMethodRefCPInfo();
                break;
            case CONSTANT_NAMEANDTYPE:
                cpInfo = new NameAndTypeCPInfo();
                break;
            default:
                throw new ClassFormatError("Invalid Constant Pool entry Type "
                     + cpTag);
        }
        cpInfo.read(cpStream);
        return cpInfo;
    }
    public boolean isResolved() {
        return resolved;
    }
    public void resolve(ConstantPool constantPool) {
        resolved = true;
    }
    public abstract void read(DataInputStream cpStream) throws IOException;
    public int getTag() {
        return tag;
    }
    public final int getNumEntries() {
        return numEntries;
    }
}