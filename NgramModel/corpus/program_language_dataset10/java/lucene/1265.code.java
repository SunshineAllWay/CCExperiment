package org.apache.lucene.spatial.geometry;
public abstract class LatLng {
  public abstract boolean isNormalized();
  public abstract boolean isFixedPoint();
  public abstract LatLng normalize();
  public abstract int getFixedLat();
  public abstract int getFixedLng();
  public abstract double getLat();
  public abstract double getLng();
  public abstract LatLng copy();
  public abstract FixedLatLng toFixed();
  public abstract FloatLatLng toFloat();
  public CartesianPoint toCartesian() {
    LatLng ll=normalize();
    int lat=ll.getFixedLat();
    int lng=ll.getFixedLng();
    return new CartesianPoint(
        lng+180*FixedLatLng.SCALE_FACTOR_INT,
        lat+90*FixedLatLng.SCALE_FACTOR_INT
    );
  }
  public static LatLng fromCartesian(CartesianPoint pt) {
    int lat=pt.getY() - 90 * FixedLatLng.SCALE_FACTOR_INT;
    int lng=pt.getX() - 180 * FixedLatLng.SCALE_FACTOR_INT;
    return new FixedLatLng(lat, lng);
  }
  public double arcDistance(LatLng ll2) {
    return arcDistance(ll2, DistanceUnits.MILES);
  }
  public double arcDistance(LatLng ll2, DistanceUnits lUnits) {
    LatLng ll1 = normalize();
    ll2 = ll2.normalize();
    double lat1 = ll1.getLat(), lng1 = ll1.getLng();
    double lat2 = ll2.getLat(), lng2 = ll2.getLng();
    if (lat1 == lat2 && lng1 == lng2)
      return 0.0;
    double dLon = lng2 - lng1;
    double a = radians(90.0 - lat1);
    double c = radians(90.0 - lat2);
    double cosB = (Math.cos(a) * Math.cos(c))
        + (Math.sin(a) * Math.sin(c) * Math.cos(radians(dLon)));
    double radius = (lUnits == DistanceUnits.MILES) ? 3963.205
    : 6378.160187;
    if (cosB < -1.0)
      return 3.14159265358979323846* radius;
    else if (cosB >= 1.0)
      return 0;
    else
      return Math.acos(cosB) * radius;
  }
  private double radians(double a) {
    return a * 0.01745329251994;
  }
  @Override
  public String toString() {
    return "[" + getLat() + "," + getLng() + "]";
  }
  public abstract LatLng calculateMidpoint(LatLng other);
  @Override
  public abstract int hashCode();
  @Override
  public abstract boolean equals(Object obj);
}
