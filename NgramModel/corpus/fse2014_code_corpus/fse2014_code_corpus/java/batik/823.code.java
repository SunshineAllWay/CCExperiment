package org.apache.batik.ext.awt.image.renderable;
import java.awt.Point;
import java.awt.image.Kernel;
import org.apache.batik.ext.awt.image.PadMode;
public interface ConvolveMatrixRable extends FilterColorInterpolation {
    Filter getSource();
    void setSource(Filter src);
    Kernel getKernel();
    void setKernel(Kernel k);
    Point getTarget();
    void setTarget(Point pt);
    double getBias();
    void setBias(double bias);
    PadMode getEdgeMode();
    void setEdgeMode(PadMode edgeMode);
    double [] getKernelUnitLength();
    void setKernelUnitLength(double [] kernelUnitLength);
    boolean getPreserveAlpha();
    void setPreserveAlpha(boolean preserveAlpha);
}
