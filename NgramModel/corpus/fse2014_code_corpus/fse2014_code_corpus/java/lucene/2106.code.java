package org.apache.solr.handler.dataimport;
public abstract class VariableResolver {
  public abstract Object resolve(String name);
  public abstract String replaceTokens(String template);
}
