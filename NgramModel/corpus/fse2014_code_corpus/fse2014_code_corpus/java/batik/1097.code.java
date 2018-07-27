package org.apache.batik.parser;
import java.util.Iterator;
import java.util.LinkedList;
import org.w3c.dom.svg.SVGPathSeg;
public class PathArrayProducer implements PathHandler {
    protected LinkedList ps;
    protected float[] p;
    protected LinkedList cs;
    protected short[] c;
    protected int cindex;
    protected int pindex;
    protected int ccount;
    protected int pcount;
    public short[] getPathCommands() {
        return c;
    }
    public float[] getPathParameters() {
        return p;
    }
    public void startPath() throws ParseException {
        cs = new LinkedList();
        c = new short[11];
        ps = new LinkedList();
        p = new float[11];
        ccount = 0;
        pcount = 0;
        cindex = 0;
        pindex = 0;
    }
    public void movetoRel(float x, float y) throws ParseException {
        command(SVGPathSeg.PATHSEG_MOVETO_REL);
        param(x);
        param(y);
    }
    public void movetoAbs(float x, float y) throws ParseException {
        command(SVGPathSeg.PATHSEG_MOVETO_ABS);
        param(x);
        param(y);
    }
    public void closePath() throws ParseException {
        command(SVGPathSeg.PATHSEG_CLOSEPATH);
    }
    public void linetoRel(float x, float y) throws ParseException {
        command(SVGPathSeg.PATHSEG_LINETO_REL);
        param(x);
        param(y);
    }
    public void linetoAbs(float x, float y) throws ParseException {
        command(SVGPathSeg.PATHSEG_LINETO_ABS);
        param(x);
        param(y);
    }
    public void linetoHorizontalRel(float x) throws ParseException {
        command(SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_REL);
        param(x);
    }
    public void linetoHorizontalAbs(float x) throws ParseException {
        command(SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_ABS);
        param(x);
    }
    public void linetoVerticalRel(float y) throws ParseException {
        command(SVGPathSeg.PATHSEG_LINETO_VERTICAL_REL);
        param(y);
    }
    public void linetoVerticalAbs(float y) throws ParseException {
        command(SVGPathSeg.PATHSEG_LINETO_VERTICAL_ABS);
        param(y);
    }
    public void curvetoCubicRel(float x1, float y1, 
                                float x2, float y2, 
                                float x, float y) throws ParseException {
        command(SVGPathSeg.PATHSEG_CURVETO_CUBIC_REL);
        param(x1);
        param(y1);
        param(x2);
        param(y2);
        param(x);
        param(y);
    }
    public void curvetoCubicAbs(float x1, float y1, 
                                float x2, float y2, 
                                float x, float y) throws ParseException {
        command(SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS);
        param(x1);
        param(y1);
        param(x2);
        param(y2);
        param(x);
        param(y);
    }
    public void curvetoCubicSmoothRel(float x2, float y2, 
                                      float x, float y) throws ParseException {
        command(SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_REL);
        param(x2);
        param(y2);
        param(x);
        param(y);
    }
    public void curvetoCubicSmoothAbs(float x2, float y2, 
                                      float x, float y) throws ParseException {
        command(SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_ABS);
        param(x2);
        param(y2);
        param(x);
        param(y);
    }
    public void curvetoQuadraticRel(float x1, float y1, 
                                    float x, float y) throws ParseException {
        command(SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_REL);
        param(x1);
        param(y1);
        param(x);
        param(y);
    }
    public void curvetoQuadraticAbs(float x1, float y1, 
                                    float x, float y) throws ParseException {
        command(SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_ABS);
        param(x1);
        param(y1);
        param(x);
        param(y);
    }
    public void curvetoQuadraticSmoothRel(float x, float y)
        throws ParseException {
        command(SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_REL);
        param(x);
        param(y);
    }
    public void curvetoQuadraticSmoothAbs(float x, float y)
        throws ParseException {
        command(SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_ABS);
        param(x);
        param(y);
    }
    public void arcRel(float rx, float ry, 
                       float xAxisRotation, 
                       boolean largeArcFlag, boolean sweepFlag, 
                       float x, float y) throws ParseException {
        command(SVGPathSeg.PATHSEG_ARC_REL);
        param(rx);
        param(ry);
        param(xAxisRotation);
        param(largeArcFlag ? 1 : 0);
        param(sweepFlag ? 1 : 0);
        param(x);
        param(y);
    }
    public void arcAbs(float rx, float ry, 
                       float xAxisRotation, 
                       boolean largeArcFlag, boolean sweepFlag, 
                       float x, float y) throws ParseException {
        command(SVGPathSeg.PATHSEG_ARC_ABS);
        param(rx);
        param(ry);
        param(xAxisRotation);
        param(largeArcFlag ? 1 : 0);
        param(sweepFlag ? 1 : 0);
        param(x);
        param(y);
    }
    protected void command(short val) throws ParseException {
        if (cindex == c.length) {
            cs.add(c);
            c = new short[c.length * 2 + 1];
            cindex = 0;
        }
        c[cindex++] = val;
        ccount++;
    }
    protected void param(float val) throws ParseException {
        if (pindex == p.length) {
            ps.add(p);
            p = new float[p.length * 2 + 1];
            pindex = 0;
        }
        p[pindex++] = val;
        pcount++;
    }
    public void endPath() throws ParseException {
        short[] allCommands = new short[ccount];
        int pos = 0;
        Iterator it = cs.iterator();
        while (it.hasNext()) {
            short[] a = (short[]) it.next();
            System.arraycopy(a, 0, allCommands, pos, a.length);
            pos += a.length;
        }
        System.arraycopy(c, 0, allCommands, pos, cindex);
        cs.clear();
        c = allCommands;
        float[] allParams = new float[pcount];
        pos = 0;
        it = ps.iterator();
        while (it.hasNext()) {
            float[] a = (float[]) it.next();
            System.arraycopy(a, 0, allParams, pos, a.length);
            pos += a.length;
        }
        System.arraycopy(p, 0, allParams, pos, pindex);
        ps.clear();
        p = allParams;
    }
}
