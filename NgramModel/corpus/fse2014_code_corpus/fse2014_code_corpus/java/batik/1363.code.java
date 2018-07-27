package org.apache.batik.transcoder.wmf.tosvg;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.batik.ext.awt.geom.Polygon2D;
import org.apache.batik.ext.awt.geom.Polyline2D;
import org.apache.batik.transcoder.wmf.WMFConstants;
public class WMFHeaderProperties extends AbstractWMFReader {
    private static final Integer INTEGER_0 = new Integer( 0 );
    protected DataInputStream stream;
    private int _bleft, _bright, _btop, _bbottom, _bwidth, _bheight;
    private int _ileft, _iright, _itop, _ibottom;
    private float scale = 1.0f;
    private int startX = 0;
    private int startY = 0;
    private int currentHorizAlign = 0;
    private int currentVertAlign = 0;
    private WMFFont wf = null;
    private static final FontRenderContext fontCtx =
            new FontRenderContext(new AffineTransform(), false, true);
    private transient boolean firstEffectivePaint = true;
    public static final int PEN = 1;
    public static final int BRUSH = 2;
    public static final int FONT = 3;
    public static final int NULL_PEN = 4;
    public static final int NULL_BRUSH = 5;
    public static final int PALETTE = 6;
    public static final int OBJ_BITMAP = 7;
    public static final int OBJ_REGION = 8;
    public WMFHeaderProperties(File wmffile) throws IOException {
        super();
        reset();
        stream = new DataInputStream(new BufferedInputStream(new FileInputStream(wmffile)));
        read(stream);
        stream.close();
    }
    public WMFHeaderProperties() {
        super();
    }
    public void closeResource() {
        try {
            if (stream != null) stream.close();
            } catch (IOException e) {
        }
    }
    public void setFile(File wmffile) throws IOException {
        stream = new DataInputStream(new BufferedInputStream(new FileInputStream(wmffile)));
        read(stream);
        stream.close();
    }
    public void reset() {
        left = 0;
        right = 0;
        top = 1000;
        bottom = 1000;
        inch = 84;
        _bleft = -1;
        _bright = -1;
        _btop = -1;
        _bbottom = -1;
        _ileft = -1;
        _iright = -1;
        _itop = -1;
        _ibottom = -1;
        _bwidth = -1;
        _bheight= -1;
        vpW = -1;
        vpH = -1;
        vpX = 0;
        vpY = 0;
        startX = 0;
        startY = 0;
        scaleXY = 1f;        
        firstEffectivePaint = true;
    }
    public DataInputStream getStream() {
        return stream;
    }
    protected boolean readRecords(DataInputStream is) throws IOException {
        short functionId = 1;
        int recSize = 0;
        int gdiIndex; 
        int brushObject = -1; 
        int penObject = -1; 
        int fontObject = -1; 
        GdiObject gdiObj;
        while (functionId > 0) {
            recSize = readInt( is );
            recSize -= 3;
            functionId = readShort( is );
            if ( functionId <= 0 )
            break;
            switch ( functionId ) {
            case WMFConstants.META_SETMAPMODE: {
                int mapmode = readShort( is );  
                if (mapmode == WMFConstants.MM_ANISOTROPIC) isotropic = false;
            }
                break;                                                
            case WMFConstants.META_SETWINDOWORG: {
                vpY = readShort( is );
                vpX = readShort( is );
            }
                break;
            case WMFConstants.META_SETWINDOWEXT: {
                vpH = readShort( is );
                vpW = readShort( is );
                if (! isotropic) scaleXY = (float)vpW / (float)vpH;
                vpW = (int)(vpW * scaleXY);                
            }
            break;
            case WMFConstants.META_CREATEPENINDIRECT:
                {
                    int objIndex = 0;
                    int penStyle = readShort( is );
                    readInt( is ); 
                    int colorref = readInt( is );
                    int red = colorref & 0xff;
                    int green = ( colorref & 0xff00 ) >> 8;
                    int blue = ( colorref & 0xff0000 ) >> 16;
                    Color color = new Color( red, green, blue);
                    if (recSize == 6) readShort(is); 
                    if ( penStyle == WMFConstants.META_PS_NULL ) {
                        objIndex = addObjectAt( NULL_PEN, color, objIndex );
                    } else {
                        objIndex = addObjectAt( PEN, color, objIndex );
                    }
                }
                break;
            case WMFConstants.META_CREATEBRUSHINDIRECT:
                {
                    int objIndex = 0;
                    int brushStyle = readShort( is );
                    int colorref = readInt( is );
                    int red = colorref & 0xff;
                    int green = ( colorref & 0xff00 ) >> 8;
                    int blue = ( colorref & 0xff0000 ) >> 16;
                    Color color = new Color( red, green, blue);
                    readShort( is ); 
                    if ( brushStyle == WMFConstants.META_PS_NULL ) {
                        objIndex = addObjectAt( NULL_BRUSH, color, objIndex);
                    } else
                        objIndex = addObjectAt(BRUSH, color, objIndex );
                }
                break;
            case WMFConstants.META_SETTEXTALIGN:
                    int align = readShort( is );
                    if (recSize > 1) for (int i = 1; i < recSize; i++) readShort( is );
                    currentHorizAlign = WMFUtilities.getHorizontalAlignment(align);
                    currentVertAlign = WMFUtilities.getVerticalAlignment(align);
                    break;
            case WMFConstants.META_EXTTEXTOUT: {
                    int y = readShort( is );
                    int x = (int)(readShort( is ) * scaleXY);
                    int lenText = readShort( is );
                    int flag = readShort( is );
                    int read = 4; 
                    boolean clipped = false;
                    int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
                    int len;
                    if ((flag & WMFConstants.ETO_CLIPPED) != 0) {
                        x1 =  (int)(readShort( is ) * scaleXY);
                        y1 =  readShort( is );
                        x2 =  (int)(readShort( is ) * scaleXY);
                        y2 =  readShort( is );
                        read += 4;
                        clipped = true;
                    }
                    byte[] bstr = new byte[ lenText ];
                    int i = 0;
                    for ( ; i < lenText; i++ ) {
                        bstr[ i ] = is.readByte();
                    }
                    String sr = WMFUtilities.decodeString(wf, bstr);
                    read += (lenText + 1)/2;
                    if (lenText % 2 != 0) is.readByte();
                    if (read < recSize) for (int j = read; j < recSize; j++) readShort( is );
                    TextLayout layout = new TextLayout( sr, wf.font, fontCtx );
                    int lfWidth = (int)layout.getBounds().getWidth();
                    x = (int)layout.getBounds().getX();
                    int lfHeight =
                        (int)getVerticalAlignmentValue(layout, currentVertAlign);
                    resizeBounds(x, y);
                    resizeBounds(x+lfWidth, y+lfHeight);
                    firstEffectivePaint = false;
                }
                break;
            case WMFConstants.META_DRAWTEXT:
            case WMFConstants.META_TEXTOUT: {
                    int len = readShort( is );
                    int read = 1; 
                    byte[] bstr = new byte[ len ];
                    for ( int i = 0; i < len; i++ ) {
                        bstr[ i ] = is.readByte();
                    }
                    String sr = WMFUtilities.decodeString(wf, bstr);
                    if (len % 2 != 0) is.readByte();
                    read += (len + 1) / 2;
                    int y = readShort( is );
                    int x = (int)(readShort( is ) * scaleXY);
                    read += 2;
                    if (read < recSize) for (int j = read; j < recSize; j++) readShort( is );
                    TextLayout layout = new TextLayout( sr, wf.font, fontCtx );
                    int lfWidth = (int)layout.getBounds().getWidth();
                    x = (int)layout.getBounds().getX();
                    int lfHeight =
                        (int)getVerticalAlignmentValue(layout, currentVertAlign);
                    resizeBounds(x, y);
                    resizeBounds(x+lfWidth, y+lfHeight);
                }
                break;
            case WMFConstants.META_CREATEFONTINDIRECT: {
                    int lfHeight = readShort( is );
                    float size = (int)(scaleY * lfHeight);
                    int lfWidth = readShort( is );
                    int escape = (int)readShort( is );
                    int orient = (int)readShort( is );
                    int weight = (int)readShort( is );
                    int italic = (int)is.readByte();
                    int underline = (int)is.readByte();
                    int strikeOut = (int)is.readByte();
                    int charset = (int)(is.readByte() & 0x00ff);
                    int lfOutPrecision = is.readByte();
                    int lfClipPrecision = is.readByte();
                    int lfQuality = is.readByte();
                    int lfPitchAndFamily = is.readByte();
                    int style = italic > 0 ? Font.ITALIC : Font.PLAIN;
                    style |= (weight > 400) ? Font.BOLD : Font.PLAIN;
                    int len = (2*(recSize-9));
                    byte[] lfFaceName = new byte[ len ];
                    byte ch;
                    for ( int i = 0; i < len; i++ ) lfFaceName[ i ] = is.readByte();
                    String face = new String( lfFaceName );
                    int d = 0;
                    while   ((d < face.length()) &&
                    ((Character.isLetterOrDigit(face.charAt(d))) ||
                    (Character.isWhitespace(face.charAt(d))))) d++;
                    if (d > 0) face = face.substring(0,d);
                    else face = "System";
                    if ( size < 0 ) size = -size ;
                    int objIndex = 0;
                    Font f = new Font(face, style, (int)size);
                    f = f.deriveFont(size);
                    WMFFont wf = new WMFFont(f, charset, underline,
                        strikeOut, italic, weight, orient, escape);
                    objIndex = addObjectAt( FONT, wf , objIndex );
                }
                break;
            case WMFConstants.META_CREATEREGION: {
                int objIndex = 0;
                for ( int j = 0; j < recSize; j++ ) readShort(is); 
                objIndex = addObjectAt( PALETTE, INTEGER_0, 0 );
                }
                break;
            case WMFConstants.META_CREATEPALETTE: {
                int objIndex = 0;
                for ( int j = 0; j < recSize; j++ ) readShort(is); 
                objIndex = addObjectAt( OBJ_REGION, INTEGER_0, 0 );
                }
                break;
                case WMFConstants.META_SELECTOBJECT:
                    gdiIndex = readShort(is);
                    if (( gdiIndex & 0x80000000 ) != 0 ) 
                        break;
                    gdiObj = getObject( gdiIndex );
                    if ( !gdiObj.used )
                        break;
                    switch( gdiObj.type ) {
                    case PEN:
                        penObject = gdiIndex;
                        break;
                    case BRUSH:
                        brushObject = gdiIndex;
                        break;
                    case FONT: {
                        this.wf =  ((WMFFont)gdiObj.obj);
                        fontObject = gdiIndex;
                        }
                        break;
                    case NULL_PEN:
                        penObject = -1;
                        break;
                    case NULL_BRUSH:
                        brushObject = -1;
                        break;
                    }
                    break;
                case WMFConstants.META_DELETEOBJECT:
                    gdiIndex = readShort(is);
                    gdiObj = getObject( gdiIndex );
                    if ( gdiIndex == brushObject ) brushObject = -1;
                    else if ( gdiIndex == penObject ) penObject = -1;
                    else if ( gdiIndex == fontObject ) fontObject = -1;
                    gdiObj.clear();
                    break;
            case WMFConstants.META_LINETO: {
                    int y = readShort( is );
                    int x = (int)(readShort( is ) * scaleXY);
                    if (penObject >= 0) {
                        resizeBounds(startX, startY);
                        resizeBounds(x, y);
                        firstEffectivePaint = false;
                    }
                    startX = x;
                    startY = y;
                }
                break;
            case WMFConstants.META_MOVETO: {
                    startY = readShort( is );
                    startX = (int)(readShort( is ) * scaleXY);
                }
                break;
            case WMFConstants.META_POLYPOLYGON: {
                    int count = readShort( is );
                    int[] pts = new int[ count ];
                    int ptCount = 0;
                    for ( int i = 0; i < count; i++ ) {
                        pts[ i ] = readShort( is );
                        ptCount += pts[ i ];
                    }
                    int offset = count+1;
                    for ( int i = 0; i < count; i++ ) {
                        for ( int j = 0; j < pts[ i ]; j++ ) {
                            int x = (int)(readShort( is ) * scaleXY);
                            int y = readShort( is );
                            if ((brushObject >= 0) || (penObject >= 0)) resizeBounds(x, y);
                        }
                    }
                    firstEffectivePaint = false;
                }
                break;
            case WMFConstants.META_POLYGON: {
                    int count = readShort( is );
                    float[] _xpts = new float[ count+1 ];
                    float[] _ypts = new float[ count+1 ];
                    for ( int i = 0; i < count; i++ ) {
                        _xpts[i] = readShort( is ) * scaleXY;  
                        _ypts[i] = readShort( is );
                    }
                    _xpts[count] = _xpts[0];
                    _ypts[count] = _ypts[0];
                    Polygon2D pol = new Polygon2D(_xpts, _ypts, count);
                    paint(brushObject, penObject, pol);
                }
                break;
                case WMFConstants.META_POLYLINE:
                    {
                        int count = readShort( is );
                        float[] _xpts = new float[ count ];
                        float[] _ypts = new float[ count ];
                        for ( int i = 0; i < count; i++ ) {
                            _xpts[i] = readShort( is ) * scaleXY;  
                            _ypts[i] = readShort( is );
                        }
                        Polyline2D pol = new Polyline2D(_xpts, _ypts, count);
                        paintWithPen(penObject, pol);
                    }
                    break;
            case WMFConstants.META_ELLIPSE:
            case WMFConstants.META_INTERSECTCLIPRECT:
            case WMFConstants.META_RECTANGLE: {
                    int bot = readShort( is );
                    int right = (int)(readShort( is ) * scaleXY);
                    int top = readShort( is );
                    int left = (int)(readShort( is ) * scaleXY);
                    Rectangle2D.Float rec = new Rectangle2D.Float(left, top, right-left, bot-top);
                    paint(brushObject, penObject, rec);
                }
                break;
            case WMFConstants.META_ROUNDRECT: {
                    readShort( is );
                    readShort( is );
                    int bot = readShort( is );
                    int right = (int)(readShort( is ) * scaleXY);
                    int top = readShort( is );
                    int left = (int)(readShort( is ) * scaleXY);
                    Rectangle2D.Float rec = new Rectangle2D.Float(left, top, right-left, bot-top);
                    paint(brushObject, penObject, rec);
                }
                break;
            case WMFConstants.META_ARC:
            case WMFConstants.META_CHORD:
            case WMFConstants.META_PIE: {
                    readShort( is );
                    readShort( is );
                    readShort( is );
                    readShort( is );
                    int bot = readShort( is );
                    int right = (int)(readShort( is ) * scaleXY);
                    int top = readShort( is );
                    int left = (int)(readShort( is ) * scaleXY);
                    Rectangle2D.Float rec = new Rectangle2D.Float(left, top, right-left, bot-top);
                    paint(brushObject, penObject, rec);
                }
                break;
            case WMFConstants.META_PATBLT : {
                    readInt( is ); 
                    int height = readShort( is );
                    int width = (int)(readShort( is ) * scaleXY);
                    int left = (int)(readShort( is ) * scaleXY);
                    int top = readShort( is );
                    if (penObject >= 0) resizeBounds(left, top);
                    if (penObject >= 0) resizeBounds(left+width, top+height);
                }
                break;
            case WMFConstants.META_DIBSTRETCHBLT:
                {
                    is.readInt(); 
                    readShort( is ); 
                    readShort( is ); 
                    readShort( is ); 
                    readShort( is ); 
                    float heightDst = (float)readShort( is );
                    float widthDst = (float)readShort( is ) * scaleXY;       
                    float dy = (float)readShort( is ) * getVpWFactor() * (float)inch / PIXEL_PER_INCH;
                    float dx = (float)readShort( is ) * getVpWFactor() * (float)inch / PIXEL_PER_INCH * scaleXY; 
                    widthDst = widthDst * getVpWFactor() * (float)inch / PIXEL_PER_INCH;
                    heightDst = heightDst * getVpHFactor() * (float)inch / PIXEL_PER_INCH;
                    resizeImageBounds((int)dx, (int)dy);
                    resizeImageBounds((int)(dx + widthDst), (int)(dy + heightDst));
                    int len = 2*recSize - 20;
                    for (int i = 0; i < len; i++) is.readByte();
                }
                break;
            case WMFConstants.META_STRETCHDIB: {
                is.readInt(); 
                readShort( is ); 
                readShort( is ); 
                readShort( is ); 
                readShort( is ); 
                readShort( is ); 
                float heightDst = (float)readShort( is );
                float widthDst = (float)readShort( is ) * scaleXY;                    
                float dy = (float)readShort( is ) * getVpHFactor() * (float)inch / PIXEL_PER_INCH;                                         
                float dx = (float)readShort( is ) * getVpHFactor() * (float)inch / PIXEL_PER_INCH * scaleXY;  
                widthDst = widthDst * getVpWFactor() * (float)inch / PIXEL_PER_INCH;
                heightDst = heightDst * getVpHFactor() * (float)inch / PIXEL_PER_INCH;                                            
                resizeImageBounds((int)dx, (int)dy);
                resizeImageBounds((int)(dx + widthDst), (int)(dy + heightDst));                
                int len = 2*recSize - 22;
                byte bitmap[] = new byte[len];                    
                for (int i = 0; i < len; i++) bitmap[i] = is.readByte();
            }
            break;                                                
            case WMFConstants.META_DIBBITBLT: {
                is.readInt(); 
                readShort( is ); 
                readShort( is ); 
                readShort( is ); 
                float height = readShort( is ) 
                    * (float)inch / PIXEL_PER_INCH * getVpHFactor();
                float width = readShort( is ) 
                    * (float)inch / PIXEL_PER_INCH * getVpWFactor() * scaleXY;
                float dy = 
                    (float)inch / PIXEL_PER_INCH * getVpHFactor() * readShort( is );
                float dx = 
                    (float)inch / PIXEL_PER_INCH * getVpWFactor() * readShort( is ) * scaleXY;
                resizeImageBounds((int)dx, (int)dy);
                resizeImageBounds((int)(dx + width), (int)(dy + height));                        
                }
                break;                
            default:
                for ( int j = 0; j < recSize; j++ )
                    readShort(is);
                break;
            }
        }
        if (! isAldus) {
            width = vpW;
            height = vpH;
            right = vpX;
            left = vpX + vpW;
            top = vpY;
            bottom = vpY + vpH;
        }        
        resetBounds();
        return true;
    }
    public int getWidthBoundsPixels() {
        return _bwidth;
    }
    public int getHeightBoundsPixels() {
        return _bheight;
    }
    public int getWidthBoundsUnits() {
        return (int)((float)inch * (float)_bwidth / PIXEL_PER_INCH);
    }
    public int getHeightBoundsUnits() {
        return (int)((float)inch * (float)_bheight / PIXEL_PER_INCH);
    }
    public int getXOffset() {
        return _bleft;
    }
    public int getYOffset() {
        return _btop;
    }
    private void resetBounds() {
        scale =  (float)getWidthPixels() / (float)vpW ;
        if (_bright != -1) {
            _bright = (int)(scale * (vpX +_bright));
            _bleft = (int)(scale * (vpX +_bleft));
            _bbottom = (int)(scale * (vpY +_bbottom));
            _btop = (int)(scale * (vpY +_btop));
        }
        if (_iright != -1) {
            _iright = (int)((float)_iright * (float)getWidthPixels() / (float)width);
            _ileft = (int)((float)_ileft * (float)getWidthPixels() / (float)width);
            _ibottom = (int)((float)_ibottom * (float)getWidthPixels() / (float)width);
            _itop = (int)((float)_itop  * (float)getWidthPixels() / (float)width);
            if ((_bright == -1) || (_iright > _bright)) _bright = _iright;
            if ((_bleft == -1) || (_ileft < _bleft)) _bleft = _ileft;
            if ((_btop == -1) || (_itop < _btop)) _btop = _itop;
            if ((_bbottom == -1) || (_ibottom > _bbottom)) _bbottom = _ibottom;
        }
        if ((_bleft != -1) && (_bright != -1)) _bwidth = _bright - _bleft;
        if ((_btop != -1) && (_bbottom != -1)) _bheight = _bbottom - _btop;
    }
    private void resizeBounds(int x, int y) {
        if (_bleft == -1) _bleft = x;
        else if (x < _bleft) _bleft = x;
        if (_bright == -1) _bright = x;
        else if (x > _bright) _bright = x;
        if (_btop == -1) _btop = y;
        else if (y < _btop) _btop = y;
        if (_bbottom == -1) _bbottom = y;
        else if (y > _bbottom) _bbottom = y;
    }
    private void resizeImageBounds(int x, int y) {
        if (_ileft == -1) _ileft = x;
        else if (x < _ileft) _ileft = x;
        if (_iright == -1) _iright = x;
        else if (x > _iright) _iright = x;
        if (_itop == -1) _itop = y;
        else if (y < _itop) _itop = y;
        if (_ibottom == -1) _ibottom = y;
        else if (y > _ibottom) _ibottom = y;
    }
    private Color getColorFromObject(int brushObject) {
        Color color = null;
        if ( brushObject >= 0 ) {
            GdiObject gdiObj = getObject( brushObject );
            return  (Color)gdiObj.obj;
        } else return null;
    }
    private void paint(int brushObject, int penObject, Shape shape) {
        if (( brushObject >= 0 ) || (penObject >= 0)) {
            Color col;
            if (brushObject >= 0) col = getColorFromObject(brushObject);
            else col = getColorFromObject(penObject);
            if (!(firstEffectivePaint && (col.equals(Color.white)))) {
                Rectangle rec = shape.getBounds();
                resizeBounds((int)rec.getMinX(), (int)rec.getMinY());
                resizeBounds((int)rec.getMaxX(), (int)rec.getMaxY());
                firstEffectivePaint = false;
            }
        }
    }
    private void paintWithPen(int penObject, Shape shape) {
        if (penObject >= 0) {
            Color col = getColorFromObject(penObject);
            if (!(firstEffectivePaint && (col.equals(Color.white)))) {
                Rectangle rec = shape.getBounds();
                resizeBounds((int)rec.getMinX(), (int)rec.getMinY());
                resizeBounds((int)rec.getMaxX(), (int)rec.getMaxY());
                firstEffectivePaint = false;
            }
        }
    }
    private float getVerticalAlignmentValue(TextLayout layout, int vertAlign) {
        if (vertAlign == WMFConstants.TA_BASELINE) return -layout.getAscent();
        else if (vertAlign == WMFConstants.TA_TOP) return (layout.getAscent() + layout.getDescent());
        else return 0;
    }
}
