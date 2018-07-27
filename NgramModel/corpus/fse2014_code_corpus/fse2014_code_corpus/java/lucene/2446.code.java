package org.apache.solr.schema;
import org.apache.lucene.search.SortField;
import org.apache.solr.search.function.ValueSource;
import org.apache.solr.search.function.IntFieldSource;
import org.apache.lucene.document.Fieldable;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import java.util.Map;
import java.io.IOException;
public class IntField extends FieldType {
  protected void init(IndexSchema schema, Map<String,String> args) {
    restrictProps(SORT_MISSING_FIRST | SORT_MISSING_LAST);
  }
  public SortField getSortField(SchemaField field,boolean reverse) {
    return new SortField(field.name,SortField.INT, reverse);
  }
  public ValueSource getValueSource(SchemaField field) {
    return new IntFieldSource(field.name);
  }
  public void write(XMLWriter xmlWriter, String name, Fieldable f) throws IOException {
    xmlWriter.writeInt(name, f.stringValue());
  }
  public void write(TextResponseWriter writer, String name, Fieldable f) throws IOException {
    String s = f.stringValue();
    if (s.length()==0) {
      writer.writeNull(name);
      return;
    }
    try {
      int val = Integer.parseInt(s);
      writer.writeInt(name, val);
    } catch (NumberFormatException e){
      writer.writeStr(name, s, true);
    }
  }
  @Override
  public Integer toObject(Fieldable f) {
    return Integer.valueOf( toExternal(f) );
  }
}
