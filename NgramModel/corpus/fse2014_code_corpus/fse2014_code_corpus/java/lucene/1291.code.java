package org.apache.lucene.spatial.tier;
public class PolyShape {
  private static double lat = 38.969398; 
  private static double lng= -77.386398;
  private static int miles = 1000;
  public static void main(String[] args) {
    CartesianPolyFilterBuilder cpf = new CartesianPolyFilterBuilder( "_localTier" );
    cpf.getBoxShape(lat, lng, miles);
  }
}
