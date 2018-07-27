package org.apache.tools.zip;
public final class ZipLong implements Cloneable {
    private static final int WORD = 4;
    private static final int BYTE_MASK = 0xFF;
    private static final int BYTE_1 = 1;
    private static final int BYTE_1_MASK = 0xFF00;
    private static final int BYTE_1_SHIFT = 8;
    private static final int BYTE_2 = 2;
    private static final int BYTE_2_MASK = 0xFF0000;
    private static final int BYTE_2_SHIFT = 16;
    private static final int BYTE_3 = 3;
    private static final long BYTE_3_MASK = 0xFF000000L;
    private static final int BYTE_3_SHIFT = 24;
    private long value;
    public ZipLong(long value) {
        this.value = value;
    }
    public ZipLong (byte[] bytes) {
        this(bytes, 0);
    }
    public ZipLong (byte[] bytes, int offset) {
        value = ZipLong.getValue(bytes, offset);
    }
    public byte[] getBytes() {
        return ZipLong.getBytes(value);
    }
    public long getValue() {
        return value;
    }
    public static byte[] getBytes(long value) {
        byte[] result = new byte[WORD];
        result[0] = (byte) ((value & BYTE_MASK));
        result[BYTE_1] = (byte) ((value & BYTE_1_MASK) >> BYTE_1_SHIFT);
        result[BYTE_2] = (byte) ((value & BYTE_2_MASK) >> BYTE_2_SHIFT);
        result[BYTE_3] = (byte) ((value & BYTE_3_MASK) >> BYTE_3_SHIFT);
        return result;
    }
    public static long getValue(byte[] bytes, int offset) {
        long value = (bytes[offset + BYTE_3] << BYTE_3_SHIFT) & BYTE_3_MASK;
        value += (bytes[offset + BYTE_2] << BYTE_2_SHIFT) & BYTE_2_MASK;
        value += (bytes[offset + BYTE_1] << BYTE_1_SHIFT) & BYTE_1_MASK;
        value += (bytes[offset] & BYTE_MASK);
        return value;
    }
    public static long getValue(byte[] bytes) {
        return getValue(bytes, 0);
    }
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ZipLong)) {
            return false;
        }
        return value == ((ZipLong) o).getValue();
    }
    public int hashCode() {
        return (int) value;
    }
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException cnfe) {
            throw new RuntimeException(cnfe);
        }
    }
}
