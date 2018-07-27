package org.apache.batik.dom.svg;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.PathIterator;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.parser.DefaultPathHandler;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathParser;
import org.w3c.dom.svg.SVGPathSeg;
public abstract class AbstractSVGNormPathSegList extends AbstractSVGPathSegList {
    protected AbstractSVGNormPathSegList() {
        super();
    }
    protected void doParse(String value, ListHandler handler) throws ParseException {
        PathParser pathParser = new PathParser();
        NormalizedPathSegListBuilder builder = new NormalizedPathSegListBuilder(handler);
        pathParser.setPathHandler(builder);
        pathParser.parse(value);
    }
    protected class NormalizedPathSegListBuilder extends DefaultPathHandler {
        protected ListHandler listHandler;
        protected SVGPathSegGenericItem lastAbs;
        public NormalizedPathSegListBuilder(ListHandler listHandler){
            this.listHandler  = listHandler;
        }
        public void startPath() throws ParseException {
            listHandler.startList();
            lastAbs = new SVGPathSegGenericItem(SVGPathSeg.PATHSEG_MOVETO_ABS,
                    PATHSEG_MOVETO_ABS_LETTER, 0,0,0,0,0,0);
        }
        public void endPath() throws ParseException {
            listHandler.endList();
        }
        public void movetoRel(float x, float y) throws ParseException {
            movetoAbs(lastAbs.getX() + x, lastAbs.getY() + y);
        }
        public void movetoAbs(float x, float y) throws ParseException {
            listHandler.item(new SVGPathSegMovetoLinetoItem
                    (SVGPathSeg.PATHSEG_MOVETO_ABS,PATHSEG_MOVETO_ABS_LETTER,
                            x,y));
            lastAbs.setX(x);
            lastAbs.setY(y);
            lastAbs.setPathSegType(SVGPathSeg.PATHSEG_MOVETO_ABS);
        }
        public void closePath() throws ParseException {
            listHandler.item(new SVGPathSegItem
                    (SVGPathSeg.PATHSEG_CLOSEPATH,PATHSEG_CLOSEPATH_LETTER));
        }
        public void linetoRel(float x, float y) throws ParseException {
            linetoAbs(lastAbs.getX() + x, lastAbs.getY() + y);
        }
        public void linetoAbs(float x, float y) throws ParseException {
            listHandler.item(new SVGPathSegMovetoLinetoItem
                    (SVGPathSeg.PATHSEG_LINETO_ABS,PATHSEG_LINETO_ABS_LETTER,
                            x,y));
            lastAbs.setX(x);
            lastAbs.setY(y);
            lastAbs.setPathSegType(SVGPathSeg.PATHSEG_LINETO_ABS);
        }
        public void linetoHorizontalRel(float x) throws ParseException {
            linetoAbs(lastAbs.getX() + x, lastAbs.getY());
        }
        public void linetoHorizontalAbs(float x) throws ParseException {
            linetoAbs(x, lastAbs.getY());
        }
        public void linetoVerticalRel(float y) throws ParseException {
            linetoAbs(lastAbs.getX(), lastAbs.getY() + y);
        }
        public void linetoVerticalAbs(float y) throws ParseException {
            linetoAbs(lastAbs.getX(), y);
        }
        public void curvetoCubicRel(float x1, float y1,
                float x2, float y2,
                float x, float y) throws ParseException {
            curvetoCubicAbs(lastAbs.getX() +x1, lastAbs.getY() + y1,
                    lastAbs.getX() +x2, lastAbs.getY() + y2,
                    lastAbs.getX() +x, lastAbs.getY() + y);
        }
        public void curvetoCubicAbs(float x1, float y1,
                float x2, float y2,
                float x, float y) throws ParseException {
            listHandler.item(new SVGPathSegCurvetoCubicItem
                    (SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS,PATHSEG_CURVETO_CUBIC_ABS_LETTER,
                            x1,y1,x2,y2,x,y));
            lastAbs.setValue(x1,y1,x2,y2,x,y);
            lastAbs.setPathSegType(SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS);
        }
        public void curvetoCubicSmoothRel(float x2, float y2,
                float x, float y) throws ParseException {
            curvetoCubicSmoothAbs(lastAbs.getX() + x2, lastAbs.getY() + y2,
                    lastAbs.getX() + x, lastAbs.getY() + y);
        }
        public void curvetoCubicSmoothAbs(float x2, float y2,
                float x, float y) throws ParseException {
            if (lastAbs.getPathSegType()==SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS) {
                curvetoCubicAbs(lastAbs.getX() + (lastAbs.getX() - lastAbs.getX2()),
                        lastAbs.getY() + (lastAbs.getY() - lastAbs.getY2()),
                        x2, y2, x, y);
            } else {
                curvetoCubicAbs(lastAbs.getX(), lastAbs.getY(), x2, y2, x, y);
            }
        }
        public void curvetoQuadraticRel(float x1, float y1,
                float x, float y) throws ParseException {
            curvetoQuadraticAbs(lastAbs.getX() + x1, lastAbs.getY() + y1,
                    lastAbs.getX() + x, lastAbs.getY() + y);
        }
        public void curvetoQuadraticAbs(float x1, float y1,
                float x, float y) throws ParseException {
                        curvetoCubicAbs(lastAbs.getX() + 2 * (x1 - lastAbs.getX()) / 3,
                                                        lastAbs.getY() + 2 * (y1 - lastAbs.getY()) / 3,
                                                        x + 2 * (x1 - x) / 3,
                                                        y + 2 * (y1 - y) / 3,
                                                        x, y);
                        lastAbs.setX1(x1);
                        lastAbs.setY1(y1);
                        lastAbs.setPathSegType(SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_ABS);
        }
        public void curvetoQuadraticSmoothRel(float x, float y)
        throws ParseException {
            curvetoQuadraticSmoothAbs(lastAbs.getX() + x, lastAbs.getY() + y);
        }
        public void curvetoQuadraticSmoothAbs(float x, float y)
        throws ParseException {
            if (lastAbs.getPathSegType()==SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_ABS) {
                curvetoQuadraticAbs(lastAbs.getX() + (lastAbs.getX() - lastAbs.getX1()),
                        lastAbs.getY() + (lastAbs.getY() - lastAbs.getY1()),
                        x, y);
            } else {
                curvetoQuadraticAbs(lastAbs.getX(), lastAbs.getY(), x, y);
            }
        }
        public void arcRel(float rx, float ry,
                float xAxisRotation,
                boolean largeArcFlag, boolean sweepFlag,
                float x, float y) throws ParseException {
            arcAbs(rx,ry,xAxisRotation, largeArcFlag, sweepFlag, lastAbs.getX() + x, lastAbs.getY() + y);
        }
        public void arcAbs(float rx, float ry,
                float xAxisRotation,
                boolean largeArcFlag, boolean sweepFlag,
                float x, float y) throws ParseException {
                        if (rx == 0 || ry == 0) {
                                linetoAbs(x, y);
                                return;
                        }
                        double x0 = lastAbs.getX();
                        double y0 = lastAbs.getY();
                        if (x0 == x && y0 == y) {
                                return;
                        }
                        Arc2D arc = ExtendedGeneralPath.computeArc(x0, y0, rx, ry, xAxisRotation,
                                        largeArcFlag, sweepFlag, x, y);
                        if (arc == null) return;
                        AffineTransform t = AffineTransform.getRotateInstance
                        (Math.toRadians(xAxisRotation), arc.getCenterX(), arc.getCenterY());
                        Shape s = t.createTransformedShape(arc);
                        PathIterator pi = s.getPathIterator(new AffineTransform());
                        float[] d = {0,0,0,0,0,0};
                        int i = -1;
                        while (!pi.isDone()) {
                                i = pi.currentSegment(d);
                                switch (i) {
                                case PathIterator.SEG_CUBICTO:
                                        curvetoCubicAbs(d[0],d[1],d[2],d[3],d[4],d[5]);
                                        break;
                                }
                                pi.next();
                        }
                        lastAbs.setPathSegType(SVGPathSeg.PATHSEG_ARC_ABS);
        }
    }
    protected class SVGPathSegGenericItem extends SVGPathSegItem {
        public SVGPathSegGenericItem(short type, String letter,
                float x1, float y1, float x2, float y2, float x, float y){
            super(type,letter);
            this.x1 = x2;
            this.y1 = y2;
            this.x2 = x2;
            this.y2 = y2;
            this.x = x;
            this.y = y;
        }
        public void setValue(float x1, float y1, float x2, float y2, float x, float y) {
            this.x1 = x2;
            this.y1 = y2;
            this.x2 = x2;
            this.y2 = y2;
            this.x = x;
            this.y = y;
        }
        public void setValue(float x, float y) {
            this.x = x;
            this.y = y;
        }
        public void setPathSegType(short type) {
            this.type = type;
        }
        public float getX(){
            return x;
        }
        public float getY(){
            return y;
        }
        public void setX(float x){
            this.x = x;
        }
        public void setY(float y){
            this.y = y;
        }
        public float getX1(){
            return x1;
        }
        public float getY1(){
            return y1;
        }
        public void setX1(float x){
            this.x1 = x;
        }
        public void setY1(float y){
            this.y1 = y;
        }
        public float getX2(){
            return x2;
        }
        public float getY2(){
            return y2;
        }
        public void setX2(float x){
            this.x2 = x;
        }
        public void setY2(float y){
            this.y2 = y;
        }
    }
}
