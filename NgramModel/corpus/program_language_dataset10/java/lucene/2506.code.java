package org.apache.solr.search;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.index.IndexReader;
import java.util.Map;
import java.io.IOException;
public abstract class SolrFilter extends Filter {
  public abstract void createWeight(Map context, Searcher searcher) throws IOException;
  public abstract DocIdSet getDocIdSet(Map context, IndexReader reader) throws IOException;
  @Override
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
    return getDocIdSet(null, reader);
  }
}
