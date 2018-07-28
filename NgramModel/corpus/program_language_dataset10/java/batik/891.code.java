package org.apache.batik.ext.awt.image.rendered;
import  java.awt.image.Raster;
public interface TileStore {
    void setTile(int x, int y, Raster ras);
    Raster getTile(int x, int y);
    Raster getTileNoCompute(int x, int y);
}
