package org.apache.lucene.spatial.tier;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.apache.lucene.search.Filter;
import org.apache.lucene.spatial.tier.projections.CartesianTierPlotter;
import org.apache.lucene.spatial.tier.projections.IProjector;
import org.apache.lucene.spatial.tier.projections.SinusoidalProjector;
import org.apache.lucene.spatial.geometry.LatLng;
import org.apache.lucene.spatial.geometry.FloatLatLng;
import org.apache.lucene.spatial.geometry.shape.LLRect;
public class CartesianPolyFilterBuilder {
  public static final double MILES_FLOOR = 1.0;
  private IProjector projector = new SinusoidalProjector();
  private final String tierPrefix;
  public CartesianPolyFilterBuilder( String tierPrefix ) {
    this.tierPrefix = tierPrefix;
  }
  public Shape getBoxShape(double latitude, double longitude, double miles)
  {  
    if (miles < MILES_FLOOR) {
      miles = MILES_FLOOR;
    }
    LLRect box1 = LLRect.createBox( new FloatLatLng( latitude, longitude ), miles, miles );
    LatLng ll = box1.getLowerLeft();
    LatLng ur = box1.getUpperRight();
    double latY = ur.getLat();
    double latX = ll.getLat();
    double longY = ur.getLng();
    double longX = ll.getLng();
    double longX2 = 0.0;
    if (ur.getLng() < 0.0 && ll.getLng() > 0.0) {
	longX2 = ll.getLng();
 	longX = -180.0;	
    }
    if (ur.getLng() > 0.0 && ll.getLng() < 0.0) {
	longX2 = ll.getLng();
 	longX = 0.0;	
    }
    CartesianTierPlotter ctp = new CartesianTierPlotter(2, projector,tierPrefix);
    int bestFit = ctp.bestFit(miles);
    ctp = new CartesianTierPlotter(bestFit, projector,tierPrefix);
    Shape shape = new Shape(ctp.getTierFieldName());
    shape = getShapeLoop(shape,ctp,latX,longX,latY,longY);
    if (longX2 != 0.0) {
	if (longX2 != 0.0) {
		if (longX == 0.0) {
			longX = longX2;
			longY = 0.0;
        		shape = getShapeLoop(shape,ctp,latX,longX,latY,longY);
		} else {
			longX = longX2;
			longY = -180.0;
        		shape = getShapeLoop(shape,ctp,latY,longY,latX,longX);
		}
	}
    }
    return shape; 
  } 
  public Shape getShapeLoop(Shape shape, CartesianTierPlotter ctp, double latX, double longX, double latY, double longY)
  {  
    double beginAt = ctp.getTierBoxId(latX, longX);
    double endAt = ctp.getTierBoxId(latY, longY);
    double tierVert = ctp.getTierVerticalPosDivider();
    double startX = beginAt - (beginAt %1);
    double startY = beginAt - startX ; 
    double endX = endAt - (endAt %1);
    double endY = endAt -endX; 
    int scale = (int)Math.log10(tierVert);
    endY = new BigDecimal(endY).setScale(scale, RoundingMode.HALF_EVEN).doubleValue();
    startY = new BigDecimal(startY).setScale(scale, RoundingMode.HALF_EVEN).doubleValue();
    double xInc = 1.0d / tierVert;
    xInc = new BigDecimal(xInc).setScale(scale, RoundingMode.HALF_EVEN).doubleValue();
    for (; startX <= endX; startX++){
      double itY = startY;
      while (itY <= endY){
        double boxId = startX + itY ;
        shape.addBox(boxId);
        itY += xInc;
        itY = new BigDecimal(itY).setScale(scale, RoundingMode.HALF_EVEN).doubleValue();
      }
    }
    return shape;
  }
  public Filter getBoundingArea(double latitude, double longitude, double miles) 
  {
    Shape shape = getBoxShape(latitude, longitude, miles);
    return new CartesianShapeFilter(shape, shape.getTierId());
  }
}
