package org.apache.solr.client.solrj.util;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.nio.ByteBuffer;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.*;
public class ClientUtils 
{
  public static final String TEXT_XML = "text/xml; charset=utf-8";  
  public static Collection<ContentStream> toContentStreams( final String str, final String contentType )
  {
    if( str == null )
      return null;
    ArrayList<ContentStream> streams = new ArrayList<ContentStream>( 1 );
    ContentStreamBase ccc = new ContentStreamBase.StringStream( str );
    ccc.setContentType( contentType );
    streams.add( ccc );
    return streams;
  }
  public static SolrInputDocument toSolrInputDocument( SolrDocument d )
  {
    SolrInputDocument doc = new SolrInputDocument();
    for( String name : d.getFieldNames() ) {
      doc.addField( name, d.getFieldValue(name), 1.0f );
    }
    return doc;
  }
  public static SolrDocument toSolrDocument( SolrInputDocument d )
  {
    SolrDocument doc = new SolrDocument();
    for( SolrInputField field : d ) {
      doc.setField( field.getName(), field.getValue() );
    }
    return doc;
  }
  public static void writeXML( SolrInputDocument doc, Writer writer ) throws IOException
  {
    writer.write("<doc boost=\""+doc.getDocumentBoost()+"\">");
    for( SolrInputField field : doc ) {
      float boost = field.getBoost();
      String name = field.getName();
      for( Object v : field ) {
        if (v instanceof Date) {
          v = DateUtil.getThreadLocalDateFormat().format( (Date)v );
        }else if (v instanceof byte[]) {
          byte[] bytes = (byte[]) v;
          v = Base64.byteArrayToBase64(bytes, 0,bytes.length);
        } else if (v instanceof ByteBuffer) {
          ByteBuffer bytes = (ByteBuffer) v;
          v = Base64.byteArrayToBase64(bytes.array(), bytes.position(),bytes.limit() - bytes.position());
        }
        if( boost != 1.0f ) {
          XML.writeXML(writer, "field", v.toString(), "name", name, "boost", boost );
        } else if (v != null) {
          XML.writeXML(writer, "field", v.toString(), "name", name );
        }
        boost = 1.0f;
      }
    }
    writer.write("</doc>");
  }
  public static String toXML( SolrInputDocument doc )
  {
    StringWriter str = new StringWriter();
    try {
      writeXML( doc, str );
    }
    catch( Exception ex ){}
    return str.toString();
  }
  public static final Collection<String> fmts = DateUtil.DEFAULT_DATE_FORMATS;
  public static Date parseDate( String d ) throws ParseException, DateParseException
  {
    return DateUtil.parseDate(d);
  }
  public static DateFormat getThreadLocalDateFormat() {
    return DateUtil.getThreadLocalDateFormat();
  }
  public static TimeZone UTC = DateUtil.UTC;
  public static String escapeQueryChars(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\\' || c == '+' || c == '-' || c == '!'  || c == '(' || c == ')' || c == ':'
        || c == '^' || c == '[' || c == ']' || c == '\"' || c == '{' || c == '}' || c == '~'
        || c == '*' || c == '?' || c == '|' || c == '&'  || c == ';'
        || Character.isWhitespace(c)) {
        sb.append('\\');
      }
      sb.append(c);
    }
    return sb.toString();
  }
  public static String toQueryString( SolrParams params, boolean xml ) {
    StringBuilder sb = new StringBuilder(128);
    try {
      String amp = xml ? "&amp;" : "&";
      boolean first=true;
      Iterator<String> names = params.getParameterNamesIterator();
      while( names.hasNext() ) {
        String key = names.next();
        String[] valarr = params.getParams( key );
        if( valarr == null ) {
          sb.append( first?"?":amp );
          sb.append(key);
          first=false;
        }
        else {
          for (String val : valarr) {
            sb.append( first? "?":amp );
            sb.append(key);
            if( val != null ) {
              sb.append('=');
              sb.append( URLEncoder.encode( val, "UTF-8" ) );
            }
            first=false;
          }
        }
      }
    }
    catch (IOException e) {throw new RuntimeException(e);}  
    return sb.toString();
  }
}
