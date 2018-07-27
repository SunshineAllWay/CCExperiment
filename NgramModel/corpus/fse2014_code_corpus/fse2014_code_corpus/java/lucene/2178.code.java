package org.apache.solr.common.params;
public interface TermVectorParams {
  public static final String TV_PREFIX = "tv.";
  public static final String TF =  TV_PREFIX + "tf";
  public static final String POSITIONS = TV_PREFIX + "positions";
  public static final String OFFSETS = TV_PREFIX + "offsets";
  public static final String DF = TV_PREFIX + "df";
  public static final String TF_IDF = TV_PREFIX + "tf_idf";
  public static final String ALL = TV_PREFIX + "all";
  public static final String FIELDS = TV_PREFIX + "fl";
  public static final String DOC_IDS = TV_PREFIX + "docIds";
}
