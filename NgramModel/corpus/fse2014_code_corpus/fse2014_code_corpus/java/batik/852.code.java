package org.apache.batik.ext.awt.image.renderable;
import java.awt.geom.Rectangle2D;
public interface TileRable extends FilterColorInterpolation {
    Rectangle2D getTileRegion();
    void setTileRegion(Rectangle2D tileRegion);
    Rectangle2D getTiledRegion();
    void setTiledRegion(Rectangle2D tiledRegion);
    boolean isOverflow();
    void setOverflow(boolean overflow);
    void setSource(Filter source);
    Filter getSource();
}
