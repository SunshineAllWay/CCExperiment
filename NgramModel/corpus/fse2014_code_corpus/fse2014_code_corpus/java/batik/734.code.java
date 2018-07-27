package org.apache.batik.ext.awt.geom;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
public class Cubic extends AbstractSegment {
    public Point2D.Double p1, p2, p3, p4;
    public Cubic() {
        p1 = new Point2D.Double();
        p2 = new Point2D.Double();
        p3 = new Point2D.Double();
        p4 = new Point2D.Double();
    }
    public Cubic(double x1, double y1,  double x2, double y2,
                 double x3, double y3,  double x4, double y4) {
        p1 = new Point2D.Double(x1, y1);
        p2 = new Point2D.Double(x2, y2);
        p3 = new Point2D.Double(x3, y3);
        p4 = new Point2D.Double(x4, y4);
    }
    public Cubic(Point2D.Double p1, Point2D.Double p2,
                 Point2D.Double p3, Point2D.Double p4) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
    }
    public Object clone() {
        return new Cubic(new Point2D.Double(p1.x, p1.y),
                         new Point2D.Double(p2.x, p2.y),
                         new Point2D.Double(p3.x, p3.y),
                         new Point2D.Double(p4.x, p4.y));
    }
    public Segment reverse() {
        return new Cubic(new Point2D.Double(p4.x, p4.y),
                         new Point2D.Double(p3.x, p3.y),
                         new Point2D.Double(p2.x, p2.y),
                         new Point2D.Double(p1.x, p1.y));
    }
    private void getMinMax(double p1, double p2,
                           double p3, double p4,
                           double [] minMax) {
        if (p4 > p1){
            minMax[0] = p1; minMax[1] = p4;
        } else {
            minMax[0] = p4; minMax[1] = p1;
        }
        double c0 = 3*(p2-p1);
        double c1 = 6*(p3-p2);
        double c2 = 3*(p4-p3);
        double [] eqn = { c0, c1-2*c0, c2-c1+c0 };
        int roots = QuadCurve2D.solveQuadratic(eqn);
        for (int r=0; r<roots; r++) {
            double tv = eqn[r];
            if ((tv <= 0) || (tv >= 1)) continue;
            tv = ((1-tv)*(1-tv)*(1-tv)*p1 +
                    3*tv*(1-tv)*(1-tv)*p2 +
                    3*tv*tv*(1-tv)*p3 +
                    tv*tv*tv*p4);
            if      (tv < minMax[0]) minMax[0] = tv;
            else if (tv > minMax[1]) minMax[1] = tv;
        }
    }
    public double minX() {
        double [] minMax = {0, 0};
        getMinMax(p1.x, p2.x, p3.x, p4.x, minMax);
        return minMax[0];
    }
    public double maxX() {
        double [] minMax = {0, 0};
        getMinMax(p1.x, p2.x, p3.x, p4.x, minMax);
        return minMax[1];
    }
    public double minY() {
        double [] minMax = {0, 0};
        getMinMax(p1.y, p2.y, p3.y, p4.y, minMax);
        return minMax[0];
    }
    public double maxY() {
        double [] minMax = {0, 0};
        getMinMax(p1.y, p2.y, p3.y, p4.y, minMax);
        return minMax[1];
    }
    public Rectangle2D getBounds2D() {
        double [] minMaxX = {0, 0};
        getMinMax(p1.x, p2.x, p3.x, p4.x, minMaxX);
        double [] minMaxY = {0, 0};
        getMinMax(p1.y, p2.y, p3.y, p4.y, minMaxY);
        return new Rectangle2D.Double
            (minMaxX[0], minMaxY[0],
             minMaxX[1]-minMaxX[0], minMaxY[1]-minMaxY[0]);
    }
    protected int findRoots(double y, double [] roots) {
        double [] eqn = { p1.y-y, 3*(p2.y-p1.y), 3*(p1.y-2*p2.y+p3.y),
                          3*p2.y-p1.y+p4.y-3*p3.y };
        return CubicCurve2D.solveCubic(eqn, roots);
    }
    public Point2D.Double evalDt(double t) {
        double x = 3*(  (p2.x-p1.x)*(1-t)*(1-t) +
                      2*(p3.x-p2.x)*(1-t)*t +
                        (p4.x-p3.x)*t*t);
        double y = 3*(  (p2.y-p1.y)*(1-t)*(1-t) +
                      2*(p3.y-p2.y)*(1-t)*t +
                        (p4.y-p3.y)*t*t);
        return new Point2D.Double(x, y);
    }
    public Point2D.Double eval(double t) {
        double x = ((1-t)*(1-t)*(1-t)*p1.x +
                    3*(t* (1-t)*(1-t)*p2.x +
                       t* t*    (1-t)*p3.x) +
                    t*t*t            *p4.x);
        double y = ((1-t)*(1-t)*(1-t)*p1.y +
                    3*(t* (1-t)*(1-t)*p2.y +
                       t* t*    (1-t)*p3.y) +
                    t*t*t            *p4.y);
        return new Point2D.Double(x, y);
    }
    public void subdivide(Segment s0, Segment s1) {
        Cubic c0=null, c1=null;
        if (s0 instanceof Cubic) c0 = (Cubic)s0;
        if (s1 instanceof Cubic) c1 = (Cubic)s1;
        subdivide(c0, c1);
    }
    public void subdivide(double t, Segment s0, Segment s1) {
        Cubic c0=null, c1=null;
        if (s0 instanceof Cubic) c0 = (Cubic)s0;
        if (s1 instanceof Cubic) c1 = (Cubic)s1;
        subdivide(t, c0, c1);
    }
    public void subdivide(Cubic c0, Cubic c1) {
        if ((c0 == null) && (c1 == null)) return;
        double npX = (p1.x+3*(p2.x+p3.x)+p4.x)*0.125;
        double npY = (p1.y+3*(p2.y+p3.y)+p4.y)*0.125;
        double npdx = ((p2.x-p1.x)+2*(p3.x-p2.x)+(p4.x-p3.x))*0.125;
        double npdy = ((p2.y-p1.y)+2*(p3.y-p2.y)+(p4.y-p3.y))*0.125;
        if (c0 != null) {
            c0.p1.x = p1.x;
            c0.p1.y = p1.y;
            c0.p2.x = (p2.x+p1.x)*0.5;
            c0.p2.y = (p2.y+p1.y)*0.5;
            c0.p3.x = npX-npdx;
            c0.p3.y = npY-npdy;
            c0.p4.x = npX;
            c0.p4.y = npY;
        }
        if (c1 != null) {
            c1.p1.x = npX;
            c1.p1.y = npY;
            c1.p2.x = npX+npdx;
            c1.p2.y = npY+npdy;
            c1.p3.x = (p4.x+p3.x)*0.5;
            c1.p3.y = (p4.y+p3.y)*0.5;
            c1.p4.x = p4.x;
            c1.p4.y = p4.y;
        }
    }
    public void subdivide(double t, Cubic c0, Cubic c1) {
        if ((c0 == null) && (c1 == null)) return;
        Point2D.Double np  = eval(t);
        Point2D.Double npd = evalDt(t);
        if (c0 != null) {
            c0.p1.x = p1.x;
            c0.p1.y = p1.y;
            c0.p2.x = p1.x + (p2.x-p1.x)*t;
            c0.p2.y = p1.y + (p2.y-p1.y)*t;
            c0.p3.x = np.x-npd.x*t/3;
            c0.p3.y = np.y-npd.y*t/3;
            c0.p4.x = np.x;
            c0.p4.y = np.y;
        }
        if (c1 != null) {
            c1.p1.x = np.x;
            c1.p1.y = np.y;
            c1.p2.x = np.x+(npd.x*(1-t)/3);
            c1.p2.y = np.y+(npd.y*(1-t)/3);
            c1.p3.x = p4.x + (p3.x-p4.x)*(1-t);
            c1.p3.y = p4.y + (p3.y-p4.y)*(1-t);
            c1.p4.x = p4.x;
            c1.p4.y = p4.y;
        }
    }
    public Segment getSegment(double t0, double t1) {
        double dt = t1-t0;
        Point2D.Double np1 = eval(t0);
        Point2D.Double dp1 = evalDt(t0);
        Point2D.Double np2 = new Point2D.Double(np1.x+dt*dp1.x/3,
                                                np1.y+dt*dp1.y/3);
        Point2D.Double np4 = eval(t1);
        Point2D.Double dp4 = evalDt(t1);
        Point2D.Double np3 = new Point2D.Double(np4.x-dt*dp4.x/3,
                                                np4.y-dt*dp4.y/3);
        return new Cubic(np1, np2, np3, np4);
    }
    private static int count = 0;
    protected double subLength(double leftLegLen, double rightLegLen,
                               double maxErr) {
        count++;
        double cldx, cldy, cdx, cdy;
        cldx = p3.x-p2.x;
        cldy = p3.y-p2.y;
        double crossLegLen = Math.sqrt(cldx*cldx+cldy*cldy);
        cdx = p4.x-p1.x;
        cdy = p4.y-p1.y;
        double cordLen = Math.sqrt(cdx*cdx+cdy*cdy);
        double hullLen = leftLegLen+rightLegLen+crossLegLen;
        if (hullLen < maxErr) return (hullLen+cordLen)/2;
        double err = (hullLen-cordLen);
        if (err < maxErr)
            return (hullLen+cordLen)/2;
        Cubic c  = new Cubic();
        double npX = (p1.x+3*(p2.x+p3.x)+p4.x)*0.125;
        double npY = (p1.y+3*(p2.y+p3.y)+p4.y)*0.125;
        double npdx = (cldx + cdx)*.125;
        double npdy = (cldy + cdy)*.125;
        c.p1.x = p1.x;
        c.p1.y = p1.y;
        c.p2.x = (p2.x+p1.x)*.5;
        c.p2.y = (p2.y+p1.y)*.5;
        c.p3.x = npX-npdx;
        c.p3.y = npY-npdy;
        c.p4.x = npX;
        c.p4.y = npY;
        double midLen = Math.sqrt(npdx*npdx+npdy*npdy);
        double len = c.subLength(leftLegLen/2, midLen, maxErr/2);
        c.p1.x = npX;
        c.p1.y = npY;
        c.p2.x = npX+npdx;
        c.p2.y = npY+npdy;
        c.p3.x = (p4.x+p3.x)*.5;
        c.p3.y = (p4.y+p3.y)*.5;
        c.p4.x = p4.x;
        c.p4.y = p4.y;
        len += c.subLength(midLen, rightLegLen/2, maxErr/2);
        return len;
    }
    public double getLength() {
        return getLength(0.000001);
    }
    public double getLength(double maxErr) {
        double dx, dy;
        dx = p2.x-p1.x;
        dy = p2.y-p1.y;
        double leftLegLen = Math.sqrt(dx*dx+dy*dy);
        dx = p4.x-p3.x;
        dy = p4.y-p3.y;
        double rightLegLen = Math.sqrt(dx*dx+dy*dy);
        dx = p3.x-p2.x;
        dy = p3.y-p2.y;
        double crossLegLen = Math.sqrt(dx*dx+dy*dy);
        double eps = maxErr*(leftLegLen+rightLegLen+crossLegLen);
        return subLength(leftLegLen, rightLegLen, eps);
    }
    public String toString() {
        return "M" + p1.x + ',' + p1.y +
                'C' + p2.x + ',' + p2.y + ' ' +
                p3.x + ',' + p3.y + ' ' +
                p4.x + ',' + p4.y;
    }
}
