package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.MultiFieldQueryNodeProcessor;
import org.apache.lucene.util.Attribute;
public interface MultiFieldAttribute extends Attribute {
  public void setFields(CharSequence[] fields);
  public CharSequence[] getFields();
}
