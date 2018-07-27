package org.apache.lucene.spatial.geometry;
public class FloatLatLng extends LatLng {
  private double lat;
  private double lng;
  private boolean normalized;
  public FloatLatLng(double lat, double lng) {
    if (lat>90.0 || lat<-90.0) throw new IllegalArgumentException("Illegal latitude value " + lat);
    this.lat=lat;
    this.lng=lng;
  }
  public FloatLatLng(LatLng ll) {
    this.lat=ll.getLat();
    this.lng=ll.getLng();
  }
  @Override
  public LatLng copy() {
    return new FloatLatLng(this);
  }
  @Override
  public int getFixedLat() {
    return FixedLatLng.doubleToFixed(this.lat);
  }
  @Override
  public int getFixedLng() {
    return FixedLatLng.doubleToFixed(this.lng);
  }
  @Override
  public double getLat() {
    return this.lat;
  }
  @Override
  public double getLng() {
    return this.lng;
  }
  @Override
  public boolean isFixedPoint() {
    return false;
  }
  @Override
  public FixedLatLng toFixed() {
    return new FixedLatLng(this);
  }
  @Override
  public FloatLatLng toFloat() {
    return this;
  }
  @Override
  public boolean isNormalized() {
    return 
      normalized || (
          (lng>=-180) &&
          (lng<=180)
          );
  }
  @Override
  public LatLng normalize() {
    if (isNormalized()) return this;
    double delta=0;
    if (lng<0) delta=360;
    if (lng>=0) delta=-360;
    double newLng=lng;
    while (newLng<=-180 || newLng>=180) {
      newLng+=delta;
    }
    FloatLatLng ret=new FloatLatLng(lat, newLng);
    ret.normalized=true;
    return ret;
  }
  @Override
  public LatLng calculateMidpoint(LatLng other) {
    return new FloatLatLng(
        (lat+other.getLat())/2.0,
        (lng+other.getLng())/2.0);
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    long temp;
    temp = Double.doubleToLongBits(lat);
    int result = prime  + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(lng);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (normalized ? 1231 : 1237);
    return result;
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (getClass() != obj.getClass())
      return false;
    FloatLatLng other = (FloatLatLng) obj;
    if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat))
      return false;
    if (Double.doubleToLongBits(lng) != Double.doubleToLongBits(other.lng))
      return false;
    if (normalized != other.normalized)
      return false;
    return true;
  }
}
