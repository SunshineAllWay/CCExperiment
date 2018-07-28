package org.apache.batik.ext.awt.image.rendered;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Vector;
public class RenderedImageCachableRed implements CachableRed {
    public static CachableRed wrap(RenderedImage ri) {
        if (ri instanceof CachableRed)
            return (CachableRed) ri;
        if (ri instanceof BufferedImage)
            return new BufferedImageCachableRed((BufferedImage)ri);
        return new RenderedImageCachableRed(ri);
    }
    private RenderedImage src;
    private Vector srcs = new Vector(0);
    public RenderedImageCachableRed(RenderedImage src) {
        if(src == null){
            throw new IllegalArgumentException();
        }
        this.src = src;
    }
    public Vector getSources() {
        return srcs; 
    }
    public Rectangle getBounds() { 
        return new Rectangle(getMinX(),    
                             getMinY(),
                             getWidth(),
                             getHeight());
    }
    public int getMinX() {
        return src.getMinX();
    }
    public int getMinY() {
        return src.getMinY();
    }
    public int getWidth() {
        return src.getWidth();
    }
    public int getHeight() {
        return src.getHeight();
    }
    public ColorModel getColorModel() {
        return src.getColorModel();
    }
    public SampleModel getSampleModel() {
        return src.getSampleModel();
    }
    public int getMinTileX() {
        return src.getMinTileX();
    }
    public int getMinTileY() {
        return src.getMinTileY();
    }
    public int getNumXTiles() {
        return src.getNumXTiles();
    }
    public int getNumYTiles() {
        return src.getNumYTiles();
    }
    public int getTileGridXOffset() {
        return src.getTileGridXOffset();
    }
    public int getTileGridYOffset() {
        return src.getTileGridYOffset();
    }
    public int getTileWidth() {
        return src.getTileWidth();
    }
    public int getTileHeight() {
        return src.getTileHeight();
    }
    public Object getProperty(String name) {
        return src.getProperty(name);
    }
    public String[] getPropertyNames() {
        return src.getPropertyNames();
    }
    public Raster getTile(int tileX, int tileY) {
        return src.getTile(tileX, tileY);
    }
    public WritableRaster copyData(WritableRaster raster) {
        return src.copyData(raster);
    }
    public Raster getData() {
        return src.getData();
    }
    public Raster getData(Rectangle rect) {
        return src.getData(rect);
    }
    public Shape getDependencyRegion(int srcIndex, Rectangle outputRgn) {
        throw new IndexOutOfBoundsException
            ("Nonexistant source requested.");
    }
    public Shape getDirtyRegion(int srcIndex, Rectangle inputRgn) {
        throw new IndexOutOfBoundsException
            ("Nonexistant source requested.");
    }
}
