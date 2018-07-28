package org.apache.batik.extension.svg;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.apache.batik.ext.awt.image.rendered.AbstractRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
public class HistogramRed extends AbstractRed {
    boolean [] computed;
    int tallied = 0;
    int [] bins = new int[256];
    public HistogramRed(CachableRed src){
        super(src, null);
        int tiles = getNumXTiles()*getNumYTiles();
        computed = new boolean[tiles];
    }
    public void tallyTile(Raster r) {
        final int minX = r.getMinX();
        final int minY = r.getMinY();
        final int w = r.getWidth();
        final int h = r.getHeight();
        int [] samples = null;
        int val;
        for (int y=minY; y<minY+h; y++) {
            samples = r.getPixels(minX, y, w, 1, samples);
            for (int x=0; x<3*w; x++) {
                val  = samples[x++]*5; 
                val += samples[x++]*9; 
                val += samples[x++]*2; 
                bins[val>>4]++;
            }
        }
        tallied++;
    }
    public int [] getHistogram() {
        if (tallied == computed.length)
            return bins;
        CachableRed src = (CachableRed)getSources().get( 0 );
        int yt0 = src.getMinTileY();
        int xtiles = src.getNumXTiles();
        int xt0 = src.getMinTileX();
        for (int y=0; y<src.getNumYTiles(); y++) {
            for (int x=0; x<xtiles; x++) {
                int idx = (x+xt0)+y*xtiles;
                if (computed[idx]) continue;
                Raster r = src.getTile(x+xt0, y+yt0);
                tallyTile(r);
                computed[idx]=true;
            }
        }
        return bins;
    }
    public WritableRaster copyData(WritableRaster wr) {
        copyToRaster(wr);
        return wr;
    }
    public Raster getTile(int tileX, int tileY) {
        int yt = tileY-getMinTileY();
        int xt = tileX-getMinTileX();
        CachableRed src = (CachableRed)getSources().get(0);
        Raster r = src.getTile(tileX, tileY);
        int idx = xt+yt*getNumXTiles();
        if (computed[idx])
            return r;
        tallyTile(r);
        computed[idx] = true;
        return r;
    }
}
