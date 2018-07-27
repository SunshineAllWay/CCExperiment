package org.apache.batik.ext.awt.geom;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
public class Quadradic extends AbstractSegment {
    public Point2D.Double p1, p2, p3;
    public Quadradic() {
        p1 = new Point2D.Double();
        p2 = new Point2D.Double();
        p3 = new Point2D.Double();
    }
    public Quadradic(double x1, double y1,
                     double x2, double y2,
                     double x3, double y3) {
        p1 = new Point2D.Double(x1, y1);
        p2 = new Point2D.Double(x2, y2);
        p3 = new Point2D.Double(x3, y3);
    }
    public Quadradic(Point2D.Double p1,
                     Point2D.Double p2,
                     Point2D.Double p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }
    public Object clone() {
        return new Quadradic(new Point2D.Double(p1.x, p1.y),
                             new Point2D.Double(p2.x, p2.y),
                             new Point2D.Double(p3.x, p3.y));
    }
    public Segment reverse() {
        return new Quadradic(new Point2D.Double(p3.x, p3.y),
                             new Point2D.Double(p2.x, p2.y),
                             new Point2D.Double(p1.x, p1.y));
    }
    private void getMinMax(double p1, double p2,
                           double p3, double [] minMax) {
        if (p3 > p1){
            minMax[0] = p1; minMax[1] = p3;
        } else {
            minMax[0] = p3; minMax[1] = p1;
        }
        double a = (p1-2*p2+p3);
        double b = (p3-p2);
        if (a == 0) return;
        double tv = b/a;
        if ((tv <= 0) || (tv >= 1)) return;
        tv = ((p1-2*p2+p3)*tv+2*(p2-p1))*tv + p1;
        if      (tv < minMax[0]) minMax[0] = tv;
        else if (tv > minMax[1]) minMax[1] = tv;
    }
    public double minX() {
        double [] minMax = {0, 0};
        getMinMax(p1.x, p2.x, p3.x, minMax);
        return minMax[0];
    }
    public double maxX() {
        double [] minMax = {0, 0};
        getMinMax(p1.x, p2.x, p3.x, minMax);
        return minMax[1];
    }
    public double minY() {
        double [] minMax = {0, 0};
        getMinMax(p1.y, p2.y, p3.y, minMax);
        return minMax[0];
    }
    public double maxY() {
        double [] minMax = {0, 0};
        getMinMax(p1.y, p2.y, p3.y, minMax);
        return minMax[1];
    }
    public Rectangle2D getBounds2D() {
        double [] minMaxX = {0, 0};
        getMinMax(p1.x, p2.x, p3.x, minMaxX);
        double [] minMaxY = {0, 0};
        getMinMax(p1.y, p2.y, p3.y, minMaxY);
        return new Rectangle2D.Double
            (minMaxX[0], minMaxY[0],
             minMaxX[1]-minMaxX[0], minMaxY[1]-minMaxY[0]);
    }
    protected int findRoots(double y, double [] roots) {
        double [] eqn = { p1.y-y, 2*(p2.y-p1.y), p1.y-2*p2.y+p3.y };
        return QuadCurve2D.solveQuadratic(eqn, roots);
    }
    public Point2D.Double evalDt(double t) {
        double x = 2*(p1.x-2*p2.x+p3.x)*t + 2*(p2.x-p1.x);
        double y = 2*(p1.y-2*p2.y+p3.y)*t + 2*(p2.y-p1.y);
        return new Point2D.Double(x, y);
    }
    public Point2D.Double eval(double t)   {
        double x = ((p1.x-2*p2.x+p3.x)*t+2*(p2.x-p1.x))*t + p1.x;
        double y = ((p1.y-2*p2.y+p3.y)*t+2*(p2.y-p1.y))*t + p1.y;
        return new Point2D.Double(x, y);
    }
    public Segment getSegment(double t0, double t1) {
        double dt = t1-t0;
        Point2D.Double np1 = eval(t0);
        Point2D.Double dp1 = evalDt(t0);
        Point2D.Double np2 = new Point2D.Double
            (np1.x+.5*dt*dp1.x, np1.y+.5*dt*dp1.y);
        Point2D.Double np3 = eval(t1);
        return new Quadradic(np1, np2, np3);
    }
    public void subdivide(Quadradic q0, Quadradic q1) {
        if ((q0 == null) && (q1 == null)) return;
        double x  = (p1.x-2*p2.x+p3.x)*.25+(p2.x-p1.x) + p1.x;
        double y  = (p1.y-2*p2.y+p3.y)*.25+(p2.y-p1.y) + p1.y;
        double dx = (p1.x-2*p2.x+p3.x)*.25 + (p2.x-p1.x)*.5;
        double dy = (p1.y-2*p2.y+p3.y)*.25 + (p2.y-p1.y)*.5;
        if (q0 != null) {
            q0.p1.x = p1.x;
            q0.p1.y = p1.y;
            q0.p2.x = x-dx;
            q0.p2.y = y-dy;
            q0.p3.x = x;
            q0.p3.y = y;
        }
        if (q1 != null) {
            q1.p1.x = x;
            q1.p1.y = y;
            q1.p2.x = x+dx;
            q1.p2.y = y+dy;
            q1.p3.x = p3.x;
            q1.p3.y = p3.y;
        }
    }
    public void subdivide(double t, Quadradic q0, Quadradic q1) {
        Point2D.Double np  = eval(t);
        Point2D.Double npd = evalDt(t);
        if (q0 != null) {
            q0.p1.x = p1.x;
            q0.p1.y = p1.y;
            q0.p2.x = np.x-(npd.x*t*.5);
            q0.p2.y = np.y-(npd.y*t*.5);
            q0.p3.x = np.x;
            q0.p3.y = np.y;
        }
        if (q1 != null) {
            q1.p1.x = np.x;
            q1.p1.y = np.y;
            q1.p2.x = np.x+(npd.x*(1-t)*.5);
            q1.p2.y = np.y+(npd.y*(1-t)*.5);
            q1.p3.x = p3.x;
            q1.p3.y = p3.y;
        }
    }
    public void subdivide(Segment s0, Segment s1) {
        Quadradic q0=null, q1=null;
        if (s0 instanceof Quadradic) q0 = (Quadradic)s0;
        if (s1 instanceof Quadradic) q1 = (Quadradic)s1;
        subdivide(q0, q1);
    }
    public void subdivide(double t, Segment s0, Segment s1) {
        Quadradic q0=null, q1=null;
        if (s0 instanceof Quadradic) q0 = (Quadradic)s0;
        if (s1 instanceof Quadradic) q1 = (Quadradic)s1;
        subdivide(t, q0, q1);
    }
    static int count = 0;
    protected double subLength(double leftLegLen, double rightLegLen,
                               double maxErr) {
        count++;
        double dx, dy;
        dx = p3.x-p1.x;
        dy = p3.y-p1.y;
        double cordLen = Math.sqrt(dx*dx+dy*dy);
        double hullLen = leftLegLen+rightLegLen;
        if (hullLen < maxErr) return (hullLen+cordLen)*.5;
        double err = (hullLen-cordLen);
        if (err < maxErr)
            return (hullLen+cordLen)*.5;
        Quadradic q  = new Quadradic();
        double x  = (p1.x+2*p2.x+p3.x)*.25;
        double y  = (p1.y+2*p2.y+p3.y)*.25;
        dx = .25*dx;
        dy = .25*dy;
        q.p1.x = p1.x;
        q.p1.y = p1.y;
        q.p2.x = x-dx;
        q.p2.y = y-dy;
        q.p3.x = x;
        q.p3.y = y;
        double midLen = .25*cordLen;
        double len = q.subLength(leftLegLen*.5, midLen, maxErr*.5);
        q.p1.x = x;
        q.p1.y = y;
        q.p2.x = x+dx;
        q.p2.y = y+dy;
        q.p3.x = p3.x;
        q.p3.y = p3.y;
        len += q.subLength(midLen, rightLegLen*.5, maxErr*.5);
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
        dx = p3.x-p2.x;
        dy = p3.y-p2.y;
        double rightLegLen = Math.sqrt(dx*dx+dy*dy);
        double eps = maxErr*(leftLegLen+rightLegLen);
        return subLength(leftLegLen, rightLegLen, eps);
    }
    public String toString() {
        return "M" + p1.x + ',' + p1.y +
               'Q' + p2.x + ',' + p2.y + ' ' +
                p3.x + ',' + p3.y;
    }
}
