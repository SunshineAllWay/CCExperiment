package org.apache.lucene.search.highlight;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.memory.MemoryIndex;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.util.StringHelper;
public class QueryScorer implements Scorer {
  private float totalScore;
  private Set<String> foundTerms;
  private Map<String,WeightedSpanTerm> fieldWeightedSpanTerms;
  private float maxTermWeight;
  private int position = -1;
  private String defaultField;
  private TermAttribute termAtt;
  private PositionIncrementAttribute posIncAtt;
  private boolean expandMultiTermQuery = true;
  private Query query;
  private String field;
  private IndexReader reader;
  private boolean skipInitExtractor;
  private boolean wrapToCaching = true;
  public QueryScorer(Query query) {
    init(query, null, null, true);
  }
  public QueryScorer(Query query, String field) {
    init(query, field, null, true);
  }
  public QueryScorer(Query query, IndexReader reader, String field) {
    init(query, field, reader, true);
  }
  public QueryScorer(Query query, IndexReader reader, String field, String defaultField) {
    this.defaultField = StringHelper.intern(defaultField);
    init(query, field, reader, true);
  }
  public QueryScorer(Query query, String field, String defaultField) {
    this.defaultField = StringHelper.intern(defaultField);
    init(query, field, null, true);
  }
  public QueryScorer(WeightedSpanTerm[] weightedTerms) {
    this.fieldWeightedSpanTerms = new HashMap<String,WeightedSpanTerm>(weightedTerms.length);
    for (int i = 0; i < weightedTerms.length; i++) {
      WeightedSpanTerm existingTerm = fieldWeightedSpanTerms.get(weightedTerms[i].term);
      if ((existingTerm == null) ||
            (existingTerm.weight < weightedTerms[i].weight)) {
        fieldWeightedSpanTerms.put(weightedTerms[i].term, weightedTerms[i]);
        maxTermWeight = Math.max(maxTermWeight, weightedTerms[i].getWeight());
      }
    }
    skipInitExtractor = true;
  }
  public float getFragmentScore() {
    return totalScore;
  }
  public float getMaxTermWeight() {
    return maxTermWeight;
  }
  public float getTokenScore() {
    position += posIncAtt.getPositionIncrement();
    String termText = termAtt.term();
    WeightedSpanTerm weightedSpanTerm;
    if ((weightedSpanTerm = fieldWeightedSpanTerms.get(
              termText)) == null) {
      return 0;
    }
    if (weightedSpanTerm.positionSensitive &&
          !weightedSpanTerm.checkPosition(position)) {
      return 0;
    }
    float score = weightedSpanTerm.getWeight();
    if (!foundTerms.contains(termText)) {
      totalScore += score;
      foundTerms.add(termText);
    }
    return score;
  }
  public TokenStream init(TokenStream tokenStream) throws IOException {
    position = -1;
    termAtt = tokenStream.addAttribute(TermAttribute.class);
    posIncAtt = tokenStream.addAttribute(PositionIncrementAttribute.class);
    if(!skipInitExtractor) {
      if(fieldWeightedSpanTerms != null) {
        fieldWeightedSpanTerms.clear();
      }
      return initExtractor(tokenStream);
    }
    return null;
  }
  public WeightedSpanTerm getWeightedSpanTerm(String token) {
    return fieldWeightedSpanTerms.get(token);
  }
  private void init(Query query, String field, IndexReader reader, boolean expandMultiTermQuery) {
    this.reader = reader;
    this.expandMultiTermQuery = expandMultiTermQuery;
    this.query = query;
    this.field = field;
  }
  private TokenStream initExtractor(TokenStream tokenStream) throws IOException {
    WeightedSpanTermExtractor qse = defaultField == null ? new WeightedSpanTermExtractor()
        : new WeightedSpanTermExtractor(defaultField);
    qse.setExpandMultiTermQuery(expandMultiTermQuery);
    qse.setWrapIfNotCachingTokenFilter(wrapToCaching);
    if (reader == null) {
      this.fieldWeightedSpanTerms = qse.getWeightedSpanTerms(query,
          tokenStream, field);
    } else {
      this.fieldWeightedSpanTerms = qse.getWeightedSpanTermsWithScores(query,
          tokenStream, field, reader);
    }
    if(qse.isCachedTokenStream()) {
      return qse.getTokenStream();
    }
    return null;
  }
  public void startFragment(TextFragment newFragment) {
    foundTerms = new HashSet<String>();
    totalScore = 0;
  }
  public boolean isExpandMultiTermQuery() {
    return expandMultiTermQuery;
  }
  public void setExpandMultiTermQuery(boolean expandMultiTermQuery) {
    this.expandMultiTermQuery = expandMultiTermQuery;
  }
  public void setWrapIfNotCachingTokenFilter(boolean wrap) {
    this.wrapToCaching = wrap;
  }
}
