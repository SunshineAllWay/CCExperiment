package org.apache.solr.search;
import org.apache.solr.core.SolrInfoMBean;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
public interface SolrCache<K,V> extends SolrInfoMBean {
  public final static Logger log = LoggerFactory.getLogger(SolrCache.class);
  public Object init(Map args, Object persistence, CacheRegenerator regenerator);
  public String name();
  public int size();
  public V put(K key, V value);
  public V get(K key);
  public void clear();
  public enum State { 
    CREATED, 
    STATICWARMING, 
    AUTOWARMING, 
    LIVE 
  }
  public void setState(State state);
  public State getState();
  void warm(SolrIndexSearcher searcher, SolrCache<K,V> old) throws IOException;
  public void close();
}
