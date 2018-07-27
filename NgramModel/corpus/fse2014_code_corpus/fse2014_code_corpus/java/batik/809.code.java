package org.apache.batik.ext.awt.image.codec.util;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
public abstract class SimpleRenderedImage implements RenderedImage {
    protected int minX;
    protected int minY;
    protected int width;
    protected int height;
    protected int tileWidth;
    protected int tileHeight;
    protected int tileGridXOffset = 0;
    protected int tileGridYOffset = 0;
    protected SampleModel sampleModel = null;
    protected ColorModel colorModel = null;
    protected List sources = new ArrayList();
    protected Map properties = new HashMap();
    public SimpleRenderedImage() {}
    public int getMinX() {
        return minX;
    }
    public final int getMaxX() {
        return getMinX() + getWidth();
    }
    public int getMinY() {
        return minY;
    }
    public final int getMaxY() {
        return getMinY() + getHeight();
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public Rectangle getBounds() {
        return new Rectangle(getMinX(), getMinY(),
                             getWidth(), getHeight());
    }
    public int getTileWidth() {
        return tileWidth;
    }
    public int getTileHeight() {
        return tileHeight;
    }
    public int getTileGridXOffset() {
        return tileGridXOffset;
    }
    public int getTileGridYOffset() {
        return tileGridYOffset;
    }
    public int getMinTileX() {
        return XToTileX(getMinX());
    }
    public int getMaxTileX() {
        return XToTileX(getMaxX() - 1);
    }
    public int getNumXTiles() {
        return getMaxTileX() - getMinTileX() + 1;
    }
    public int getMinTileY() {
        return YToTileY(getMinY());
    }
    public int getMaxTileY() {
        return YToTileY(getMaxY() - 1);
    }
    public int getNumYTiles() {
        return getMaxTileY() - getMinTileY() + 1;
    }
    public SampleModel getSampleModel() {
        return sampleModel;
    }
    public ColorModel getColorModel() {
        return colorModel;
    }
    public Object getProperty(String name) {
        name = name.toLowerCase();
        return properties.get(name);
    }
    public String[] getPropertyNames() {
        String[] names = new String[properties.size()];
        properties.keySet().toArray( names );
        return names;
    }
    public String[] getPropertyNames(String prefix) {
        String[] propertyNames = getPropertyNames();
        if (propertyNames == null) {
            return null;
        }
        prefix = prefix.toLowerCase();
        List names = new ArrayList();
        for (int i = 0; i < propertyNames.length; i++) {
            if (propertyNames[i].startsWith(prefix)) {
                names.add(propertyNames[i]);
            }
        }
        if (names.size() == 0) {
            return null;
        }
        String[] prefixNames = new String[names.size()];
        names.toArray( prefixNames );
        return prefixNames;
    }
    public static int XToTileX(int x, int tileGridXOffset, int tileWidth) {
        x -= tileGridXOffset;
        if (x < 0) {
            x += 1 - tileWidth; 
        }
        return x/tileWidth;
    }
    public static int YToTileY(int y, int tileGridYOffset, int tileHeight) {
        y -= tileGridYOffset;
        if (y < 0) {
            y += 1 - tileHeight; 
        }
        return y/tileHeight;
    }
    public int XToTileX(int x) {
        return XToTileX(x, getTileGridXOffset(), getTileWidth());
    }
    public int YToTileY(int y) {
        return YToTileY(y, getTileGridYOffset(), getTileHeight());
    }
    public static int tileXToX(int tx, int tileGridXOffset, int tileWidth) {
        return tx*tileWidth + tileGridXOffset;
    }
    public static int tileYToY(int ty, int tileGridYOffset, int tileHeight) {
        return ty*tileHeight + tileGridYOffset;
    }
    public int tileXToX(int tx) {
        return tx*tileWidth + tileGridXOffset;
    }
    public int tileYToY(int ty) {
        return ty*tileHeight + tileGridYOffset;
    }
    public Vector getSources() {
        return null;
    }
    public Raster getData() {
        Rectangle rect = new Rectangle(getMinX(), getMinY(),
                                       getWidth(), getHeight());
        return getData(rect);
    }
    public Raster getData(Rectangle bounds) {
        int startX = XToTileX(bounds.x);
        int startY = YToTileY(bounds.y);
        int endX = XToTileX(bounds.x + bounds.width - 1);
        int endY = YToTileY(bounds.y + bounds.height - 1);
        Raster tile;
        if ((startX == endX) && (startY == endY)) {
            tile = getTile(startX, startY);
            return tile.createChild(bounds.x, bounds.y,
                                    bounds.width, bounds.height,
                                    bounds.x, bounds.y, null);
        } else {
            SampleModel sm =
                sampleModel.createCompatibleSampleModel(bounds.width,
                                                       bounds.height);
            WritableRaster dest =
                Raster.createWritableRaster(sm, bounds.getLocation());
            for (int j = startY; j <= endY; j++) {
                for (int i = startX; i <= endX; i++) {
                    tile = getTile(i, j);
                    Rectangle intersectRect =
                        bounds.intersection(tile.getBounds());
                    Raster liveRaster = tile.createChild(intersectRect.x,
                                                         intersectRect.y,
                                                         intersectRect.width,
                                                         intersectRect.height,
                                                         intersectRect.x,
                                                         intersectRect.y,
                                                         null);
                    dest.setDataElements(0, 0, liveRaster);
                }
            }
            return dest;
        }
    }
    public WritableRaster copyData(WritableRaster dest) {
        Rectangle bounds;
        Raster tile;
        if (dest == null) {
            bounds = getBounds();
            Point p = new Point(minX, minY);
            SampleModel sm = sampleModel.createCompatibleSampleModel(
                                         width, height);
            dest = Raster.createWritableRaster(sm, p);
        } else {
            bounds = dest.getBounds();
        }
        int startX = XToTileX(bounds.x);
        int startY = YToTileY(bounds.y);
        int endX = XToTileX(bounds.x + bounds.width - 1);
        int endY = YToTileY(bounds.y + bounds.height - 1);
        for (int j = startY; j <= endY; j++) {
            for (int i = startX; i <= endX; i++) {
                tile = getTile(i, j);
                Rectangle intersectRect =
                    bounds.intersection(tile.getBounds());
                Raster liveRaster = tile.createChild(intersectRect.x,
                                                     intersectRect.y,
                                                     intersectRect.width,
                                                     intersectRect.height,
                                                     intersectRect.x,
                                                     intersectRect.y,
                                                     null);
                dest.setDataElements(0, 0, liveRaster);
            }
        }
        return dest;
    }
}
