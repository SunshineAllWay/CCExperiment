package org.apache.solr.schema;
import org.apache.solr.schema.DateField;
import org.apache.solr.util.DateMathParser;
import org.apache.lucene.document.Fieldable;
import java.util.Date;
import java.util.TimeZone;
import java.util.Locale;
import java.text.DateFormat;
import junit.framework.TestCase;
public class DateFieldTest extends LegacyDateFieldTest {
  public void setUp()  throws Exception {
    super.setUp();
    f = new DateField();
  }
  public void testToInternal() throws Exception {
    assertToI("1995-12-31T23:59:59.999", "1995-12-31T23:59:59.999666Z");
    assertToI("1995-12-31T23:59:59.999", "1995-12-31T23:59:59.999Z");
    assertToI("1995-12-31T23:59:59.99",  "1995-12-31T23:59:59.99Z");
    assertToI("1995-12-31T23:59:59.9",   "1995-12-31T23:59:59.9Z");
    assertToI("1995-12-31T23:59:59",     "1995-12-31T23:59:59Z");
    assertToI("1995-12-31T23:59:59.99",  "1995-12-31T23:59:59.990Z");
    assertToI("1995-12-31T23:59:59.9",   "1995-12-31T23:59:59.900Z");
    assertToI("1995-12-31T23:59:59.9",   "1995-12-31T23:59:59.90Z");
    assertToI("1995-12-31T23:59:59",     "1995-12-31T23:59:59.000Z");
    assertToI("1995-12-31T23:59:59",     "1995-12-31T23:59:59.00Z");
    assertToI("1995-12-31T23:59:59",     "1995-12-31T23:59:59.0Z");
    assertToI(f.toInternal(p.parseMath("/DAY")), "NOW/DAY");
    assertToI("1995-12-31T00:00:00", "1995-12-31T23:59:59Z/DAY");
    assertToI("1995-12-31T00:00:00", "1995-12-31T23:59:59.123Z/DAY");
    assertToI("1995-12-31T00:00:00", "1995-12-31T23:59:59.123999Z/DAY");
  }
  public void testToInternalObj() throws Exception {
    assertToI("1995-12-31T23:59:59.999", 820454399999l);
    assertToI("1995-12-31T23:59:59.99",  820454399990l);
    assertToI("1995-12-31T23:59:59.9",   820454399900l);
    assertToI("1995-12-31T23:59:59",     820454399000l);
  }
  public void assertParseMath(long expected, String input) {
    Date d = new Date(0);
    assertEquals("Input: "+input, expected, f.parseMath(d, input).getTime());
  }
  public void testParseMath() {
    assertParseMath(820454699999l, "1995-12-31T23:59:59.999765Z+5MINUTES");
    assertParseMath(820454699999l, "1995-12-31T23:59:59.999Z+5MINUTES");
    assertParseMath(820454699990l, "1995-12-31T23:59:59.99Z+5MINUTES");
    assertParseMath(194918400000l, "1976-03-06T03:06:00Z/DAY");
    assertParseMath(820454699990l, "1995-12-31T23:59:59.990Z+5MINUTES");
    assertParseMath(194918400000l, "1976-03-06T03:06:00.0Z/DAY");
    assertParseMath(194918400000l, "1976-03-06T03:06:00.00Z/DAY");
    assertParseMath(194918400000l, "1976-03-06T03:06:00.000Z/DAY");
  }
  public void assertToObject(long expected, String input) throws Exception {
    assertEquals("Input: "+input, expected, f.toObject(input).getTime());
  }
  public void testToObject() throws Exception {
    assertToObject(820454399987l, "1995-12-31T23:59:59.987666Z");
    assertToObject(820454399987l, "1995-12-31T23:59:59.987Z");
    assertToObject(820454399980l, "1995-12-31T23:59:59.98Z");
    assertToObject(820454399900l, "1995-12-31T23:59:59.9Z");
    assertToObject(820454399000l, "1995-12-31T23:59:59Z");
  }
  public void testFormatter() {
    assertEquals("1970-01-01T00:00:00.005", f.formatDate(new Date(5)));
    assertEquals("1970-01-01T00:00:00",     f.formatDate(new Date(0)));
    assertEquals("1970-01-01T00:00:00.37",  f.formatDate(new Date(370)));
    assertEquals("1970-01-01T00:00:00.9",   f.formatDate(new Date(900)));
  }
}
