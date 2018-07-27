package org.apache.batik.ext.awt.image;
import java.awt.Color;
public class DistantLight extends AbstractLight {
    private double azimuth;
    private double elevation;
    private double Lx, Ly, Lz;
    public double getAzimuth(){
        return azimuth;
    }
    public double getElevation(){
        return elevation;
    }
    public DistantLight(double azimuth, double elevation, Color color){
        super(color);
        this.azimuth = azimuth;
        this.elevation = elevation;
        Lx = Math.cos( Math.toRadians( azimuth ) ) * Math.cos( Math.toRadians( elevation ) );
        Ly = Math.sin( Math.toRadians( azimuth ) ) * Math.cos( Math.toRadians( elevation ) );
        Lz = Math.sin( Math.toRadians( elevation ));
    }
    public boolean isConstant(){
        return true;
    }
    public void getLight(final double x, final double y, final double z,
                         final double[] L){
        L[0] = Lx;
        L[1] = Ly;
        L[2] = Lz;
    }
    public double[][] getLightRow(double x, double y,
                                  final double dx, final int width,
                                  final double[][] z,
                                  final double[][] lightRow) {
        double [][] ret = lightRow;
        if (ret == null) {
            ret = new double[width][];
            double[] CL = new double[3];
            CL[0]=Lx;
            CL[1]=Ly;
            CL[2]=Lz;
            for(int i=0; i<width; i++){
                ret[i] = CL;
            }
        } else {
            final double lx = Lx;
            final double ly = Ly;
            final double lz = Lz;
            for(int i=0; i<width; i++){
                ret[i][0] = lx;
                ret[i][1] = ly;
                ret[i][2] = lz;
            }
        }
        return ret;
    }
}
