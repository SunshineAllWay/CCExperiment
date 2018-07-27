package org.apache.solr.spelling;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.HighFrequencyDictionary;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class IndexBasedSpellChecker extends AbstractLuceneSpellChecker {
  private static final Logger log = LoggerFactory.getLogger(IndexBasedSpellChecker.class);
  public static final String THRESHOLD_TOKEN_FREQUENCY = "thresholdTokenFrequency";
  protected float threshold;
  protected IndexReader reader;
  public String init(NamedList config, SolrCore core) {
    super.init(config, core);
    threshold = config.get(THRESHOLD_TOKEN_FREQUENCY) == null ? 0.0f
            : (Float) config.get(THRESHOLD_TOKEN_FREQUENCY);
    initSourceReader();
    return name;
  }
  private void initSourceReader() {
    if (sourceLocation != null) {
      try {
        FSDirectory luceneIndexDir = FSDirectory.open(new File(sourceLocation));
        this.reader = IndexReader.open(luceneIndexDir);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  public void build(SolrCore core, SolrIndexSearcher searcher) {
    IndexReader reader = null;
    try {
      if (sourceLocation == null) {
        reader = searcher.getReader();
      } else {
        reader = this.reader;
      }
      dictionary = new HighFrequencyDictionary(reader, field,
          threshold);
      spellChecker.clearIndex();
      spellChecker.indexDictionary(dictionary);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  @Override
  protected IndexReader determineReader(IndexReader reader) {
    IndexReader result = null;
    if (sourceLocation != null) {
      result = this.reader;
    } else {
      result = reader;
    }
    return result;
  }
  @Override
  public void reload() throws IOException {
    super.reload();
    initSourceReader();
  }
  public float getThreshold() {
    return threshold;
  }
}
