package org.apache.solr.common.util;
import java.util.*;
public class SimpleOrderedMap<T> extends NamedList<T> {
  public SimpleOrderedMap() {
    super();
  }
  @Deprecated
  public SimpleOrderedMap(List nameValuePairs) {
    super(nameValuePairs);
  }
  public SimpleOrderedMap(Map.Entry<String, T>[] nameValuePairs) { 
    super(nameValuePairs);
  }
  @Override
  public SimpleOrderedMap<T> clone() {
    ArrayList newList = new ArrayList(nvPairs.size());
    newList.addAll(nvPairs);
    return new SimpleOrderedMap<T>(newList);
  }
}
