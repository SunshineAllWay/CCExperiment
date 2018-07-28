package org.apache.batik.transcoder.wmf.tosvg;
import java.awt.Paint;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.awt.geom.*;
import java.util.Map;
import java.util.HashMap;
import org.apache.batik.transcoder.wmf.WMFConstants;
public class TextureFactory {
    private static TextureFactory fac = null;
    private Map textures = new HashMap(1);
    private static final int SIZE = 10;
    private float scale = 1.0f;
    private TextureFactory(float scale) {
    }
    public static TextureFactory getInstance() {
        if (fac == null) fac = new TextureFactory(1.0f);
        return fac;
    }
    public static TextureFactory getInstance(float scale) {
        if (fac == null) fac = new TextureFactory(scale);
        return fac;
    }
    public void reset() {
        textures.clear();
    }
    public Paint getTexture(int textureId) {
        Integer _itexture = new Integer(textureId);
        if (textures.containsKey( _itexture)) {
            Paint paint = (Paint)(textures.get(_itexture));
            return paint;
        } else {
            Paint paint = createTexture(textureId, null, null);
            if (paint != null) textures.put(_itexture, paint);
            return paint;
        }
    }
    public Paint getTexture(int textureId, Color foreground) {
        ColoredTexture _ctexture = new ColoredTexture(textureId, foreground, null);
        if (textures.containsKey(_ctexture)) {
            Paint paint = (Paint)(textures.get(_ctexture));
            return paint;
        } else {
            Paint paint = createTexture(textureId, foreground, null);
            if (paint != null) textures.put(_ctexture, paint);
            return paint;
        }
    }
    public Paint getTexture(int textureId, Color foreground, Color background) {
        ColoredTexture _ctexture = new ColoredTexture(textureId, foreground, background);
        if (textures.containsKey(_ctexture)) {
            Paint paint = (Paint)(textures.get(_ctexture));
            return paint;
        } else {
            Paint paint = createTexture(textureId, foreground, background);
            if (paint != null) textures.put(_ctexture, paint);
            return paint;
        }
    }
    private Paint createTexture(int textureId, Color foreground, Color background) {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        Rectangle2D rec = new Rectangle2D.Float(0, 0, SIZE, SIZE);
        Paint paint = null;
        boolean ok = false;
        if (background != null) {
            g2d.setColor(background);
            g2d.fillRect(0, 0, SIZE, SIZE);
        }
        if (foreground == null) g2d.setColor(Color.black);
        else g2d.setColor(foreground);
        if (textureId == WMFConstants.HS_VERTICAL) {
            for (int i = 0; i < 5; i++) {
                g2d.drawLine(i*10, 0, i*10, SIZE);
            }
            ok = true;
        } else if (textureId == WMFConstants.HS_HORIZONTAL) {
            for (int i = 0; i < 5; i++) {
                g2d.drawLine(0, i*10, SIZE, i*10);
            }
            ok = true;
        } else if (textureId == WMFConstants.HS_BDIAGONAL) {
            for (int i = 0; i < 5; i++) {
                g2d.drawLine(0, i*10, i*10, 0);
            }
            ok = true;
        } else if (textureId == WMFConstants.HS_FDIAGONAL) {
            for (int i = 0; i < 5; i++) {
                g2d.drawLine(0, i*10, SIZE - i*10, SIZE);
            }
            ok = true;
        } else if (textureId == WMFConstants.HS_DIAGCROSS) {
            for (int i = 0; i < 5; i++) {
                g2d.drawLine(0, i*10, i*10, 0);
                g2d.drawLine(0, i*10, SIZE - i*10, SIZE);
            }
            ok = true;
        } else if (textureId == WMFConstants.HS_CROSS) {
            for (int i = 0; i < 5; i++) {
                g2d.drawLine(i*10, 0, i*10, SIZE);
                g2d.drawLine(0, i*10, SIZE, i*10);
            }
            ok = true;
        }
        img.flush();
        if (ok) paint = new TexturePaint(img, rec);
        return paint;
    }
    private class ColoredTexture {
        final int textureId;
        final Color foreground;
        final Color background;
        ColoredTexture(int textureId, Color foreground, Color background) {
            this.textureId = textureId;
            this.foreground = foreground;
            this.background = background;
        }
    }
}
