package org.apache.solr.spelling;
import org.apache.lucene.analysis.Token;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
public class SpellingResult {
  private Collection<Token> tokens;
  private Map<Token, LinkedHashMap<String, Integer>> suggestions = new LinkedHashMap<Token, LinkedHashMap<String, Integer>>();
  private Map<Token, Integer> tokenFrequency;
  public static final int NO_FREQUENCY_INFO = -1;
  public SpellingResult() {
  }
  public SpellingResult(Collection<Token> tokens) {
    this.tokens = tokens;
  }
  public void add(Token token, List<String> suggestions) {
    LinkedHashMap<String, Integer> map = this.suggestions.get(token);
    if (map == null ) {
      map = new LinkedHashMap<String, Integer>();
      this.suggestions.put(token, map);
    }
    for (String suggestion : suggestions) {
      map.put(suggestion, NO_FREQUENCY_INFO);
    }
  }
  public void add(Token token, int docFreq) {
    if (tokenFrequency == null) {
      tokenFrequency = new LinkedHashMap<Token, Integer>();
    }
    tokenFrequency.put(token, docFreq);
  }
  public void add(Token token, String suggestion, int docFreq) {
    LinkedHashMap<String, Integer> map = this.suggestions.get(token);
    if (map == null) {
      map = new LinkedHashMap<String, Integer>();
      this.suggestions.put(token, map);
    }
    map.put(suggestion, docFreq);
  }
  public LinkedHashMap<String, Integer> get(Token token) {
    return suggestions.get(token);
  }
  public Integer getTokenFrequency(Token token) {
    return tokenFrequency.get(token);
  }
  public boolean hasTokenFrequencyInfo() {
    return tokenFrequency != null && !tokenFrequency.isEmpty();
  }
  public Map<Token, LinkedHashMap<String, Integer>> getSuggestions() {
    return suggestions;
  }
  public Map<Token, Integer> getTokenFrequency() {
    return tokenFrequency;
  }
  public Collection<Token> getTokens() {
    return tokens;
  }
  public void setTokens(Collection<Token> tokens) {
    this.tokens = tokens;
  }
}
