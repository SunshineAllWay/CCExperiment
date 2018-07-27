package org.apache.batik.ext.awt.image.codec.tiff;
import java.util.Iterator;
import java.util.zip.Deflater;
import org.apache.batik.ext.awt.image.codec.util.ImageEncodeParam;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
public class TIFFEncodeParam implements ImageEncodeParam {
    public static final int COMPRESSION_NONE          = 1;
    public static final int COMPRESSION_GROUP3_1D     = 2;
    public static final int COMPRESSION_GROUP3_2D     = 3;
    public static final int COMPRESSION_GROUP4        = 4;
    public static final int COMPRESSION_LZW           = 5;
    public static final int COMPRESSION_JPEG_BROKEN   = 6;
    public static final int COMPRESSION_JPEG_TTN2     = 7;
    public static final int COMPRESSION_PACKBITS      = 32773;
    public static final int COMPRESSION_DEFLATE       = 32946;
    private int compression = COMPRESSION_NONE;
    private boolean writeTiled = false;
    private int tileWidth;
    private int tileHeight;
    private Iterator extraImages;
    private TIFFField[] extraFields;
    private boolean convertJPEGRGBToYCbCr = true;
    private JPEGEncodeParam jpegEncodeParam = null;
    private int deflateLevel = Deflater.DEFAULT_COMPRESSION;
    public TIFFEncodeParam() {}
    public int getCompression() {
        return compression;
    }
    public void setCompression(int compression) {
        switch(compression) {
        case COMPRESSION_NONE:
        case COMPRESSION_PACKBITS:
        case COMPRESSION_JPEG_TTN2:
        case COMPRESSION_DEFLATE:
            break;
        default:
            throw new Error("TIFFEncodeParam0");
        }
        this.compression = compression;
    }
    public boolean getWriteTiled() {
        return writeTiled;
    }
    public void setWriteTiled(boolean writeTiled) {
        this.writeTiled = writeTiled;
    }
    public void setTileSize(int tileWidth, int tileHeight) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
    }
    public int getTileWidth() {
        return tileWidth;
    }
    public int getTileHeight() {
        return tileHeight;
    }
    public synchronized void setExtraImages(Iterator extraImages) {
        this.extraImages = extraImages;
    }
    public synchronized Iterator getExtraImages() {
        return extraImages;
    }
    public void setDeflateLevel(int deflateLevel) {
        if(deflateLevel < 1 && deflateLevel > 9 &&
           deflateLevel != Deflater.DEFAULT_COMPRESSION) {
            throw new Error("TIFFEncodeParam1");
        }
        this.deflateLevel = deflateLevel;
    }
    public int getDeflateLevel() {
        return deflateLevel;
    }
    public void setJPEGCompressRGBToYCbCr(boolean convertJPEGRGBToYCbCr) {
        this.convertJPEGRGBToYCbCr = convertJPEGRGBToYCbCr;
    }
    public boolean getJPEGCompressRGBToYCbCr() {
        return convertJPEGRGBToYCbCr;
    }
    public void setJPEGEncodeParam(JPEGEncodeParam jpegEncodeParam) {
        if(jpegEncodeParam != null) {
            jpegEncodeParam = (JPEGEncodeParam)jpegEncodeParam.clone();
            jpegEncodeParam.setTableInfoValid(false);
            jpegEncodeParam.setImageInfoValid(true);
        }
        this.jpegEncodeParam = jpegEncodeParam;
    }
    public JPEGEncodeParam getJPEGEncodeParam() {
        return jpegEncodeParam;
    }
    public void setExtraFields(TIFFField[] extraFields) {
        this.extraFields = extraFields;
    }
    public TIFFField[] getExtraFields() {
        return extraFields;
    }
}
