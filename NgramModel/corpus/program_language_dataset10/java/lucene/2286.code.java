package org.apache.solr.analysis;
import java.io.*;
import java.util.Map;
import org.apache.solr.core.SolrConfig;
import org.apache.lucene.analysis.*;
public interface TokenizerFactory {
  public void init(Map<String,String> args);
  public Map<String,String> getArgs();
  public Tokenizer create(Reader input);
}
