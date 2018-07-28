package org.apache.batik.ext.awt.image.codec.tiff;
import  org.apache.batik.ext.awt.image.codec.util.ImageDecodeParam;
public class TIFFDecodeParam implements ImageDecodeParam {
    private boolean decodePaletteAsShorts = false;
    private Long ifdOffset = null;
    private boolean convertJPEGYCbCrToRGB = true;
    public TIFFDecodeParam() {
    }
    public void setDecodePaletteAsShorts(boolean decodePaletteAsShorts) {
        this.decodePaletteAsShorts = decodePaletteAsShorts;
    }
    public boolean getDecodePaletteAsShorts() {
        return decodePaletteAsShorts;
    }
    public byte decode16BitsTo8Bits(int s) {
        return (byte)((s >> 8) & 0xffff);
    }
    public byte decodeSigned16BitsTo8Bits(short s) {
        return (byte)((s + Short.MIN_VALUE) >> 8);
    }
    public void setIFDOffset(long offset) {
        ifdOffset = new Long(offset);
    }
    public Long getIFDOffset() {
        return ifdOffset;
    }
    public void setJPEGDecompressYCbCrToRGB(boolean convertJPEGYCbCrToRGB) {
        this.convertJPEGYCbCrToRGB = convertJPEGYCbCrToRGB;
    }
    public boolean getJPEGDecompressYCbCrToRGB() {
        return convertJPEGYCbCrToRGB;
    }
}
