package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGPathElement;
import org.w3c.dom.svg.SVGPathSeg;
import org.w3c.dom.svg.SVGPathSegArcAbs;
import org.w3c.dom.svg.SVGPathSegArcRel;
import org.w3c.dom.svg.SVGPathSegClosePath;
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
import org.w3c.dom.svg.SVGPoint;
public class SVGOMPathElement
    extends    SVGGraphicsElement
    implements SVGPathElement,
               SVGPathSegConstants {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGGraphicsElement.xmlTraitInformation);
        t.put(null, SVG_D_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_PATH_DATA));
        t.put(null, SVG_PATH_LENGTH_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER));
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedPathData d;
    protected SVGOMPathElement() {
    }
    public SVGOMPathElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        d = createLiveAnimatedPathData(null, SVG_D_ATTRIBUTE, "");
    }
    public String getLocalName() {
        return SVG_PATH_TAG;
    }
    public SVGAnimatedNumber getPathLength() {
        throw new UnsupportedOperationException
            ("SVGPathElement.getPathLength is not implemented"); 
    }
    public float getTotalLength() {
        return SVGPathSupport.getTotalLength(this);
    }
    public SVGPoint getPointAtLength(float distance) {
        return SVGPathSupport.getPointAtLength(this, distance);
    }
    public int getPathSegAtLength(float distance) {
        return SVGPathSupport.getPathSegAtLength(this, distance);
    }
    public SVGOMAnimatedPathData getAnimatedPathData() {
        return d;
    }
    public SVGPathSegList getPathSegList() {
        return d.getPathSegList();
    }
    public SVGPathSegList getNormalizedPathSegList() {
        return d.getNormalizedPathSegList();
    }
    public SVGPathSegList getAnimatedPathSegList() {
        return d.getAnimatedPathSegList();
    }
    public SVGPathSegList getAnimatedNormalizedPathSegList() {
        return d.getAnimatedNormalizedPathSegList();
    }
    public SVGPathSegClosePath createSVGPathSegClosePath() {
        return new SVGPathSegClosePath(){
                public short getPathSegType(){
                    return SVGPathSeg.PATHSEG_CLOSEPATH;
                }
                public String getPathSegTypeAsLetter(){
                    return PATHSEG_CLOSEPATH_LETTER;
                }
            };
    }
    public SVGPathSegMovetoAbs createSVGPathSegMovetoAbs(final float x_value, final float y_value) {
        return new SVGPathSegMovetoAbs(){
                protected float x = x_value;
                protected float y = y_value;
                public short getPathSegType(){
                    return SVGPathSeg.PATHSEG_MOVETO_ABS;
                }
                public String getPathSegTypeAsLetter(){
                    return PATHSEG_MOVETO_ABS_LETTER;
                }
                public float getX(){
                    return x;
                }
                public void setX(float x){
                    this.x = x;
                }
                public float getY(){
                    return y;
                }
                public void setY(float y){
                    this.y = y;
                }
            };
    }
    public SVGPathSegMovetoRel createSVGPathSegMovetoRel(final float x_value, final float y_value) {
        return new SVGPathSegMovetoRel(){
                protected float x = x_value;
                protected float y = y_value;
                public short getPathSegType(){
                    return SVGPathSeg.PATHSEG_MOVETO_REL;
                }
                public String getPathSegTypeAsLetter(){
                    return PATHSEG_MOVETO_REL_LETTER;
                }
                public float getX(){
                    return x;
                }
                public void setX(float x){
                    this.x = x;
                }
                public float getY(){
                    return y;
                }
                public void setY(float y){
                    this.y = y;
                }
            };
    }
    public SVGPathSegLinetoAbs createSVGPathSegLinetoAbs(final float x_value, final float y_value) {
        return new SVGPathSegLinetoAbs(){
                protected float x = x_value;
                protected float y = y_value;
                public short getPathSegType(){
                    return SVGPathSeg.PATHSEG_LINETO_ABS;
                }
                public String getPathSegTypeAsLetter(){
                    return PATHSEG_LINETO_ABS_LETTER;
                }
                public float getX(){
                    return x;
                }
                public void setX(float x){
                    this.x = x;
                }
                public float getY(){
                    return y;
                }
                public void setY(float y){
                    this.y = y;
                }
            };
    }
    public SVGPathSegLinetoRel createSVGPathSegLinetoRel(final float x_value, final float y_value) {
        return new SVGPathSegLinetoRel(){
                protected float x = x_value;
                protected float y = y_value;
                public short getPathSegType(){
                    return SVGPathSeg.PATHSEG_LINETO_REL;
                }
                public String getPathSegTypeAsLetter(){
                    return PATHSEG_LINETO_REL_LETTER;
                }
                public float getX(){
                    return x;
                }
                public void setX(float x){
                    this.x = x;
                }
                public float getY(){
                    return y;
                }
                public void setY(float y){
                    this.y = y;
                }
            };
    }
    public SVGPathSegLinetoHorizontalAbs createSVGPathSegLinetoHorizontalAbs(final float x_value) {
        return new SVGPathSegLinetoHorizontalAbs(){
                protected float x = x_value;
                public short getPathSegType(){
                    return SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_ABS;
                }
                public String getPathSegTypeAsLetter(){
                    return PATHSEG_LINETO_HORIZONTAL_ABS_LETTER;
                }
                public float getX(){
                    return x;
                }
                public void setX(float x){
                    this.x = x;
                }
            };
    }
    public SVGPathSegLinetoHorizontalRel createSVGPathSegLinetoHorizontalRel(final float x_value) {
        return new SVGPathSegLinetoHorizontalRel(){
                protected float x = x_value;
                public short getPathSegType(){
                    return SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_REL;
                }
                public String getPathSegTypeAsLetter(){
                    return PATHSEG_LINETO_HORIZONTAL_REL_LETTER;
                }
                public float getX(){
                    return x;
                }
                public void setX(float x){
                    this.x = x;
                }
            };
    }
    public SVGPathSegLinetoVerticalAbs createSVGPathSegLinetoVerticalAbs(final float y_value) {
        return new SVGPathSegLinetoVerticalAbs(){
                protected float y = y_value;
                public short getPathSegType(){
                    return SVGPathSeg.PATHSEG_LINETO_VERTICAL_ABS;
                }
                public String getPathSegTypeAsLetter(){
                    return PATHSEG_LINETO_VERTICAL_ABS_LETTER;
                }
                public float getY(){
                    return y;
                }
                public void setY(float y){
                    this.y = y;
                }
            };
    }
    public SVGPathSegLinetoVerticalRel createSVGPathSegLinetoVerticalRel(final float y_value) {
        return new SVGPathSegLinetoVerticalRel(){
                protected float y = y_value;
                public short getPathSegType(){
                    return SVGPathSeg.PATHSEG_LINETO_VERTICAL_REL;
                }
                public String getPathSegTypeAsLetter(){
                    return PATHSEG_LINETO_VERTICAL_REL_LETTER;
                }
                public float getY(){
                    return y;
                }
                public void setY(float y){
                    this.y = y;
                }
            };
    }
    public SVGPathSegCurvetoCubicAbs createSVGPathSegCurvetoCubicAbs
        (final float x_value, final float y_value, 
         final float x1_value, final float y1_value, 
         final float x2_value, final float y2_value) {
        return new SVGPathSegCurvetoCubicAbs(){
                protected float x = x_value;
                protected float y = y_value;
                protected float x1 = x1_value;
                protected float y1 = y1_value;
                protected float x2 = x2_value;
                protected float y2 = y2_value;
                public short getPathSegType(){
                    return SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS;
                }
                public String getPathSegTypeAsLetter(){
                    return PATHSEG_CURVETO_CUBIC_ABS_LETTER;
                }
                public float getX(){
                    return x;
                }
                public void setX(float x){
                    this.x = x;
                }
                public float getY(){
                    return y;
                }
                public void setY(float y){
                    this.y = y;
                }
                public float getX1(){
                    return x1;
                }
                public void setX1(float x1){
                    this.x1 = x1;
                }
                public float getY1(){
                    return y1;
                }
                public void setY1(float y1){
                    this.y1 = y1;
                }
                public float getX2(){
                    return x2;
                }
                public void setX2(float x2){
                    this.x2 = x2;
                }
                public float getY2(){
                    return y2;
                }
                public void setY2(float y2){
                    this.y2 = y2;
                }
            };
    }
    public SVGPathSegCurvetoCubicRel createSVGPathSegCurvetoCubicRel
        (final float x_value, final float y_value, 
         final float x1_value, final float y1_value, 
         final float x2_value, final float y2_value) {
        return new SVGPathSegCurvetoCubicRel(){
                protected float x = x_value;
                protected float y = y_value;
                protected float x1 = x1_value;
                protected float y1 = y1_value;
                protected float x2 = x2_value;
                protected float y2 = y2_value;
                public short getPathSegType(){
                    return SVGPathSeg.PATHSEG_CURVETO_CUBIC_REL;
                }
                public String getPathSegTypeAsLetter(){
                    return PATHSEG_CURVETO_CUBIC_REL_LETTER;
                }
                public float getX(){
                    return x;
                }
                public void setX(float x){
                    this.x = x;
                }
                public float getY(){
                    return y;
                }
                public void setY(float y){
                    this.y = y;
                }
                public float getX1(){
                    return x1;
                }
                public void setX1(float x1){
                    this.x1 = x1;
                }
                public float getY1(){
                    return y1;
                }
                public void setY1(float y1){
                    this.y1 = y1;
                }
                public float getX2(){
                    return x2;
                }
                public void setX2(float x2){
                    this.x2 = x2;
                }
                public float getY2(){
                    return y2;
                }
                public void setY2(float y2){
                    this.y2 = y2;
                }
            };
    }
    public SVGPathSegCurvetoQuadraticAbs createSVGPathSegCurvetoQuadraticAbs
        (final float x_value, final float y_value, 
         final float x1_value, final float y1_value) {
        return new SVGPathSegCurvetoQuadraticAbs(){
                protected float x = x_value;
                protected float y = y_value;
                protected float x1 = x1_value;
                protected float y1 = y1_value;
                public short getPathSegType(){
                    return SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_ABS;
                }
                public String getPathSegTypeAsLetter(){
                    return PATHSEG_CURVETO_QUADRATIC_ABS_LETTER;
                }
                public float getX(){
                    return x;
                }
                public void setX(float x){
                    this.x = x;
                }
                public float getY(){
                    return y;
                }
                public void setY(float y){
                    this.y = y;
                }
                public float getX1(){
                    return x1;
                }
                public void setX1(float x1){
                    this.x1 = x1;
                }
                public float getY1(){
                    return y1;
                }
                public void setY1(float y1){
                    this.y1 = y1;
                }
            };
    }
    public SVGPathSegCurvetoQuadraticRel createSVGPathSegCurvetoQuadraticRel
        (final float x_value, final float y_value, 
         final float x1_value, final float y1_value) {
        return new SVGPathSegCurvetoQuadraticRel(){
                protected float x = x_value;
                protected float y = y_value;
                protected float x1 = x1_value;
                protected float y1 = y1_value;
                public short getPathSegType(){
                    return SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_REL;
                }
                public String getPathSegTypeAsLetter(){
                    return PATHSEG_CURVETO_QUADRATIC_REL_LETTER;
                }
                public float getX(){
                    return x;
                }
                public void setX(float x){
                    this.x = x;
                }
                public float getY(){
                    return y;
                }
                public void setY(float y){
                    this.y = y;
                }
                public float getX1(){
                    return x1;
                }
                public void setX1(float x1){
                    this.x1 = x1;
                }
                public float getY1(){
                    return y1;
                }
                public void setY1(float y1){
                    this.y1 = y1;
                }
            };
    }
    public SVGPathSegCurvetoCubicSmoothAbs
            createSVGPathSegCurvetoCubicSmoothAbs
        (final float x_value, final float y_value, 
         final float x2_value, final float y2_value) {
        return new SVGPathSegCurvetoCubicSmoothAbs(){
                protected float x = x_value;
                protected float y = y_value;
                protected float x2 = x2_value;
                protected float y2 = y2_value;
                public short getPathSegType(){
                    return SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_ABS;
                }
                public String getPathSegTypeAsLetter(){
                    return PATHSEG_CURVETO_CUBIC_SMOOTH_ABS_LETTER;
                }
                public float getX(){
                    return x;
                }
                public void setX(float x){
                    this.x = x;
                }
                public float getY(){
                    return y;
                }
                public void setY(float y){
                    this.y = y;
                }
                public float getX2(){
                    return x2;
                }
                public void setX2(float x2){
                    this.x2 = x2;
                }
                public float getY2(){
                    return y2;
                }
                public void setY2(float y2){
                    this.y2 = y2;
                }
            };
    }
    public SVGPathSegCurvetoCubicSmoothRel
            createSVGPathSegCurvetoCubicSmoothRel
        (final float x_value, final float y_value, 
         final float x2_value, final float y2_value) {
        return new SVGPathSegCurvetoCubicSmoothRel(){
                protected float x = x_value;
                protected float y = y_value;
                protected float x2 = x2_value;
                protected float y2 = y2_value;
                public short getPathSegType(){
                    return SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_REL;
                }
                public String getPathSegTypeAsLetter(){
                    return PATHSEG_CURVETO_CUBIC_SMOOTH_REL_LETTER;
                }
                public float getX(){
                    return x;
                }
                public void setX(float x){
                    this.x = x;
                }
                public float getY(){
                    return y;
                }
                public void setY(float y){
                    this.y = y;
                }
                public float getX2(){
                    return x2;
                }
                public void setX2(float x2){
                    this.x2 = x2;
                }
                public float getY2(){
                    return y2;
                }
                public void setY2(float y2){
                    this.y2 = y2;
                }
            };
    }
    public SVGPathSegCurvetoQuadraticSmoothAbs
            createSVGPathSegCurvetoQuadraticSmoothAbs
        (final float x_value, final float y_value) {
        return new SVGPathSegCurvetoQuadraticSmoothAbs(){
                protected float x = x_value;
                protected float y = y_value;
                public short getPathSegType(){
                    return SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_ABS;
                }
                public String getPathSegTypeAsLetter(){
                    return PATHSEG_CURVETO_QUADRATIC_SMOOTH_ABS_LETTER;
                }
                public float getX(){
                    return x;
                }
                public void setX(float x){
                    this.x = x;
                }
                public float getY(){
                    return y;
                }
                public void setY(float y){
                    this.y = y;
                }
            };
    }
    public SVGPathSegCurvetoQuadraticSmoothRel
            createSVGPathSegCurvetoQuadraticSmoothRel
        (final float x_value, final float y_value) {
        return new SVGPathSegCurvetoQuadraticSmoothRel(){
                protected float x = x_value;
                protected float y = y_value;
                public short getPathSegType(){
                    return SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_REL;
                }
                public String getPathSegTypeAsLetter(){
                    return PATHSEG_CURVETO_QUADRATIC_SMOOTH_REL_LETTER;
                }
                public float getX(){
                    return x;
                }
                public void setX(float x){
                    this.x = x;
                }
                public float getY(){
                    return y;
                }
                public void setY(float y){
                    this.y = y;
                }
            };
    }
    public SVGPathSegArcAbs createSVGPathSegArcAbs
        (final float x_value, final float y_value, 
         final float r1_value, final float r2_value, 
         final float angle_value,
         final boolean largeArcFlag_value, 
         final boolean sweepFlag_value) {
        return new SVGPathSegArcAbs(){
                protected float x = x_value;
                protected float y = y_value;
                protected float r1 = r1_value;
                protected float r2 = r2_value;
                protected float angle = angle_value;
                protected boolean largeArcFlag = largeArcFlag_value;
                protected boolean sweepFlag = sweepFlag_value;
                public short getPathSegType(){
                    return SVGPathSeg.PATHSEG_ARC_ABS;
                }
                public String getPathSegTypeAsLetter(){
                    return PATHSEG_ARC_ABS_LETTER;
                }
                public float getX(){
                    return x;
                }
                public void setX(float x){
                    this.x = x;
                }
                public float getY(){
                    return y;
                }
                public void setY(float y){
                    this.y = y;
                }
                public float getR1(){
                    return r1;
                }
                public void setR1(float r1){
                    this.r1 = r1;
                }
                public float getR2(){
                    return r2;
                }
                public void setR2(float r2){
                    this.r2 = r2;
                }
                public float getAngle(){
                    return angle;
                }
                public void setAngle(float angle){
                    this.angle = angle;
                }
                public boolean getLargeArcFlag(){
                    return largeArcFlag;
                }
                public void setLargeArcFlag(boolean largeArcFlag){
                    this.largeArcFlag = largeArcFlag;
                }
                public boolean getSweepFlag(){
                    return sweepFlag;
                }
                public void setSweepFlag(boolean sweepFlag){
                    this.sweepFlag = sweepFlag;
                }
            };
    }
    public SVGPathSegArcRel createSVGPathSegArcRel
        (final float x_value, final float y_value, 
         final float r1_value, final float r2_value, 
         final float angle_value,
         final boolean largeArcFlag_value, 
         final boolean sweepFlag_value) {
        return new SVGPathSegArcRel(){
                protected float x = x_value;
                protected float y = y_value;
                protected float r1 = r1_value;
                protected float r2 = r2_value;
                protected float angle = angle_value;
                protected boolean largeArcFlag = largeArcFlag_value;
                protected boolean sweepFlag = sweepFlag_value;
                public short getPathSegType(){
                    return SVGPathSeg.PATHSEG_ARC_REL;
                }
                public String getPathSegTypeAsLetter(){
                    return PATHSEG_ARC_REL_LETTER;
                }
                public float getX(){
                    return x;
                }
                public void setX(float x){
                    this.x = x;
                }
                public float getY(){
                    return y;
                }
                public void setY(float y){
                    this.y = y;
                }
                public float getR1(){
                    return r1;
                }
                public void setR1(float r1){
                    this.r1 = r1;
                }
                public float getR2(){
                    return r2;
                }
                public void setR2(float r2){
                    this.r2 = r2;
                }
                public float getAngle(){
                    return angle;
                }
                public void setAngle(float angle){
                    this.angle = angle;
                }
                public boolean getLargeArcFlag(){
                    return largeArcFlag;
                }
                public void setLargeArcFlag(boolean largeArcFlag){
                    this.largeArcFlag = largeArcFlag;
                }
                public boolean getSweepFlag(){
                    return sweepFlag;
                }
                public void setSweepFlag(boolean sweepFlag){
                    this.sweepFlag = sweepFlag;
                }
            };
    }
    protected Node newNode() {
        return new SVGOMPathElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
