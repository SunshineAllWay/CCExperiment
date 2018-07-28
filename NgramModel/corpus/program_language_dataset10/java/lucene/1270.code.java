package org.apache.lucene.spatial.geometry.shape;
public class LineSegment {
  public final Point2D A = new Point2D();
  public final Point2D B = new Point2D();
  public LineSegment() {
    A.set(0, 0);
    B.set(0, 0);
  }
  public LineSegment(Point2D p1, Point2D p2) {
    A.set(p1);
    B.set(p2);
  }
  public double distance(Point2D P, Point2D closestPt) {
    if (closestPt == null)
      closestPt = new Point2D();
    Vector2D v = new Vector2D(A, B);
    Vector2D w = new Vector2D(A, P);
    double n = w.dot(v);
    if (n <= 0.0f) {
      closestPt.set(A);
      return w.norm();
    }
    double d = v.dot(v);
    if (d <= n) {
      closestPt.set(B);
      return new Vector2D(B, P).norm();
    }
    closestPt.set(v.mult(n / d));
    closestPt.add(A);
    return new Vector2D(closestPt, P).norm();
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((A == null) ? 0 : A.hashCode());
    result = prime * result + ((B == null) ? 0 : B.hashCode());
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
    LineSegment other = (LineSegment) obj;
    if (A == null) {
      if (other.A != null)
        return false;
    } else if (!A.equals(other.A))
      return false;
    if (B == null) {
      if (other.B != null)
        return false;
    } else if (!B.equals(other.B))
      return false;
    return true;
  }
}
