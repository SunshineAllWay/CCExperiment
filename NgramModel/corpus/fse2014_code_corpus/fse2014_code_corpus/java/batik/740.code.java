package org.apache.batik.ext.awt.geom;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Polygon;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
public class Polygon2D implements Shape, Cloneable, Serializable {
    public int npoints;
    public float[] xpoints;
    public float[] ypoints;
    protected Rectangle2D bounds;
    private GeneralPath path;
    private GeneralPath closedPath;
    public Polygon2D() {
        xpoints = new float[4];
        ypoints = new float[4];
    }
    public Polygon2D(Rectangle2D rec) {
        if (rec == null) {
            throw new IndexOutOfBoundsException("null Rectangle");
        }
        npoints = 4;
        xpoints = new float[4];
        ypoints = new float[4];
        xpoints[0] = (float)rec.getMinX();
        ypoints[0] = (float)rec.getMinY();
        xpoints[1] = (float)rec.getMaxX();
        ypoints[1] = (float)rec.getMinY();
        xpoints[2] = (float)rec.getMaxX();
        ypoints[2] = (float)rec.getMaxY();
        xpoints[3] = (float)rec.getMinX();
        ypoints[3] = (float)rec.getMaxY();
        calculatePath();
    }
    public Polygon2D(Polygon pol) {
        if (pol == null) {
            throw new IndexOutOfBoundsException("null Polygon");
        }
        this.npoints = pol.npoints;
        this.xpoints = new float[pol.npoints];
        this.ypoints = new float[pol.npoints];
        for (int i = 0; i < pol.npoints; i++) {
            xpoints[i] = pol.xpoints[i];
            ypoints[i] = pol.ypoints[i];
        }
        calculatePath();
    }
    public Polygon2D(float[] xpoints, float[] ypoints, int npoints) {
        if (npoints > xpoints.length || npoints > ypoints.length) {
            throw new IndexOutOfBoundsException("npoints > xpoints.length || npoints > ypoints.length");
        }
        this.npoints = npoints;
        this.xpoints = new float[npoints];
        this.ypoints = new float[npoints];
        System.arraycopy(xpoints, 0, this.xpoints, 0, npoints);
        System.arraycopy(ypoints, 0, this.ypoints, 0, npoints);
        calculatePath();
    }
    public Polygon2D(int[] xpoints, int[] ypoints, int npoints) {
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
    public void reset() {
        npoints = 0;
        bounds = null;
        path = new GeneralPath();
        closedPath = null;
    }
    public Object clone() {
        Polygon2D pol = new Polygon2D();
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
    public Polyline2D getPolyline2D() {
        Polyline2D pol = new Polyline2D( xpoints, ypoints, npoints );
        pol.addPoint( xpoints[0], ypoints[0]);
        return pol;
    }
    public Polygon getPolygon() {
        int[] _xpoints = new int[npoints];
        int[] _ypoints = new int[npoints];
        for (int i = 0; i < npoints; i++) {
            _xpoints[i] = (int)xpoints[i];     
            _ypoints[i] = (int)ypoints[i];
        }
        return new Polygon(_xpoints, _ypoints, npoints);
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
    public boolean contains(Point p) {
        return contains(p.x, p.y);
    }
    public boolean contains(int x, int y) {
        return contains((double) x, (double) y);
    }
    public Rectangle2D getBounds2D() {
        return bounds;
    }
    public Rectangle getBounds() {
        if (bounds == null) return null;
        else return bounds.getBounds();
    }
    public boolean contains(double x, double y) {
        if (npoints <= 2 || !bounds.contains(x, y)) {
            return false;
        }
        updateComputingPath();
        return closedPath.contains(x, y);
    }
    private void updateComputingPath() {
        if (npoints >= 1) {
            if (closedPath == null) {
                closedPath = (GeneralPath)path.clone();
                closedPath.closePath();
            }
        }
    }
    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
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
        if (npoints <= 0 || !bounds.intersects(x, y, w, h)) {
            return false;
        }
        updateComputingPath();
        return closedPath.contains(x, y, w, h);
    }
    public boolean contains(Rectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }
    public PathIterator getPathIterator(AffineTransform at) {
        updateComputingPath();
        if (closedPath == null) return null;
        else return closedPath.getPathIterator(at);
    }
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return getPathIterator(at);
    }
}
