package org.apache.lucene.queryParser.ext;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.TestQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
public class TestExtendableQueryParser extends TestQueryParser {
  private static char[] DELIMITERS = new char[] {
      Extensions.DEFAULT_EXTENSION_FIELD_DELIMITER, '-', '|' };
  public TestExtendableQueryParser(String name) {
    super(name);
  }
  @Override
  public QueryParser getParser(Analyzer a) throws Exception {
    return getParser(a, null);
  }
  public QueryParser getParser(Analyzer a, Extensions extensions)
      throws Exception {
    if (a == null)
      a = new SimpleAnalyzer(TEST_VERSION_CURRENT);
    QueryParser qp = extensions == null ? new ExtendableQueryParser(
        TEST_VERSION_CURRENT, "field", a) : new ExtendableQueryParser(
        TEST_VERSION_CURRENT, "field", a, extensions);
    qp.setDefaultOperator(QueryParser.OR_OPERATOR);
    return qp;
  }
  public void testUnescapedExtDelimiter() throws Exception {
    Extensions ext = newExtensions(':');
    ext.add("testExt", new ExtensionStub());
    ExtendableQueryParser parser = (ExtendableQueryParser) getParser(null, ext);
    try {
      parser.parse("aField:testExt:\"foo \\& bar\"");
      fail("extension field delimiter is not escaped");
    } catch (ParseException e) {
    }
  }
  public void testExtFieldUnqoted() throws Exception {
    for (int i = 0; i < DELIMITERS.length; i++) {
      Extensions ext = newExtensions(DELIMITERS[i]);
      ext.add("testExt", new ExtensionStub());
      ExtendableQueryParser parser = (ExtendableQueryParser) getParser(null,
          ext);
      String field = ext.buildExtensionField("testExt", "aField");
      Query query = parser.parse(String.format("%s:foo bar", field));
      assertTrue("expected instance of BooleanQuery but was "
          + query.getClass(), query instanceof BooleanQuery);
      BooleanQuery bquery = (BooleanQuery) query;
      BooleanClause[] clauses = bquery.getClauses();
      assertEquals(2, clauses.length);
      BooleanClause booleanClause = clauses[0];
      query = booleanClause.getQuery();
      assertTrue("expected instance of TermQuery but was " + query.getClass(),
          query instanceof TermQuery);
      TermQuery tquery = (TermQuery) query;
      assertEquals("aField", tquery.getTerm()
          .field());
      assertEquals("foo", tquery.getTerm().text());
      booleanClause = clauses[1];
      query = booleanClause.getQuery();
      assertTrue("expected instance of TermQuery but was " + query.getClass(),
          query instanceof TermQuery);
      tquery = (TermQuery) query;
      assertEquals("field", tquery.getTerm().field());
      assertEquals("bar", tquery.getTerm().text());
    }
  }
  public void testExtDefaultField() throws Exception {
    for (int i = 0; i < DELIMITERS.length; i++) {
      Extensions ext = newExtensions(DELIMITERS[i]);
      ext.add("testExt", new ExtensionStub());
      ExtendableQueryParser parser = (ExtendableQueryParser) getParser(null,
          ext);
      String field = ext.buildExtensionField("testExt");
      Query parse = parser.parse(String.format("%s:\"foo \\& bar\"", field));
      assertTrue("expected instance of TermQuery but was " + parse.getClass(),
          parse instanceof TermQuery);
      TermQuery tquery = (TermQuery) parse;
      assertEquals("field", tquery.getTerm().field());
      assertEquals("foo & bar", tquery.getTerm().text());
    }
  }
  public Extensions newExtensions(char delimiter) {
    return new Extensions(delimiter);
  }
  public void testExtField() throws Exception {
    for (int i = 0; i < DELIMITERS.length; i++) {
      Extensions ext = newExtensions(DELIMITERS[i]);
      ext.add("testExt", new ExtensionStub());
      ExtendableQueryParser parser = (ExtendableQueryParser) getParser(null,
          ext);
      String field = ext.buildExtensionField("testExt", "afield");
      Query parse = parser.parse(String.format("%s:\"foo \\& bar\"", field));
      assertTrue("expected instance of TermQuery but was " + parse.getClass(),
          parse instanceof TermQuery);
      TermQuery tquery = (TermQuery) parse;
      assertEquals("afield", tquery.getTerm().field());
      assertEquals("foo & bar", tquery.getTerm().text());
    }
  }
}
