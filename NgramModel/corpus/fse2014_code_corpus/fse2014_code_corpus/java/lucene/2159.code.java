package org.apache.solr.common.params;
public interface AnalysisParams {
  static final String PREFIX = "analysis";
  static final String QUERY = PREFIX + ".query";
  static final String SHOW_MATCH = PREFIX + ".showmatch";
  static final String FIELD_NAME = PREFIX + ".fieldname";
  static final String FIELD_TYPE = PREFIX + ".fieldtype";
  static final String FIELD_VALUE = PREFIX + ".fieldvalue";
}
