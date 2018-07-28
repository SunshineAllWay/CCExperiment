package org.apache.batik.ext.awt.image.spi;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.RedRable;
import org.apache.batik.i18n.LocalizableSupport;
public class DefaultBrokenLinkProvider
    extends BrokenLinkProvider {
    static Filter brokenLinkImg = null;
    static final String MESSAGE_RSRC = "resources.Messages";
    static final Color BROKEN_LINK_COLOR = new Color( 255,255,255,190 );
    public static String formatMessage(Object base,
                                       String code,
                                       Object [] params) {
        ClassLoader cl = null;
        try {
            cl = DefaultBrokenLinkProvider.class.getClassLoader();
            cl = base.getClass().getClassLoader();
        } catch (SecurityException se) {
        }
        LocalizableSupport ls;
        ls = new LocalizableSupport(MESSAGE_RSRC, base.getClass(), cl);
        return ls.formatMessage(code, params);
    }
    public Filter getBrokenLinkImage(Object base,
                                     String code, Object [] params) {
        synchronized (DefaultBrokenLinkProvider.class) {
            if (brokenLinkImg != null)
                return brokenLinkImg;
            BufferedImage bi;
            bi = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            Hashtable ht = new Hashtable();
            ht.put(BROKEN_LINK_PROPERTY,
                   formatMessage(base, code, params));
            bi = new BufferedImage(bi.getColorModel(), bi.getRaster(),
                                   bi.isAlphaPremultiplied(),
                                   ht);
            Graphics2D g2d = bi.createGraphics();
            g2d.setColor( BROKEN_LINK_COLOR );
            g2d.fillRect(0, 0, 100, 100);
            g2d.setColor(Color.black);
            g2d.drawRect(2, 2, 96, 96);
            g2d.drawString("Broken Image", 6, 50);
            g2d.dispose();
            brokenLinkImg = new RedRable(GraphicsUtil.wrap(bi));
            return brokenLinkImg;
        }
    }
}
