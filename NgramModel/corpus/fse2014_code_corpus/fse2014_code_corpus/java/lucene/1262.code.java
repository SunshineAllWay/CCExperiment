package org.apache.lucene.spatial.geometry;
public enum DistanceUnits {
  MILES("miles", 3959, 24902),
  KILOMETERS("km", 6371, 40076);
  private static final double MILES_KILOMETRES_RATIO = 1.609344;
  private final String unit;
  private final double earthCircumference;
  private final double earthRadius;
  DistanceUnits(String unit, double earthRadius, double earthCircumfence) {
    this.unit = unit;
    this.earthCircumference = earthCircumfence;
    this.earthRadius = earthRadius;
  }
  public static DistanceUnits findDistanceUnit(String unit) {
    if (MILES.getUnit().equals(unit)) {
      return MILES;
    }
    if (KILOMETERS.getUnit().equals(unit)) {
      return KILOMETERS;
    }
    throw new IllegalArgumentException("Unknown distance unit " + unit);
  }
  public double convert(double distance, DistanceUnits from) {
    if (from == this) {
      return distance;
    }
    return (this == MILES) ? distance / MILES_KILOMETRES_RATIO : distance * MILES_KILOMETRES_RATIO;
  }
  public String getUnit() {
    return unit;
  }
  public double earthRadius() {
    return earthRadius;
  }
  public double earthCircumference() {
    return earthCircumference;
  }
}
