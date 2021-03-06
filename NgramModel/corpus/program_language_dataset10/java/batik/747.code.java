package org.apache.batik.ext.awt.image;
import java.awt.Color;
public abstract class AbstractLight implements Light {
    public static final double sRGBToLsRGB(double value) {
        if(value <= 0.003928)
            return value/12.92;
        return Math.pow((value+0.055)/1.055, 2.4);
    }
    private double[] color;
    public double[] getColor(boolean linear){
        double [] ret = new double[3];
        if (linear) {
            ret[0] = sRGBToLsRGB(color[0]);
            ret[1] = sRGBToLsRGB(color[1]);
            ret[2] = sRGBToLsRGB(color[2]);
        } else {
            ret[0] = color[0];
            ret[1] = color[1];
            ret[2] = color[2];
        }
        return ret;
    }
    public AbstractLight(Color color){
        setColor(color);
    }
    public void setColor(Color newColor){
        color = new double[3];
        color[0] = newColor.getRed()  /255.;
        color[1] = newColor.getGreen()/255.;
        color[2] = newColor.getBlue() /255.;
    }
    public boolean isConstant(){
        return true;
    }
    public double[][][] getLightMap(double x, double y, 
                                    final double dx, final double dy,
                                    final int width, final int height,
                                    final double[][][] z)
    {
        double[][][] L = new double[height][][];
        for(int i=0; i<height; i++){
            L[i] = getLightRow(x, y, dx, width, z[i], null);
            y += dy;
        }
        return L;
    }
    public double[][] getLightRow(double x, double y, 
                                  final double dx, final int width,
                                  final double[][] z,
                                  final double[][] lightRow) {
        double [][] ret = lightRow;
        if (ret == null) 
            ret = new double[width][3];
        for(int i=0; i<width; i++){
            getLight(x, y, z[i][3], ret[i]);
            x += dx;
        }
        return ret;
    }
}
