package org.apache.batik.ext.awt.image;
import java.awt.Color;
public class SpotLight extends AbstractLight {
    private double lightX, lightY, lightZ;
    private double pointAtX, pointAtY, pointAtZ;
    private double specularExponent;
    private double limitingConeAngle, limitingCos;
    private final double[] S = new double[3];
    public double getLightX(){
        return lightX;
    }
    public double getLightY(){
        return lightY;
    }
    public double getLightZ(){
        return lightZ;
    }
    public double getPointAtX(){
        return pointAtX;
    }
    public double getPointAtY(){
        return pointAtY;
    }
    public double getPointAtZ(){
        return pointAtZ;
    }
    public double getSpecularExponent(){
        return specularExponent;
    }
    public double getLimitingConeAngle(){
        return limitingConeAngle;
    }
    public SpotLight(double lightX, double lightY, double lightZ,
                     double pointAtX, double pointAtY, double pointAtZ,
                     double specularExponent, double limitingConeAngle,
                     Color lightColor){
        super(lightColor);
        this.lightX = lightX;
        this.lightY = lightY;
        this.lightZ = lightZ;
        this.pointAtX = pointAtX;
        this.pointAtY = pointAtY;
        this.pointAtZ = pointAtZ;
        this.specularExponent = specularExponent;
        this.limitingConeAngle = limitingConeAngle;
        this.limitingCos = Math.cos( Math.toRadians( limitingConeAngle ) );
        S[0] = pointAtX - lightX;
        S[1] = pointAtY - lightY;
        S[2] = pointAtZ - lightZ;
        double invNorm = 1/Math.sqrt(S[0]*S[0] + S[1]*S[1] + S[2]*S[2]);
        S[0] *= invNorm;
        S[1] *= invNorm;
        S[2] *= invNorm;
    }
    public boolean isConstant(){
        return false;
    }
    public final double getLightBase(final double x, final double y,
                                     final double z,
                                     final double[] L){
        double L0 = lightX - x;
        double L1 = lightY - y;
        double L2 = lightZ - z;
        final double invNorm = 1.0/Math.sqrt( L0*L0 + L1*L1 + L2*L2 );
        L0 *= invNorm;
        L1 *= invNorm;
        L2 *= invNorm;
        double LS = -(L0*S[0] + L1*S[1] + L2*S[2]);
        L[0] = L0;
        L[1] = L1;
        L[2] = L2;
        if(LS <= limitingCos){
            return 0;
        } else {
            double Iatt = limitingCos/LS;
            Iatt *= Iatt;
            Iatt *= Iatt;
            Iatt *= Iatt;
            Iatt *= Iatt;
            Iatt *= Iatt;
            Iatt *= Iatt; 
            Iatt = 1 - Iatt;
            return Iatt*Math.pow(LS, specularExponent);
        }
    }
    public final void getLight(final double x, final double y,
                               final double z,
                               final double[] L){
        final double s = getLightBase(x, y, z, L);
        L[0] *= s;
        L[1] *= s;
        L[2] *= s;
    }
    public final void getLight4(final double x, final double y, final double z,
                               final double[] L){
        L[3] = getLightBase(x, y, z, L);
    }
    public double[][] getLightRow4(double x, double y,
                                  final double dx, final int width,
                                  final double[][] z,
                                  final double[][] lightRow) {
        double [][] ret = lightRow;
        if (ret == null)
            ret = new double[width][4];
        for(int i=0; i<width; i++){
            getLight4(x, y, z[i][3], ret[i]);
            x += dx;
        }
        return ret;
    }
}
