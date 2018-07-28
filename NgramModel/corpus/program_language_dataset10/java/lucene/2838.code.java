package org.apache.solr.schema;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.spatial.tier.CartesianPolyFilterBuilder;
import org.apache.lucene.spatial.tier.Shape;
import org.apache.solr.core.SolrCore;
import org.apache.solr.util.AbstractSolrTestCase;
import org.apache.solr.common.SolrException;
import org.apache.solr.search.function.ValueSource;
import java.util.Map;
import java.util.Random;
import java.util.List;
public class PolyFieldTest extends AbstractSolrTestCase {
  @Override
  public String getSchemaFile() {
    return "schema.xml";
  }
  @Override
  public String getSolrConfigFile() {
    return "solrconfig.xml";
  }
  public void testSchemaBasics() throws Exception {
    IndexSchema schema = h.getCore().getSchema();
    SchemaField home = schema.getField("home");
    assertNotNull(home);
    assertTrue(home.isPolyField());
    SchemaField[] dynFields = schema.getDynamicFieldPrototypes();
    boolean seen = false;
    for (SchemaField dynField : dynFields) {
      if (dynField.getName().equals("*" + FieldType.POLY_FIELD_SEPARATOR + "double")) {
        seen = true;
      }
    }
    assertTrue("Didn't find the expected dynamic field", seen);
    FieldType homeFT = schema.getFieldType("home");
    assertEquals(home.getType(), homeFT);
    FieldType xy = schema.getFieldTypeByName("xy");
    assertNotNull(xy);
    assertTrue(xy instanceof PointType);
    assertTrue(xy.isPolyField());
    home = schema.getFieldOrNull("home_0" + FieldType.POLY_FIELD_SEPARATOR + "double");
    assertNotNull(home);
    home = schema.getField("home");
    assertNotNull(home);
    home = schema.getField("homed");
    assertNotNull(home);
    assertTrue(home.isPolyField());
  }
  public void testPointFieldType() throws Exception {
    SolrCore core = h.getCore();
    IndexSchema schema = core.getSchema();
    SchemaField home = schema.getField("home");
    assertNotNull(home);
    assertTrue("home is not a poly field", home.isPolyField());
    FieldType tmp = home.getType();
    assertTrue(tmp instanceof PointType);
    PointType pt = (PointType) tmp;
    assertEquals(pt.getDimension(), 2);
    double[] xy = new double[]{35.0, -79.34};
    String point = xy[0] + "," + xy[1];
    Fieldable[] fields = home.createFields(point, 2);
    assertEquals(fields.length, 3);
    for (int i = 0; i < 3; i++) {
      boolean hasValue = fields[1].tokenStreamValue() != null
              || fields[1].getBinaryValue() != null
              || fields[1].stringValue() != null;
      assertTrue("Doesn't have a value: " + fields[1], hasValue);
    }
    home = schema.getField("home_ns");
    assertNotNull(home);
    fields = home.createFields(point, 2);
    assertEquals(fields.length, 2);
    home = schema.getField("home_ns");
    assertNotNull(home);
    try {
      fields = home.createFields("35.0,foo", 2);
      assertTrue(false);
    } catch (Exception e) {
    }
    SchemaField s1 = schema.getField("test_p");
    SchemaField s2 = schema.getField("test_p");
    ValueSource v1 = s1.getType().getValueSource(s1, null);
    ValueSource v2 = s2.getType().getValueSource(s2, null);
    assertEquals(v1, v2);
    assertEquals(v1.hashCode(), v2.hashCode());
  }
  public void testSearching() throws Exception {
    for (int i = 0; i < 50; i++) {
      assertU(adoc("id", "" + i, "home", i + "," + (i * 100), "homed", (i * 1000) + "," + (i * 10000)));
    }
    assertU(commit());
    assertQ(req("fl", "*,score", "q", "*:*"), "//*[@numFound='50']");
    assertQ(req("fl", "*,score", "q", "home:1,100"),
            "//*[@numFound='1']",
            "//str[@name='home'][.='1,100']");
    assertQ(req("fl", "*,score", "q", "homed:1000,10000"),
            "//*[@numFound='1']",
            "//str[@name='homed'][.='1000,10000']");
    assertQ(req("fl", "*,score", "q",
            "{!func}sqedist(home, vector(0, 0))"),
            "\"//*[@numFound='50']\"");
    assertQ(req("fl", "*,score", "q",
            "{!func}dist(2, home, vector(0, 0))"),
            "\"//*[@numFound='50']\"");
    assertQ(req("fl", "*,score", "q",
            "home:[10,10000 TO 30,30000]"),
            "\"//*[@numFound='3']\"");
    assertQ(req("fl", "*,score", "q",
            "homed:[1,1000 TO 2000,35000]"),
            "\"//*[@numFound='2']\"");
    assertQEx("Query should throw an exception due to incorrect dimensions", req("fl", "*,score", "q",
            "homed:[1 TO 2000]"), SolrException.ErrorCode.BAD_REQUEST);
  }
  public void testSearchDetails() throws Exception {
    SolrCore core = h.getCore();
    IndexSchema schema = core.getSchema();
    double[] xy = new double[]{35.0, -79.34};
    String point = xy[0] + "," + xy[1];
    assertU(adoc("id", "0", "home_ns", point));
    assertU(commit());
    SchemaField home = schema.getField("home_ns");
    PointType pt = (PointType) home.getType();
    assertEquals(pt.getDimension(), 2);
    Query q = pt.getFieldQuery(null, home, point);
    assertNotNull(q);
    assertTrue(q instanceof BooleanQuery);
    BooleanQuery bq = (BooleanQuery) q;
    BooleanClause[] clauses = bq.getClauses();
    assertEquals(clauses.length, 2);
  }
  public void testCartesian() throws Exception {
    for (int i = 40; i < 50; i++) {
      for (int j = -85; j < -79; j++) {
        assertU(adoc("id", "" + i, "home_tier",
                i + "," + j));
      }
    }
    assertU(commit());
    CartesianPolyFilterBuilder cpfb = new CartesianPolyFilterBuilder("");
    final Shape shape = cpfb.getBoxShape(45, -80, 10);
    final List<Double> boxIds = shape.getArea();
    StringBuilder qry = new StringBuilder();
    boolean first = true;
    for (Double boxId : boxIds) {
      if (first == true){
        first = false;
      } else {
        qry.append(" OR ");
      }
      qry.append("home_tier:");
      if (boxId < 0) {
        qry.append('\\').append(boxId);
      } else {
        qry.append(boxId);
      }
    }
    assertQ(req("fl", "*,score", "q", qry.toString()),
            "//*[@numFound='1']");
  }
}