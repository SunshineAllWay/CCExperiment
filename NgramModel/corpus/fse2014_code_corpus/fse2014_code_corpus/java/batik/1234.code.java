package org.apache.batik.svggen.font.table;
import java.io.IOException;
import java.io.RandomAccessFile;
public class Feature {
    private int featureParams;
    private int lookupCount;
    private int[] lookupListIndex;
    protected Feature(RandomAccessFile raf, int offset) throws IOException {
        raf.seek(offset);
        featureParams = raf.readUnsignedShort();
        lookupCount = raf.readUnsignedShort();
        lookupListIndex = new int[lookupCount];
        for (int i = 0; i < lookupCount; i++) {
            lookupListIndex[i] = raf.readUnsignedShort();
        }
    }
    public int getLookupCount() {
        return lookupCount;
    }
    public int getLookupListIndex(int i) {
        return lookupListIndex[i];
    }
}
