package org.apache.batik.ext.awt.image.rendered;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.RenderedImage;
public interface CachableRed extends RenderedImage {
    Rectangle getBounds();
    Shape getDependencyRegion(int srcIndex, Rectangle outputRgn);
    Shape getDirtyRegion(int srcIndex, Rectangle inputRgn);
}
