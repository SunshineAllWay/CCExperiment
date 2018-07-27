package org.apache.batik.svggen.font.table;
public interface GlyphDescription {
    int getEndPtOfContours(int i);
    byte getFlags(int i);
    short getXCoordinate(int i);
    short getYCoordinate(int i);
    short getXMaximum();
    short getXMinimum();
    short getYMaximum();
    short getYMinimum();
    boolean isComposite();
    int getPointCount();
    int getContourCount();
}
