package org.apache.solr.response;
import java.io.Writer;
import java.io.IOException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
public class RubyResponseWriter implements QueryResponseWriter {
  static String CONTENT_TYPE_RUBY_UTF8="text/x-ruby;charset=UTF-8";
  public void init(NamedList n) {
  }
 public void write(Writer writer, SolrQueryRequest req, SolrQueryResponse rsp) throws IOException {
    RubyWriter w = new RubyWriter(writer, req, rsp);
    try {
      w.writeResponse();
    } finally {
      w.close();
    }
  }
  public String getContentType(SolrQueryRequest request, SolrQueryResponse response) {
    return CONTENT_TYPE_TEXT_UTF8;
  }
}
class RubyWriter extends NaNFloatWriter {
  protected String getNaN() { return "(0.0/0.0)"; }
  protected String getInf() { return "(1.0/0.0)"; }
  public RubyWriter(Writer writer, SolrQueryRequest req, SolrQueryResponse rsp) {
    super(writer, req, rsp);
  }
  @Override
  public void writeNull(String name) throws IOException {
    writer.write("nil");
  }
  @Override
  protected void writeKey(String fname, boolean needsEscaping) throws IOException {
    writeStr(null, fname, needsEscaping);
    writer.write('=');
    writer.write('>');
  }
  @Override
  public void writeStr(String name, String val, boolean needsEscaping) throws IOException {
    writer.write('\'');
    if (needsEscaping) {
      for (int i=0; i<val.length(); i++) {
        char ch = val.charAt(i);
        if (ch=='\'' || ch=='\\') {
          writer.write('\\');
        }
        writer.write(ch);
      }
    } else {
      writer.write(val);
    }
    writer.write('\'');
  }
}
