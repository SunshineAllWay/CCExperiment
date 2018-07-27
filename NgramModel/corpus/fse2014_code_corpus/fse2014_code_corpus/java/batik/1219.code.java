package org.apache.batik.svggen.font.table;
import java.io.IOException;
import java.io.RandomAccessFile;
public class ClassDefFormat1 extends ClassDef {
    private int startGlyph;
    private int glyphCount;
    private int[] classValues;
    public ClassDefFormat1(RandomAccessFile raf) throws IOException {
        startGlyph = raf.readUnsignedShort();
        glyphCount = raf.readUnsignedShort();
        classValues = new int[glyphCount];
        for (int i = 0; i < glyphCount; i++) {
            classValues[i] = raf.readUnsignedShort();
        }
    }
    public int getFormat() {
        return 1;
    }
}
