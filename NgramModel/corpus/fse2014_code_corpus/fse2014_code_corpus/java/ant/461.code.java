package org.apache.tools.ant.taskdefs.optional.jlink;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
class ConstantPool {
    static final
        byte UTF8 = 1, UNUSED = 2, INTEGER = 3, FLOAT = 4, LONG = 5, DOUBLE = 6,
        CLASS = 7, STRING = 8, FIELDREF = 9, METHODREF = 10,
        INTERFACEMETHODREF = 11, NAMEANDTYPE = 12;
    byte[] types;
    Object[] values;
    ConstantPool(DataInput data) throws IOException {
        super();
        int count = data.readUnsignedShort();
        types = new byte [ count ];
        values = new Object [ count ];
        for (int i = 1; i < count; i++) {
            byte type = data.readByte();
            types[i] = type;
            switch (type) {
            case UTF8 :
                values[i] = data.readUTF();
                break;
            case UNUSED :
                break;
            case INTEGER :
                values[i] = new Integer(data.readInt());
                break;
            case FLOAT :
                values[i] = new Float(data.readFloat());
                break;
            case LONG :
                values[i] = new Long(data.readLong());
                ++i;
                break;
            case DOUBLE :
                values[i] = new Double(data.readDouble());
                ++i;
                break;
            case CLASS :
            case STRING :
                values[i] = new Integer(data.readUnsignedShort());
                break;
            case FIELDREF :
            case METHODREF :
            case INTERFACEMETHODREF :
            case NAMEANDTYPE :
                values[i] = new Integer(data.readInt());
                break;
            default:
            }
        }
    }
}
public class ClassNameReader extends Object {
    private static final int CLASS_MAGIC_NUMBER =  0xCAFEBABE;
    public static String getClassName(InputStream input) throws IOException {
        DataInputStream data = new DataInputStream(input);
        int cookie = data.readInt();
        if (cookie != CLASS_MAGIC_NUMBER) {
            return null;
        }
         data.readInt();
        ConstantPool constants = new ConstantPool(data);
        Object[] values = constants.values;
         data.readUnsignedShort();
        int classIndex = data.readUnsignedShort();
        Integer stringIndex = (Integer) values[classIndex];
        String className = (String) values[stringIndex.intValue()];
        return className;
    }
}
