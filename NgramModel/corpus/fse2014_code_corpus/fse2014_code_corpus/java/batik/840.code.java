package org.apache.batik.ext.awt.image.renderable;
public interface GaussianBlurRable extends FilterColorInterpolation {
    Filter getSource();
    void setSource(Filter src);
    void setStdDeviationX(double stdDeviationX);
    void setStdDeviationY(double stdDeviationY);
    double getStdDeviationX();
    double getStdDeviationY();
}
