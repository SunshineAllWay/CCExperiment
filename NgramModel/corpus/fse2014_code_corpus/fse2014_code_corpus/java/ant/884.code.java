package org.apache.tools.zip;
public final class ZipShort implements Cloneable {
    private static final int BYTE_MASK = 0xFF;
    private static final int BYTE_1_MASK = 0xFF00;
    private static final int BYTE_1_SHIFT = 8;
    private int value;
    public ZipShort (int value) {
        this.value = value;
    }
    public ZipShort (byte[] bytes) {
        this(bytes, 0);
    }
    public ZipShort (byte[] bytes, int offset) {
        value = ZipShort.getValue(bytes, offset);
    }
    public byte[] getBytes() {
        byte[] result = new byte[2];
        result[0] = (byte) (value & BYTE_MASK);
        result[1] = (byte) ((value & BYTE_1_MASK) >> BYTE_1_SHIFT);
        return result;
    }
    public int getValue() {
        return value;
    }
    public static byte[] getBytes(int value) {
        byte[] result = new byte[2];
        result[0] = (byte) (value & BYTE_MASK);
        result[1] = (byte) ((value & BYTE_1_MASK) >> BYTE_1_SHIFT);
        return result;
    }
    public static int getValue(byte[] bytes, int offset) {
        int value = (bytes[offset + 1] << BYTE_1_SHIFT) & BYTE_1_MASK;
        value += (bytes[offset] & BYTE_MASK);
        return value;
    }
    public static int getValue(byte[] bytes) {
        return getValue(bytes, 0);
    }
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ZipShort)) {
            return false;
        }
        return value == ((ZipShort) o).getValue();
    }
    public int hashCode() {
        return value;
    }
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException cnfe) {
            throw new RuntimeException(cnfe);
        }
    }
}
