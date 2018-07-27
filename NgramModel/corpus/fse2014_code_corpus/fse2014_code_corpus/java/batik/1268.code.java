package org.apache.batik.svggen.font.table;
import java.io.IOException;
import java.io.RandomAccessFile;
public class NameTable implements Table {
    private short formatSelector;
    private short numberOfNameRecords;
    private short stringStorageOffset;
    private NameRecord[] records;
    protected NameTable(DirectoryEntry de,RandomAccessFile raf) throws IOException {
        raf.seek(de.getOffset());
        formatSelector = raf.readShort();
        numberOfNameRecords = raf.readShort();
        stringStorageOffset = raf.readShort();
        records = new NameRecord[numberOfNameRecords];
        for (int i = 0; i < numberOfNameRecords; i++) {
            records[i] = new NameRecord(raf);
        }
        for (int i = 0; i < numberOfNameRecords; i++) {
            records[i].loadString(raf, de.getOffset() + stringStorageOffset);
        }
    }
    public String getRecord(short nameId) {
        for (int i = 0; i < numberOfNameRecords; i++) {
            if (records[i].getNameId() == nameId) {
                return records[i].getRecordString();
            }
        }
        return "";
    }
    public int getType() {
        return name;
    }
}
