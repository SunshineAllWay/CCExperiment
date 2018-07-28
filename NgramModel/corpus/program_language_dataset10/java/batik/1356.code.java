package org.apache.batik.transcoder.wmf.tosvg;
import java.io.BufferedInputStream;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.text.AttributedCharacterIterator;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
public class AbstractWMFPainter {
    public static final String WMF_FILE_EXTENSION = ".wmf";
    protected WMFFont wmfFont = null;
    protected int currentHorizAlign = 0;
    protected int currentVertAlign = 0;
    public static final int PEN = 1;
    public static final int BRUSH = 2;
    public static final int FONT = 3;
    public static final int NULL_PEN = 4;
    public static final int NULL_BRUSH = 5;
    public static final int PALETTE = 6;
    public static final int OBJ_BITMAP = 7;
    public static final int OBJ_REGION = 8;
    protected WMFRecordStore currentStore;
    protected transient boolean bReadingWMF = true;
    protected transient BufferedInputStream bufStream = null;
    protected BufferedImage getImage(byte[] bit, int width, int height) {
        int _width = (((int)bit[7] & 0x00ff) << 24) | (((int)bit[6] & 0x00ff) << 16)
                    | (((int)bit[5] & 0x00ff) << 8) | (int)bit[4] & 0x00ff;
        int _height = (((int)bit[11] & 0x00ff) << 24) | (((int)bit[10] & 0x00ff) << 16)
                    | (((int)bit[9] & 0x00ff) <<8) | (int)bit[8] & 0x00ff;
        if ((width != _width) || (height != _height)) return null;
        return getImage(bit);
    }
    protected Dimension getImageDimension(byte[] bit) {
        int _width = (((int)bit[7] & 0x00ff) << 24) | (((int)bit[6] & 0x00ff) << 16)
                    | (((int)bit[5] & 0x00ff) << 8) | (int)bit[4] & 0x00ff;
        int _height = (((int)bit[11] & 0x00ff) << 24) | (((int)bit[10] & 0x00ff) << 16)
                    | (((int)bit[9] & 0x00ff) << 8) | (int)bit[8] & 0x00ff;
        return new Dimension(_width, _height);
    }
    protected BufferedImage getImage(byte[] bit) {
        int _width = (((int)bit[7] & 0x00ff) << 24) | (((int)bit[6] & 0x00ff) << 16)
                    | (((int)bit[5] & 0x00ff) << 8) | (int)bit[4] & 0x00ff;
        int _height = (((int)bit[11] & 0x00ff) << 24) | (((int)bit[10] & 0x00ff) << 16)
                    | (((int)bit[9] & 0x00ff) << 8) | (int)bit[8] & 0x00ff;
        int[] bitI = new int[_width * _height];
        BufferedImage img = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = img.getRaster();
        int _headerSize = (((int)bit[3] & 0x00ff) << 24) | (((int)bit[2] & 0x00ff)<<16)
                            | (((int)bit[1] & 0x00ff) << 8) | (int)bit[0] & 0x00ff;
        int _planes = (((int)bit[13] & 0x00ff) << 8) | (int)bit[12] & 0x00ff;
        int _nbit = (((int)bit[15] & 0x00ff) << 8) | (int)bit[14] & 0x00ff;
        int _size = (((int)bit[23] & 0x00ff) << 24) | (((int)bit[22] & 0x00ff) << 16)
                        | (((int)bit[21] & 0x00ff) << 8) | (int)bit[20] & 0x00ff;
        if (_size == 0) _size = ((((_width * _nbit) + 31) & ~31 ) >> 3) * _height;
        int _clrused = (((int)bit[35] & 0x00ff) << 24) | (((int)bit[34]&0x00ff) << 16)
                        | (((int)bit[33] & 0x00ff) << 8) | (int)bit[32]&0x00ff;
        if (_nbit == 24) {
            int pad = (_size / _height) - _width * 3;
            int offset = _headerSize; 
            for (int j = 0; j < _height; j++) {
                for (int i = 0; i < _width; i++) {
                    bitI[_width * (_height - j - 1) + i] =
                        (255 & 0x00ff) << 24 | (((int)bit[offset+2] & 0x00ff) << 16)
                        | (((int)bit[offset+1] & 0x00ff) << 8) | (int)bit[offset] & 0x00ff;
                    offset += 3;
                }
                offset += pad;
            }
        } else if (_nbit == 8) {
            int nbColors = 0;
            if (_clrused > 0) nbColors = _clrused;
            else nbColors = (1 & 0x00ff) << 8;
            int offset = _headerSize;
            int[]  palette = new int[nbColors];
            for (int i = 0; i < nbColors; i++) {
                palette[i] = (255 & 0x00ff) << 24 | (((int)bit[offset+2] & 0x00ff) << 16)
                            | (((int)bit[offset+1] & 0x00ff) << 8)
                            | (int)bit[offset] & 0x00ff;
                offset += 4;
            }
            _size = bit.length - offset;
            int pad = (_size / _height) - _width;            
            for (int j = 0; j < _height; j++) {
                for (int i = 0; i < _width; i++) {
                    bitI[_width*(_height-j-1)+i] = palette [((int)bit[offset] & 0x00ff)];
                    offset++;
                }
                offset += pad;
            }
        } else if (_nbit == 1) {
            int nbColors = 2;
            int offset = _headerSize;
            int[]  palette = new int[nbColors];
            for (int i = 0; i < nbColors; i++) {
                palette[i] = (255 & 0x00ff) << 24 | (((int)bit[offset+2] & 0x00ff) << 16)
                            | (((int)bit[offset+1] & 0x00ff) << 8)
                            | (int)bit[offset] & 0x00ff;
                offset += 4;
            }
            int pos = 7;
            byte currentByte = bit[offset];
            int pad = (_size / _height) - _width/8;
            for (int j = 0; j < _height; j++) {
                for (int i = 0; i < _width; i++) {
                    if ((currentByte & (1 << pos)) != 0) bitI[_width*(_height-j-1)+i] = palette[1];
                    else bitI[_width*(_height-j-1)+i] = palette[0];
                    pos--;
                    if (pos == -1) {
                        pos = 7;
                        offset++;
                        currentByte = bit[offset];
                    }
                }
                offset +=pad;
                pos = 7;
                if (offset < bit.length) currentByte = bit[offset];
            }
        }
        raster.setDataElements(0, 0, _width, _height, bitI);
        return img;
    }
    protected AttributedCharacterIterator getCharacterIterator(Graphics2D g2d, String sr, WMFFont wmffont) {
        return getAttributedString(g2d, sr, wmffont).getIterator();
    }
    protected AttributedCharacterIterator getCharacterIterator(Graphics2D g2d, String sr,
        WMFFont wmffont, int align) {
        AttributedString ats = getAttributedString(g2d, sr, wmffont);
        return ats.getIterator();
    }
    protected AttributedString getAttributedString(Graphics2D g2d, String sr, WMFFont wmffont) {
        AttributedString ats = new AttributedString(sr);
        Font font = g2d.getFont();
        ats.addAttribute(TextAttribute.SIZE, new Float(font.getSize2D()));
        ats.addAttribute(TextAttribute.FONT, font);
        if (wmfFont.underline != 0)
            ats.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        if (wmfFont.italic != 0)
            ats.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        else ats.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_REGULAR);
        if (wmfFont.weight > 400)
            ats.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        else ats.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);
        return ats;
    }
    public void setRecordStore(WMFRecordStore currentStore){
        if (currentStore == null){
            throw new IllegalArgumentException();
        }
        this.currentStore = currentStore;
    }
    public WMFRecordStore getRecordStore(){
        return currentStore;
    }
    protected int addObject( WMFRecordStore store, int type, Object obj ) {
        return currentStore.addObject( type, obj );
    }
    protected int addObjectAt( WMFRecordStore store, int type, Object obj, int idx ) {
        return currentStore.addObjectAt( type, obj, idx );
    }
}
