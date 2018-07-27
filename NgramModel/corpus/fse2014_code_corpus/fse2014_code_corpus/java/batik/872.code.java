package org.apache.batik.ext.awt.image.rendered;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import org.apache.batik.ext.awt.image.GraphicsUtil;
public class FloodRed extends AbstractRed {
    private WritableRaster raster;
    public FloodRed(Rectangle bounds) {
        this(bounds, new Color(0, 0, 0, 0));
    }
    public FloodRed(Rectangle bounds,
                    Paint paint) {
        super(); 
        ColorModel cm = GraphicsUtil.sRGB_Unpre;
        int defSz = AbstractTiledRed.getDefaultTileSize();
        int tw = bounds.width;
        if (tw > defSz) tw = defSz;
        int th = bounds.height;
        if (th > defSz) th = defSz;
        SampleModel sm = cm.createCompatibleSampleModel(tw, th);
        init((CachableRed)null, bounds, cm, sm, 0, 0, null);
        raster = Raster.createWritableRaster(sm, new Point(0, 0));
        BufferedImage offScreen = new BufferedImage(cm, raster,
                                                    cm.isAlphaPremultiplied(),
                                                    null);
        Graphics2D g = GraphicsUtil.createGraphics(offScreen);
        g.setPaint(paint);
        g.fillRect(0, 0, bounds.width, bounds.height);
        g.dispose();
    }
    public Raster getTile(int x, int y) {
        int tx = tileGridXOff+x*tileWidth;
        int ty = tileGridYOff+y*tileHeight;
        return raster.createTranslatedChild(tx, ty);
    }
    public WritableRaster copyData(WritableRaster wr) {
        int tx0 = getXTile(wr.getMinX());
        int ty0 = getYTile(wr.getMinY());
        int tx1 = getXTile(wr.getMinX()+wr.getWidth() -1);
        int ty1 = getYTile(wr.getMinY()+wr.getHeight()-1);
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
        return wr;
    }
}
