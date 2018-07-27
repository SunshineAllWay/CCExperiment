package org.apache.batik.ext.awt.image;
import java.awt.Color;
public class PointLight extends AbstractLight {
    private double lightX, lightY, lightZ;
    public double getLightX(){
        return lightX;
    }
    public double getLightY(){
        return lightY;
    }
    public double getLightZ(){
        return lightZ;
    }
    public PointLight(double lightX, double lightY, double lightZ,
                      Color lightColor){
        super(lightColor);
        this.lightX = lightX;
        this.lightY = lightY;
        this.lightZ = lightZ;
    }
    public boolean isConstant(){
        return false;
    }
    public final void getLight(final double x, final double y, final double z,
                               final double[] L){
        double L0 = lightX - x;
        double L1 = lightY - y;
        double L2 = lightZ - z;
        final double norm = Math.sqrt( L0*L0 + L1*L1 + L2*L2 );
        if(norm > 0){
            final double invNorm = 1.0/norm;
            L0 *= invNorm;
            L1 *= invNorm;
            L2 *= invNorm;
        }
        L[ 0 ] = L0;
        L[ 1 ] = L1;
        L[ 2 ] = L2;
    }
}
