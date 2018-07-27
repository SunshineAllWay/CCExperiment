package org.apache.solr.spelling;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.index.IndexReader;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.search.SolrIndexSearcher;
import java.io.IOException;
import java.util.Collection;
public abstract class SolrSpellChecker {
  public static final String DICTIONARY_NAME = "name";
  public static final String DEFAULT_DICTIONARY_NAME = "default";
  protected String name;
  protected Analyzer analyzer;
  public String init(NamedList config, SolrCore core) {
    name = (String) config.get(DICTIONARY_NAME);
    if (name == null) {
      name = DEFAULT_DICTIONARY_NAME;
    }
    return name;
  }
  public Analyzer getQueryAnalyzer() {
    return analyzer;
  }
  public String getDictionaryName() {
    return name;
  }
  public abstract void reload() throws IOException;
  public abstract void build(SolrCore core, SolrIndexSearcher searcher);
  public SpellingResult getSuggestions(Collection<Token> tokens, IndexReader reader) throws IOException {
    return getSuggestions(tokens, reader, 1, false, false);
  }
  public SpellingResult getSuggestions(Collection<Token> tokens, IndexReader reader, int count) throws IOException {
    return getSuggestions(tokens, reader, count, false, false);
  }
  public SpellingResult getSuggestions(Collection<Token> tokens, IndexReader reader, boolean onlyMorePopular, boolean extendedResults) throws IOException {
    return getSuggestions(tokens, reader, 1, onlyMorePopular, extendedResults);
  }
  public abstract SpellingResult getSuggestions(Collection<Token> tokens, IndexReader reader, int count,
                                                boolean onlyMorePopular, boolean extendedResults)
          throws IOException;
}
