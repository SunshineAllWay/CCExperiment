package org.apache.batik.parser;
public interface TransformListHandler {
    void startTransformList() throws ParseException;
    void matrix(float a, float b, float c, float d, float e, float f)
        throws ParseException;
    void rotate(float theta) throws ParseException;
    void rotate(float theta, float cx, float cy) throws ParseException;
    void translate(float tx) throws ParseException;
    void translate(float tx, float ty) throws ParseException;
    void scale(float sx) throws ParseException;
    void scale(float sx, float sy) throws ParseException;
    void skewX(float skx) throws ParseException;
    void skewY(float sky) throws ParseException;
    void endTransformList() throws ParseException;
}
