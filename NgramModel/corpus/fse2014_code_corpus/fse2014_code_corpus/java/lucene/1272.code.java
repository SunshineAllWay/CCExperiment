package org.apache.lucene.spatial.geometry.shape;
public class Point2D {
  private double x;
  private double y;
  public Point2D(double x, double y) {
    this.x=x;
    this.y=y;
  }
  public Point2D() {
    this.x=0;
    this.y=0;
  }
  public Point2D(Point2D other) {
    this.x=other.x;
    this.y=other.y;
  }
  @Override
  public String toString() {
    return "(" + x + "," + y + ")";
  }
  public double getX() {
    return x;
  }
  public double getY() {
    return y;
  }
  public double x() {
    return x;
  }
  public double y() {
    return y;
  }
  public void x(double x) {
    this.x=x;
  }
  public void y(double y) {
    this.y=y;
  }
  public void setX(double x) {
    this.x = x;
  }
  public void setY(double y) {
    this.y = y;
  }
  public void set(double x, double y) {
    this.x=x;
    this.y=y;
  }
  public void add(Vector2D v) {
    this.x+=v.getX();
    this.y+=v.getY();
  }
  public void set(Point2D p1) {
    this.x=p1.getX();
    this.y=p1.getY();
  }
  public void add(Point2D a) {
    this.x+=a.getX();
    this.y+=a.getY();
  }
  public void set(Vector2D v) {
    this.x=v.getX();
    this.y=v.getY();
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(x);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(y);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    Point2D other = (Point2D) obj;
    if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
      return false;
    if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
      return false;
    return true;
  }
}
