package org.apache.batik.parser;
import java.awt.Shape;
public interface ShapeProducer {
    Shape getShape();
    void setWindingRule(int i);
    int getWindingRule();
}
