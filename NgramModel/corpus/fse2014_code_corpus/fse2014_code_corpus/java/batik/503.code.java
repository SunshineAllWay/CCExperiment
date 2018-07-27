package org.apache.batik.dom.svg;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGMatrix;
public abstract class AbstractSVGMatrix implements SVGMatrix {
    protected static final AffineTransform FLIP_X_TRANSFORM =
        new AffineTransform(-1, 0, 0, 1, 0, 0);
    protected static final AffineTransform FLIP_Y_TRANSFORM =
        new AffineTransform(1, 0, 0, -1, 0, 0);
    protected abstract AffineTransform getAffineTransform();
    public float getA() {
        return (float)getAffineTransform().getScaleX();
    }
    public void setA(float a) throws DOMException {
        AffineTransform at = getAffineTransform();
        at.setTransform(a,
                        at.getShearY(),
                        at.getShearX(),
                        at.getScaleY(),
                        at.getTranslateX(),
                        at.getTranslateY());
    }
    public float getB() {
        return (float)getAffineTransform().getShearY();
    }
    public void setB(float b) throws DOMException {
        AffineTransform at = getAffineTransform();
        at.setTransform(at.getScaleX(),
                        b,
                        at.getShearX(),
                        at.getScaleY(),
                        at.getTranslateX(),
                        at.getTranslateY());
    }
    public float getC() {
        return (float)getAffineTransform().getShearX();
    }
    public void setC(float c) throws DOMException {
        AffineTransform at = getAffineTransform();
        at.setTransform(at.getScaleX(),
                        at.getShearY(),
                        c,
                        at.getScaleY(),
                        at.getTranslateX(),
                        at.getTranslateY());
    }
    public float getD() {
        return (float)getAffineTransform().getScaleY();
    }
    public void setD(float d) throws DOMException {
        AffineTransform at = getAffineTransform();
        at.setTransform(at.getScaleX(),
                        at.getShearY(),
                        at.getShearX(),
                        d,
                        at.getTranslateX(),
                        at.getTranslateY());
    }
    public float getE() {
        return (float)getAffineTransform().getTranslateX();
    }
    public void setE(float e) throws DOMException {
        AffineTransform at = getAffineTransform();
        at.setTransform(at.getScaleX(),
                        at.getShearY(),
                        at.getShearX(),
                        at.getScaleY(),
                        e,
                        at.getTranslateY());
    }
    public float getF() {
        return (float)getAffineTransform().getTranslateY();
    }
    public void setF(float f) throws DOMException {
        AffineTransform at = getAffineTransform();
        at.setTransform(at.getScaleX(),
                        at.getShearY(),
                        at.getShearX(),
                        at.getScaleY(),
                        at.getTranslateX(),
                        f);
    }
    public SVGMatrix multiply(SVGMatrix secondMatrix) {
        AffineTransform at = new AffineTransform(secondMatrix.getA(),
                                                 secondMatrix.getB(),
                                                 secondMatrix.getC(),
                                                 secondMatrix.getD(),
                                                 secondMatrix.getE(),
                                                 secondMatrix.getF());
        AffineTransform tr = (AffineTransform)getAffineTransform().clone();
        tr.concatenate(at);
        return new SVGOMMatrix(tr);
    }
    public SVGMatrix inverse() throws SVGException {
        try {
            return new SVGOMMatrix(getAffineTransform().createInverse());
        } catch (NoninvertibleTransformException e) {
            throw new SVGOMException(SVGException.SVG_MATRIX_NOT_INVERTABLE,
                                     e.getMessage());
        }
    }
    public SVGMatrix translate(float x, float y) {
        AffineTransform tr = (AffineTransform)getAffineTransform().clone();
        tr.translate(x, y);
        return new SVGOMMatrix(tr);
    }
    public SVGMatrix scale(float scaleFactor) {
        AffineTransform tr = (AffineTransform)getAffineTransform().clone();
        tr.scale(scaleFactor, scaleFactor);
        return new SVGOMMatrix(tr);
    }
    public SVGMatrix scaleNonUniform (float scaleFactorX, float scaleFactorY) {
        AffineTransform tr = (AffineTransform)getAffineTransform().clone();
        tr.scale(scaleFactorX, scaleFactorY);
        return new SVGOMMatrix(tr);
    }
    public SVGMatrix rotate(float angle) {
        AffineTransform tr = (AffineTransform)getAffineTransform().clone();
        tr.rotate( Math.toRadians( angle ) );
        return new SVGOMMatrix(tr);
    }
    public SVGMatrix rotateFromVector(float x, float y) throws SVGException {
        if (x == 0 || y == 0) {
            throw new SVGOMException(SVGException.SVG_INVALID_VALUE_ERR, "");
        }
        AffineTransform tr = (AffineTransform)getAffineTransform().clone();
        tr.rotate(Math.atan2(y, x));
        return new SVGOMMatrix(tr);
    }
    public SVGMatrix flipX() {
        AffineTransform tr = (AffineTransform)getAffineTransform().clone();
        tr.concatenate(FLIP_X_TRANSFORM);
        return new SVGOMMatrix(tr);
    }
    public SVGMatrix flipY() {
        AffineTransform tr = (AffineTransform)getAffineTransform().clone();
        tr.concatenate(FLIP_Y_TRANSFORM);
        return new SVGOMMatrix(tr);
    }
    public SVGMatrix skewX(float angleDeg) {
        AffineTransform tr = (AffineTransform)getAffineTransform().clone();
        tr.concatenate
            (AffineTransform.getShearInstance( Math.tan( Math.toRadians( angleDeg )), 0));
        return new SVGOMMatrix(tr);
    }
    public SVGMatrix skewY(float angleDeg ) {
        AffineTransform tr = (AffineTransform)getAffineTransform().clone();
        tr.concatenate
            (AffineTransform.getShearInstance(0,  Math.tan( Math.toRadians( angleDeg ) ) ));
        return new SVGOMMatrix(tr);
    }
}
