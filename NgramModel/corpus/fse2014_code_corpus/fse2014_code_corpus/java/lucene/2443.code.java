package org.apache.solr.schema;
import org.apache.lucene.search.SortField;
import org.apache.solr.search.function.ValueSource;
import org.apache.solr.search.function.FloatFieldSource;
import org.apache.lucene.document.Fieldable;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import java.util.Map;
import java.io.IOException;
public class FloatField extends FieldType {
  protected void init(IndexSchema schema, Map<String,String> args) {
    restrictProps(SORT_MISSING_FIRST | SORT_MISSING_LAST);
  }
  public SortField getSortField(SchemaField field,boolean reverse) {
    return new SortField(field.name,SortField.FLOAT, reverse);
  }
  public ValueSource getValueSource(SchemaField field) {
    return new FloatFieldSource(field.name);
  }
  public void write(XMLWriter xmlWriter, String name, Fieldable f) throws IOException {
    xmlWriter.writeFloat(name, f.stringValue());
  }
  public void write(TextResponseWriter writer, String name, Fieldable f) throws IOException {
    String s = f.stringValue();
    if (s.length()==0) {
      writer.writeNull(name);
      return;
    }
    try {
      float fval = Float.parseFloat(s);
      writer.writeFloat(name, fval);
    } catch (NumberFormatException e){
      writer.writeStr(name, s, true);
    }
  }
  @Override
  public Float toObject(Fieldable f) {
    return Float.valueOf( toExternal(f) );
  }
}
