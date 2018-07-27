package org.apache.solr.util;
import java.util.List;
import java.util.Map;
@Deprecated
public class NamedList<T> extends org.apache.solr.common.util.NamedList<T> {
  public NamedList() {
    super();
  }
  @Deprecated
  public NamedList(List nameValuePairs) {
    super(nameValuePairs);
  }
  public NamedList(Map.Entry<String, T>[] nameValuePairs) { 
    super(nameValuePairs);
  }
}
