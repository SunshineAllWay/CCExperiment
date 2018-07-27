package org.apache.batik.ext.awt.geom;
import java.awt.geom.PathIterator;
public interface ExtendedPathIterator {
    int SEG_CLOSE   = PathIterator.SEG_CLOSE;
    int SEG_MOVETO  = PathIterator.SEG_MOVETO;
    int SEG_LINETO  = PathIterator.SEG_LINETO;
    int SEG_QUADTO  = PathIterator.SEG_QUADTO;
    int SEG_CUBICTO = PathIterator.SEG_CUBICTO;
    int SEG_ARCTO = 4321;
    int WIND_EVEN_ODD = PathIterator.WIND_EVEN_ODD; 
    int WIND_NON_ZERO = PathIterator.WIND_NON_ZERO;
    int currentSegment();
    int currentSegment(double[] coords);
    int currentSegment(float[] coords);
    int getWindingRule(); 
    boolean isDone();
    void next();
}
