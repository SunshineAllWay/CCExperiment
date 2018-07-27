package org.apache.batik.ext.awt.image;
import java.awt.Color;
public interface Light {
    boolean isConstant();
    void getLight(final double x, final double y, final double z, final double[] L);
    double[][][] getLightMap(double x, double y,
                                  final double dx, final double dy,
                                  final int width, final int height,
                                  final double[][][] z);
    double[][] getLightRow(double x, double y,
                                  final double dx, final int width,
                                  final double[][] z,
                                  final double[][] lightRow);
    double[] getColor(boolean linear);
    void setColor(Color color);
}
