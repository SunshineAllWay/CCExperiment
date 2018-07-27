package org.apache.lucene.queryParser.standard;
import java.text.Collator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.config.FieldConfig;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.parser.SyntaxParser;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessor;
import org.apache.lucene.queryParser.standard.builders.StandardQueryBuilder;
import org.apache.lucene.queryParser.standard.builders.StandardQueryTreeBuilder;
import org.apache.lucene.queryParser.standard.config.AllowLeadingWildcardAttribute;
import org.apache.lucene.queryParser.standard.config.AnalyzerAttribute;
import org.apache.lucene.queryParser.standard.config.DateResolutionAttribute;
import org.apache.lucene.queryParser.standard.config.DefaultOperatorAttribute;
import org.apache.lucene.queryParser.standard.config.DefaultPhraseSlopAttribute;
import org.apache.lucene.queryParser.standard.config.LocaleAttribute;
import org.apache.lucene.queryParser.standard.config.LowercaseExpandedTermsAttribute;
import org.apache.lucene.queryParser.standard.config.MultiTermRewriteMethodAttribute;
import org.apache.lucene.queryParser.standard.config.PositionIncrementsAttribute;
import org.apache.lucene.queryParser.standard.config.RangeCollatorAttribute;
import org.apache.lucene.queryParser.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.queryParser.standard.parser.StandardSyntaxParser;
import org.apache.lucene.queryParser.standard.processors.StandardQueryNodeProcessorPipeline;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
@Deprecated
public class QueryParserWrapper {
  static public enum Operator { OR, AND }
  public static final Operator AND_OPERATOR = Operator.AND;
  public static final Operator OR_OPERATOR = Operator.OR;
  public static String escape(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '(' || c == ')'
          || c == ':' || c == '^' || c == '[' || c == ']' || c == '\"'
          || c == '{' || c == '}' || c == '~' || c == '*' || c == '?'
          || c == '|' || c == '&') {
        sb.append('\\');
      }
      sb.append(c);
    }
    return sb.toString();
  }
  private SyntaxParser syntaxParser = new StandardSyntaxParser();
  private StandardQueryConfigHandler config;
  private StandardQueryParser qpHelper;
  private QueryNodeProcessor processorPipeline;
  private StandardQueryBuilder builder = new StandardQueryTreeBuilder();
  private String defaultField;
  public QueryParserWrapper(String defaultField, Analyzer analyzer) {
    this.defaultField = defaultField;
    this.qpHelper = new StandardQueryParser();
    this.config = (StandardQueryConfigHandler) qpHelper.getQueryConfigHandler();
    this.qpHelper.setAnalyzer(analyzer);
    this.processorPipeline = new StandardQueryNodeProcessorPipeline(this.config);
  }
  StandardQueryParser getQueryParserHelper() {
    return qpHelper;
  }
  public String getField() {
    return this.defaultField;
  }
  public Analyzer getAnalyzer() {
    if (this.config != null
        && this.config.hasAttribute(AnalyzerAttribute.class)) {
      return this.config.getAttribute(AnalyzerAttribute.class).getAnalyzer();
    }
    return null;
  }
  public void setQueryBuilder(StandardQueryBuilder builder) {
    this.builder = builder;
  }
  public void setQueryProcessor(QueryNodeProcessor processor) {
    this.processorPipeline = processor;
    this.processorPipeline.setQueryConfigHandler(this.config);
  }
  public void setQueryConfig(StandardQueryConfigHandler queryConfig) {
    this.config = queryConfig;
    if (this.processorPipeline != null) {
      this.processorPipeline.setQueryConfigHandler(this.config);
    }
  }
  public QueryConfigHandler getQueryConfigHandler() {
    return this.config;
  }
  public QueryNodeProcessor getQueryProcessor() {
    return this.processorPipeline;
  }
  public ParseException generateParseException() {
    return null;
  }
  public boolean getAllowLeadingWildcard() {
    if (this.config != null
        && this.config.hasAttribute(AllowLeadingWildcardAttribute.class)) {
      return this.config.getAttribute(AllowLeadingWildcardAttribute.class)
          .isAllowLeadingWildcard();
    }
    return false;
  }
  public MultiTermQuery.RewriteMethod getMultiTermRewriteMethod() {
    if (this.config != null
        && this.config.hasAttribute(MultiTermRewriteMethodAttribute.class)) {
      return this.config.getAttribute(MultiTermRewriteMethodAttribute.class)
          .getMultiTermRewriteMethod();
    }
    return MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT;
  }
  public Resolution getDateResolution(String fieldName) {
    if (this.config != null) {
      FieldConfig fieldConfig = this.config.getFieldConfig(fieldName);
      if (fieldConfig != null) {
        if (this.config.hasAttribute(DateResolutionAttribute.class)) {
          return this.config.getAttribute(DateResolutionAttribute.class)
              .getDateResolution();
        }
      }
    }
    return null;
  }
  public boolean getEnablePositionIncrements() {
    if (this.config != null
        && this.config.hasAttribute(PositionIncrementsAttribute.class)) {
      return this.config.getAttribute(PositionIncrementsAttribute.class)
          .isPositionIncrementsEnabled();
    }
    return false;
  }
  public float getFuzzyMinSim() {
    return FuzzyQuery.defaultMinSimilarity;
  }
  public int getFuzzyPrefixLength() {
    return FuzzyQuery.defaultPrefixLength;
  }
  public Locale getLocale() {
    if (this.config != null && this.config.hasAttribute(LocaleAttribute.class)) {
      return this.config.getAttribute(LocaleAttribute.class).getLocale();
    }
    return Locale.getDefault();
  }
  public boolean getLowercaseExpandedTerms() {
    if (this.config != null
        && this.config.hasAttribute(LowercaseExpandedTermsAttribute.class)) {
      return this.config.getAttribute(LowercaseExpandedTermsAttribute.class)
          .isLowercaseExpandedTerms();
    }
    return true;
  }
  public int getPhraseSlop() {
    if (this.config != null
        && this.config.hasAttribute(AllowLeadingWildcardAttribute.class)) {
      return this.config.getAttribute(DefaultPhraseSlopAttribute.class)
          .getDefaultPhraseSlop();
    }
    return 0;
  }
  public Collator getRangeCollator() {
    if (this.config != null
        && this.config.hasAttribute(RangeCollatorAttribute.class)) {
      return this.config.getAttribute(RangeCollatorAttribute.class)
          .getRangeCollator();
    }
    return null;
  }
  public boolean getUseOldRangeQuery() {
    if (getMultiTermRewriteMethod() == MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE) {
      return true;
    } else {
      return false;
    }
  }
  public Query parse(String query) throws ParseException {
    try {
      QueryNode queryTree = this.syntaxParser.parse(query, getField());
      queryTree = this.processorPipeline.process(queryTree);
      return this.builder.build(queryTree);
    } catch (QueryNodeException e) {
      throw new ParseException("parse exception");
    }
  }
  public void setAllowLeadingWildcard(boolean allowLeadingWildcard) {
    this.qpHelper.setAllowLeadingWildcard(allowLeadingWildcard);
  }
  public void setMultiTermRewriteMethod(MultiTermQuery.RewriteMethod method) {
    this.qpHelper.setMultiTermRewriteMethod(method);
  }
  public void setDateResolution(Resolution dateResolution) {
    this.qpHelper.setDateResolution(dateResolution);
  }
  private Map<CharSequence, DateTools.Resolution> dateRes = new HashMap<CharSequence, DateTools.Resolution>();
  public void setDateResolution(String fieldName, Resolution dateResolution) {
    dateRes.put(fieldName, dateResolution);
    this.qpHelper.setDateResolution(dateRes);
  }
  public void setDefaultOperator(Operator op) {
    this.qpHelper
        .setDefaultOperator(OR_OPERATOR.equals(op) ? org.apache.lucene.queryParser.standard.config.DefaultOperatorAttribute.Operator.OR
            : org.apache.lucene.queryParser.standard.config.DefaultOperatorAttribute.Operator.AND);
  }
  public Operator getDefaultOperator() {
    if (this.config != null
        && this.config.hasAttribute(DefaultOperatorAttribute.class)) {
      return (this.config.getAttribute(DefaultOperatorAttribute.class)
          .getOperator() == org.apache.lucene.queryParser.standard.config.DefaultOperatorAttribute.Operator.AND) ? AND_OPERATOR
          : OR_OPERATOR;
    }
    return OR_OPERATOR;
  }
  public void setEnablePositionIncrements(boolean enable) {
    this.qpHelper.setEnablePositionIncrements(enable);
  }
  public void setFuzzyMinSim(float fuzzyMinSim) {
  }
  public void setFuzzyPrefixLength(int fuzzyPrefixLength) {
  }
  public void setLocale(Locale locale) {
    this.qpHelper.setLocale(locale);
  }
  public void setLowercaseExpandedTerms(boolean lowercaseExpandedTerms) {
    this.qpHelper.setLowercaseExpandedTerms(lowercaseExpandedTerms);
  }
  public void setPhraseSlop(int phraseSlop) {
    this.qpHelper.setDefaultPhraseSlop(phraseSlop);
  }
  public void setRangeCollator(Collator rc) {
    this.qpHelper.setRangeCollator(rc);
  }
  public void setUseOldRangeQuery(boolean useOldRangeQuery) {
    if (useOldRangeQuery) {
      setMultiTermRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
    } else {
      setMultiTermRewriteMethod(MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT);
    }
  }
  protected Query getPrefixQuery(String field, String termStr)
      throws ParseException {
    throw new UnsupportedOperationException();
  }
  protected Query getWildcardQuery(String field, String termStr)
      throws ParseException {
    throw new UnsupportedOperationException();
  }
  protected Query getFuzzyQuery(String field, String termStr,
      float minSimilarity) throws ParseException {
    throw new UnsupportedOperationException();
  }
  protected Query getFieldQuery(String field, String queryText)
      throws ParseException {
    throw new UnsupportedOperationException();
  }
  @SuppressWarnings("unchecked")
  protected Query getBooleanQuery(List clauses, boolean disableCoord)
      throws ParseException {
    throw new UnsupportedOperationException();
  }
  protected Query getFieldQuery(String field, String queryText, int slop)
      throws ParseException {
    throw new UnsupportedOperationException();
  }
  protected Query getRangeQuery(String field, String part1, String part2,
      boolean inclusive) throws ParseException {
    throw new UnsupportedOperationException();
  }
}
