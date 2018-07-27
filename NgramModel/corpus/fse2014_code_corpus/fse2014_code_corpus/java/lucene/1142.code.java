package org.apache.lucene.queryParser.standard;
import java.text.Collator;
import java.util.Locale;
import java.util.Map;
import java.util.TooManyListenersException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.QueryParserHelper;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.builders.StandardQueryTreeBuilder;
import org.apache.lucene.queryParser.standard.config.AllowLeadingWildcardAttribute;
import org.apache.lucene.queryParser.standard.config.AnalyzerAttribute;
import org.apache.lucene.queryParser.standard.config.DateResolutionAttribute;
import org.apache.lucene.queryParser.standard.config.DefaultOperatorAttribute;
import org.apache.lucene.queryParser.standard.config.DefaultPhraseSlopAttribute;
import org.apache.lucene.queryParser.standard.config.FieldBoostMapAttribute;
import org.apache.lucene.queryParser.standard.config.FieldDateResolutionMapAttribute;
import org.apache.lucene.queryParser.standard.config.FuzzyAttribute;
import org.apache.lucene.queryParser.standard.config.LocaleAttribute;
import org.apache.lucene.queryParser.standard.config.LowercaseExpandedTermsAttribute;
import org.apache.lucene.queryParser.standard.config.MultiFieldAttribute;
import org.apache.lucene.queryParser.standard.config.MultiTermRewriteMethodAttribute;
import org.apache.lucene.queryParser.standard.config.PositionIncrementsAttribute;
import org.apache.lucene.queryParser.standard.config.RangeCollatorAttribute;
import org.apache.lucene.queryParser.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.queryParser.standard.config.DefaultOperatorAttribute.Operator;
import org.apache.lucene.queryParser.standard.nodes.RangeQueryNode;
import org.apache.lucene.queryParser.standard.parser.StandardSyntaxParser;
import org.apache.lucene.queryParser.standard.processors.StandardQueryNodeProcessorPipeline;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
public class StandardQueryParser extends QueryParserHelper {
  public StandardQueryParser() {
    super(new StandardQueryConfigHandler(), new StandardSyntaxParser(),
        new StandardQueryNodeProcessorPipeline(null),
        new StandardQueryTreeBuilder());
  }
  public StandardQueryParser(Analyzer analyzer) {
    this();
    this.setAnalyzer(analyzer);
  }
  @Override
  public String toString(){
    return "<StandardQueryParser config=\"" + this.getQueryConfigHandler() + "\"/>";
  }
  @Override
  public Query parse(String query, String defaultField)
      throws QueryNodeException {
    return (Query) super.parse(query, defaultField);
  }
  public Operator getDefaultOperator() {
    DefaultOperatorAttribute attr = getQueryConfigHandler().getAttribute(DefaultOperatorAttribute.class);
    return attr.getOperator();
  }
  public void setRangeCollator(Collator collator) {
    RangeCollatorAttribute attr = getQueryConfigHandler().getAttribute(RangeCollatorAttribute.class);
    attr.setDateResolution(collator);
  }
  public Collator getRangeCollator() {
    RangeCollatorAttribute attr = getQueryConfigHandler().getAttribute(RangeCollatorAttribute.class);
    return attr.getRangeCollator();
  }
  public void setDefaultOperator(Operator operator) {
    DefaultOperatorAttribute attr = getQueryConfigHandler().getAttribute(DefaultOperatorAttribute.class);
    attr.setOperator(operator);
  }
  public void setLowercaseExpandedTerms(boolean lowercaseExpandedTerms) {
    LowercaseExpandedTermsAttribute attr = getQueryConfigHandler().getAttribute(LowercaseExpandedTermsAttribute.class);
    attr.setLowercaseExpandedTerms(lowercaseExpandedTerms);
  }
  public boolean getLowercaseExpandedTerms() {
    LowercaseExpandedTermsAttribute attr = getQueryConfigHandler().getAttribute(LowercaseExpandedTermsAttribute.class);
    return attr.isLowercaseExpandedTerms();
  }
  public void setAllowLeadingWildcard(boolean allowLeadingWildcard) {
    AllowLeadingWildcardAttribute attr = getQueryConfigHandler().getAttribute(AllowLeadingWildcardAttribute.class);
    attr.setAllowLeadingWildcard(allowLeadingWildcard);
  }
  public void setEnablePositionIncrements(boolean enabled) {
    PositionIncrementsAttribute attr = getQueryConfigHandler().getAttribute(PositionIncrementsAttribute.class);
    attr.setPositionIncrementsEnabled(enabled);
  }
  public boolean getEnablePositionIncrements() {
    PositionIncrementsAttribute attr = getQueryConfigHandler().getAttribute(PositionIncrementsAttribute.class);
    return attr.isPositionIncrementsEnabled();
  }
  public void setMultiTermRewriteMethod(MultiTermQuery.RewriteMethod method) {
    MultiTermRewriteMethodAttribute attr = getQueryConfigHandler().getAttribute(MultiTermRewriteMethodAttribute.class);
    attr.setMultiTermRewriteMethod(method);
  }
  public MultiTermQuery.RewriteMethod getMultiTermRewriteMethod() {
    MultiTermRewriteMethodAttribute attr = getQueryConfigHandler().getAttribute(MultiTermRewriteMethodAttribute.class);    
    return attr.getMultiTermRewriteMethod();
  }
  public void setMultiFields(CharSequence[] fields) {
    if (fields == null) {
      fields = new CharSequence[0];
    }
    MultiFieldAttribute attr = getQueryConfigHandler().addAttribute(MultiFieldAttribute.class);
    attr.setFields(fields);
  }
  public void setFuzzyPrefixLength(int fuzzyPrefixLength) {
    FuzzyAttribute attr = getQueryConfigHandler().addAttribute(FuzzyAttribute.class);
    attr.setPrefixLength(fuzzyPrefixLength);
  }
  public void setLocale(Locale locale) {
    LocaleAttribute attr = getQueryConfigHandler().addAttribute(LocaleAttribute.class);
    attr.setLocale(locale);
  }
  public Locale getLocale() {
    LocaleAttribute attr = getQueryConfigHandler().addAttribute(LocaleAttribute.class);
    return attr.getLocale();
  }
  public void setDefaultPhraseSlop(int defaultPhraseSlop) {
    DefaultPhraseSlopAttribute attr = getQueryConfigHandler().addAttribute(DefaultPhraseSlopAttribute.class);
    attr.setDefaultPhraseSlop(defaultPhraseSlop);
  }
  public void setAnalyzer(Analyzer analyzer) {
    AnalyzerAttribute attr = getQueryConfigHandler().getAttribute(AnalyzerAttribute.class);
    attr.setAnalyzer(analyzer);
  }
  public Analyzer getAnalyzer() {    
    QueryConfigHandler config = this.getQueryConfigHandler();
    if ( config.hasAttribute(AnalyzerAttribute.class)) {
      AnalyzerAttribute attr = config.getAttribute(AnalyzerAttribute.class);
      return attr.getAnalyzer();
    }
    return null;       
  }
  public boolean getAllowLeadingWildcard() {
    AllowLeadingWildcardAttribute attr = getQueryConfigHandler().addAttribute(AllowLeadingWildcardAttribute.class);
    return attr.isAllowLeadingWildcard();
  }
  public float getFuzzyMinSim() {
    FuzzyAttribute attr = getQueryConfigHandler().addAttribute(FuzzyAttribute.class);
    return attr.getFuzzyMinSimilarity();
  }
  public int getFuzzyPrefixLength() {
    FuzzyAttribute attr = getQueryConfigHandler().addAttribute(FuzzyAttribute.class);
    return attr.getPrefixLength();
  }
  public int getPhraseSlop() {
    DefaultPhraseSlopAttribute attr = getQueryConfigHandler().addAttribute(DefaultPhraseSlopAttribute.class);
    return attr.getDefaultPhraseSlop();
  }
  public void setFuzzyMinSim(float fuzzyMinSim) {
    FuzzyAttribute attr = getQueryConfigHandler().addAttribute(FuzzyAttribute.class);
    attr.setFuzzyMinSimilarity(fuzzyMinSim);
  }
  public void setFieldsBoost(Map<CharSequence, Float> boosts) {
    FieldBoostMapAttribute attr = getQueryConfigHandler().addAttribute(FieldBoostMapAttribute.class);
    attr.setFieldBoostMap(boosts);
  }
  public void setDateResolution(DateTools.Resolution dateResolution) {
    DateResolutionAttribute attr = getQueryConfigHandler().addAttribute(DateResolutionAttribute.class);
    attr.setDateResolution(dateResolution);
  }
  public void setDateResolution(Map<CharSequence, DateTools.Resolution> dateRes) {
    FieldDateResolutionMapAttribute attr = getQueryConfigHandler().addAttribute(FieldDateResolutionMapAttribute.class);
    attr.setFieldDateResolutionMap(dateRes);
  }
}
