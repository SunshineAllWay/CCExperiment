package org.apache.solr.common.util;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
public abstract class ContentStreamBase implements ContentStream
{
  public static final String DEFAULT_CHARSET = "utf-8";
  protected String name;
  protected String sourceInfo;
  protected String contentType;
  protected Long size;
  public static String getCharsetFromContentType( String contentType )
  {
    if( contentType != null ) {
      int idx = contentType.toLowerCase().indexOf( "charset=" );
      if( idx > 0 ) {
        return contentType.substring( idx + "charset=".length() ).trim();
      }
    }
    return null;
  }
  public static class URLStream extends ContentStreamBase
  {
    private final URL url;
    final URLConnection conn;
    public URLStream( URL url ) throws IOException {
      this.url = url; 
      this.conn = this.url.openConnection();
      contentType = conn.getContentType();
      name = url.toExternalForm();
      size = new Long( conn.getContentLength() );
      sourceInfo = "url";
    }
    public InputStream getStream() throws IOException {
      return conn.getInputStream();
    }
  }
  public static class FileStream extends ContentStreamBase
  {
    private final File file;
    public FileStream( File f ) throws IOException {
      file = f; 
      contentType = null; 
      name = file.getName();
      size = file.length();
      sourceInfo = file.toURI().toString();
    }
    public InputStream getStream() throws IOException {
      return new FileInputStream( file );
    }
    @Override
    public Reader getReader() throws IOException {
      String charset = getCharsetFromContentType( contentType );
      return charset == null 
        ? new FileReader( file )
        : new InputStreamReader( getStream(), charset );
    }
  }
  public static class StringStream extends ContentStreamBase
  {
    private final String str;
    public StringStream( String str ) {
      this.str = str; 
      contentType = null;
      name = null;
      size = new Long( str.length() );
      sourceInfo = "string";
    }
    public InputStream getStream() throws IOException {
      return new ByteArrayInputStream( str.getBytes(DEFAULT_CHARSET) );
    }
    @Override
    public Reader getReader() throws IOException {
      String charset = getCharsetFromContentType( contentType );
      return charset == null 
        ? new StringReader( str )
        : new InputStreamReader( getStream(), charset );
    }
  }
  public Reader getReader() throws IOException {
    String charset = getCharsetFromContentType( getContentType() );
    return charset == null 
      ? new InputStreamReader( getStream(), DEFAULT_CHARSET )
      : new InputStreamReader( getStream(), charset );
  }
  public String getContentType() {
    return contentType;
  }
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public Long getSize() {
    return size;
  }
  public void setSize(Long size) {
    this.size = size;
  }
  public String getSourceInfo() {
    return sourceInfo;
  }
  public void setSourceInfo(String sourceInfo) {
    this.sourceInfo = sourceInfo;
  }
}
