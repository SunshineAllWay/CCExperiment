package org.apache.batik.ext.awt.image.renderable;
import java.awt.geom.Rectangle2D;
public interface FilterChainRable extends Filter {
    int getFilterResolutionX();
    void setFilterResolutionX(int filterResolutionX);
    int getFilterResolutionY();
    void setFilterResolutionY(int filterResolutionY);
    void setFilterRegion(Rectangle2D filterRegion);
    Rectangle2D getFilterRegion();
    void setSource(Filter src);
    Filter getSource();
}
