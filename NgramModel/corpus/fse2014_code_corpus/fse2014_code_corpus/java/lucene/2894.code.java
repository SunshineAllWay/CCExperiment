package org.apache.solr.servlet.cache;
public enum Method {
  GET, POST, HEAD, OTHER;
  public static Method getMethod(String method) {
    try {
      return Method.valueOf(method.toUpperCase());
    } catch (Exception e) {
      return OTHER;
    }
  }
}
