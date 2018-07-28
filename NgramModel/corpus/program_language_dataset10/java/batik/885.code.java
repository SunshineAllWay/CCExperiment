package org.apache.batik.ext.awt.image.rendered;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
public class TileCacheRed extends AbstractTiledRed {
    public TileCacheRed(CachableRed cr) {
        super(cr, null);
    }
    public TileCacheRed(CachableRed cr, int tileWidth, int tileHeight) {
        super();
        ColorModel  cm = cr.getColorModel();
        Rectangle bounds = cr.getBounds();
        if (tileWidth  > bounds.width)  tileWidth  = bounds.width;
        if (tileHeight > bounds.height) tileHeight = bounds.height;
        SampleModel sm = cm.createCompatibleSampleModel(tileWidth, tileHeight);
        init(cr, bounds, cm, sm, 
             cr.getTileGridXOffset(), cr.getTileGridYOffset(),
             null);
    }
    public void genRect(WritableRaster wr) {
        CachableRed src = (CachableRed)getSources().get(0);
        src.copyData(wr);
    }
    public void flushCache(Rectangle rect) {
        int tx0 = getXTile(rect.x);
        int ty0 = getYTile(rect.y);
        int tx1 = getXTile(rect.x+rect.width -1);
        int ty1 = getYTile(rect.y+rect.height-1);
        if (tx0 < minTileX) tx0 = minTileX;
        if (ty0 < minTileY) ty0 = minTileY;
        if (tx1 >= minTileX+numXTiles) tx1 = minTileX+numXTiles-1;
        if (ty1 >= minTileY+numYTiles) ty1 = minTileY+numYTiles-1;
        if ((tx1 < tx0) || (ty1 < ty0))
            return;
        TileStore store = getTileStore();
        for (int y=ty0; y<=ty1; y++)
            for (int x=tx0; x<=tx1; x++)
                store.setTile(x, y, null);
    }
}
