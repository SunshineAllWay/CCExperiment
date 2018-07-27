package org.apache.lucene.spatial.geometry.shape;
public class Ellipse implements Geometry2D {
  private Point2D center;
  private double a;
  private double b;
  private double k1, k2, k3;
  private double s;
  private double c;
  public Ellipse() {
    center = new Point2D(0, 0);
  }
  private double SQR(double d) {
    return d * d;
  }
  public Ellipse(Point2D p1, Point2D p2, double angle) {
    center = new Point2D();
    center.x((p1.x() + p2.x()) * 0.5f);
    center.y((p1.y() + p2.y()) * 0.5f);
    double angleRad = Math.toRadians(angle);
    c = Math.cos(angleRad);
    s = Math.sin(angleRad);
    double dx = Math.abs(p2.x() - p1.x()) * 0.5;
    double dy = Math.abs(p2.y() - p1.y()) * 0.5;
    if (dx >= dy) {
      a = dx;
      b = dy;
    } else {
      a = dy;
      b = dx;
    }
    k1 = SQR(c / a) + SQR(s / b);
    k2 = 2 * s * c * ((1 / SQR(a)) - (1 / SQR(b)));
    k3 = SQR(s / a) + SQR(c / b);
  }
  public int intersect(LineSegment seg, Point2D pt0, Point2D pt1) {
    if (pt0 == null)
      pt0 = new Point2D();
    if (pt1 == null)
      pt1 = new Point2D();
    double x1 = center.x();
    double y1 = center.y();
    double u1 = seg.A.x();
    double v1 = seg.A.y();
    double u2 = seg.B.x();
    double v2 = seg.B.y();
    double dx = u2 - u1;
    double dy = v2 - v1;
    double q0 = k1 * SQR(u1 - x1) + k2 * (u1 - x1) * (v1 - y1) + k3
        * SQR(v1 - y1) - 1;
    double q1 = (2 * k1 * dx * (u1 - x1)) + (k2 * dx * (v1 - y1))
        + (k2 * dy * (u1 - x1)) + (2 * k3 * dy * (v1 - y1));
    double q2 = (k1 * SQR(dx)) + (k2 * dx * dy) + (k3 * SQR(dy));
    double d = SQR(q1) - (4 * q0 * q2);
    if (d < 0) {
      return 0;
    }
    if (d == 0) {
      double t = -q1 / (2 * q2);
      if (0 <= t && t <= 1) {
        pt0.x(u1 + t * dx);
        pt0.y(v1 + t * dy);
        return 1;
      } else
        return 0;
    } else {
      int n = 0;
      double q = Math.sqrt(d);
      double t = (-q1 - q) / (2 * q2);
      if (0 <= t && t <= 1) {
        pt0.x(u1 + t * dx);
        pt0.y(v1 + t * dy);
        n++;
      }
      t = (-q1 + q) / (2 * q2);
      if (0 <= t && t <= 1) {
        if (n == 0) {
          pt0.x(u1 + t * dx);
          pt0.y(v1 + t * dy);
          n++;
        } else {
          pt1.x(u1 + t * dx);
          pt1.y(v1 + t * dy);
          n++;
        }
      }
      return n;
    }
  }
  public IntersectCase intersect(Rectangle r) {
    Point2D ul = new Point2D(r.MinPt().x(), r.MaxPt().y());
    Point2D ur = new Point2D(r.MaxPt().x(), r.MaxPt().y());
    Point2D ll = new Point2D(r.MinPt().x(), r.MinPt().y());
    Point2D lr = new Point2D(r.MaxPt().x(), r.MinPt().y());
    if (contains(ul) && contains(ur) && contains(ll) && contains(lr))
      return IntersectCase.CONTAINS;
    Point2D pt0 = new Point2D(), pt1 = new Point2D();
    LineSegment bottom = new LineSegment(ll, lr);
    if (intersect(bottom, pt0, pt1) > 0)
      return IntersectCase.INTERSECTS;
    LineSegment top = new LineSegment(ul, ur);
    if (intersect(top, pt0, pt1) > 0)
      return IntersectCase.INTERSECTS;
    LineSegment left = new LineSegment(ll, ul);
    if (intersect(left, pt0, pt1) > 0)
      return IntersectCase.INTERSECTS;
    LineSegment right = new LineSegment(lr, ur);
    if (intersect(right, pt0, pt1) > 0)
      return IntersectCase.INTERSECTS;
    return (r.contains(center)) ? IntersectCase.WITHIN
        : IntersectCase.OUTSIDE;
  }
  public double area() {
    throw new UnsupportedOperationException();
  }
  public Point2D centroid() {
    throw new UnsupportedOperationException();
  }
  public boolean contains(Point2D pt) {
    double dx = pt.x() - center.x();
    double dy = pt.y() - center.y();
    double eq=(((k1 * SQR(dx)) + (k2 * dx * dy) + (k3 * SQR(dy)) - 1));
    return eq<=0;
  }
  public void translate(Vector2D v) {
    throw new UnsupportedOperationException();
  }
}
