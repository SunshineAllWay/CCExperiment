package org.apache.batik.ext.awt.image.renderable;
import java.awt.geom.Rectangle2D;
import org.apache.batik.ext.awt.image.Light;
public interface DiffuseLightingRable extends FilterColorInterpolation {
    Filter getSource();
    void setSource(Filter src);
    Light getLight();
    void setLight(Light light);
    double getSurfaceScale();
    void setSurfaceScale(double surfaceScale);
    double getKd();
    void setKd(double kd);
    Rectangle2D getLitRegion();
    void setLitRegion(Rectangle2D litRegion);
    double [] getKernelUnitLength();
    void setKernelUnitLength(double [] kernelUnitLength);
}
