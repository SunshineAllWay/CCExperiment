package org.apache.lucene.spatial.geometry.shape;
public interface Geometry2D {
  public void translate(Vector2D v);
  public boolean contains(Point2D p);
  public double area();
  public Point2D centroid();
  public IntersectCase intersect(Rectangle r);
}
