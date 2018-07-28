package org.apache.batik.svggen.font.table;
import java.io.IOException;
import java.io.RandomAccessFile;
public class GposTable implements Table {
    protected GposTable(DirectoryEntry de,RandomAccessFile raf) throws IOException {
        raf.seek(de.getOffset());
         raf.readInt();
         raf.readInt();
         raf.readInt();
         raf.readInt();
    }
    public int getType() {
        return GPOS;
    }
    public String toString() {
        return "GPOS";
    }
}
