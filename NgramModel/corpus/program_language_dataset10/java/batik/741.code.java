package org.apache.batik.ext.awt.geom;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
public class Polyline2D implements Shape, Cloneable, Serializable {
    private static final float ASSUME_ZERO = 0.001f;
    public int npoints;
    public float[] xpoints;
    public float[] ypoints;
    protected Rectangle2D bounds;
    private GeneralPath path;
    private GeneralPath closedPath;
    public Polyline2D() {
        xpoints = new float[4];
        ypoints = new float[4];
    }
    public Polyline2D(float[] xpoints, float[] ypoints, int npoints) {
        if (npoints > xpoints.length || npoints > ypoints.length) {
            throw new IndexOutOfBoundsException("npoints > xpoints.length || npoints > ypoints.length");
        }
        this.npoints = npoints;
        this.xpoints = new float[npoints+1];   
        this.ypoints = new float[npoints+1];   
        System.arraycopy(xpoints, 0, this.xpoints, 0, npoints);
        System.arraycopy(ypoints, 0, this.ypoints, 0, npoints);
        calculatePath();
    }
    public Polyline2D(int[] xpoints, int[] ypoints, int npoints) {
        if (npoints > xpoints.length || npoints > ypoints.length) {
            throw new IndexOutOfBoundsException("npoints > xpoints.length || npoints > ypoints.length");
        }
        this.npoints = npoints;
        this.xpoints = new float[npoints];
        this.ypoints = new float[npoints];
        for (int i = 0; i < npoints; i++) {
            this.xpoints[i] = xpoints[i];
            this.ypoints[i] = ypoints[i];
        }
        calculatePath();
    }
    public Polyline2D(Line2D line) {
        npoints = 2;
        xpoints = new float[2];
        ypoints = new float[2];
        xpoints[0] = (float)line.getX1();
        xpoints[1] = (float)line.getX2();
        ypoints[0] = (float)line.getY1();
        ypoints[1] = (float)line.getY2();
        calculatePath();
    }
    public void reset() {
        npoints = 0;
        bounds = null;
        path = new GeneralPath();
        closedPath = null;
    }
    public Object clone() {
        Polyline2D pol = new Polyline2D();
        for (int i = 0; i < npoints; i++) {
            pol.addPoint(xpoints[i], ypoints[i]);
        }
        return pol;
    }
    private void calculatePath() {
        path = new GeneralPath();
        path.moveTo(xpoints[0], ypoints[0]);
        for (int i = 1; i < npoints; i++) {
            path.lineTo(xpoints[i], ypoints[i]);
        }
        bounds = path.getBounds2D();
        closedPath = null;
    }
    private void updatePath(float x, float y) {
        closedPath = null;
        if (path == null) {
            path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
            path.moveTo(x, y);
            bounds = new Rectangle2D.Float(x, y, 0, 0);
        } else {
            path.lineTo(x, y);
            float _xmax = (float)bounds.getMaxX();
            float _ymax = (float)bounds.getMaxY();
            float _xmin = (float)bounds.getMinX();
            float _ymin = (float)bounds.getMinY();
            if (x < _xmin) _xmin = x;
            else if (x > _xmax) _xmax = x;
            if (y < _ymin) _ymin = y;
            else if (y > _ymax) _ymax = y;
            bounds = new Rectangle2D.Float(_xmin, _ymin, _xmax - _xmin, _ymax - _ymin);
        }
    }
    public void addPoint(Point2D p) {
        addPoint((float)p.getX(), (float)p.getY());
    }
    public void addPoint(float x, float y) {
        if (npoints == xpoints.length) {
            float[] tmp;
            tmp = new float[npoints * 2];
            System.arraycopy(xpoints, 0, tmp, 0, npoints);
            xpoints = tmp;
            tmp = new float[npoints * 2];
            System.arraycopy(ypoints, 0, tmp, 0, npoints);
            ypoints = tmp;
        }
        xpoints[npoints] = x;
        ypoints[npoints] = y;
        npoints++;
        updatePath(x, y);
    }
    public Rectangle getBounds() {
        if (bounds == null) return null;
        else return bounds.getBounds();
    }
    private void updateComputingPath() {
        if (npoints >= 1) {
            if (closedPath == null) {
                closedPath = (GeneralPath)path.clone();
                closedPath.closePath();
            }
        }
    }
    public boolean contains(Point p) {
        return false;
    }
    public boolean contains(double x, double y) {
        return false;
    }
    public boolean contains(int x, int y) {
        return false;
    }
    public Rectangle2D getBounds2D() {
        return bounds;
    }
    public boolean contains(Point2D p) {
        return false;
    }
    public boolean intersects(double x, double y, double w, double h) {
        if (npoints <= 0 || !bounds.intersects(x, y, w, h)) {
            return false;
        }
        updateComputingPath();
        return closedPath.intersects(x, y, w, h);
    }
    public boolean intersects(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }
    public boolean contains(double x, double y, double w, double h) {
        return false;
    }
    public boolean contains(Rectangle2D r) {
        return false;
    }
    public PathIterator getPathIterator(AffineTransform at) {
        if (path == null) return null;
        else return path.getPathIterator(at);
    }
    public Polygon2D getPolygon2D() {
        Polygon2D pol = new Polygon2D();
        for (int i = 0; i < npoints - 1; i++) {
           pol.addPoint(xpoints[i], ypoints[i]);
        }
        Point2D.Double p0 =
            new Point2D.Double(xpoints[0], ypoints[0]);
        Point2D.Double p1 =
            new Point2D.Double(xpoints[npoints-1], ypoints[npoints-1]);
        if (p0.distance(p1) > ASSUME_ZERO)
            pol.addPoint(xpoints[npoints-1], ypoints[npoints-1]);
        return pol;
    }
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return path.getPathIterator(at);
    }
}
