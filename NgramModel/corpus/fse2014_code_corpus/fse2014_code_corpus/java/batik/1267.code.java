package org.apache.batik.svggen.font.table;
import java.io.IOException;
import java.io.RandomAccessFile;
public class NameRecord {
    private short platformId;
    private short encodingId;
    private short languageId;
    private short nameId;
    private short stringLength;
    private short stringOffset;
    private String record;
    protected NameRecord(RandomAccessFile raf) throws IOException {
        platformId = raf.readShort();
        encodingId = raf.readShort();
        languageId = raf.readShort();
        nameId = raf.readShort();
        stringLength = raf.readShort();
        stringOffset = raf.readShort();
    }
    public short getEncodingId() {
        return encodingId;
    }
    public short getLanguageId() {
        return languageId;
    }
    public short getNameId() {
        return nameId;
    }
    public short getPlatformId() {
        return platformId;
    }
    public String getRecordString() {
        return record;
    }
    protected void loadString(RandomAccessFile raf, int stringStorageOffset) throws IOException {
        StringBuffer sb = new StringBuffer();
        raf.seek(stringStorageOffset + stringOffset);
        if (platformId == Table.platformAppleUnicode) {
            for (int i = 0; i < stringLength/2; i++) {
                sb.append(raf.readChar());
            }
        } else if (platformId == Table.platformMacintosh) {
            for (int i = 0; i < stringLength; i++) {
                sb.append((char) raf.readByte());
            }
        } else if (platformId == Table.platformISO) {
            for (int i = 0; i < stringLength; i++) {
                sb.append((char) raf.readByte());
            }
        } else if (platformId == Table.platformMicrosoft) {
            char c;
            for (int i = 0; i < stringLength/2; i++) {
                c = raf.readChar();
                sb.append(c);
            }
        }
        record = sb.toString();
    }
}
