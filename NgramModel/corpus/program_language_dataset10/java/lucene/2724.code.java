package org.apache.solr.analysis;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.SolrQueryParser;
import org.apache.solr.util.AbstractSolrTestCase;
import static org.apache.solr.analysis.BaseTokenTestCase.*;
public class TestReversedWildcardFilterFactory extends AbstractSolrTestCase {
  Map<String,String> args = new HashMap<String, String>();
  ReversedWildcardFilterFactory factory = new ReversedWildcardFilterFactory();
  IndexSchema schema;
  public String getSchemaFile() {
    return "schema-reversed.xml";
  }
  public String getSolrConfigFile() {
    return "solrconfig.xml";
  }
  public void setUp() throws Exception {
    super.setUp();
    schema = new IndexSchema(solrConfig, getSchemaFile(), null);
  }
  public void testReversedTokens() throws IOException {
    String text = "simple text";
    args.put("withOriginal", "true");
    factory.init(args);
    TokenStream input = factory.create(new WhitespaceTokenizer(new StringReader(text)));
    assertTokenStreamContents(input, 
        new String[] { "\u0001elpmis", "simple", "\u0001txet", "text" },
        new int[] { 1, 0, 1, 0 });
    args.put("withOriginal", "false");
    factory.init(args);
    input = factory.create(new WhitespaceTokenizer(new StringReader(text)));
    assertTokenStreamContents(input,
        new String[] { "\u0001elpmis", "\u0001txet" },
        new int[] { 1, 1 });
  }
  public void testIndexingAnalysis() throws Exception {
    Analyzer a = schema.getAnalyzer();
    String text = "one two three si\uD834\uDD1Ex";
    TokenStream input = a.tokenStream("one", new StringReader(text));
    assertTokenStreamContents(input,
        new String[] { "\u0001eno", "one", "\u0001owt", "two", 
          "\u0001eerht", "three", "\u0001x\uD834\uDD1Eis", "si\uD834\uDD1Ex" },
        new int[] { 0, 0, 4, 4, 8, 8, 14, 14 },
        new int[] { 3, 3, 7, 7, 13, 13, 19, 19 },
        new int[] { 1, 0, 1, 0, 1, 0, 1, 0 }
    );
    input = a.tokenStream("two", new StringReader(text));
    assertTokenStreamContents(input,
        new String[] { "\u0001eno", "\u0001owt", 
          "\u0001eerht", "\u0001x\uD834\uDD1Eis" },
        new int[] { 0, 4, 8, 14 },
        new int[] { 3, 7, 13, 19 },
        new int[] { 1, 1, 1, 1 }
    );
    input = a.tokenStream("three", new StringReader(text));
    assertTokenStreamContents(input,
        new String[] { "one", "two", "three", "si\uD834\uDD1Ex" },
        new int[] { 0, 4, 8, 14 },
        new int[] { 3, 7, 13, 19 }
    );
  }
  public void testQueryParsing() throws IOException, ParseException {
    SolrQueryParser parserOne = new SolrQueryParser(schema, "one");
    assertTrue(parserOne.getAllowLeadingWildcard());
    SolrQueryParser parserTwo = new SolrQueryParser(schema, "two");
    assertTrue(parserTwo.getAllowLeadingWildcard());
    SolrQueryParser parserThree = new SolrQueryParser(schema, "three");
    assertTrue(parserThree.getAllowLeadingWildcard());
    String text = "one +two *hree f*ur fiv* *si\uD834\uDD1Ex";
    String expectedOne = "one:one +one:two one:\u0001eerh* one:\u0001ru*f one:fiv* one:\u0001x\uD834\uDD1Eis*";
    String expectedTwo = "two:one +two:two two:\u0001eerh* two:\u0001ru*f two:fiv* two:\u0001x\uD834\uDD1Eis*";
    String expectedThree = "three:one +three:two three:*hree three:f*ur three:fiv* three:*si\uD834\uDD1Ex";
    Query q = parserOne.parse(text);
    assertEquals(expectedOne, q.toString());
    q = parserTwo.parse(text);
    assertEquals(expectedTwo, q.toString());
    q = parserThree.parse(text);
    assertEquals(expectedThree, q.toString());
    String condText = "*hree t*ree th*ee thr*e ?hree t?ree th?ee th?*ee " + 
        "short*token ver*longtoken";
    String expected = "two:\u0001eerh* two:\u0001eer*t two:\u0001ee*ht " +
        "two:thr*e " +
        "two:\u0001eerh? two:\u0001eer?t " +
        "two:th?ee " +
        "two:th?*ee " +
        "two:short*token " +
        "two:\u0001nekotgnol*rev";
    q = parserTwo.parse(condText);
    assertEquals(expected, q.toString());
  }
}
