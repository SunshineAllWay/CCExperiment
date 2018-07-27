package org.apache.solr.handler.dataimport;
import java.util.Properties;
public abstract class DataSource<T> {
  public abstract void init(Context context, Properties initProps);
  public abstract T getData(String query);
  public abstract void close();
}
