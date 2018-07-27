package org.apache.batik.gvt.filter;
import java.awt.geom.Rectangle2D;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.GraphicsNode;
public interface Mask extends Filter {
    Rectangle2D getFilterRegion();
    void setFilterRegion(Rectangle2D filterRegion);
    void setSource(Filter src);
    Filter getSource();
    void setMaskNode(GraphicsNode gn);
    GraphicsNode getMaskNode();
}
