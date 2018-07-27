package org.apache.solr.schema;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.SortField;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import org.apache.solr.search.function.DoubleFieldSource;
import org.apache.solr.search.function.ValueSource;
import java.io.IOException;
import java.util.Map;
public class DoubleField extends FieldType {
  protected void init(IndexSchema schema, Map<String, String> args) {
    restrictProps(SORT_MISSING_FIRST | SORT_MISSING_LAST);
  }
  public SortField getSortField(SchemaField field, boolean reverse) {
    return new SortField(field.name, SortField.DOUBLE, reverse);
  }
  public ValueSource getValueSource(SchemaField field) {
    return new DoubleFieldSource(field.name);
  }
  public void write(XMLWriter xmlWriter, String name, Fieldable f) throws IOException {
    xmlWriter.writeDouble(name, f.stringValue());
  }
  public void write(TextResponseWriter writer, String name, Fieldable f) throws IOException {
    String s = f.stringValue();
    if (s.length()==0) {
      writer.writeNull(name);
      return;
    }
    try {
      double val = Double.parseDouble(s);
      writer.writeDouble(name, val);
    } catch (NumberFormatException e){
      writer.writeStr(name, s, true);
    }
  }
  @Override
  public Double toObject(Fieldable f) {
    return Double.valueOf(toExternal(f));
  }
}
