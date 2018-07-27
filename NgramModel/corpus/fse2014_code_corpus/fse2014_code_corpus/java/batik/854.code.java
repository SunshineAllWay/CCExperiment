package org.apache.batik.ext.awt.image.renderable;
import java.awt.geom.Rectangle2D;
public interface TurbulenceRable extends FilterColorInterpolation {
    void setTurbulenceRegion(Rectangle2D turbulenceRegion);
    Rectangle2D getTurbulenceRegion();
    int getSeed();
    double getBaseFrequencyX();
    double getBaseFrequencyY();
    int getNumOctaves();
    boolean isStitched();
    boolean isFractalNoise();
    void setSeed(int seed);
    void setBaseFrequencyX(double xfreq);
    void setBaseFrequencyY(double yfreq);
    void setNumOctaves(int numOctaves);
    void setStitched(boolean stitched);
    void setFractalNoise(boolean fractalNoise);
}
