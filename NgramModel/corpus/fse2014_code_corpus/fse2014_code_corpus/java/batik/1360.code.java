package org.apache.batik.transcoder.wmf.tosvg;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import org.apache.batik.transcoder.wmf.WMFConstants;
public class RecordStore {
    public RecordStore(){
        reset();
    }
    public void reset(){
        numRecords = 0;
        vpX = 0;
        vpY = 0;
        vpW = 1000;
        vpH = 1000;
        numObjects = 0;
        records = new Vector( 20, 20 );
        objectVector = new Vector();
    }
    synchronized void setReading( boolean state ){
        bReading = state;
    }
    synchronized boolean isReading(){
        return bReading;
    }
    public boolean read( DataInputStream is ) throws IOException{
        setReading( true );
        reset();
        int functionId = 0;
        numRecords = 0;
        numObjects = is.readShort();
        objectVector.ensureCapacity( numObjects );
        for ( int i = 0; i < numObjects; i++ ) {
            objectVector.add( new GdiObject( i, false ));
        }
        while ( functionId != -1 ) {
            functionId = is.readShort();
            if ( functionId == -1 ){
                break;
            }
            MetaRecord mr;
            switch ( functionId ) {
            case WMFConstants.META_TEXTOUT:
            case WMFConstants.META_DRAWTEXT:
            case WMFConstants.META_EXTTEXTOUT:
            case WMFConstants.META_CREATEFONTINDIRECT:{
                short len = is.readShort();
                byte[] b = new byte[ len ];
                for ( int i = 0; i < len; i++ ) {
                    b[ i ] = is.readByte();
                }
                String str = new String( b );
                mr = new MetaRecord.StringRecord( str );
            }
            break;
            default:
                mr = new MetaRecord();
                break;
            }
            int numPts = is.readShort();
            mr.numPoints = numPts;
            mr.functionId = functionId;
            for ( int j = 0; j < numPts; j++ ){
                mr.AddElement( new Integer( is.readShort()));
            }
            records.add( mr );
            numRecords++;
        }
        setReading( false );
        return true;
    }
    public void addObject( int type, Object obj ) {
        for ( int i = 0; i < numObjects; i++ ) {
            GdiObject gdi = (GdiObject)objectVector.get( i );
            if ( ! gdi.used ) {
                gdi.Setup( type, obj );
                lastObjectIdx = i;
                break;
            }
        }
    }
    public void addObjectAt( int type, Object obj, int idx ) {
        if (( idx == 0 ) || ( idx > numObjects )) {
            addObject( type, obj );
            return;
        }
        lastObjectIdx = idx;
        for ( int i = 0; i < numObjects; i++ ) {
            GdiObject gdi = (GdiObject)objectVector.get( i );
            if ( i == idx ) {
                gdi.Setup( type, obj );
                break;
            }
        }
    }
    public URL getUrl() {
        return url;
    }
    public void setUrl( URL newUrl) {
        url = newUrl;
    }
    public GdiObject getObject( int idx ) {
        return (GdiObject)objectVector.get( idx );
    }
    public MetaRecord getRecord( int idx ) {
        return (MetaRecord)records.get( idx );
    }
    public int getNumRecords() {
        return numRecords;
    }
    public int getNumObjects() {
        return numObjects;
    }
    public int getVpX() {
        return vpX;
    }
    public int getVpY() {
        return vpY;
    }
    public int getVpW() {
        return vpW;
    }
    public int getVpH() {
        return vpH;
    }
    public void setVpX( int newValue ) {
        vpX = newValue;
    }
    public void setVpY( int newValue ) {
        vpY = newValue;
    }
    public void setVpW( int newValue ) {
        vpW = newValue;
    }
    public void setVpH( int newValue ) {
        vpH = newValue;
    }
    private transient URL url;
    protected transient int numRecords;
    protected transient int numObjects;
    public transient int lastObjectIdx;
    protected transient int vpX;
    protected transient int vpY;
    protected transient int vpW;
    protected transient int vpH;
    protected transient Vector records;
    protected transient Vector objectVector;
    protected transient boolean bReading = false;
}
