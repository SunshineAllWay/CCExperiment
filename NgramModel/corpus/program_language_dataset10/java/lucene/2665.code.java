package org.apache.solr.client.solrj.response;
import org.apache.solr.common.util.NamedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class TermsResponse {
  private Map<String, List<Term>> termMap = new HashMap<String, List<Term>>();
  public TermsResponse(NamedList<Object> termsInfo) {
    for (int i = 0; i < termsInfo.size(); i++) {
      String fieldName = termsInfo.getName(i);
      List<Term> itemList = new ArrayList<Term>();
      NamedList<Object> items = (NamedList<Object>) termsInfo.getVal(i);
      for (int j = 0; j < items.size(); j++) {
        Term t = new Term(items.getName(j), ((Number) items.getVal(j)).longValue());
        itemList.add(t);
      }
      termMap.put(fieldName, itemList);
    }
  }
  public List<Term> getTerms(String field) {
    return termMap.get(field);
  }
  public Map<String, List<Term>> getTermMap() {
    return termMap;
  }
  public static class Term {
    private String term;
    private long frequency;
    public Term(String term, long frequency) {
      this.term = term;
      this.frequency = frequency;
    }
    public String getTerm() {
      return term;
    }
    public void setTerm(String term) {
      this.term = term;
    }
    public long getFrequency() {
      return frequency;
    }
    public void setFrequency(long frequency) {
      this.frequency = frequency;
    }
    public void addFrequency(long frequency) {
      this.frequency += frequency;
    }
  }
}
