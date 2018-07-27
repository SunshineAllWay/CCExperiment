package org.apache.lucene.spatial.geometry.shape;
import org.apache.lucene.spatial.geometry.FloatLatLng;
import org.apache.lucene.spatial.geometry.LatLng;
public class LLRect {
  private LatLng ll, ur;
  public LLRect(LatLng ll, LatLng ur) {
    this.ll=ll;
    this.ur=ur;
  }
  public LLRect(LLRect other) {
    this.ll=other.ll;
    this.ur=other.ur;
  }
  public double area() {
    return Math.abs((ll.getLat()-ur.getLat()) * (ll.getLng()-ur.getLng()));
  }
  public LatLng getLowerLeft() {
    return ll;
  }
  public LatLng getUpperRight() {
    return ur;
  }
  @Override
  public String toString() {
    return "{" + ll + ", " + ur + "}";
  }
  public LatLng getMidpoint() {
    return ll.calculateMidpoint(ur);
  }
  public static LLRect createBox(LatLng center, double widthMi, double heightMi) {
    double d = widthMi;
    LatLng ur = boxCorners(center, d, 45.0); 
    LatLng ll = boxCorners(center, d, 225.0);
    return new LLRect(ll, ur);
  }
  public Rectangle toRectangle() {
    return new Rectangle(ll.getLng(), ll.getLat(), ur.getLng(), ur.getLat());
  }
  private static LatLng boxCorners(LatLng center, double d, double brngdeg) {
    double a = center.getLat();
    double b = center.getLng();
    double R = 3963.0; 
    double brng = (Math.PI*brngdeg/180);
    double lat1 = (Math.PI*a/180);
    double lon1 = (Math.PI*b/180);
    double lat2 = Math.asin( Math.sin(lat1)*Math.cos(d/R) +
                             Math.cos(lat1)*Math.sin(d/R)*Math.cos(brng) );
    double lon2 = lon1 + Math.atan2(Math.sin(brng)*Math.sin(d/R)*Math.cos(lat1),
                                    Math.cos(d/R)-Math.sin(lat1)*Math.sin(lat2));
    lat2 = (lat2*180)/Math.PI;
    lon2 = (lon2*180)/Math.PI;
    LatLng ll = normLng(lat2,lon2);
    ll = normLat(ll.getLat(),ll.getLng());
    return ll;
}
  private static LatLng normLat(double lat, double lng) {
    if (lat > 90.0) {
        lat = 90.0 - (lat - 90.0);
        if (lng < 0) {
                lng = lng+180;
        } else {
                lng=lng-180;
        }
    }
    else if (lat < -90.0) {
        lat = -90.0 - (lat + 90.0);
        if (lng < 0) {
                lng = lng+180;
        } else {
                lng=lng-180;
        }
    }
    LatLng ll=new FloatLatLng(lat, lng);
    return ll;
  }
  private static LatLng normLng(double lat,double lng) {
    if (lng > 180.0) {
        lng = -1.0*(180.0 - (lng - 180.0));
    }
    else if (lng < -180.0) {
        lng = (lng + 180.0)+180.0;
    }
    LatLng ll=new FloatLatLng(lat, lng);
    return ll;
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((ll == null) ? 0 : ll.hashCode());
    result = prime * result + ((ur == null) ? 0 : ur.hashCode());
    return result;
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LLRect other = (LLRect) obj;
    if (ll == null) {
      if (other.ll != null)
        return false;
    } else if (!ll.equals(other.ll))
      return false;
    if (ur == null) {
      if (other.ur != null)
        return false;
    } else if (!ur.equals(other.ur))
      return false;
    return true;
  }
}
