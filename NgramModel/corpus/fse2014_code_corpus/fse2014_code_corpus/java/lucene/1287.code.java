package org.apache.lucene.spatial.tier.projections;
public class SinusoidalProjector implements IProjector {
  public String coordsAsString(double latitude, double longitude) {
    return null;
  }
  public double[] coords(double latitude, double longitude) {
    double rlat = Math.toRadians(latitude);
    double rlong = Math.toRadians(longitude);
    double nlat = rlong * Math.cos(rlat);
    double r[] = {nlat, rlong};
    return r;
  }
}
