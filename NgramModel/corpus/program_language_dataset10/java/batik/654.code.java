package org.apache.batik.dom.svg;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
public interface SVGTextContent
{
    int getNumberOfChars();
    Rectangle2D getExtentOfChar(int charnum );
    Point2D getStartPositionOfChar(int charnum);
    Point2D getEndPositionOfChar(int charnum);
    float getRotationOfChar(int charnum);
    void selectSubString(int charnum, int nchars);
    float getComputedTextLength();
    float getSubStringLength(int charnum, int nchars);
    int getCharNumAtPosition(float x, float y);
}
