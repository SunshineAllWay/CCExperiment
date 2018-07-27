package org.apache.solr.handler.extraction;
public interface ExtractingParams {
  public static final String LOWERNAMES = "lowernames";
  public static final String MAP_PREFIX = "fmap.";
  public static final String BOOST_PREFIX = "boost.";
  public static final String LITERALS_PREFIX = "literal.";
  public static final String XPATH_EXPRESSION = "xpath";
  public static final String EXTRACT_ONLY = "extractOnly";
  public static final String EXTRACT_FORMAT = "extractFormat";
  public static final String CAPTURE_ATTRIBUTES = "captureAttr";
  public static final String CAPTURE_ELEMENTS = "capture";
  public static final String STREAM_TYPE = "stream.type";
  public static final String RESOURCE_NAME = "resource.name";
  public static final String UNKNOWN_FIELD_PREFIX = "uprefix";
  public static final String DEFAULT_FIELD = "defaultField";
}
