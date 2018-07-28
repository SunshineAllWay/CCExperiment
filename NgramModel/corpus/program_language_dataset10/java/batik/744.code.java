package org.apache.batik.ext.awt.geom;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
public interface Segment extends Cloneable {
    double minX();
    double maxX();
    double minY();
    double maxY();
    Rectangle2D getBounds2D();
    Point2D.Double evalDt(double t);
    Point2D.Double eval(double t);
    Segment getSegment(double t0, double t1);
    Segment splitBefore(double t);
    Segment splitAfter(double t);
    void    subdivide(Segment s0, Segment s1);
    void    subdivide(double t, Segment s0, Segment s1);
    double  getLength();
    double  getLength(double maxErr);
    SplitResults split(double y);
    class SplitResults {
        Segment [] above;
        Segment [] below;
        SplitResults(Segment []below, Segment []above) {
            this.below = below;
            this.above = above;
        }
        Segment [] getBelow() {
            return below;
        }
        Segment [] getAbove() {
            return above;
        }
    }
}
