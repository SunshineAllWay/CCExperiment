package org.apache.lucene.search;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.TermFreqVector;
public class QueryTermVector implements TermFreqVector {
  private String [] terms = new String[0];
  private int [] termFreqs = new int[0];
  public String getField() { return null;  }
  public QueryTermVector(String [] queryTerms) {
    processTerms(queryTerms);
  }
  public QueryTermVector(String queryString, Analyzer analyzer) {    
    if (analyzer != null)
    {
      TokenStream stream = analyzer.tokenStream("", new StringReader(queryString));
      if (stream != null)
      {
        List<String> terms = new ArrayList<String>();
        try {
          boolean hasMoreTokens = false;
          stream.reset(); 
          TermAttribute termAtt = stream.addAttribute(TermAttribute.class);
          hasMoreTokens = stream.incrementToken();
          while (hasMoreTokens) {
            terms.add(termAtt.term());
            hasMoreTokens = stream.incrementToken();
          }
          processTerms(terms.toArray(new String[terms.size()]));
        } catch (IOException e) {
        }
      }
    }                                                              
  }
  private void processTerms(String[] queryTerms) {
    if (queryTerms != null) {
      Arrays.sort(queryTerms);
      Map<String,Integer> tmpSet = new HashMap<String,Integer>(queryTerms.length);
      List<String> tmpList = new ArrayList<String>(queryTerms.length);
      List<Integer> tmpFreqs = new ArrayList<Integer>(queryTerms.length);
      int j = 0;
      for (int i = 0; i < queryTerms.length; i++) {
        String term = queryTerms[i];
        Integer position = tmpSet.get(term);
        if (position == null) {
          tmpSet.put(term, Integer.valueOf(j++));
          tmpList.add(term);
          tmpFreqs.add(Integer.valueOf(1));
        }       
        else {
          Integer integer = tmpFreqs.get(position.intValue());
          tmpFreqs.set(position.intValue(), Integer.valueOf(integer.intValue() + 1));          
        }
      }
      terms = tmpList.toArray(terms);
      termFreqs = new int[tmpFreqs.size()];
      int i = 0;
      for (final Integer integer : tmpFreqs) {
        termFreqs[i++] = integer.intValue();
      }
    }
  }
  @Override
  public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (int i=0; i<terms.length; i++) {
            if (i>0) sb.append(", ");
            sb.append(terms[i]).append('/').append(termFreqs[i]);
        }
        sb.append('}');
        return sb.toString();
    }
  public int size() {
    return terms.length;
  }
  public String[] getTerms() {
    return terms;
  }
  public int[] getTermFrequencies() {
    return termFreqs;
  }
  public int indexOf(String term) {
    int res = Arrays.binarySearch(terms, term);
        return res >= 0 ? res : -1;
  }
  public int[] indexesOf(String[] terms, int start, int len) {
    int res[] = new int[len];
    for (int i=0; i < len; i++) {
        res[i] = indexOf(terms[i]);
    }
    return res;                  
  }
}
