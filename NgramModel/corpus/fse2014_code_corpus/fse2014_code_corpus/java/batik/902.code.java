package org.apache.batik.ext.awt.image.spi;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.renderable.DeferRable;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.RedRable;
import org.apache.batik.util.ParsedURL;
public class JDKRegistryEntry extends AbstractRegistryEntry
    implements URLRegistryEntry {
    public static final float PRIORITY =
        1000*MagicNumberRegistryEntry.PRIORITY;
    public JDKRegistryEntry() {
        super ("JDK", PRIORITY, new String[0], new String [] {"image/gif"});
    }
    public boolean isCompatibleURL(ParsedURL purl) {
        try {
            new URL(purl.toString());
        } catch (MalformedURLException mue) {
            return false;
        }
        return true;
    }
    public Filter handleURL(ParsedURL purl, boolean needRawData) {
        final URL url;
        try {
            url = new URL(purl.toString());
        } catch (MalformedURLException mue) {
            return null;
        }
        final DeferRable  dr  = new DeferRable();
        final String      errCode;
        final Object []   errParam;
        if (purl != null) {
            errCode  = ERR_URL_FORMAT_UNREADABLE;
            errParam = new Object[] {"JDK", url};
        } else {
            errCode  = ERR_STREAM_FORMAT_UNREADABLE;
            errParam = new Object[] {"JDK"};
        }
        Thread t = new Thread() {
                public void run() {
                    Filter filt = null;
                    try {
                        Toolkit tk = Toolkit.getDefaultToolkit();
                        Image img = tk.createImage(url);
                        if (img != null) {
                            RenderedImage ri = loadImage(img, dr);
                            if (ri != null) {
                                filt = new RedRable(GraphicsUtil.wrap(ri));
                            }
                        }
                    } catch (ThreadDeath td) {
                        filt = ImageTagRegistry.getBrokenLinkImage
                            (JDKRegistryEntry.this, errCode, errParam);
                        dr.setSource(filt);
                        throw td;
                    } catch (Throwable t) { }
                    if (filt == null)
                        filt = ImageTagRegistry.getBrokenLinkImage
                            (JDKRegistryEntry.this, errCode, errParam);
                    dr.setSource(filt);
                }
            };
        t.start();
        return dr;
    }
    public RenderedImage loadImage(Image img, final DeferRable  dr) {
        if (img instanceof RenderedImage)
            return (RenderedImage)img;
        MyImgObs observer = new MyImgObs();
        Toolkit.getDefaultToolkit().prepareImage(img, -1, -1, observer);
        observer.waitTilWidthHeightDone();
        if (observer.imageError)
            return null;
        int width  = observer.width;
        int height = observer.height;
        dr.setBounds(new Rectangle2D.Double(0, 0, width, height));
        BufferedImage bi = new BufferedImage
            (width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        observer.waitTilImageDone();
        if (observer.imageError)
            return null;
        dr.setProperties(new HashMap());
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();
        return bi;
    }
    public static class MyImgObs implements ImageObserver {
        boolean widthDone = false;
        boolean heightDone = false;
        boolean imageDone = false;
        int width = -1;
        int height = -1;
        boolean imageError = false;
        int IMG_BITS = ALLBITS|ERROR|ABORT;
        public void clear() {
            width=-1;
            height=-1;
            widthDone = false;
            heightDone = false;
            imageDone       = false;
        }
        public boolean imageUpdate(Image img, int infoflags,
                                   int x, int y, int width, int height) {
            synchronized (this) {
                boolean notify = false;
                if ((infoflags & WIDTH)   != 0) this.width  = width;
                if ((infoflags & HEIGHT)  != 0) this.height = height;
                if ((infoflags & ALLBITS) != 0) {
                    this.width  = width;
                    this.height = height;
                }
                if ((infoflags & IMG_BITS) != 0) {
                    if ((!widthDone) || (!heightDone) || (!imageDone)) {
                        widthDone  = true;
                        heightDone = true;
                        imageDone  = true;
                        notify     = true;
                    }
                    if ((infoflags & ERROR) != 0) {
                        imageError = true;
                    }
                }
                if ((!widthDone) && (this.width != -1)) {
                    notify = true;
                    widthDone = true;
                }
                if ((!heightDone) && (this.height != -1)) {
                    notify = true;
                    heightDone = true;
                }
                if (notify)
                    notifyAll();
            }
            return true;
        }
        public synchronized void waitTilWidthHeightDone() {
            while ((!widthDone) || (!heightDone)) {
                try {
                    wait();
                }
                catch(InterruptedException ie) {
                }
            }
        }
        public synchronized void waitTilWidthDone() {
            while (!widthDone) {
                try {
                    wait();
                }
                catch(InterruptedException ie) {
                }
            }
        }
        public synchronized void waitTilHeightDone() {
            while (!heightDone) {
                try {
                    wait();
                }
                catch(InterruptedException ie) {
                }
            }
        }
        public synchronized void waitTilImageDone() {
            while (!imageDone) {
                try {
                    wait();
                }
                catch(InterruptedException ie) {
                }
            }
        }
    }
}
