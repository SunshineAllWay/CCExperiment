package org.apache.lucene.spatial.tier;
import org.apache.lucene.spatial.geometry.DistanceUnits;
import org.apache.lucene.spatial.geometry.FloatLatLng;
import org.apache.lucene.spatial.geometry.LatLng;
import org.apache.lucene.spatial.geometry.shape.LLRect;
import org.apache.lucene.spatial.geometry.shape.Rectangle;
public class DistanceUtils {
  static DistanceUtils instance = new DistanceUtils();
  public static DistanceUtils getInstance()
  {
    return instance;
  }
  public double getDistanceMi(double x1, double y1, double x2, double y2) {
    return getLLMDistance(x1, y1, x2, y2);
  }
  public Rectangle getBoundary (double x1, double y1, double miles) {
    LLRect box = LLRect.createBox( new FloatLatLng( x1, y1 ), miles, miles );
    return box.toRectangle();
  }
  public double getLLMDistance (double x1, double y1, double x2, double y2) {  
    LatLng p1 = new FloatLatLng( x1, y1 );
    LatLng p2 = new FloatLatLng( x2, y2 );
    return p1.arcDistance( p2, DistanceUnits.MILES );
  }
}
