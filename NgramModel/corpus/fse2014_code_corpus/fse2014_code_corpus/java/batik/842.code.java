package org.apache.batik.ext.awt.image.renderable;
public interface MorphologyRable extends Filter {
    Filter getSource();
    void setSource(Filter src);
    void setRadiusX(double radiusX);
    void setRadiusY(double radiusY);
    void setDoDilation(boolean doDilation);
    boolean getDoDilation();
    double getRadiusX();
    double getRadiusY();
}
