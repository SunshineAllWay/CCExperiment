package org.apache.batik.transcoder.wmf.tosvg;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import org.apache.batik.transcoder.wmf.WMFConstants;
import org.apache.batik.util.Platform;
public abstract class AbstractWMFReader {
    public static final float PIXEL_PER_INCH = Platform.getScreenResolution();
    public static final float MM_PER_PIXEL = 25.4f / Platform.getScreenResolution();
    protected int left, right, top, bottom, width, height, inch;
    protected float scaleX, scaleY, scaleXY;
    protected int vpW, vpH, vpX, vpY;
    protected int xSign = 1;
    protected int ySign = 1;
    protected volatile boolean bReading = false;
    protected boolean isAldus = false;
    protected boolean isotropic = true;
    protected int mtType, mtHeaderSize, mtVersion, mtSize, mtNoObjects;
    protected int mtMaxRecord, mtNoParameters;
    protected int windowWidth, windowHeight;
    protected int numObjects;
    protected List objectVector;
    public int lastObjectIdx;
    public AbstractWMFReader() {
        scaleX = 1;
        scaleY = 1;
        scaleXY = 1f;
        left = -1;
        top = -1;
        width = -1;
        height = -1;
        right = left + width;
        bottom = top + height;
        numObjects = 0;
        objectVector = new ArrayList();
    }
    public AbstractWMFReader(int width, int height) {
        this();
        this.width = width;
        this.height = height;
    }
    protected short readShort(DataInputStream is) throws IOException {
        byte js[] = new byte[ 2 ];
        is.readFully(js);
        int iTemp = ((0xff) & js[ 1 ] ) << 8;
        short i = (short)(0xffff & iTemp);
        i |= ((0xff) & js[ 0 ] );
        return i;
    }
    protected int readInt( DataInputStream is) throws IOException {
        byte js[] = new byte[ 4 ];
        is.readFully(js);
        int i = ((0xff) & js[ 3 ] ) << 24;
        i |= ((0xff) & js[ 2 ] ) << 16;
        i |= ((0xff) & js[ 1 ] ) << 8;
        i |= ((0xff) & js[ 0 ] );
        return i;
    }
    public float getViewportWidthUnits() {
      return vpW;
    }
    public float getViewportHeightUnits() {
        return vpH;
    }
    public float getViewportWidthInch() {
      return (float)vpW / (float)inch;
    }
    public float getViewportHeightInch() {
      return (float)vpH / (float)inch;
    }
    public float getPixelsPerUnit() {
        return PIXEL_PER_INCH / (float)inch;
    }
    public int getVpW() {
      return (int)(PIXEL_PER_INCH * (float)vpW / (float)inch);
    }
    public int getVpH() {
      return (int)(PIXEL_PER_INCH * (float)vpH / (float)inch);
    }
    public int getLeftUnits() {
        return left;
    }
    public int getRightUnits() {
        return right;
    }
    public int getTopUnits() {
        return top;
    }
    public int getWidthUnits() {
        return width;
    }
    public int getHeightUnits() {
        return height;
    }
    public int getBottomUnits() {
        return bottom;
    }
    public int getMetaFileUnitsPerInch() {
        return inch;
    }
    public Rectangle getRectangleUnits() {
        Rectangle rec = new Rectangle(left, top, width, height);
        return rec;
    }
    public Rectangle2D getRectanglePixel() {
        float _left = PIXEL_PER_INCH * (float)left / (float)inch;
        float _right = PIXEL_PER_INCH * (float)right / (float)inch;
        float _top = PIXEL_PER_INCH * (float)top / (float)inch;
        float _bottom = PIXEL_PER_INCH * (float)bottom / (float)inch;
        Rectangle2D.Float rec = new Rectangle2D.Float(_left, _top, _right - _left, _bottom - _top);
        return rec;
    }
    public Rectangle2D getRectangleInch() {
        float _left = (float)left / (float)inch;
        float _right = (float)right / (float)inch;
        float _top = (float)top / (float)inch;
        float _bottom = (float)bottom / (float)inch;
        Rectangle2D.Float rec = new Rectangle2D.Float(_left, _top, _right - _left, _bottom - _top);
        return rec;
    }
    public int getWidthPixels() {
        return (int)(PIXEL_PER_INCH * (float)width / (float)inch);
    }
    public float getUnitsToPixels() {
        return (PIXEL_PER_INCH / (float)inch);
    }
    public float getVpWFactor() {
        return (PIXEL_PER_INCH * (float)width / (float)inch) / (float)vpW;
    }
    public float getVpHFactor() {
        return (PIXEL_PER_INCH * (float)height / (float)inch) / (float)vpH;
    }
    public int getHeightPixels() {
        return (int)(PIXEL_PER_INCH * (float)height / (float)inch);
    }
    public int getXSign() {
        return xSign;
    }
    public int getYSign() {
        return ySign;
    }
    protected synchronized void setReading( boolean state ){
      bReading = state;
    }
    public synchronized boolean isReading(){
      return bReading;
    }
    public abstract void reset();
    protected abstract boolean readRecords(DataInputStream is) throws IOException;
    public void read(DataInputStream is) throws IOException {
        reset();
        setReading( true );
        int dwIsAldus = readInt( is );
        if ( dwIsAldus == WMFConstants.META_ALDUS_APM ) {
            int   key = dwIsAldus;
            isAldus = true;
            readShort( is ); 
            left = readShort( is );
            top = readShort( is );
            right = readShort( is );
            bottom = readShort( is );
            inch = readShort( is );
            int   reserved = readInt( is );
            short checksum = readShort( is );
            if (left > right) {
                int _i = right;
                right = left;
                left = _i;
                xSign = -1;
            }
            if (top > bottom) {
                int _i = bottom;
                bottom = top;
                top = _i;
                ySign = -1;
            }
            width = right - left;
            height = bottom - top;
            mtType = readShort( is );
            mtHeaderSize = readShort( is );
        } else {
            mtType = ((dwIsAldus << 16) >> 16);
            mtHeaderSize = dwIsAldus >> 16;
        }
        mtVersion = readShort( is );
        mtSize = readInt( is );
        mtNoObjects = readShort( is );
        mtMaxRecord = readInt( is );
        mtNoParameters = readShort( is );
        numObjects = mtNoObjects;
        List tempList = new ArrayList( numObjects );
        for ( int i = 0; i < numObjects; i++ ) {
            tempList.add( new GdiObject( i, false ));
        }
        objectVector.addAll( tempList );
        boolean ret = readRecords(is);
        is.close();
        if (!ret) throw new IOException("Unhandled exception while reading records");
    }
    public int addObject( int type, Object obj ){
        int startIdx = 0;
        for ( int i = startIdx; i < numObjects; i++ ) {
            GdiObject gdi = (GdiObject)objectVector.get( i );
            if ( ! gdi.used ) {
                gdi.Setup( type, obj );
                lastObjectIdx = i;
                break;
            }
        }
        return lastObjectIdx;
    }
    public int addObjectAt( int type, Object obj, int idx ) {
      if (( idx == 0 ) || ( idx > numObjects )) {
        addObject( type, obj );
        return lastObjectIdx;
      }
      lastObjectIdx = idx;
      for ( int i = 0; i < numObjects; i++ ) {
        GdiObject gdi = (GdiObject)objectVector.get( i );
        if ( i == idx ) {
          gdi.Setup( type, obj );
          break;
        }
      }
      return idx;
    }
    public GdiObject getObject( int idx ) {
        return (GdiObject)objectVector.get( idx );
    }
    public int getNumObjects() {
      return numObjects;
    }
}
