package org.apache.batik.svggen.font.table;
import java.io.IOException;
import java.io.RandomAccessFile;
public class CmapFormat4 extends CmapFormat {
    public  int language;
    private int segCountX2;
    private int searchRange;
    private int entrySelector;
    private int rangeShift;
    private int[] endCode;
    private int[] startCode;
    private int[] idDelta;
    private int[] idRangeOffset;
    private int[] glyphIdArray;
    private int segCount;
    private int first, last;
    protected CmapFormat4(RandomAccessFile raf) throws IOException {
        super(raf);
        format = 4;
        segCountX2 = raf.readUnsignedShort();
        segCount = segCountX2 / 2;
        endCode = new int[segCount];
        startCode = new int[segCount];
        idDelta = new int[segCount];
        idRangeOffset = new int[segCount];
        searchRange = raf.readUnsignedShort();
        entrySelector = raf.readUnsignedShort();
        rangeShift = raf.readUnsignedShort();
        last = -1;
        for (int i = 0; i < segCount; i++) {
            endCode[i] = raf.readUnsignedShort();
            if (endCode[i] > last) last = endCode[i];
        }
        raf.readUnsignedShort(); 
        for (int i = 0; i < segCount; i++) {
            startCode[i] = raf.readUnsignedShort();
            if ((i==0 ) || (startCode[i] < first)) first = startCode[i];
        }
        for (int i = 0; i < segCount; i++) {
            idDelta[i] = raf.readUnsignedShort();
        }
        for (int i = 0; i < segCount; i++) {
            idRangeOffset[i] = raf.readUnsignedShort();
        }
        int count = (length - 16 - (segCount*8)) / 2;
        glyphIdArray = new int[count];
        for (int i = 0; i < count; i++) {
            glyphIdArray[i] = raf.readUnsignedShort();
        }
    }
    public int getFirst() { return first; }
    public int getLast()  { return last; }
    public int mapCharCode(int charCode) {
        try {
            if ((charCode < 0) || (charCode >= 0xFFFE))
                return 0;
            for (int i = 0; i < segCount; i++) {
                if (endCode[i] >= charCode) {
                    if (startCode[i] <= charCode) {
                        if (idRangeOffset[i] > 0) {
                            return glyphIdArray[idRangeOffset[i]/2 +
                                                (charCode - startCode[i]) -
                                                (segCount - i)];
                        } else {
                            return (idDelta[i] + charCode) % 65536;
                        }
                    } else {
                        break;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("error: Array out of bounds - " + e.getMessage());
        }
        return 0;
    }
    public String toString() {
        return new StringBuffer( 80 )
        .append(super.toString())
        .append(", segCountX2: ")
        .append(segCountX2)
        .append(", searchRange: ")
        .append(searchRange)
        .append(", entrySelector: ")
        .append(entrySelector)
        .append(", rangeShift: ")
        .append(rangeShift)
        .append(", endCode: ")
        .append( intToStr( endCode ))
        .append(", startCode: ")
        .append( intToStr( startCode ))
        .append(", idDelta: ")
        .append( intToStr( idDelta ))
        .append(", idRangeOffset: ")
        .append( intToStr( idRangeOffset ) ).toString();
    }
    private static String intToStr( int[] array ){
        int nSlots = array.length;
        StringBuffer workBuff = new StringBuffer( nSlots * 8 );
        workBuff.append( '[' );
        for( int i= 0; i < nSlots; i++ ){
            workBuff.append( array[ i ] );
            if ( i < nSlots-1 ) {
                workBuff.append( ',' );
            }
        }
        workBuff.append( ']');
        return workBuff.toString();
    }
}
