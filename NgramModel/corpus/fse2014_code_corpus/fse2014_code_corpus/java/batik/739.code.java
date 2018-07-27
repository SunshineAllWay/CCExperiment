package org.apache.batik.ext.awt.geom;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.ArrayList;
public class PathLength {
    protected Shape path;
    protected List segments;
    protected int[] segmentIndexes;
    protected float pathLength;
    protected boolean initialised;
    public PathLength(Shape path) {
        setPath(path);
    }
    public Shape getPath() {
        return path;
    }
    public void setPath(Shape v) {
        this.path = v;
        initialised = false;
    }
    public float lengthOfPath() {
        if (!initialised) {
            initialise();
        }
        return pathLength;
    }
    protected void initialise() {
        pathLength = 0f;
        PathIterator pi = path.getPathIterator(new AffineTransform());
        SingleSegmentPathIterator sspi = new SingleSegmentPathIterator();
        segments = new ArrayList(20);
        List indexes = new ArrayList(20);
        int index = 0;
        int origIndex = -1;
        float lastMoveX = 0f;
        float lastMoveY = 0f;
        float currentX = 0f;
        float currentY = 0f;
        float[] seg = new float[6];
        int segType;
        segments.add(new PathSegment(PathIterator.SEG_MOVETO, 0f, 0f, 0f,
                                     origIndex));
        while (!pi.isDone()) {
            origIndex++;
            indexes.add(new Integer(index));
            segType = pi.currentSegment(seg);
            switch (segType) {
                case PathIterator.SEG_MOVETO:
                    segments.add(new PathSegment(segType, seg[0], seg[1],
                                                 pathLength, origIndex));
                    currentX = seg[0];
                    currentY = seg[1];
                    lastMoveX = currentX;
                    lastMoveY = currentY;
                    index++;
                    pi.next();
                    break;
                case PathIterator.SEG_LINETO:
                    pathLength += Point2D.distance(currentX, currentY, seg[0],
                                                   seg[1]);
                    segments.add(new PathSegment(segType, seg[0], seg[1],
                                                 pathLength, origIndex));
                    currentX = seg[0];
                    currentY = seg[1];
                    index++;
                    pi.next();
                    break;
                case PathIterator.SEG_CLOSE:
                    pathLength += Point2D.distance(currentX, currentY,
                                                   lastMoveX, lastMoveY);
                    segments.add(new PathSegment(PathIterator.SEG_LINETO,
                                                 lastMoveX, lastMoveY,
                                                 pathLength, origIndex));
                    currentX = lastMoveX;
                    currentY = lastMoveY;
                    index++;
                    pi.next();
                    break;
                default:
                    sspi.setPathIterator(pi, currentX, currentY);
                    FlatteningPathIterator fpi =
                        new FlatteningPathIterator(sspi, 0.01f);
                    while (!fpi.isDone()) {
                        segType = fpi.currentSegment(seg);
                        if (segType == PathIterator.SEG_LINETO) {
                            pathLength += Point2D.distance(currentX, currentY,
                                                           seg[0], seg[1]);
                            segments.add(new PathSegment(segType, seg[0],
                                                         seg[1], pathLength,
                                                         origIndex));
                            currentX = seg[0];
                            currentY = seg[1];
                            index++;
                        }
                        fpi.next();
                    }
            }
        }
        segmentIndexes = new int[indexes.size()];
        for (int i = 0; i < segmentIndexes.length; i++) {
            segmentIndexes[i] = ((Integer) indexes.get(i)).intValue();
        }
        initialised = true;
    }
    public int getNumberOfSegments() {
        if (!initialised) {
            initialise();
        }
        return segmentIndexes.length;
    }
    public float getLengthAtSegment(int index) {
        if (!initialised) {
            initialise();
        }
        if (index <= 0) {
            return 0;
        }
        if (index >= segmentIndexes.length) {
            return pathLength;
        }
        PathSegment seg = (PathSegment) segments.get(segmentIndexes[index]);
        return seg.getLength();
    }
    public int segmentAtLength(float length) {
        int upperIndex = findUpperIndex(length);
        if (upperIndex == -1) {
            return -1;
        }
        if (upperIndex == 0) {
            PathSegment upper = (PathSegment) segments.get(upperIndex);
            return upper.getIndex();
        }
        PathSegment lower = (PathSegment) segments.get(upperIndex - 1);
        return lower.getIndex();
    }
    public Point2D pointAtLength(int index, float proportion) {
        if (!initialised) {
            initialise();
        }
        if (index < 0 || index >= segmentIndexes.length) {
            return null;
        }
        PathSegment seg = (PathSegment) segments.get(segmentIndexes[index]);
        float start = seg.getLength();
        float end;
        if (index == segmentIndexes.length - 1) {
            end = pathLength;
        } else {
            seg = (PathSegment) segments.get(segmentIndexes[index + 1]);
            end = seg.getLength();
        }
        return pointAtLength(start + (end - start) * proportion);
    }
    public Point2D pointAtLength(float length) {
        int upperIndex = findUpperIndex(length);
        if (upperIndex == -1) {
            return null;
        }
        PathSegment upper = (PathSegment) segments.get(upperIndex);
        if (upperIndex == 0) {
            return new Point2D.Float(upper.getX(), upper.getY());
        }
        PathSegment lower = (PathSegment) segments.get(upperIndex - 1);
        float offset = length - lower.getLength();
        double theta = Math.atan2(upper.getY() - lower.getY(),
                                  upper.getX() - lower.getX());
        float xPoint = (float) (lower.getX() + offset * Math.cos(theta));
        float yPoint = (float) (lower.getY() + offset * Math.sin(theta));
        return new Point2D.Float(xPoint, yPoint);
    }
    public float angleAtLength(int index, float proportion) {
        if (!initialised) {
            initialise();
        }
        if (index < 0 || index >= segmentIndexes.length) {
            return 0f;
        }
        PathSegment seg = (PathSegment) segments.get(segmentIndexes[index]);
        float start = seg.getLength();
        float end;
        if (index == segmentIndexes.length - 1) {
            end = pathLength;
        } else {
            seg = (PathSegment) segments.get(segmentIndexes[index + 1]);
            end = seg.getLength();
        }
        return angleAtLength(start + (end - start) * proportion);
    }
    public float angleAtLength(float length) {
        int upperIndex = findUpperIndex(length);
        if (upperIndex == -1) {
            return 0f;
        }
        PathSegment upper = (PathSegment) segments.get(upperIndex);
        if (upperIndex == 0) {
            upperIndex = 1;
        }
        PathSegment lower = (PathSegment) segments.get(upperIndex - 1);
        return (float) Math.atan2(upper.getY() - lower.getY(),
                                  upper.getX() - lower.getX());
    }
    public int findUpperIndex(float length) {
        if (!initialised) {
            initialise();
        }
        if (length < 0 || length > pathLength) {
            return -1;
        }
        int lb = 0;
        int ub = segments.size() - 1;
        while (lb != ub) {
            int curr = (lb + ub) >> 1;
            PathSegment ps = (PathSegment) segments.get(curr);
            if (ps.getLength() >= length) {
                ub = curr;
            } else {
                lb = curr + 1;
            }
        }
        for (;;) {
            PathSegment ps = (PathSegment) segments.get(ub);
            if (ps.getSegType() != PathIterator.SEG_MOVETO
                    || ub == segments.size() - 1) {
                break;
            }
            ub++;
        }
        int upperIndex = -1;
        int currentIndex = 0;
        int numSegments = segments.size();
        while (upperIndex <= 0 && currentIndex < numSegments) {
            PathSegment ps = (PathSegment) segments.get(currentIndex);
            if (ps.getLength() >= length
                    && ps.getSegType() != PathIterator.SEG_MOVETO) {
                upperIndex = currentIndex;
            }
            currentIndex++;
        }
        return upperIndex;
    }
    protected static class SingleSegmentPathIterator implements PathIterator {
        protected PathIterator it;
        protected boolean done;
        protected boolean moveDone;
        protected double x;
        protected double y;
        public void setPathIterator(PathIterator it, double x, double y) {
            this.it = it;
            this.x = x;
            this.y = y;
            done = false;
            moveDone = false;
        }
        public int currentSegment(double[] coords) {
            int type = it.currentSegment(coords);
            if (!moveDone) {
                coords[0] = x;
                coords[1] = y;
                return SEG_MOVETO;
            }
            return type;
        }
        public int currentSegment(float[] coords) {
            int type = it.currentSegment(coords);
            if (!moveDone) {
                coords[0] = (float) x;
                coords[1] = (float) y;
                return SEG_MOVETO;
            }
            return type;
        }
        public int getWindingRule() {
            return it.getWindingRule();
        }
        public boolean isDone() {
            return done || it.isDone();
        }
        public void next() {
            if (!done) {
                if (!moveDone) {
                    moveDone = true;
                } else {
                    it.next();
                    done = true;
                }
            }
        }
    }
    protected static class PathSegment {
        protected final int segType;
        protected float x;
        protected float y;
        protected float length;
        protected int index;
        PathSegment(int segType, float x, float y, float len, int idx) {
            this.segType = segType;
            this.x = x;
            this.y = y;
            this.length = len;
            this.index = idx;
        }
        public int getSegType() {
            return segType;
        }
        public float getX() {
            return x;
        }
        public void setX(float v) {
            x = v;
        }
        public float getY() {
            return y;
        }
        public void setY(float v) {
            y = v;
        }
        public float getLength() {
            return length;
        }
        public void setLength(float v) {
            length = v;
        }
        public int getIndex() {
            return index;
        }
        public void setIndex(int v) {
            index = v;
        }
    }
}
