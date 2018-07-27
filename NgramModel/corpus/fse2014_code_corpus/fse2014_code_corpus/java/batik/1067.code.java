package org.apache.batik.parser;
import java.awt.geom.AffineTransform;
import java.io.Reader;
public class AWTTransformProducer implements TransformListHandler {
    protected AffineTransform affineTransform;
    public static AffineTransform createAffineTransform(Reader r)
        throws ParseException {
        TransformListParser p = new TransformListParser();
        AWTTransformProducer th = new AWTTransformProducer();
        p.setTransformListHandler(th);
        p.parse(r);
        return th.getAffineTransform();
    }
    public static AffineTransform createAffineTransform(String s)
        throws ParseException {
        TransformListParser p = new TransformListParser();
        AWTTransformProducer th = new AWTTransformProducer();
        p.setTransformListHandler(th);
        p.parse(s);
        return th.getAffineTransform();
    }
    public AffineTransform getAffineTransform() {
        return affineTransform;
    }
    public void startTransformList() throws ParseException {
        affineTransform = new AffineTransform();
    }
    public void matrix(float a, float b, float c, float d, float e, float f)
        throws ParseException {
        affineTransform.concatenate(new AffineTransform(a, b, c, d, e, f));
    }
    public void rotate(float theta) throws ParseException {
        affineTransform.concatenate
            (AffineTransform.getRotateInstance( Math.toRadians( theta ) ));
    }
    public void rotate(float theta, float cx, float cy) throws ParseException {
        AffineTransform at
            = AffineTransform.getRotateInstance( Math.toRadians( theta ), cx, cy);
        affineTransform.concatenate(at);
    }
    public void translate(float tx) throws ParseException {
        AffineTransform at = AffineTransform.getTranslateInstance(tx, 0);
        affineTransform.concatenate(at);
    }
    public void translate(float tx, float ty) throws ParseException {
        AffineTransform at = AffineTransform.getTranslateInstance(tx, ty);
        affineTransform.concatenate(at);
    }
    public void scale(float sx) throws ParseException {
        affineTransform.concatenate(AffineTransform.getScaleInstance(sx, sx));
    }
    public void scale(float sx, float sy) throws ParseException {
        affineTransform.concatenate(AffineTransform.getScaleInstance(sx, sy));
    }
    public void skewX(float skx) throws ParseException {
        affineTransform.concatenate
            (AffineTransform.getShearInstance(Math.tan( Math.toRadians( skx ) ), 0));
    }
    public void skewY(float sky) throws ParseException {
        affineTransform.concatenate
            (AffineTransform.getShearInstance(0, Math.tan( Math.toRadians( sky ) )));
    }
    public void endTransformList() throws ParseException {
    }
}
