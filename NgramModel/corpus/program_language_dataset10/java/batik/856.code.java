package org.apache.batik.ext.awt.image.rendered;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.apache.batik.ext.awt.image.GraphicsUtil;
public abstract class AbstractRed implements CachableRed {
    protected Rectangle   bounds;
    protected Vector      srcs;
    protected Map         props;
    protected SampleModel sm;
    protected ColorModel  cm;
    protected int         tileGridXOff, tileGridYOff;
    protected int         tileWidth,    tileHeight;
    protected int         minTileX,     minTileY;
    protected int         numXTiles,    numYTiles;
    protected AbstractRed() {
    }
    protected AbstractRed(Rectangle bounds, Map props) {
        init((CachableRed)null, bounds, null, null,
             bounds.x, bounds.y, props);
    }
    protected AbstractRed(CachableRed src, Map props) {
        init(src, src.getBounds(), src.getColorModel(), src.getSampleModel(),
             src.getTileGridXOffset(),
             src.getTileGridYOffset(),
             props);
    }
    protected AbstractRed(CachableRed src, Rectangle bounds, Map props) {
        init(src, bounds, src.getColorModel(), src.getSampleModel(),
             src.getTileGridXOffset(),
             src.getTileGridYOffset(),
             props);
    }
    protected AbstractRed(CachableRed src, Rectangle bounds,
                          ColorModel cm, SampleModel sm,
                          Map props) {
        init(src, bounds, cm, sm,
             (src==null)?0:src.getTileGridXOffset(),
             (src==null)?0:src.getTileGridYOffset(),
             props);
    }
    protected AbstractRed(CachableRed src, Rectangle bounds,
                          ColorModel cm, SampleModel sm,
                          int tileGridXOff, int tileGridYOff,
                          Map props) {
        init(src, bounds, cm, sm, tileGridXOff, tileGridYOff, props);
    }
    protected void init(CachableRed src, Rectangle   bounds,
                        ColorModel  cm,   SampleModel sm,
                        int tileGridXOff, int tileGridYOff,
                        Map props) {
        this.srcs         = new Vector(1);
        if (src != null) {
            this.srcs.add(src);
            if (bounds == null) bounds = src.getBounds();
            if (cm     == null) cm     = src.getColorModel();
            if (sm     == null) sm     = src.getSampleModel();
        }
        this.bounds       = bounds;
        this.tileGridXOff = tileGridXOff;
        this.tileGridYOff = tileGridYOff;
        this.props        = new HashMap();
        if(props != null){
            this.props.putAll(props);
        }
        if (cm == null)
            cm = new ComponentColorModel
                (ColorSpace.getInstance(ColorSpace.CS_GRAY),
                 new int [] { 8 }, false, false, Transparency.OPAQUE,
                 DataBuffer.TYPE_BYTE);
        this.cm = cm;
        if (sm == null)
            sm = cm.createCompatibleSampleModel(bounds.width, bounds.height);
        this.sm = sm;
        updateTileGridInfo();
    }
    protected AbstractRed(List srcs, Rectangle bounds, Map props) {
        init(srcs, bounds, null, null, bounds.x, bounds.y, props);
    }
    protected AbstractRed(List srcs, Rectangle bounds,
                          ColorModel cm, SampleModel sm,
                          Map props) {
        init(srcs, bounds, cm, sm, bounds.x, bounds.y, props);
    }
    protected AbstractRed(List srcs, Rectangle bounds,
                          ColorModel cm, SampleModel sm,
                          int tileGridXOff, int tileGridYOff,
                          Map props) {
        init(srcs, bounds, cm, sm, tileGridXOff, tileGridYOff, props);
    }
    protected void init(List srcs, Rectangle bounds,
                        ColorModel cm, SampleModel sm,
                        int tileGridXOff, int tileGridYOff,
                        Map props) {
        this.srcs = new Vector();
        if(srcs != null){
            this.srcs.addAll(srcs);
        }
        if (srcs.size() != 0) {
            CachableRed src = (CachableRed)srcs.get(0);
            if (bounds == null) bounds = src.getBounds();
            if (cm     == null) cm     = src.getColorModel();
            if (sm     == null) sm     = src.getSampleModel();
        }
        this.bounds       = bounds;
        this.tileGridXOff = tileGridXOff;
        this.tileGridYOff = tileGridYOff;
        this.props        = new HashMap();
        if(props != null){
            this.props.putAll(props);
        }
        if (cm == null)
            cm = new ComponentColorModel
                (ColorSpace.getInstance(ColorSpace.CS_GRAY),
                 new int [] { 8 }, false, false, Transparency.OPAQUE,
                 DataBuffer.TYPE_BYTE);
        this.cm = cm;
        if (sm == null)
            sm = cm.createCompatibleSampleModel(bounds.width, bounds.height);
        this.sm = sm;
        updateTileGridInfo();
    }
    protected void updateTileGridInfo() {
        this.tileWidth  = sm.getWidth();
        this.tileHeight = sm.getHeight();
        int x1, y1, maxTileX, maxTileY;
        minTileX = getXTile(bounds.x);
        minTileY = getYTile(bounds.y);
        x1       = bounds.x + bounds.width-1;     
        maxTileX = getXTile(x1);
        numXTiles = maxTileX-minTileX+1;
        y1       = bounds.y + bounds.height-1;     
        maxTileY = getYTile(y1);
        numYTiles = maxTileY-minTileY+1;
    }
    public Rectangle getBounds() {
        return new Rectangle(getMinX(),
                             getMinY(),
                             getWidth(),
                             getHeight());
    }
    public Vector getSources() {
        return srcs;
    }
    public ColorModel getColorModel() {
        return cm;
    }
    public SampleModel getSampleModel() {
        return sm;
    }
    public int getMinX() {
        return bounds.x;
    }
    public int getMinY() {
        return bounds.y;
    }
    public int getWidth() {
        return bounds.width;
    }
    public int getHeight() {
        return bounds.height;
    }
    public int getTileWidth() {
        return tileWidth;
    }
    public int getTileHeight() {
        return tileHeight;
    }
    public int getTileGridXOffset() {
        return tileGridXOff;
    }
    public int getTileGridYOffset() {
        return tileGridYOff;
    }
    public int getMinTileX() {
        return minTileX;
    }
    public int getMinTileY() {
        return minTileY;
    }
    public int getNumXTiles() {
        return numXTiles;
    }
    public int getNumYTiles() {
        return numYTiles;
    }
    public Object getProperty(String name) {
        Object ret = props.get(name);
        if (ret != null) return ret;
        Iterator i = srcs.iterator();
        while (i.hasNext()) {
            RenderedImage ri = (RenderedImage)i.next();
            ret = ri.getProperty(name);
            if (ret != null) return ret;
        }
        return null;
    }
    public String [] getPropertyNames() {
        Set keys = props.keySet();
        String[] ret  = new String[keys.size()];
        keys.toArray( ret );
        Iterator iter = srcs.iterator();
        while (iter.hasNext()) {
            RenderedImage ri = (RenderedImage)iter.next();
            String [] srcProps = ri.getPropertyNames();
            if (srcProps.length != 0) {
                String [] tmp = new String[ret.length+srcProps.length];
                System.arraycopy(ret,0,tmp,0,ret.length);
                System.arraycopy( srcProps, 0, tmp, ret.length, srcProps.length);
                ret = tmp;
            }
        }
        return ret;
    }
    public Shape getDependencyRegion(int srcIndex, Rectangle outputRgn) {
        if ((srcIndex < 0) || (srcIndex > srcs.size()))
            throw new IndexOutOfBoundsException
                ("Nonexistant source requested.");
        if ( ! outputRgn.intersects(bounds) )
            return new Rectangle();
        return outputRgn.intersection(bounds);
    }
    public Shape getDirtyRegion(int srcIndex, Rectangle inputRgn) {
        if (srcIndex != 0)
            throw new IndexOutOfBoundsException
                ("Nonexistant source requested.");
        if ( ! inputRgn.intersects(bounds) )
            return new Rectangle();
        return inputRgn.intersection(bounds);
    }
    public Raster getTile(int tileX, int tileY) {
        WritableRaster wr = makeTile(tileX, tileY);
        return copyData(wr);
    }
    public Raster getData() {
        return getData(bounds);
    }
    public Raster getData(Rectangle rect) {
        SampleModel smRet = sm.createCompatibleSampleModel
            (rect.width, rect.height);
        Point pt = new Point(rect.x, rect.y);
        WritableRaster wr = Raster.createWritableRaster(smRet, pt);
        return copyData(wr);
    }
    public final int getXTile(int xloc) {
        int tgx = xloc-tileGridXOff;
        if (tgx>=0)
            return tgx/tileWidth;
        else
            return (tgx-tileWidth+1)/tileWidth;
    }
    public final int getYTile(int yloc) {
        int tgy = yloc-tileGridYOff;
        if (tgy>=0)
            return tgy/tileHeight;
        else
            return (tgy-tileHeight+1)/tileHeight;
    }
    public void copyToRaster(WritableRaster wr) {
        int tx0 = getXTile(wr.getMinX());
        int ty0 = getYTile(wr.getMinY());
        int tx1 = getXTile(wr.getMinX()+wr.getWidth() -1);
        int ty1 = getYTile(wr.getMinY()+wr.getHeight()-1);
        if (tx0 < minTileX) tx0 = minTileX;
        if (ty0 < minTileY) ty0 = minTileY;
        if (tx1 >= minTileX+numXTiles) tx1 = minTileX+numXTiles-1;
        if (ty1 >= minTileY+numYTiles) ty1 = minTileY+numYTiles-1;
        final boolean is_INT_PACK =
            GraphicsUtil.is_INT_PACK_Data(getSampleModel(), false);
        for (int y=ty0; y<=ty1; y++)
            for (int x=tx0; x<=tx1; x++) {
                Raster r = getTile(x, y);
                if (is_INT_PACK)
                    GraphicsUtil.copyData_INT_PACK(r, wr);
                else
                    GraphicsUtil.copyData_FALLBACK(r, wr);
            }
    }
    public WritableRaster makeTile(int tileX, int tileY) {
        if ((tileX < minTileX) || (tileX >= minTileX+numXTiles) ||
            (tileY < minTileY) || (tileY >= minTileY+numYTiles))
            throw new IndexOutOfBoundsException
                ("Requested Tile (" + tileX + ',' + tileY +
                 ") lies outside the bounds of image");
        Point pt = new Point(tileGridXOff+tileX*tileWidth,
                             tileGridYOff+tileY*tileHeight);
        WritableRaster wr;
        wr = Raster.createWritableRaster(sm, pt);
        int x0 = wr.getMinX();
        int y0 = wr.getMinY();
        int x1 = x0+wr.getWidth() -1;
        int y1 = y0+wr.getHeight()-1;
        if ((x0 < bounds.x) || (x1 >= (bounds.x+bounds.width)) ||
            (y0 < bounds.y) || (y1 >= (bounds.y+bounds.height))) {
            if (x0 < bounds.x) x0 = bounds.x;
            if (y0 < bounds.y) y0 = bounds.y;
            if (x1 >= (bounds.x+bounds.width))  x1 = bounds.x+bounds.width-1;
            if (y1 >= (bounds.y+bounds.height)) y1 = bounds.y+bounds.height-1;
            wr = wr.createWritableChild(x0, y0, x1-x0+1, y1-y0+1,
                                        x0, y0, null);
        }
        return wr;
    }
    public static void copyBand(Raster         src, int srcBand,
                                WritableRaster dst, int dstBand) {
        Rectangle srcR = new Rectangle(src.getMinX(),  src.getMinY(),
                                       src.getWidth(), src.getHeight());
        Rectangle dstR = new Rectangle(dst.getMinX(),  dst.getMinY(),
                                       dst.getWidth(), dst.getHeight());
        Rectangle cpR  = srcR.intersection(dstR);
        int [] samples = null;
        for (int y=cpR.y; y< cpR.y+cpR.height; y++) {
            samples = src.getSamples(cpR.x, y, cpR.width, 1, srcBand, samples);
            dst.setSamples(cpR.x, y, cpR.width, 1, dstBand, samples);
        }
    }
}
