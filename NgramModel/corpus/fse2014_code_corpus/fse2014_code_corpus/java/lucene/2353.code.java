package org.apache.solr.handler.component;
import org.apache.lucene.search.FieldCache;
import org.apache.solr.schema.FieldType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
public class FieldFacetStats {
  public final String name;
  final FieldCache.StringIndex si;
  final FieldType ft;
  final String[] terms;
  final int[] termNum;
  final int startTermIndex;
  final int endTermIndex;
  final int nTerms;
  final int numStatsTerms;
  public final Map<String, StatsValues> facetStatsValues;
  final List<HashMap<String, Integer>> facetStatsTerms;
  public FieldFacetStats(String name, FieldCache.StringIndex si, FieldType ft, int numStatsTerms) {
    this.name = name;
    this.si = si;
    this.ft = ft;
    this.numStatsTerms = numStatsTerms;
    terms = si.lookup;
    termNum = si.order;
    startTermIndex = 1;
    endTermIndex = terms.length;
    nTerms = endTermIndex - startTermIndex;
    facetStatsValues = new HashMap<String, StatsValues>();
    facetStatsTerms = new ArrayList<HashMap<String, Integer>>();
    if (numStatsTerms == 0) return;
    int i = 0;
    for (; i < numStatsTerms; i++) {
      facetStatsTerms.add(new HashMap<String, Integer>());
    }
  }
  String getTermText(int docID) {
    return terms[termNum[docID]];
  }
  public boolean facet(int docID, Double v) {
    int term = termNum[docID];
    int arrIdx = term - startTermIndex;
    if (arrIdx >= 0 && arrIdx < nTerms) {
      String key = ft.indexedToReadable(terms[term]);
      StatsValues stats = facetStatsValues.get(key);
      if (stats == null) {
        stats = new StatsValues();
        facetStatsValues.put(key, stats);
      }
      if (v != null) {
        stats.accumulate(v);
      } else {
        stats.missing++;
        return false;
      }
      return true;
    }
    return false;
  }
  public boolean facetTermNum(int docID, int statsTermNum) {
    int term = termNum[docID];
    int arrIdx = term - startTermIndex;
    if (arrIdx >= 0 && arrIdx < nTerms) {
      String key = ft.indexedToReadable(terms[term]);
      HashMap<String, Integer> statsTermCounts = facetStatsTerms.get(statsTermNum);
      Integer statsTermCount = statsTermCounts.get(key);
      if (statsTermCount == null) {
        statsTermCounts.put(key, 1);
      } else {
        statsTermCounts.put(key, statsTermCount + 1);
      }
      return true;
    }
    return false;
  }
  public boolean accumulateTermNum(int statsTermNum, Double value) {
    if (value == null) return false;
    for (Map.Entry<String, Integer> stringIntegerEntry : facetStatsTerms.get(statsTermNum).entrySet()) {
      Map.Entry pairs = (Map.Entry) stringIntegerEntry;
      String key = (String) pairs.getKey();
      StatsValues facetStats = facetStatsValues.get(key);
      if (facetStats == null) {
        facetStats = new StatsValues();
        facetStatsValues.put(key, facetStats);
      }
      Integer count = (Integer) pairs.getValue();
      if (count != null) {
        facetStats.accumulate(value, count);
      }
    }
    return true;
  }
}
