package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.PhraseSlopQueryNodeProcessor;
import org.apache.lucene.util.Attribute;
public interface DefaultPhraseSlopAttribute extends Attribute {
  public void setDefaultPhraseSlop(int defaultPhraseSlop);
  public int getDefaultPhraseSlop();
}
