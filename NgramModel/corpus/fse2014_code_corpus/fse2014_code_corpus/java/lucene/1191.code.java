package org.apache.lucene.queryParser.standard.config;
import java.text.Collator;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.ParametricRangeQueryNodeProcessor;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.Attribute;
public interface RangeCollatorAttribute extends Attribute {
  public void setDateResolution(Collator rangeCollator);
  public Collator getRangeCollator();
}
