package org.apache.lucene.spatial.geometry;
import static junit.framework.Assert.*;
import org.junit.Test;
public class TestDistanceUnits {
  @Test
  public void testFindDistanceUnit() {
    assertEquals(DistanceUnits.KILOMETERS, DistanceUnits.findDistanceUnit("km"));
    assertEquals(DistanceUnits.MILES, DistanceUnits.findDistanceUnit("miles"));
  }
  @Test
  public void testFindDistanceUnit_unknownUnit() {
    try {
      DistanceUnits.findDistanceUnit("mls");
      assertTrue("IllegalArgumentException should have been thrown", false);
    } catch (IllegalArgumentException iae) {
    }
  }
  @Test
  public void testConvert() {
    assertEquals(10.5, DistanceUnits.MILES.convert(10.5, DistanceUnits.MILES), 0D);
    assertEquals(10.5, DistanceUnits.KILOMETERS.convert(10.5, DistanceUnits.KILOMETERS), 0D);
    assertEquals(10.5 * 1.609344, DistanceUnits.KILOMETERS.convert(10.5, DistanceUnits.MILES), 0D);
    assertEquals(10.5 / 1.609344, DistanceUnits.MILES.convert(10.5, DistanceUnits.KILOMETERS), 0D);
  }
}
