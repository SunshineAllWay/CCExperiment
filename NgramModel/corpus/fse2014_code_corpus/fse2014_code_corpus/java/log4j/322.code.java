package org.apache.log4j.pattern;
import junit.framework.TestCase;
public class NameAbbreviatorTest extends TestCase {
  public NameAbbreviatorTest(final String name) {
    super(name);
  }
  public void testGetDefault() {
    NameAbbreviator abbrev = NameAbbreviator.getDefaultAbbreviator();
    assertNotNull(abbrev);
  }
  public void testZero() {
    NameAbbreviator abbrev = NameAbbreviator.getAbbreviator("0");
    StringBuffer buf = new StringBuffer("DEBUG - ");
    int fieldStart = buf.length();
    buf.append("org.example.foo.bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - ", buf.toString());
  }
  public void testBlank() {
    NameAbbreviator abbrev = NameAbbreviator.getAbbreviator("   ");
    NameAbbreviator defaultAbbrev = NameAbbreviator.getDefaultAbbreviator();
    assertTrue(abbrev == defaultAbbrev);
  }
  public void testOne() {
    NameAbbreviator abbrev = NameAbbreviator.getAbbreviator("1");
    StringBuffer buf = new StringBuffer("DEBUG - ");
    int fieldStart = buf.length();
    buf.append("org.example.foo.bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - bar", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    buf.append("bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - bar", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - ", buf.toString());
  }
  public void testBlankOne() {
    NameAbbreviator abbrev = NameAbbreviator.getAbbreviator(" 1 ");
    StringBuffer buf = new StringBuffer("DEBUG - ");
    int fieldStart = buf.length();
    buf.append("org.example.foo.bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - bar", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    buf.append("bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - bar", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - ", buf.toString());
  }
  public void testTwo() {
    NameAbbreviator abbrev = NameAbbreviator.getAbbreviator("2");
    StringBuffer buf = new StringBuffer("DEBUG - ");
    int fieldStart = buf.length();
    buf.append("org.example.foo.bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - foo.bar", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    buf.append("foo.bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - foo.bar", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    buf.append("bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - bar", buf.toString());
  }
  public void testOneDot() {
    NameAbbreviator abbrev = NameAbbreviator.getAbbreviator("1.");
    StringBuffer buf = new StringBuffer("DEBUG - ");
    int fieldStart = buf.length();
    buf.append("org.example.foo.bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - o.e.f.bar", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    buf.append("org.example.foo.");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - o.e.f.", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    buf.append("foo.bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - f.bar", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    buf.append("bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - bar", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - ", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    buf.append(".");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - .", buf.toString());
  }
  public void testOneTildeDot() {
    NameAbbreviator abbrev = NameAbbreviator.getAbbreviator("1~.");
    StringBuffer buf = new StringBuffer("DEBUG - ");
    int fieldStart = buf.length();
    buf.append("org.example.foo.bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - o~.e~.f~.bar", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    buf.append("org.example.foo.");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - o~.e~.f~.", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    buf.append("foo.bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - f~.bar", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    buf.append("bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - bar", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - ", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    buf.append(".");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - .", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    buf.append("o.e.f.bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - o.e.f.bar", buf.toString());
  }
  public void testMulti() {
    NameAbbreviator abbrev = NameAbbreviator.getAbbreviator("1.*.2");
    StringBuffer buf = new StringBuffer("DEBUG - ");
    int fieldStart = buf.length();
    buf.append("org.example.foo.bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - o.example.fo.bar", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    buf.append("org.example.foo.");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - o.example.fo.", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    buf.append("foo.bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - f.bar", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    buf.append("bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - bar", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - ", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    buf.append(".");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - .", buf.toString());
  }
  public void testMinusOne() {
    NameAbbreviator abbrev = NameAbbreviator.getAbbreviator("-1");
    StringBuffer buf = new StringBuffer("DEBUG - ");
    int fieldStart = buf.length();
    buf.append("org.example.foo.bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - example.foo.bar", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    buf.append("bar");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - bar", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - ", buf.toString());
    buf.setLength(0);
    buf.append("DEBUG - ");
    fieldStart = buf.length();
    buf.append(".");
    abbrev.abbreviate(fieldStart, buf);
    assertEquals("DEBUG - ", buf.toString());
  }
}
