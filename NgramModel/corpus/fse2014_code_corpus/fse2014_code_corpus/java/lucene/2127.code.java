package org.apache.solr.handler.dataimport;
import static org.apache.solr.handler.dataimport.RegexTransformer.REGEX;
import static org.apache.solr.handler.dataimport.RegexTransformer.GROUP_NAMES;
import static org.apache.solr.handler.dataimport.RegexTransformer.REPLACE_WITH;
import static org.apache.solr.handler.dataimport.DataImporter.COLUMN;
import static org.apache.solr.handler.dataimport.AbstractDataImportHandlerTest.createMap;
import static org.apache.solr.handler.dataimport.AbstractDataImportHandlerTest.getContext;
import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class TestRegexTransformer {
  @Test
  public void commaSeparated() {
    List<Map<String, String>> fields = new ArrayList<Map<String, String>>();
    fields.add(getField("col1", "string", null, "a", ","));
    Context context = AbstractDataImportHandlerTest.getContext(null, null, null, Context.FULL_DUMP, fields, null);
    Map<String, Object> src = new HashMap<String, Object>();
    src.put("a", "a,bb,cc,d");
    Map<String, Object> result = new RegexTransformer().transformRow(src, context);
    Assert.assertEquals(2, result.size());
    Assert.assertEquals(4, ((List) result.get("col1")).size());
  }
  @Test
  public void groupNames() {
    List<Map<String, String>> fields = new ArrayList<Map<String, String>>();
    Map<String ,String > m = new HashMap<String, String>();
    m.put(COLUMN,"fullName");
    m.put(GROUP_NAMES,",firstName,lastName");
    m.put(REGEX,"(\\w*) (\\w*) (\\w*)");
    fields.add(m);
    Context context = AbstractDataImportHandlerTest.getContext(null, null, null, Context.FULL_DUMP, fields, null);
    Map<String, Object> src = new HashMap<String, Object>();
    src.put("fullName", "Mr Noble Paul");
    Map<String, Object> result = new RegexTransformer().transformRow(src, context);
    Assert.assertEquals("Noble", result.get("firstName"));
    Assert.assertEquals("Paul", result.get("lastName"));
    src= new HashMap<String, Object>();
    List<String> l= new ArrayList();
    l.add("Mr Noble Paul") ;
    l.add("Mr Shalin Mangar") ;
    src.put("fullName", l);
    result = new RegexTransformer().transformRow(src, context);
    List l1 = (List) result.get("firstName");
    List l2 = (List) result.get("lastName");
    Assert.assertEquals("Noble", l1.get(0));
    Assert.assertEquals("Shalin", l1.get(1));
    Assert.assertEquals("Paul", l2.get(0));
    Assert.assertEquals("Mangar", l2.get(1));
  }
  @Test
  public void replaceWith() {
    List<Map<String, String>> fields = new ArrayList<Map<String, String>>();
    Map<String, String> fld = getField("name", "string", "'", null, null);
    fld.put(REPLACE_WITH, "''");
    fields.add(fld);
    Context context = AbstractDataImportHandlerTest.getContext(null, null,
            null, Context.FULL_DUMP, fields, null);
    Map<String, Object> src = new HashMap<String, Object>();
    String s = "D'souza";
    src.put("name", s);
    Map<String, Object> result = new RegexTransformer().transformRow(src,
            context);
    Assert.assertEquals("D''souza", result.get("name"));
  }
  @Test
  public void mileage() {
    List<Map<String, String>> fields = getFields();
    Map<String, String> fld = getField("hltCityMPG", "string",
            ".*(${e.city_mileage})", "rowdata", null);
    fld.put(REPLACE_WITH, "*** $1 ***");
    fields.add(fld);
    fld = getField("t1", "string","duff", "rowdata", null);
    fields.add(fld);
    fld = getField("t2", "string","duff", "rowdata", null);
    fld.put(REPLACE_WITH, "60");
    fields.add(fld);
    fld = getField("t3", "string","(Range)", "rowdata", null);
    fld.put(REPLACE_WITH, "range");
    fld.put(GROUP_NAMES,"t4,t5");
    fields.add(fld);
    Map<String, Object> row = new HashMap<String, Object>();
    String s = "Fuel Economy Range: 26 mpg Hwy, 19 mpg City";
    row.put("rowdata", s);
    VariableResolverImpl resolver = new VariableResolverImpl();
    resolver.addNamespace("e", row);
    Map<String, String> eAttrs = AbstractDataImportHandlerTest.createMap("name", "e");
    Context context = AbstractDataImportHandlerTest.getContext(null, resolver, null, Context.FULL_DUMP, fields, eAttrs);
    Map<String, Object> result = new RegexTransformer().transformRow(row, context);
    Assert.assertEquals(5, result.size());
    Assert.assertEquals(s, result.get("rowdata"));
    Assert.assertEquals("26", result.get("highway_mileage"));
    Assert.assertEquals("19", result.get("city_mileage"));
    Assert.assertEquals("*** 19 *** mpg City", result.get("hltCityMPG"));
    Assert.assertEquals("Fuel Economy range: 26 mpg Hwy, 19 mpg City", result.get("t3"));
  }
  @Test
  public void testMultiValuedRegex(){
      List<Map<String, String>> fields = new ArrayList<Map<String, String>>();
    Map<String, String> fld = getField("participant", null, "(.*)", "person", null);
    fields.add(fld);
    Context context = getContext(null, null,
            null, Context.FULL_DUMP, fields, null);
    ArrayList<String> strings = new ArrayList<String>();
    strings.add("hello");
    strings.add("world");
    Map<String, Object> result = new RegexTransformer().transformRow(createMap("person", strings), context);
    Assert.assertEquals(strings,result.get("participant"));
  }
  public static List<Map<String, String>> getFields() {
    List<Map<String, String>> fields = new ArrayList<Map<String, String>>();
    fields.add(getField("city_mileage", "sint",
            "Fuel Economy Range:\\s*?\\d*?\\s*?mpg Hwy,\\s*?(\\d*?)\\s*?mpg City",
            "rowdata", null));
    fields.add(getField("highway_mileage", "sint",
            "Fuel Economy Range:\\s*?(\\d*?)\\s*?mpg Hwy,\\s*?\\d*?\\s*?mpg City",
            "rowdata", null));
    fields.add(getField("seating_capacity", "sint", "Seating capacity:(.*)",
            "rowdata", null));
    fields.add(getField("warranty", "string", "Warranty:(.*)", "rowdata", null));
    fields.add(getField("rowdata", "string", null, "rowdata", null));
    return fields;
  }
  public static Map<String, String> getField(String col, String type,
                                             String re, String srcCol, String splitBy) {
    HashMap<String, String> vals = new HashMap<String, String>();
    vals.put("column", col);
    vals.put("type", type);
    vals.put("regex", re);
    vals.put("sourceColName", srcCol);
    vals.put("splitBy", splitBy);
    return vals;
  }
}
