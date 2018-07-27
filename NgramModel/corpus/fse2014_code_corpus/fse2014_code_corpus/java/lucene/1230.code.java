package org.apache.lucene.queryParser.spans;
import javax.management.Query;
import junit.framework.TestCase;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.nodes.OrQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.parser.SyntaxParser;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessorPipeline;
import org.apache.lucene.queryParser.standard.parser.StandardSyntaxParser;
import org.apache.lucene.queryParser.standard.processors.WildcardQueryNodeProcessor;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
public class TestSpanQueryParser extends TestCase {
  private QueryNodeProcessorPipeline spanProcessorPipeline;
  private SpansQueryConfigHandler spanQueryConfigHandler;
  private SpansQueryTreeBuilder spansQueryTreeBuilder;
  private SyntaxParser queryParser = new StandardSyntaxParser();
  public TestSpanQueryParser() {
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.spanProcessorPipeline = new QueryNodeProcessorPipeline();
    this.spanQueryConfigHandler = new SpansQueryConfigHandler();
    this.spansQueryTreeBuilder = new SpansQueryTreeBuilder();
    this.spanProcessorPipeline
        .setQueryConfigHandler(this.spanQueryConfigHandler);
    this.spanProcessorPipeline.addProcessor(new WildcardQueryNodeProcessor());
    this.spanProcessorPipeline
        .addProcessor(new SpansValidatorQueryNodeProcessor());
    this.spanProcessorPipeline
        .addProcessor(new UniqueFieldQueryNodeProcessor());
  }
  public SpanQuery getSpanQuery(CharSequence query) throws QueryNodeException {
    return getSpanQuery("", query);
  }
  public SpanQuery getSpanQuery(CharSequence uniqueField, CharSequence query)
      throws QueryNodeException {
    UniqueFieldAttribute uniqueFieldAtt = this.spanQueryConfigHandler
        .getAttribute(UniqueFieldAttribute.class);
    uniqueFieldAtt.setUniqueField(uniqueField);
    QueryNode queryTree = this.queryParser.parse(query, "defaultField");
    queryTree = this.spanProcessorPipeline.process(queryTree);
    return this.spansQueryTreeBuilder.build(queryTree);
  }
  public void testTermSpans() throws Exception {
    assertEquals(getSpanQuery("field:term").toString(), "term");
    assertEquals(getSpanQuery("term").toString(), "term");
    assertTrue(getSpanQuery("field:term") instanceof SpanTermQuery);
    assertTrue(getSpanQuery("term") instanceof SpanTermQuery);
  }
  public void testUniqueField() throws Exception {
    assertEquals(getSpanQuery("field", "term").toString(), "field:term");
    assertEquals(getSpanQuery("field", "field:term").toString(), "field:term");
    assertEquals(getSpanQuery("field", "anotherField:term").toString(),
        "field:term");
  }
  public void testOrSpans() throws Exception {
    assertEquals(getSpanQuery("term1 term2").toString(),
        "spanOr([term1, term2])");
    assertEquals(getSpanQuery("term1 OR term2").toString(),
        "spanOr([term1, term2])");
    assertTrue(getSpanQuery("term1 term2") instanceof SpanOrQuery);
    assertTrue(getSpanQuery("term1 term2") instanceof SpanOrQuery);
  }
  public void testQueryValidator() throws QueryNodeException {
    try {
      getSpanQuery("term*");
      fail("QueryNodeException was expected, wildcard queries should not be supported");
    } catch (QueryNodeException ex) {
    }
    try {
      getSpanQuery("[a TO z]");
      fail("QueryNodeException was expected, range queries should not be supported");
    } catch (QueryNodeException ex) {
    }
    try {
      getSpanQuery("a~0.5");
      fail("QueryNodeException was expected, boost queries should not be supported");
    } catch (QueryNodeException ex) {
    }
    try {
      getSpanQuery("a^0.5");
      fail("QueryNodeException was expected, fuzzy queries should not be supported");
    } catch (QueryNodeException ex) {
    }
    try {
      getSpanQuery("\"a b\"");
      fail("QueryNodeException was expected, quoted queries should not be supported");
    } catch (QueryNodeException ex) {
    }
    try {
      getSpanQuery("(a b)");
      fail("QueryNodeException was expected, parenthesized queries should not be supported");
    } catch (QueryNodeException ex) {
    }
    try {
      getSpanQuery("a AND b");
      fail("QueryNodeException was expected, and queries should not be supported");
    } catch (QueryNodeException ex) {
    }
  }
}
