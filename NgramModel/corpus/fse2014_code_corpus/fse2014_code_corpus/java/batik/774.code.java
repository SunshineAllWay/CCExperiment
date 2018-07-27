package org.apache.batik.ext.awt.image.codec.imageio;
import org.apache.batik.ext.awt.image.spi.MagicNumberRegistryEntry;
public class ImageIOTIFFRegistryEntry 
    extends AbstractImageIORegistryEntry {
    static final byte [] sig1 = {(byte)0x49, (byte)0x49, 42,  0};
    static final byte [] sig2 = {(byte)0x4D, (byte)0x4D,  0, 42};
    static MagicNumberRegistryEntry.MagicNumber [] magicNumbers = {
        new MagicNumberRegistryEntry.MagicNumber(0, sig1),
        new MagicNumberRegistryEntry.MagicNumber(0, sig2) };
    static final String [] exts      = {"tiff", "tif" };
    static final String [] mimeTypes = {"image/tiff", "image/tif" };
    public ImageIOTIFFRegistryEntry() {
        super("TIFF", exts, mimeTypes, magicNumbers);
    }
}
