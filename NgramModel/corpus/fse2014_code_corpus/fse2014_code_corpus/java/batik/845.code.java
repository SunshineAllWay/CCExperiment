package org.apache.batik.ext.awt.image.renderable;
import java.awt.geom.Rectangle2D;
import org.apache.batik.ext.awt.image.PadMode;
public interface PadRable extends Filter {
    Filter getSource();
    void setSource(Filter src);
    void setPadRect(Rectangle2D rect);
    Rectangle2D getPadRect();
    void setPadMode(PadMode mode);
    PadMode getPadMode();
}
