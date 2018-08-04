package org.apache.batik.dom.svg;
import org.apache.batik.parser.PathHandler;
import org.w3c.dom.svg.SVGPathSeg;
import org.w3c.dom.svg.SVGPathSegArcAbs;
import org.w3c.dom.svg.SVGPathSegArcRel;
import org.w3c.dom.svg.SVGPathSegCurvetoCubicAbs;
import org.w3c.dom.svg.SVGPathSegCurvetoCubicRel;
import org.w3c.dom.svg.SVGPathSegCurvetoCubicSmoothAbs;
import org.w3c.dom.svg.SVGPathSegCurvetoCubicSmoothRel;
import org.w3c.dom.svg.SVGPathSegCurvetoQuadraticAbs;
import org.w3c.dom.svg.SVGPathSegCurvetoQuadraticRel;
import org.w3c.dom.svg.SVGPathSegCurvetoQuadraticSmoothAbs;
import org.w3c.dom.svg.SVGPathSegCurvetoQuadraticSmoothRel;
import org.w3c.dom.svg.SVGPathSegLinetoAbs;
import org.w3c.dom.svg.SVGPathSegLinetoHorizontalAbs;
import org.w3c.dom.svg.SVGPathSegLinetoHorizontalRel;
import org.w3c.dom.svg.SVGPathSegLinetoRel;
import org.w3c.dom.svg.SVGPathSegLinetoVerticalAbs;
import org.w3c.dom.svg.SVGPathSegLinetoVerticalRel;
import org.w3c.dom.svg.SVGPathSegList;
import org.w3c.dom.svg.SVGPathSegMovetoAbs;
import org.w3c.dom.svg.SVGPathSegMovetoRel;
public abstract class SVGAnimatedPathDataSupport {
    public static void handlePathSegList(SVGPathSegList p, PathHandler h) {
        int n = p.getNumberOfItems();
        h.startPath();
        for (int i = 0; i < n; i++) {
            SVGPathSeg seg = p.getItem(i);
            switch (seg.getPathSegType()) {
                case SVGPathSeg.PATHSEG_CLOSEPATH:
                    h.closePath();
                    break;
                case SVGPathSeg.PATHSEG_MOVETO_ABS: {
                    SVGPathSegMovetoAbs s = (SVGPathSegMovetoAbs) seg;
                    h.movetoAbs(s.getX(), s.getY());
                    break;
                }
                case SVGPathSeg.PATHSEG_MOVETO_REL: {
                    SVGPathSegMovetoRel s = (SVGPathSegMovetoRel) seg;
                    h.movetoRel(s.getX(), s.getY());
                    break;
                }
                case SVGPathSeg.PATHSEG_LINETO_ABS: {
                    SVGPathSegLinetoAbs s = (SVGPathSegLinetoAbs) seg;
                    h.linetoAbs(s.getX(), s.getY());
                    break;
                }
                case SVGPathSeg.PATHSEG_LINETO_REL: {
                    SVGPathSegLinetoRel s = (SVGPathSegLinetoRel) seg;
                    h.linetoRel(s.getX(), s.getY());
                    break;
                }
                case SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS: {
                    SVGPathSegCurvetoCubicAbs s =
                        (SVGPathSegCurvetoCubicAbs) seg;
                    h.curvetoCubicAbs
                        (s.getX1(), s.getY1(), s.getX2(), s.getY2(),
                         s.getX(), s.getY());
                    break;
                }
                case SVGPathSeg.PATHSEG_CURVETO_CUBIC_REL: {
                    SVGPathSegCurvetoCubicRel s =
                        (SVGPathSegCurvetoCubicRel) seg;
                    h.curvetoCubicRel
                        (s.getX1(), s.getY1(), s.getX2(), s.getY2(),
                         s.getX(), s.getY());
                    break;
                }
                case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_ABS: {
                    SVGPathSegCurvetoQuadraticAbs s =
                        (SVGPathSegCurvetoQuadraticAbs) seg;
                    h.curvetoQuadraticAbs
                        (s.getX1(), s.getY1(), s.getX(), s.getY());
                    break;
                }
                case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_REL: {
                    SVGPathSegCurvetoQuadraticRel s =
                        (SVGPathSegCurvetoQuadraticRel) seg;
                    h.curvetoQuadraticRel
                        (s.getX1(), s.getY1(), s.getX(), s.getY());
                    break;
                }
                case SVGPathSeg.PATHSEG_ARC_ABS: {
                    SVGPathSegArcAbs s = (SVGPathSegArcAbs) seg;
                    h.arcAbs
                        (s.getR1(), s.getR2(), s.getAngle(),
                         s.getLargeArcFlag(), s.getSweepFlag(),
                         s.getX(), s.getY());
                    break;
                }
                case SVGPathSeg.PATHSEG_ARC_REL: {
                    SVGPathSegArcRel s = (SVGPathSegArcRel) seg;
                    h.arcRel
                        (s.getR1(), s.getR2(), s.getAngle(),
                         s.getLargeArcFlag(), s.getSweepFlag(),
                         s.getX(), s.getY());
                    break;
                }
                case SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_ABS: {
                    SVGPathSegLinetoHorizontalAbs s =
                        (SVGPathSegLinetoHorizontalAbs) seg;
                    h.linetoHorizontalAbs(s.getX());
                    break;
                }
                case SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_REL: {
                    SVGPathSegLinetoHorizontalRel s =
                        (SVGPathSegLinetoHorizontalRel) seg;
                    h.linetoHorizontalRel(s.getX());
                    break;
                }
                case SVGPathSeg.PATHSEG_LINETO_VERTICAL_ABS: {
                    SVGPathSegLinetoVerticalAbs s =
                        (SVGPathSegLinetoVerticalAbs) seg;
                    h.linetoVerticalAbs(s.getY());
                    break;
                }
                case SVGPathSeg.PATHSEG_LINETO_VERTICAL_REL: {
                    SVGPathSegLinetoVerticalRel s =
                        (SVGPathSegLinetoVerticalRel) seg;
                    h.linetoVerticalRel(s.getY());
                    break;
                }
                case SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_ABS: {
                    SVGPathSegCurvetoCubicSmoothAbs s =
                        (SVGPathSegCurvetoCubicSmoothAbs) seg;
                    h.curvetoCubicSmoothAbs
                        (s.getX2(), s.getY2(), s.getX(), s.getY());
                    break;
                }
                case SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_REL: {
                    SVGPathSegCurvetoCubicSmoothRel s =
                        (SVGPathSegCurvetoCubicSmoothRel) seg;
                    h.curvetoCubicSmoothRel
                        (s.getX2(), s.getY2(), s.getX(), s.getY());
                    break;
                }
                case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_ABS: {
                    SVGPathSegCurvetoQuadraticSmoothAbs s =
                        (SVGPathSegCurvetoQuadraticSmoothAbs) seg;
                    h.curvetoQuadraticSmoothAbs(s.getX(), s.getY());
                    break;
                }
                case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_REL: {
                    SVGPathSegCurvetoQuadraticSmoothRel s =
                        (SVGPathSegCurvetoQuadraticSmoothRel) seg;
                    h.curvetoQuadraticSmoothRel(s.getX(), s.getY());
                    break;
                }
            }
        }
        h.endPath();
    }
}