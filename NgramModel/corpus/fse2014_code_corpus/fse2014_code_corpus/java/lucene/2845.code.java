package org.apache.solr.search;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.solr.common.SolrException;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.util.AbstractSolrTestCase;
public class QueryParsingTest extends AbstractSolrTestCase {
  public String getSchemaFile() {
    return "schema.xml";
  }
  public String getSolrConfigFile() {
    return "solrconfig.xml";
  }
  public void testSort() throws Exception {
    Sort sort;
    IndexSchema schema = h.getCore().getSchema();
    sort = QueryParsing.parseSort("score desc", schema);
    assertNull("sort", sort);
    sort = QueryParsing.parseSort("score asc", schema);
    SortField[] flds = sort.getSort();
    assertEquals(flds[0].getType(), SortField.SCORE);
    assertTrue(flds[0].getReverse());
    sort = QueryParsing.parseSort("weight desc", schema);
    flds = sort.getSort();
    assertEquals(flds[0].getType(), SortField.FLOAT);
    assertEquals(flds[0].getField(), "weight");
    assertEquals(flds[0].getReverse(), true);
    sort = QueryParsing.parseSort("weight desc,bday asc", schema);
    flds = sort.getSort();
    assertEquals(flds[0].getType(), SortField.FLOAT);
    assertEquals(flds[0].getField(), "weight");
    assertEquals(flds[0].getReverse(), true);
    assertEquals(flds[1].getType(), SortField.LONG);
    assertEquals(flds[1].getField(), "bday");
    assertEquals(flds[1].getReverse(), false);
    sort = QueryParsing.parseSort("weight top,bday asc", schema);
    flds = sort.getSort();
    assertEquals(flds[0].getType(), SortField.FLOAT);
    assertEquals(flds[0].getField(), "weight");
    assertEquals(flds[0].getReverse(), true);
    assertEquals(flds[1].getType(), SortField.LONG);
    assertEquals(flds[1].getField(), "bday");
    assertEquals(flds[1].getReverse(), false);
    sort = QueryParsing.parseSort("weight top,bday bottom", schema);
    flds = sort.getSort();
    assertEquals(flds[0].getType(), SortField.FLOAT);
    assertEquals(flds[0].getField(), "weight");
    assertEquals(flds[0].getReverse(), true);
    assertEquals(flds[1].getType(), SortField.LONG);
    assertEquals(flds[1].getField(), "bday");
    assertEquals(flds[1].getReverse(), false);
    sort = QueryParsing.parseSort("weight         desc,            bday         asc", schema);
    flds = sort.getSort();
    assertEquals(flds[0].getType(), SortField.FLOAT);
    assertEquals(flds[0].getField(), "weight");
    assertEquals(flds[1].getField(), "bday");
    assertEquals(flds[1].getType(), SortField.LONG);
    sort = QueryParsing.parseSort("weight desc,", schema);
    flds = sort.getSort();
    assertEquals(flds[0].getType(), SortField.FLOAT);
    assertEquals(flds[0].getField(), "weight");
    sort = QueryParsing.parseSort("pow(weight, 2) desc", schema);
    flds = sort.getSort();
    assertEquals(flds[0].getType(), SortField.CUSTOM);
    assertEquals(flds[0].getField(), "pow(float(weight),const(2.0))");
    sort = QueryParsing.parseSort("sum(product(r_f,sum(d_f,t_f,1)),a_f) asc", schema);
    flds = sort.getSort();
    assertEquals(flds[0].getType(), SortField.CUSTOM);
    assertEquals(flds[0].getField(), "sum(product(float(r_f),sum(float(d_f),float(t_f),const(1.0))),float(a_f))");
    sort = QueryParsing.parseSort("pow(weight,                 2)         desc", schema);
    flds = sort.getSort();
    assertEquals(flds[0].getType(), SortField.CUSTOM);
    assertEquals(flds[0].getField(), "pow(float(weight),const(2.0))");
    sort = QueryParsing.parseSort("pow(weight, 2) desc, weight    desc,   bday    asc", schema);
    flds = sort.getSort();
    assertEquals(flds[0].getType(), SortField.CUSTOM);
    assertEquals(flds[0].getField(), "pow(float(weight),const(2.0))");
    assertEquals(flds[1].getType(), SortField.FLOAT);
    assertEquals(flds[1].getField(), "weight");
    assertEquals(flds[2].getField(), "bday");
    assertEquals(flds[2].getType(), SortField.LONG);
    sort = QueryParsing.parseSort("weight desc,", schema);
    flds = sort.getSort();
    assertEquals(flds[0].getType(), SortField.FLOAT);
    assertEquals(flds[0].getField(), "weight");
    try {
      sort = QueryParsing.parseSort("pow(weight,2)) desc, bday asc", schema);
    } catch (SolrException e) {
      assertTrue(false);
    }
    sort = QueryParsing.parseSort("strdist(foo_s, \"junk\", jw) desc", schema);
    flds = sort.getSort();
    assertEquals(flds[0].getType(), SortField.CUSTOM);
    assertEquals(flds[0].getField(), "strdist(str(foo_s),literal(junk), dist=org.apache.lucene.search.spell.JaroWinklerDistance)");
    sort = QueryParsing.parseSort("", schema);
    assertNull(sort);
  }
  public void testBad() throws Exception {
    Sort sort;
    IndexSchema schema = h.getCore().getSchema();
    try {
      sort = QueryParsing.parseSort("weight, desc", schema);
      assertTrue(false);
    } catch (SolrException e) {
    }
    try {
      sort = QueryParsing.parseSort("w", schema);
      assertTrue(false);
    } catch (SolrException e) {
    }
    try {
      sort = QueryParsing.parseSort("weight desc, bday", schema);
      assertTrue(false);
    } catch (SolrException e) {
    }
    try {
      sort = QueryParsing.parseSort("pow(weight,,2) desc, bday asc", schema);
      assertTrue(false);
    } catch (SolrException e) {
    }
    try {
      sort = QueryParsing.parseSort("pow() desc, bday asc", schema);
      assertTrue(false);
    } catch (SolrException e) {
    }
    try {
      sort = QueryParsing.parseSort("pow((weight,2) desc, bday asc", schema);
      assertTrue(false);
    } catch (SolrException e) {
    }
  }
}
