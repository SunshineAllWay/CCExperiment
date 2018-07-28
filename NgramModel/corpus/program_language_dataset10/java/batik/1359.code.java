package org.apache.batik.transcoder.wmf.tosvg;
import java.util.List;
import java.util.ArrayList;
public class MetaRecord  {
    public int functionId;
    public int numPoints;
    private final List ptVector = new ArrayList();
    public MetaRecord() {
    }
    public void EnsureCapacity( int cc ) {
    }
    public void AddElement( Object obj ) {
        ptVector.add( obj );
    }
    public final void addElement( int iValue ){
        ptVector.add( new Integer( iValue ) );
    }
    public Integer ElementAt( int offset ) {
        return (Integer)ptVector.get( offset );
    }
    public final int elementAt( int offset ){
        return ((Integer)ptVector.get( offset )).intValue();
    }
    public static class ByteRecord extends MetaRecord {
        public final byte[] bstr;
        public ByteRecord(byte[] bstr) {
            this.bstr = bstr;
        }
    }
    public static class StringRecord extends MetaRecord  {
        public final String text;
        public StringRecord( String newText ) {
            text = newText;
        }
    }
}
