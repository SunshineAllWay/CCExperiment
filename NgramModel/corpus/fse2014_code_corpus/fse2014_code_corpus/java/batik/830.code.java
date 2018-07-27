package org.apache.batik.ext.awt.image.renderable;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.renderable.RenderableImage;
public interface Filter extends RenderableImage {
    Rectangle2D getBounds2D();
    long getTimeStamp();
    Shape getDependencyRegion(int srcIndex, Rectangle2D outputRgn);
    Shape getDirtyRegion(int srcIndex, Rectangle2D inputRgn);
}
