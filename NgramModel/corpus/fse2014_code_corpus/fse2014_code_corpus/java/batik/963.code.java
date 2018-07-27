package org.apache.batik.gvt;
import java.awt.Shape;
public interface Selectable {
    boolean selectAt(double x, double y);
    boolean selectTo(double x, double y);
    boolean selectAll(double x, double y);
    Object getSelection();
    Shape getHighlightShape();
}
