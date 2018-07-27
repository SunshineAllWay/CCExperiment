package org.apache.solr.schema;
import org.apache.lucene.search.SortField;
import org.apache.solr.search.function.ValueSource;
import org.apache.solr.search.function.OrdFieldSource;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Fieldable;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import org.apache.solr.analysis.SolrAnalyzer;
import java.util.Map;
import java.io.Reader;
import java.io.IOException;
public class BoolField extends FieldType {
  protected void init(IndexSchema schema, Map<String,String> args) {
  }
  public SortField getSortField(SchemaField field,boolean reverse) {
    return getStringSort(field,reverse);
  }
  public ValueSource getValueSource(SchemaField field) {
    return new OrdFieldSource(field.name);
  }
  protected final static char[] TRUE_TOKEN = {'T'};
  protected final static char[] FALSE_TOKEN = {'F'};
  protected final static Analyzer boolAnalyzer = new SolrAnalyzer() {
    public TokenStreamInfo getStream(String fieldName, Reader reader) {
      Tokenizer tokenizer = new Tokenizer(reader) {
        final TermAttribute termAtt = (TermAttribute) addAttribute(TermAttribute.class);
        boolean done = false;
        @Override
        public void reset(Reader input) throws IOException {
          done = false;
          super.reset(input);
        }
        @Override
        public boolean incrementToken() throws IOException {
          clearAttributes();
          if (done) return false;
          done = true;
          int ch = input.read();
          if (ch==-1) return false;
          termAtt.setTermBuffer(
                  ((ch=='t' || ch=='T' || ch=='1') ? TRUE_TOKEN : FALSE_TOKEN)
                  ,0,1);
          return true;
        }
      };
      return new TokenStreamInfo(tokenizer, tokenizer);
    }
  };
  public Analyzer getAnalyzer() {
    return boolAnalyzer;
  }
  public Analyzer getQueryAnalyzer() {
    return boolAnalyzer;
  }
  public String toInternal(String val) {
    char ch = (val!=null && val.length()>0) ? val.charAt(0) : 0;
    return (ch=='1' || ch=='t' || ch=='T') ? "T" : "F";
  }
  public String toExternal(Fieldable f) {
    return indexedToReadable(f.stringValue());
  }
  @Override
  public Boolean toObject(Fieldable f) {
    return Boolean.valueOf( toExternal(f) );
  }
  public String indexedToReadable(String indexedForm) {
    char ch = indexedForm.charAt(0);
    return ch=='T' ? "true" : "false";
  }
  public void write(XMLWriter xmlWriter, String name, Fieldable f) throws IOException {
    xmlWriter.writeBool(name, f.stringValue().charAt(0) =='T');
  }
  public void write(TextResponseWriter writer, String name, Fieldable f) throws IOException {
    writer.writeBool(name, f.stringValue().charAt(0) =='T');
  }
}
