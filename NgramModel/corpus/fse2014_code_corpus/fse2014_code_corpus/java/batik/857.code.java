package org.apache.batik.ext.awt.image.rendered;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.List;
import java.util.Map;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.util.HaltingThread;
public abstract class AbstractTiledRed
    extends    AbstractRed
    implements TileGenerator {
    private TileStore tiles;
    private static int defaultTileSize = 128;
    public static int getDefaultTileSize() { return defaultTileSize; }
    protected AbstractTiledRed() { }
    protected AbstractTiledRed(Rectangle bounds, Map props) {
        super(bounds, props);
    }
    protected AbstractTiledRed(CachableRed src, Map props) {
        super(src, props);
    }
    protected AbstractTiledRed(CachableRed src, Rectangle bounds, Map props) {
        super(src, bounds, props);
    }
    protected AbstractTiledRed(CachableRed src, Rectangle bounds,
                          ColorModel cm, SampleModel sm,
                          Map props) {
        super(src, bounds, cm, sm, props);
    }
    protected AbstractTiledRed(CachableRed src, Rectangle bounds,
                          ColorModel cm, SampleModel sm,
                          int tileGridXOff, int tileGridYOff,
                          Map props) {
        super(src, bounds, cm, sm, tileGridXOff, tileGridYOff, props);
    }
    protected void init(CachableRed src, Rectangle   bounds,
                        ColorModel  cm,   SampleModel sm,
                        int tileGridXOff, int tileGridYOff,
                        Map props) {
        init(src, bounds, cm, sm, tileGridXOff, tileGridYOff, null, props);
    }
    protected void init(CachableRed src, Rectangle   bounds,
                        ColorModel  cm,   SampleModel sm,
                        int tileGridXOff, int tileGridYOff,
                        TileStore tiles,
                        Map props) {
        super.init(src, bounds, cm, sm, tileGridXOff, tileGridYOff, props);
        this.tiles = tiles;
        if (this.tiles == null)
            this.tiles = createTileStore();
    }
    protected AbstractTiledRed(List srcs, Rectangle bounds, Map props) {
        super(srcs, bounds, props);
    }
    protected AbstractTiledRed(List srcs, Rectangle bounds,
                          ColorModel cm, SampleModel sm,
                          Map props) {
        super(srcs, bounds, cm, sm, props);
    }
    protected AbstractTiledRed(List srcs, Rectangle bounds,
                          ColorModel cm, SampleModel sm,
                          int tileGridXOff, int tileGridYOff,
                          Map props) {
        super(srcs, bounds, cm, sm, tileGridXOff, tileGridYOff, props);
    }
    protected void init(List srcs, Rectangle bounds,
                        ColorModel cm, SampleModel sm,
                        int tileGridXOff, int tileGridYOff,
                        Map props) {
        super.init(srcs, bounds, cm, sm, tileGridXOff, tileGridYOff, props);
        tiles = createTileStore();
    }
    public TileStore getTileStore() {
        return tiles;
    }
    protected void setTileStore(TileStore tiles) {
        this.tiles = tiles;
    }
    protected TileStore createTileStore() {
        return TileCache.getTileMap(this);
    }
    public WritableRaster copyData(WritableRaster wr) {
        copyToRasterByBlocks(wr);
        return wr;
    }
    public Raster getData(Rectangle rect) {
        int xt0 = getXTile(rect.x);
        int xt1 = getXTile(rect.x+rect.width-1);
        int yt0 = getYTile(rect.y);
        int yt1 = getYTile(rect.y+rect.height-1);
        if ((xt0 == xt1) && (yt0 == yt1)) {
            Raster r = getTile(xt0, yt0);
            return r.createChild(rect.x, rect.y, rect.width, rect.height,
                                 rect.x, rect.y, null);
        }
        return super.getData(rect);
    }
    public Raster getTile(int x, int y) {
        return tiles.getTile(x, y);
    }
    public Raster genTile(int x, int y) {
        WritableRaster wr = makeTile(x, y);
        genRect(wr);
        return wr;
    }
    public abstract void genRect(WritableRaster wr);
    public void setTile(int x, int y, Raster ras) {
        tiles.setTile(x, y, ras);
    }
    public void copyToRasterByBlocks(WritableRaster wr) {
        final boolean is_INT_PACK =
            GraphicsUtil.is_INT_PACK_Data(getSampleModel(), false);
        Rectangle bounds = getBounds();
        Rectangle wrR    = wr.getBounds();
        int tx0 = getXTile(wrR.x);
        int ty0 = getYTile(wrR.y);
        int tx1 = getXTile(wrR.x+wrR.width -1);
        int ty1 = getYTile(wrR.y+wrR.height-1);
        if (tx0 < minTileX) tx0 = minTileX;
        if (ty0 < minTileY) ty0 = minTileY;
        if (tx1 >= minTileX+numXTiles) tx1 = minTileX+numXTiles-1;
        if (ty1 >= minTileY+numYTiles) ty1 = minTileY+numYTiles-1;
        if ((tx1 < tx0) || (ty1 < ty0))
            return;
        int insideTx0 = tx0;
        int insideTx1 = tx1;
        int insideTy0 = ty0;
        int insideTy1 = ty1;
        int tx, ty;
        tx = tx0*tileWidth+tileGridXOff;
        if ((tx < wrR.x)  && (bounds.x != wrR.x))
            insideTx0++;
        ty= ty0*tileHeight+tileGridYOff;
        if ((ty < wrR.y) && (bounds.y != wrR.y))
            insideTy0++;
        tx= (tx1+1)*tileWidth+tileGridXOff-1;
        if ((tx >= (wrR.x+wrR.width)) &&
            ((bounds.x+bounds.width) != (wrR.x+wrR.width)))
            insideTx1--;
        ty= (ty1+1)*tileHeight+tileGridYOff-1;
        if ((ty >= (wrR.y+wrR.height)) &&
            ((bounds.y+bounds.height) != (wrR.y+wrR.height)))
            insideTy1--;
        int xtiles = insideTx1-insideTx0+1;
        int ytiles = insideTy1-insideTy0+1;
        boolean [] occupied = null;
        if ((xtiles > 0) && (ytiles > 0))
            occupied = new boolean[xtiles*ytiles];
        boolean [] got = new boolean[2*(tx1-tx0+1) + 2*(ty1-ty0+1)];
        int idx = 0;
        int numFound = 0;
        for (int y=ty0; y<=ty1; y++) {
            for (int x=tx0; x<=tx1; x++) {
                Raster ras = tiles.getTileNoCompute(x, y);
                boolean found = (ras != null);
                if ((y>=insideTy0) && (y<=insideTy1) &&
                    (x>=insideTx0) && (x<=insideTx1))
                    occupied[(x-insideTx0)+(y-insideTy0)*xtiles] = found;
                else
                    got[idx++] = found;
                if (!found) continue;
                numFound++;
                if (is_INT_PACK)
                    GraphicsUtil.copyData_INT_PACK(ras, wr);
                else
                    GraphicsUtil.copyData_FALLBACK(ras, wr);
            }
        }
        if ((xtiles > 0) && (ytiles > 0)) {
            TileBlock block = new TileBlock
                (insideTx0, insideTy0, xtiles, ytiles, occupied,
                 0, 0, xtiles, ytiles);
            drawBlock(block, wr);
        }
        Thread currentThread = Thread.currentThread();
        if (HaltingThread.hasBeenHalted())
            return;
        idx = 0;
        for (ty=ty0; ty<=ty1; ty++) {
            for (tx=tx0; tx<=tx1; tx++) {
                Raster ras = tiles.getTileNoCompute(tx, ty);
                if ((ty>=insideTy0) && (ty<=insideTy1) &&
                    (tx>=insideTx0) && (tx<=insideTx1)) {
                    if (ras != null) continue;
                    WritableRaster tile = makeTile(tx, ty);
                    if (is_INT_PACK)
                        GraphicsUtil.copyData_INT_PACK(wr, tile);
                    else
                        GraphicsUtil.copyData_FALLBACK(wr, tile);
                    tiles.setTile(tx, ty, tile);
                }
                else {
                    if (got[idx++]) continue;
                    ras = getTile(tx, ty);
                    if (HaltingThread.hasBeenHalted( currentThread ))
                        return;
                    if (is_INT_PACK)
                        GraphicsUtil.copyData_INT_PACK(ras, wr);
                    else
                        GraphicsUtil.copyData_FALLBACK(ras, wr);
                }
            }
        }
    }
    public void copyToRaster(WritableRaster wr) {
        Rectangle wrR = wr.getBounds();
        int tx0 = getXTile(wrR.x);
        int ty0 = getYTile(wrR.y);
        int tx1 = getXTile(wrR.x+wrR.width -1);
        int ty1 = getYTile(wrR.y+wrR.height-1);
        if (tx0 < minTileX) tx0 = minTileX;
        if (ty0 < minTileY) ty0 = minTileY;
        if (tx1 >= minTileX+numXTiles) tx1 = minTileX+numXTiles-1;
        if (ty1 >= minTileY+numYTiles) ty1 = minTileY+numYTiles-1;
        final boolean is_INT_PACK =
            GraphicsUtil.is_INT_PACK_Data(getSampleModel(), false);
        int xtiles = (tx1-tx0+1);
        boolean [] got = new boolean[xtiles*(ty1-ty0+1)];
        for (int y=ty0; y<=ty1; y++)
            for (int x=tx0; x<=tx1; x++) {
                Raster r = tiles.getTileNoCompute(x, y);
                if (r == null) continue; 
                got[x-tx0 + (y-ty0)*xtiles] = true;
                if (is_INT_PACK)
                    GraphicsUtil.copyData_INT_PACK(r, wr);
                else
                    GraphicsUtil.copyData_FALLBACK(r, wr);
            }
        for (int y=ty0; y<=ty1; y++)
            for (int x=tx0; x<=tx1; x++) {
                if (got[x-tx0 + (y-ty0)*xtiles]) continue; 
                Raster r = getTile(x, y);
                if (is_INT_PACK)
                    GraphicsUtil.copyData_INT_PACK(r, wr);
                else
                    GraphicsUtil.copyData_FALLBACK(r, wr);
            }
    }
    protected void drawBlock( TileBlock block, WritableRaster wr ) {
        TileBlock [] blocks = block.getBestSplit();
        if ( blocks == null ) {
            return;
        }
        drawBlockInPlace( blocks, wr );
    }
    protected void drawBlockAndCopy( TileBlock []blocks, WritableRaster wr ) {
        if ( blocks.length == 1 ) {
            TileBlock curr = blocks[ 0 ];
            int xloc = curr.getXLoc() * tileWidth + tileGridXOff;
            int yloc = curr.getYLoc() * tileHeight + tileGridYOff;
            if ( ( xloc == wr.getMinX() ) &&
                 ( yloc == wr.getMinY() ) ) {
                drawBlockInPlace( blocks, wr );
                return;
            }
        }
        int workTileWidth = tileWidth;    
        int workTileHeight = tileHeight;  
        int maxTileSize = 0;
        for ( int i = 0; i < blocks.length; i++ ) {
            TileBlock curr = blocks[ i ];
            int sz = ( ( curr.getWidth() * workTileWidth ) *
                       ( curr.getHeight() * workTileHeight ) );
            if ( sz > maxTileSize ) {
                maxTileSize = sz;
            }
        }
        DataBufferInt dbi = new DataBufferInt( maxTileSize );
        int [] masks = {0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000};
        boolean use_INT_PACK = GraphicsUtil.is_INT_PACK_Data( wr.getSampleModel(), false );
        Thread currentThread = Thread.currentThread();
        for ( int i = 0; i < blocks.length; i++ ) {
            TileBlock curr = blocks[ i ];
            int xloc = curr.getXLoc() * workTileWidth + tileGridXOff;
            int yloc = curr.getYLoc() * workTileHeight + tileGridYOff;
            Rectangle tb = new Rectangle( xloc, yloc,
                    curr.getWidth() * workTileWidth,
                    curr.getHeight() * workTileHeight );
            tb = tb.intersection( bounds );
            Point loc = new Point( tb.x, tb.y );
            WritableRaster child = Raster.createPackedRaster( dbi, tb.width, tb.height, tb.width, masks, loc );
            genRect( child );
            if ( use_INT_PACK ) {
                GraphicsUtil.copyData_INT_PACK( child, wr );
            } else {
                GraphicsUtil.copyData_FALLBACK( child, wr );
            }
            if ( HaltingThread.hasBeenHalted( currentThread ) ) {
                return;
            }
        }
    }
    protected void drawBlockInPlace( TileBlock [] blocks, WritableRaster wr ) {
        Thread currentThread = Thread.currentThread();
        int workTileWidth = tileWidth;    
        int workTileHeight = tileHeight;  
        for ( int i = 0; i < blocks.length; i++ ) {
            TileBlock curr = blocks[ i ];
            int xloc = curr.getXLoc() * workTileWidth + tileGridXOff;
            int yloc = curr.getYLoc() * workTileHeight + tileGridYOff;
            Rectangle tb = new Rectangle( xloc, yloc,
                    curr.getWidth() * workTileWidth,
                    curr.getHeight() * workTileHeight );
            tb = tb.intersection( bounds );
            WritableRaster child =
                    wr.createWritableChild( tb.x, tb.y, tb.width, tb.height,
                            tb.x, tb.y, null );
            genRect( child );
            if ( HaltingThread.hasBeenHalted( currentThread ) ) {
                return;
            }
        }
    }
}
