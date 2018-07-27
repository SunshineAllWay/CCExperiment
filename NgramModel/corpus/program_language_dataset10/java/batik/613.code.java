package org.apache.batik.dom.svg;
import java.awt.geom.AffineTransform;
public class SVGOMMatrix extends AbstractSVGMatrix {
    protected AffineTransform affineTransform;
    public SVGOMMatrix(AffineTransform at) {
        affineTransform = at;
    }
    protected AffineTransform getAffineTransform() {
        return affineTransform;
    }
}
