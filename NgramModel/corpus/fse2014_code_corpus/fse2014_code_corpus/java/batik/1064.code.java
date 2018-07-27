package org.apache.batik.parser;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Reader;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
public class AWTPathProducer implements PathHandler, ShapeProducer {
    protected ExtendedGeneralPath path;
    protected float currentX;
    protected float currentY;
    protected float xCenter;
    protected float yCenter;
    protected int windingRule;
    public static Shape createShape(Reader r, int wr)
        throws IOException,
               ParseException {
        PathParser p = new PathParser();
        AWTPathProducer ph = new AWTPathProducer();
        ph.setWindingRule(wr);
        p.setPathHandler(ph);
        p.parse(r);
        return ph.getShape();
    }
    public void setWindingRule(int i) {
        windingRule = i;
    }
    public int getWindingRule() {
        return windingRule;
    }
    public Shape getShape() {
        return path;
    }
    public void startPath() throws ParseException {
        currentX = 0;
        currentY = 0;
        xCenter = 0;
        yCenter = 0;
        path = new ExtendedGeneralPath(windingRule);
    }
    public void endPath() throws ParseException {
    }
    public void movetoRel(float x, float y) throws ParseException {
        path.moveTo(xCenter = currentX += x, yCenter = currentY += y);
    }
    public void movetoAbs(float x, float y) throws ParseException {
        path.moveTo(xCenter = currentX = x, yCenter = currentY = y);
    }
    public void closePath() throws ParseException {
        path.closePath();
        Point2D pt = path.getCurrentPoint();
        currentX = (float)pt.getX();
        currentY = (float)pt.getY();
    }
    public void linetoRel(float x, float y) throws ParseException {
        path.lineTo(xCenter = currentX += x, yCenter = currentY += y);
    }
    public void linetoAbs(float x, float y) throws ParseException {
        path.lineTo(xCenter = currentX = x, yCenter = currentY = y);
    }
    public void linetoHorizontalRel(float x) throws ParseException {
        path.lineTo(xCenter = currentX += x, yCenter = currentY);
    }
    public void linetoHorizontalAbs(float x) throws ParseException {
        path.lineTo(xCenter = currentX = x, yCenter = currentY);
    }
    public void linetoVerticalRel(float y) throws ParseException {
        path.lineTo(xCenter = currentX, yCenter = currentY += y);
    }
    public void linetoVerticalAbs(float y) throws ParseException {
        path.lineTo(xCenter = currentX, yCenter = currentY = y);
    }
    public void curvetoCubicRel(float x1, float y1,
                                float x2, float y2,
                                float x, float y) throws ParseException {
        path.curveTo(currentX + x1, currentY + y1,
                     xCenter = currentX + x2, yCenter = currentY + y2,
                     currentX += x, currentY += y);
    }
    public void curvetoCubicAbs(float x1, float y1,
                                float x2, float y2,
                                float x, float y) throws ParseException {
        path.curveTo(x1, y1, xCenter = x2, yCenter = y2, currentX = x,
                     currentY = y);
    }
    public void curvetoCubicSmoothRel(float x2, float y2,
                                      float x, float y) throws ParseException {
        path.curveTo(currentX * 2 - xCenter,
                     currentY * 2 - yCenter,
                     xCenter = currentX + x2,
                     yCenter = currentY + y2,
                     currentX += x,
                     currentY += y);
    }
    public void curvetoCubicSmoothAbs(float x2, float y2,
                                      float x, float y) throws ParseException {
        path.curveTo(currentX * 2 - xCenter,
                     currentY * 2 - yCenter,
                     xCenter = x2,
                     yCenter = y2,
                     currentX = x,
                     currentY = y);
    }
    public void curvetoQuadraticRel(float x1, float y1,
                                    float x, float y) throws ParseException {
        path.quadTo(xCenter = currentX + x1, yCenter = currentY + y1,
                    currentX += x, currentY += y);
    }
    public void curvetoQuadraticAbs(float x1, float y1,
                                    float x, float y) throws ParseException {
        path.quadTo(xCenter = x1, yCenter = y1, currentX = x, currentY = y);
    }
    public void curvetoQuadraticSmoothRel(float x, float y)
        throws ParseException {
        path.quadTo(xCenter = currentX * 2 - xCenter,
                    yCenter = currentY * 2 - yCenter,
                    currentX += x,
                    currentY += y);
    }
    public void curvetoQuadraticSmoothAbs(float x, float y)
        throws ParseException {
        path.quadTo(xCenter = currentX * 2 - xCenter,
                    yCenter = currentY * 2 - yCenter,
                    currentX = x,
                    currentY = y);
    }
    public void arcRel(float rx, float ry,
                       float xAxisRotation,
                       boolean largeArcFlag, boolean sweepFlag,
                       float x, float y) throws ParseException {
        path.arcTo(rx, ry, xAxisRotation, largeArcFlag, sweepFlag,
                   xCenter = currentX += x, yCenter = currentY += y);
    }
    public void arcAbs(float rx, float ry,
                       float xAxisRotation,
                       boolean largeArcFlag, boolean sweepFlag,
                       float x, float y) throws ParseException {
        path.arcTo(rx, ry, xAxisRotation, largeArcFlag, sweepFlag,
                   xCenter = currentX = x, yCenter = currentY = y);
    }
}
