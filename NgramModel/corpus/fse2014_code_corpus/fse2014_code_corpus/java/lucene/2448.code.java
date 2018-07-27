package org.apache.solr.schema;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.SortField;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import org.apache.solr.search.function.IntFieldSource;
import org.apache.solr.search.function.ValueSource;
import org.apache.solr.search.function.LongFieldSource;
import java.io.IOException;
import java.util.Map;
public class LongField extends FieldType {
  protected void init(IndexSchema schema, Map<String,String> args) {
    restrictProps(SORT_MISSING_FIRST | SORT_MISSING_LAST);
  }
  public SortField getSortField(SchemaField field,boolean reverse) {
    return new SortField(field.name,SortField.LONG, reverse);
  }
  public ValueSource getValueSource(SchemaField field) {
    return new LongFieldSource(field.name);
  }
  public void write(XMLWriter xmlWriter, String name, Fieldable f) throws IOException {
    xmlWriter.writeLong(name, f.stringValue());
  }
  @Override
  public void write(TextResponseWriter writer, String name, Fieldable f) throws IOException {
    String s = f.stringValue();
    if (s.length()==0) {
      writer.writeNull(name);
      return;
    }
    try {
      long val = Long.parseLong(s);
      writer.writeLong(name, val);
    } catch (NumberFormatException e){
      writer.writeStr(name, s, true);
    }
  }
  @Override
  public Long toObject(Fieldable f) {
    return Long.valueOf( toExternal(f) );
  }
}
