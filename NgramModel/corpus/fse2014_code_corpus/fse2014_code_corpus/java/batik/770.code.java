package org.apache.batik.ext.awt.image.codec.imageio;
public class ImageIOJPEGRegistryEntry 
    extends AbstractImageIORegistryEntry {
    static final byte [] sigJPEG   = {(byte)0xFF, (byte)0xd8, 
                                      (byte)0xFF};
    static final String [] exts      = {"jpeg", "jpg" };
    static final String [] mimeTypes = {"image/jpeg", "image/jpg" };
    static final MagicNumber [] magicNumbers = {
        new MagicNumber(0, sigJPEG)
    };
    public ImageIOJPEGRegistryEntry() {
        super("JPEG", exts, mimeTypes, magicNumbers);
    }
}
