package org.apache.batik.gvt;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.apache.batik.gvt.text.Mark;
public interface TextPainter {
    void paint(TextNode node, Graphics2D g2d);
    Mark selectAt(double x, double y, TextNode node);
    Mark selectTo(double x, double y, Mark beginMark);
    Mark selectFirst(TextNode node);
    Mark selectLast(TextNode node);
     Mark getMark(TextNode node, int index, boolean beforeGlyph);
    int[] getSelected(Mark start, Mark finish);
    Shape getHighlightShape(Mark beginMark, Mark endMark);
    Shape getOutline(TextNode node);
    Rectangle2D getBounds2D(TextNode node);
    Rectangle2D getGeometryBounds(TextNode node);
}
