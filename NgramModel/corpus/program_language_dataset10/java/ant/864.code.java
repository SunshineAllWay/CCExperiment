package org.apache.tools.zip;
import java.io.UnsupportedEncodingException;
import java.util.zip.CRC32;
import java.util.zip.ZipException;
public abstract class AbstractUnicodeExtraField implements ZipExtraField {
    private long nameCRC32;
    private byte[] unicodeName;
    private byte[] data;
    protected AbstractUnicodeExtraField() {
    }
    protected AbstractUnicodeExtraField(String text, byte[] bytes, int off,
                                        int len) {
        CRC32 crc32 = new CRC32();
        crc32.update(bytes, off, len);
        nameCRC32 = crc32.getValue();
        try {
            unicodeName = text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("FATAL: UTF-8 encoding not supported.",
                                       e);
        }
    }
    protected AbstractUnicodeExtraField(String text, byte[] bytes) {
        this(text, bytes, 0, bytes.length);
    }
    private void assembleData() {
        if (unicodeName == null) {
            return;
        }
        data = new byte[5 + unicodeName.length];
        data[0] = 0x01;
        System.arraycopy(ZipLong.getBytes(nameCRC32), 0, data, 1, 4);
        System.arraycopy(unicodeName, 0, data, 5, unicodeName.length);
    }
    public long getNameCRC32() {
        return nameCRC32;
    }
    public void setNameCRC32(long nameCRC32) {
        this.nameCRC32 = nameCRC32;
        data = null;
    }
    public byte[] getUnicodeName() {
        return unicodeName;
    }
    public void setUnicodeName(byte[] unicodeName) {
        this.unicodeName = unicodeName;
        data = null;
    }
    public byte[] getCentralDirectoryData() {
        if (data == null) {
            this.assembleData();
        }
        return data;
    }
    public ZipShort getCentralDirectoryLength() {
        if (data == null) {
            assembleData();
        }
        return new ZipShort(data.length);
    }
    public byte[] getLocalFileDataData() {
        return getCentralDirectoryData();
    }
    public ZipShort getLocalFileDataLength() {
        return getCentralDirectoryLength();
    }
    public void parseFromLocalFileData(byte[] buffer, int offset, int length)
        throws ZipException {
        if (length < 5) {
            throw new ZipException("UniCode path extra data must have at least"
                                   + " 5 bytes.");
        }
        int version = buffer[offset];
        if (version != 0x01) {
            throw new ZipException("Unsupported version [" + version
                                   + "] for UniCode path extra data.");
        }
        nameCRC32 = ZipLong.getValue(buffer, offset + 1);
        unicodeName = new byte[length - 5];
        System.arraycopy(buffer, offset + 5, unicodeName, 0, length - 5);
        data = null;
    }
}
