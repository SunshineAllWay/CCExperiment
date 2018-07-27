package org.apache.lucene.spatial.geometry.shape;
public class Vector2D {
  private double x;
  private double y;
  public Vector2D(double x, double y) {
    this.x = x;
    this.y = y;
  }
  public Vector2D(Point2D p) {
    this(p.getX(), p.getY());
  }
  public Vector2D(Point2D from, Point2D to) {
    this(to.getX() - from.getX(), to.getY() - from.getY());
  }
  public Vector2D() {
    this.x = 0;
    this.y = 0;
  }
  public Vector2D(Vector2D other) {
    this.x = other.x;
    this.y = other.y;
  }
  public double getX() {
    return x;
  }
  public double getY() {
    return y;
  }
  public void setX(double x) {
    this.x = x;
  }
  public void setY(double y) {
    this.y = y;
  }
  public void set(double x, double y) {
    this.x = x;
    this.y = y;
  }
  public boolean equals(Vector2D other) {
    return other != null && x == other.x && y == other.y;
  }
  public double dot(Vector2D in) {
    return ((x) * in.x) + (y * in.y);
  }
  public double normSqr() {
    return (x * x) + (y * y);
  }
  public double norm() {
    return Math.sqrt(normSqr());
  }
  public Vector2D mult(double d) {
    return new Vector2D(x*d, y*d);
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
    Vector2D other = (Vector2D) obj;
    if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
      return false;
    if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
      return false;
    return true;
  }
}
