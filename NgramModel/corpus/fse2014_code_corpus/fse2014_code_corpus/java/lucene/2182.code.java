package org.apache.solr.common.util;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
public interface ContentStream {
  String getName();
  String getSourceInfo();
  String getContentType();
  Long getSize(); 
  InputStream getStream() throws IOException;
  Reader getReader() throws IOException;
}
