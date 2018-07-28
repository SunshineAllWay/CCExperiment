package org.apache.batik.gvt.text;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import org.apache.batik.ext.awt.geom.PathLength;
public class TextPath {
    private PathLength pathLength;
    private float startOffset;
    public TextPath(GeneralPath path) {
        pathLength = new PathLength(path);
        startOffset = 0;
    }
    public void setStartOffset(float startOffset) {
        this.startOffset = startOffset;
    }
    public float getStartOffset() {
        return startOffset;
    }
    public float lengthOfPath() {
        return pathLength.lengthOfPath();
    }
    public float angleAtLength(float length) {
        return pathLength.angleAtLength(length);
    }
    public Point2D pointAtLength(float length) {
        return pathLength.pointAtLength(length);
    }
}
