package org.apache.batik.gvt;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.ext.awt.geom.ExtendedPathIterator;
import org.apache.batik.ext.awt.geom.ExtendedShape;
import org.apache.batik.ext.awt.geom.ShapeExtender;
public class MarkerShapePainter implements ShapePainter {
    protected ExtendedShape extShape;
    protected Marker startMarker;
    protected Marker middleMarker;
    protected Marker endMarker;
    private ProxyGraphicsNode startMarkerProxy;
    private ProxyGraphicsNode[] middleMarkerProxies;
    private ProxyGraphicsNode endMarkerProxy;
    private CompositeGraphicsNode markerGroup;
    private Rectangle2D dPrimitiveBounds;
    private Rectangle2D dGeometryBounds;
    public MarkerShapePainter(Shape shape) {
        if (shape == null) {
            throw new IllegalArgumentException();
        }
        if (shape instanceof ExtendedShape) {
            this.extShape = (ExtendedShape)shape;
        } else {
            this.extShape = new ShapeExtender(shape);
        }
    }
     public void paint(Graphics2D g2d) {
         if (markerGroup == null) {
             buildMarkerGroup();
         }
         if (markerGroup.getChildren().size() > 0) {
             markerGroup.paint(g2d);
         }
     }
    public Shape getPaintedArea(){
         if (markerGroup == null) {
             buildMarkerGroup();
         }
        return markerGroup.getOutline();
    }
    public Rectangle2D getPaintedBounds2D(){
         if (markerGroup == null) {
             buildMarkerGroup();
         }
         return markerGroup.getPrimitiveBounds();
    }
    public boolean inPaintedArea(Point2D pt){
         if (markerGroup == null) {
             buildMarkerGroup();
         }
         GraphicsNode gn = markerGroup.nodeHitAt(pt);
         return (gn != null);
    }
    public Shape getSensitiveArea() { return null; }
    public Rectangle2D getSensitiveBounds2D() { return null; }
    public boolean inSensitiveArea(Point2D pt) { return false; }
    public void setShape(Shape shape){
        if (shape == null) {
            throw new IllegalArgumentException();
        }
        if (shape instanceof ExtendedShape) {
            this.extShape = (ExtendedShape)shape;
        } else {
            this.extShape = new ShapeExtender(shape);
        }
        this.startMarkerProxy = null;
        this.middleMarkerProxies = null;
        this.endMarkerProxy = null;
        this.markerGroup = null;
    }
    public ExtendedShape getExtShape(){
        return extShape;
    }
    public Shape getShape(){
        return extShape;
    }
    public Marker getStartMarker(){
        return startMarker;
    }
    public void setStartMarker(Marker startMarker){
        this.startMarker = startMarker;
        this.startMarkerProxy = null;
        this.markerGroup = null;
    }
    public Marker getMiddleMarker(){
        return middleMarker;
    }
    public void setMiddleMarker(Marker middleMarker){
        this.middleMarker = middleMarker;
        this.middleMarkerProxies = null;
        this.markerGroup = null;
    }
    public Marker getEndMarker(){
        return endMarker;
    }
    public void setEndMarker(Marker endMarker){
        this.endMarker = endMarker;
        this.endMarkerProxy = null;
        this.markerGroup = null;
    }
    protected void buildMarkerGroup(){
        if (startMarker != null && startMarkerProxy == null) {
            startMarkerProxy = buildStartMarkerProxy();
        }
        if (middleMarker != null && middleMarkerProxies == null) {
            middleMarkerProxies = buildMiddleMarkerProxies();
        }
        if (endMarker != null && endMarkerProxy == null) {
            endMarkerProxy = buildEndMarkerProxy();
        }
        CompositeGraphicsNode group = new CompositeGraphicsNode();
        List children = group.getChildren();
        if (startMarkerProxy != null) {
            children.add(startMarkerProxy);
        }
        if (middleMarkerProxies != null) {
            for(int i=0; i<middleMarkerProxies.length; i++){
                children.add(middleMarkerProxies[i]);
            }
        }
        if (endMarkerProxy != null) {
            children.add(endMarkerProxy);
        }
        markerGroup = group;
    }
    protected ProxyGraphicsNode buildStartMarkerProxy() {
        ExtendedPathIterator iter = getExtShape().getExtendedPathIterator();
        double[] coords = new double[7];
        int segType = 0;
        if (iter.isDone()) {
            return null;
        }
        segType = iter.currentSegment(coords);
        if (segType != ExtendedPathIterator.SEG_MOVETO) {
            return null;
        }
        iter.next();
        Point2D markerPosition = new Point2D.Double(coords[0], coords[1]);
        double rotation = startMarker.getOrient();
        if (Double.isNaN(rotation)) {
            if (!iter.isDone()) {
                double[] next = new double[7];
                int nextSegType = 0;
                nextSegType = iter.currentSegment(next);
                if(nextSegType == PathIterator.SEG_CLOSE){
                    nextSegType = PathIterator.SEG_LINETO;
                    next[0] = coords[0];
                    next[1] = coords[1];
                }
                rotation = computeRotation(null, 0,  
                                           coords, segType,    
                                           next, nextSegType); 
            }
        }
        AffineTransform markerTxf = computeMarkerTransform(startMarker,
                                                           markerPosition,
                                                           rotation);
        ProxyGraphicsNode gn = new ProxyGraphicsNode();
        gn.setSource(startMarker.getMarkerNode());
        gn.setTransform(markerTxf);
        return gn;
    }
    protected ProxyGraphicsNode buildEndMarkerProxy() {
        ExtendedPathIterator iter = getExtShape().getExtendedPathIterator();
        int nPoints = 0;
        if (iter.isDone()) {
            return null;
        }
        double[] coords = new double[7];
        double[] moveTo = new double[2];
        int segType = 0;
        segType = iter.currentSegment(coords);
        if (segType != ExtendedPathIterator.SEG_MOVETO) {
            return null;
        }
        nPoints++;
        moveTo[0] = coords[0];
        moveTo[1] = coords[1];
        iter.next();
        double[] lastButOne = new double[7];
        double[] last = {coords[0], coords[1], coords[2],
                         coords[3], coords[4], coords[5], coords[6] };
        double[] tmp = null;
        int lastSegType = segType;
        int lastButOneSegType = 0;
        while (!iter.isDone()) {
            tmp = lastButOne;
            lastButOne = last;
            last = tmp;
            lastButOneSegType = lastSegType;
            lastSegType = iter.currentSegment(last);
            if (lastSegType == PathIterator.SEG_MOVETO) {
                moveTo[0] = last[0];
                moveTo[1] = last[1];
            } else if (lastSegType == PathIterator.SEG_CLOSE) {
                lastSegType = PathIterator.SEG_LINETO;
                last[0] = moveTo[0];
                last[1] = moveTo[1];
            }
            iter.next();
            nPoints++;
        }
        if (nPoints < 2) {
            return null;
        }
        Point2D markerPosition = getSegmentTerminatingPoint(last, lastSegType);
        double rotation = endMarker.getOrient();
        if (Double.isNaN(rotation)) {
            rotation = computeRotation(lastButOne,
                                       lastButOneSegType,
                                       last, lastSegType,
                                       null, 0);
        }
        AffineTransform markerTxf = computeMarkerTransform(endMarker,
                                                           markerPosition,
                                                           rotation);
        ProxyGraphicsNode gn = new ProxyGraphicsNode();
        gn.setSource(endMarker.getMarkerNode());
        gn.setTransform(markerTxf);
        return gn;
    }
    protected ProxyGraphicsNode[] buildMiddleMarkerProxies() {
        ExtendedPathIterator iter = getExtShape().getExtendedPathIterator();
        double[] prev = new double[7];
        double[] curr = new double[7];
        double[] next = new double[7], tmp = null;
        int prevSegType = 0, currSegType = 0, nextSegType = 0;
        if (iter.isDone()) {
            return null;
        }
        prevSegType = iter.currentSegment(prev);
        double[] moveTo = new double[2];
        if (prevSegType != PathIterator.SEG_MOVETO) {
            return null;
        }
        moveTo[0] = prev[0];
        moveTo[1] = prev[1];
        iter.next();
        if (iter.isDone()) {
            return null;
        }
        currSegType = iter.currentSegment(curr);
        if (currSegType == PathIterator.SEG_MOVETO) {
            moveTo[0] = curr[0];
            moveTo[1] = curr[1];
        } else if (currSegType == PathIterator.SEG_CLOSE) {
            currSegType = PathIterator.SEG_LINETO;
            curr[0] = moveTo[0];
            curr[1] = moveTo[1];
        }
        iter.next();
        List proxies = new ArrayList();
        while (!iter.isDone()) {
            nextSegType = iter.currentSegment(next);
            if (nextSegType == PathIterator.SEG_MOVETO) {
                moveTo[0] = next[0];
                moveTo[1] = next[1];
            } else if (nextSegType == PathIterator.SEG_CLOSE) {
                nextSegType = PathIterator.SEG_LINETO;
                next[0] = moveTo[0];
                next[1] = moveTo[1];
            }
            proxies.add(createMiddleMarker(prev, prevSegType,
                                                  curr, currSegType,
                                                  next, nextSegType));
            tmp = prev;
            prev = curr;
            prevSegType = currSegType;
            curr = next;
            currSegType = nextSegType;
            next = tmp;
            iter.next();
        }
        ProxyGraphicsNode [] gn = new ProxyGraphicsNode[proxies.size()];
        proxies.toArray( gn );
        return gn;
    }
    private ProxyGraphicsNode createMiddleMarker
        (double[] prev, int prevSegType,
         double[] curr, int currSegType,
         double[] next, int nextSegType) {
        Point2D markerPosition = getSegmentTerminatingPoint(curr, currSegType);
        double rotation = middleMarker.getOrient();
        if (Double.isNaN(rotation)) {
            rotation = computeRotation(prev, prevSegType,
                                       curr, currSegType,
                                       next, nextSegType);
        }
        AffineTransform markerTxf = computeMarkerTransform(middleMarker,
                                                           markerPosition,
                                                           rotation);
        ProxyGraphicsNode gn = new ProxyGraphicsNode();
        gn.setSource(middleMarker.getMarkerNode());
        gn.setTransform(markerTxf);
        return gn;
    }
    private double computeRotation(double[] prev, int prevSegType,
                                   double[] curr, int currSegType,
                                   double[] next, int nextSegType){
        double[] inSlope = computeInSlope(prev, prevSegType,
                                          curr, currSegType);
        double[] outSlope = computeOutSlope(curr, currSegType,
                                            next, nextSegType);
        if (inSlope == null) {
            inSlope = outSlope;
        }
        if (outSlope == null) {
            outSlope = inSlope;
        }
        if (inSlope == null) {
            return 0;
        }
        double dx = inSlope[0] + outSlope[0];
        double dy = inSlope[1] + outSlope[1];
        if (dx == 0 && dy == 0) {
            return Math.toDegrees( Math.atan2(inSlope[1], inSlope[0]) ) + 90;
        } else {
            return Math.toDegrees( Math.atan2(dy, dx) );
        }
    }
    private double[] computeInSlope(double[] prev, int prevSegType,
                                    double[] curr, int currSegType){
        Point2D currEndPoint = getSegmentTerminatingPoint(curr, currSegType);
        double dx = 0;
        double dy = 0;
        switch(currSegType){
        case PathIterator.SEG_LINETO: {
            Point2D prevEndPoint =
                getSegmentTerminatingPoint(prev, prevSegType);
            dx = currEndPoint.getX() - prevEndPoint.getX();
            dy = currEndPoint.getY() - prevEndPoint.getY();
        }
            break;
        case PathIterator.SEG_QUADTO:
            dx = currEndPoint.getX() - curr[0];
            dy = currEndPoint.getY() - curr[1];
            break;
        case PathIterator.SEG_CUBICTO:
            dx = currEndPoint.getX() - curr[2];
            dy = currEndPoint.getY() - curr[3];
            break;
        case ExtendedPathIterator.SEG_ARCTO: {
            Point2D prevEndPoint =
                getSegmentTerminatingPoint(prev, prevSegType);
            boolean large   = (curr[3]!=0.);
            boolean goLeft = (curr[4]!=0.);
            Arc2D arc = ExtendedGeneralPath.computeArc
                (prevEndPoint.getX(), prevEndPoint.getY(),
                 curr[0], curr[1], curr[2],
                 large, goLeft, curr[5], curr[6]);
            double theta = arc.getAngleStart()+arc.getAngleExtent();
            theta = Math.toRadians(theta);
            dx = -arc.getWidth()/2.0*Math.sin(theta);
            dy = arc.getHeight()/2.0*Math.cos(theta);
            if (curr[2] != 0) {
                double ang = Math.toRadians(-curr[2]);
                double sinA = Math.sin(ang);
                double cosA = Math.cos(ang);
                double tdx = dx*cosA - dy*sinA;
                double tdy = dx*sinA + dy*cosA;
                dx = tdx;
                dy = tdy;
            }
            if (goLeft) {
                dx = -dx;
            } else {
                dy = -dy;
            }
        }
            break;
        case PathIterator.SEG_CLOSE:
            throw new Error("should not have SEG_CLOSE here");
        case PathIterator.SEG_MOVETO:
        default:
            return null;
        }
        if (dx == 0 && dy == 0) {
            return null;
        }
        return normalize(new double[] { dx, dy });
    }
    private double[] computeOutSlope(double[] curr, int currSegType,
                                     double[] next, int nextSegType){
        Point2D currEndPoint = getSegmentTerminatingPoint(curr, currSegType);
        double dx = 0, dy = 0;
        switch(nextSegType){
        case PathIterator.SEG_CLOSE:
            break;
        case PathIterator.SEG_CUBICTO:
        case PathIterator.SEG_LINETO:
        case PathIterator.SEG_QUADTO:
            dx = next[0] - currEndPoint.getX();
            dy = next[1] - currEndPoint.getY();
            break;
        case ExtendedPathIterator.SEG_ARCTO: {
            boolean large   = (next[3]!=0.);
            boolean goLeft = (next[4]!=0.);
            Arc2D arc = ExtendedGeneralPath.computeArc
                (currEndPoint.getX(), currEndPoint.getY(),
                 next[0], next[1], next[2],
                 large, goLeft, next[5], next[6]);
            double theta = arc.getAngleStart();
            theta = Math.toRadians(theta);
            dx = -arc.getWidth()/2.0*Math.sin(theta);
            dy = arc.getHeight()/2.0*Math.cos(theta);
            if (next[2] != 0) {
                double ang = Math.toRadians(-next[2]);
                double sinA = Math.sin(ang);
                double cosA = Math.cos(ang);
                double tdx = dx*cosA - dy*sinA;
                double tdy = dx*sinA + dy*cosA;
                dx = tdx;
                dy = tdy;
            }
            if (goLeft) {
                dx = -dx;
            } else {
                dy = -dy;
            }
        }
            break;
        case PathIterator.SEG_MOVETO:
        default:
            return null;
        }
        if (dx == 0 && dy == 0) {
            return null;
        }
        return normalize(new double[] { dx, dy });
    }
    public double[] normalize(double[] v) {
        double n = Math.sqrt(v[0]*v[0]+v[1]*v[1]);
        v[0] /= n;
        v[1] /= n;
        return v;
    }
    private AffineTransform computeMarkerTransform(Marker marker,
                                                   Point2D markerPosition,
                                                   double rotation) {
        Point2D ref = marker.getRef();
        AffineTransform txf = new AffineTransform();
        txf.translate(markerPosition.getX() - ref.getX(),
                      markerPosition.getY() - ref.getY());
        if (!Double.isNaN(rotation)) {
            txf.rotate( Math.toRadians( rotation ), ref.getX(), ref.getY());
        }
        return txf;
    }
    protected Point2D getSegmentTerminatingPoint(double[] coords, int segType) {
        switch(segType){
        case PathIterator.SEG_CUBICTO:
            return new Point2D.Double(coords[4], coords[5]);
        case PathIterator.SEG_LINETO:
            return new Point2D.Double(coords[0], coords[1]);
        case PathIterator.SEG_MOVETO:
            return new Point2D.Double(coords[0], coords[1]);
        case PathIterator.SEG_QUADTO:
            return new Point2D.Double(coords[2], coords[3]);
        case ExtendedPathIterator.SEG_ARCTO:
            return new Point2D.Double(coords[5], coords[6]);
        case PathIterator.SEG_CLOSE:
        default:
            throw new Error( "invalid segmentType:" + segType );
        }
    }
}
