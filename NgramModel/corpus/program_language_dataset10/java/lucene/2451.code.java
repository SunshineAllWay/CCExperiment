package org.apache.solr.schema;
public interface SchemaAware {
  public void inform(IndexSchema schema);
}
