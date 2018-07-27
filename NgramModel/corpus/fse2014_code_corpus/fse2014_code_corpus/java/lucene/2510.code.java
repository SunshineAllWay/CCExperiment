package org.apache.solr.search;
import org.apache.lucene.search.DefaultSimilarity;
import java.util.HashMap;
class SolrSimilarity extends DefaultSimilarity {
  private final HashMap<String,Float> lengthNormConfig = new HashMap<String,Float>();
  public float lengthNorm(String fieldName, int numTerms) {
    return super.lengthNorm(fieldName, numTerms);
  }
}
