package org.apache.batik.svggen.font.table;
import java.io.IOException;
import java.io.RandomAccessFile;
public class KernSubtableFormat0 extends KernSubtable {
    private int nPairs;
    private int searchRange;
    private int entrySelector;
    private int rangeShift;
    private KerningPair[] kerningPairs;
    protected KernSubtableFormat0(RandomAccessFile raf) throws IOException {
        nPairs = raf.readUnsignedShort();
        searchRange = raf.readUnsignedShort();
        entrySelector = raf.readUnsignedShort();
        rangeShift = raf.readUnsignedShort();
        kerningPairs = new KerningPair[nPairs];
        for (int i = 0; i < nPairs; i++) {
            kerningPairs[i] = new KerningPair(raf);
        }
    }
    public int getKerningPairCount() {
        return nPairs;
    }
    public KerningPair getKerningPair(int i) {
        return kerningPairs[i];
    }
}
