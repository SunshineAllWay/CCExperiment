package org.apache.batik.ext.awt.image.codec.tiff;
import java.io.IOException;
import java.io.InputStream;
import org.apache.batik.ext.awt.image.codec.util.SeekableStream;
import org.apache.batik.ext.awt.image.renderable.DeferRable;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.RedRable;
import org.apache.batik.ext.awt.image.rendered.Any2sRGBRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.apache.batik.ext.awt.image.spi.MagicNumberRegistryEntry;
import org.apache.batik.util.ParsedURL;
public class TIFFRegistryEntry
    extends MagicNumberRegistryEntry {
    static final byte [] sig1 = {(byte)0x49, (byte)0x49, 42,  0};
    static final byte [] sig2 = {(byte)0x4D, (byte)0x4D,  0, 42};
    static MagicNumberRegistryEntry.MagicNumber [] magicNumbers = {
        new MagicNumberRegistryEntry.MagicNumber(0, sig1),
        new MagicNumberRegistryEntry.MagicNumber(0, sig2) };
    static final String [] exts      = {"tiff", "tif" };
    static final String [] mimeTypes = {"image/tiff", "image/tif" };
    public TIFFRegistryEntry() {
        super("TIFF", exts, mimeTypes, magicNumbers);
    }
    public Filter handleStream(InputStream inIS,
                               ParsedURL   origURL,
                               boolean needRawData) {
        final DeferRable  dr  = new DeferRable();
        final InputStream is  = inIS;
        final String      errCode;
        final Object []   errParam;
        if (origURL != null) {
            errCode  = ERR_URL_FORMAT_UNREADABLE;
            errParam = new Object[] {"TIFF", origURL};
        } else {
            errCode  = ERR_STREAM_FORMAT_UNREADABLE;
            errParam = new Object[] {"TIFF"};
        }
        Thread t = new Thread() {
                public void run() {
                    Filter filt;
                    try {
                        TIFFDecodeParam param = new TIFFDecodeParam();
                        SeekableStream ss =
                            SeekableStream.wrapInputStream(is, true);
                        CachableRed cr = new TIFFImage(ss, param, 0);
                        cr = new Any2sRGBRed(cr);
                        filt = new RedRable(cr);
                    } catch (IOException ioe) {
                        filt = ImageTagRegistry.getBrokenLinkImage
                            (TIFFRegistryEntry.this, errCode, errParam);
                    } catch (ThreadDeath td) {
                        filt = ImageTagRegistry.getBrokenLinkImage
                            (TIFFRegistryEntry.this, errCode, errParam);
                        dr.setSource(filt);
                        throw td;
                    } catch (Throwable t) {
                        filt = ImageTagRegistry.getBrokenLinkImage
                            (TIFFRegistryEntry.this, errCode, errParam);
                    }
                    dr.setSource(filt);
                }
            };
        t.start();
        return dr;
    }
}
