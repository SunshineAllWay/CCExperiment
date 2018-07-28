package org.apache.batik.ext.awt.image.rendered;
import  java.awt.image.Raster;
import org.apache.batik.util.HaltingThread;
public class TileGrid implements TileStore {
    private static final boolean DEBUG = false;
    private static final boolean COUNT = false;
    private int xSz, ySz;
    private int minTileX, minTileY;
    private TileLRUMember   [][] rasters=null;
    private TileGenerator source = null;
    private LRUCache      cache = null;
    public TileGrid(int minTileX, int minTileY,
                    int xSz, int ySz,
                    TileGenerator source,
                    LRUCache cache) {
        this.cache    = cache;
        this.source   = source;
        this.minTileX = minTileX;
        this.minTileY = minTileY;
        this.xSz      = xSz;
        this.ySz      = ySz;
        rasters = new TileLRUMember[ySz][];
    }
    public void setTile(int x, int y, Raster ras) {
        x-= minTileX;
        y-= minTileY;
        if ((x<0) || (x>=xSz)) return;
        if ((y<0) || (y>=ySz)) return;
        TileLRUMember [] row = rasters[y];
        TileLRUMember item;
        if (ras == null) {
            if (row == null) return;
            item = row[x];
            if (item == null) return;
            row[x] = null;
            cache.remove(item);
            return;
        }
        if (row != null) {
            item = row[x];
            if (item == null) {
                item = new TileLRUMember();
                row[x] = item;
            }
        } else {
            row = new TileLRUMember[xSz];
            item = new TileLRUMember();
            row[x] = item;
            rasters[y] = row;
        }
        item.setRaster(ras);
        cache.add(item);
        if (DEBUG) System.out.println("Setting: (" + (x+minTileX) + ", " +
                                      (y+minTileY) + ")");
    }
    public Raster getTileNoCompute(int x, int y) {
        x-=minTileX;
        y-=minTileY;
        if ((x<0) || (x>=xSz)) return null;
        if ((y<0) || (y>=ySz)) return null;
        TileLRUMember [] row = rasters[y];
        if (row == null)
            return null;
        TileLRUMember item = row[x];
        if (item == null)
            return null;
        Raster ret = item.retrieveRaster();
        if (ret != null)
            cache.add(item);
        return ret;
    }
    public Raster getTile(int x, int y) {
        x-=minTileX;
        y-=minTileY;
        if ((x<0) || (x>=xSz)) return null;
        if ((y<0) || (y>=ySz)) return null;
        if (DEBUG) System.out.println("Fetching: (" + (x+minTileX) + ", " +
                                      (y+minTileY) + ")");
        if (COUNT) synchronized (TileGrid.class) { requests++; }
        Raster       ras  = null;
        TileLRUMember [] row  = rasters[y];
        TileLRUMember    item = null;
        if (row != null) {
            item = row[x];
            if (item != null)
                ras = item.retrieveRaster();
            else {
                item = new TileLRUMember();
                row[x] = item;
            }
        } else {
            row = new TileLRUMember[xSz];
            rasters[y] = row;
            item = new TileLRUMember();
            row[x] = item;
        }
        if (ras == null) {
            if (DEBUG) System.out.println("Generating: ("+(x+minTileX)+", "+
                                          (y+minTileY) + ")");
            if (COUNT) synchronized (TileGrid.class) { misses++; }
            ras = source.genTile(x+minTileX, y+minTileY);
            if (HaltingThread.hasBeenHalted())
                return ras;
            item.setRaster(ras);
        }
        cache.add(item);
        return ras;
    }
    static int requests;
    static int misses;
}
