package org.apache.solr.schema;
import org.apache.lucene.search.SortField;
import org.apache.solr.search.function.ValueSource;
import org.apache.lucene.document.Fieldable;
import org.apache.solr.util.BCDUtils;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import java.util.Map;
import java.io.IOException;
public class BCDIntField extends FieldType {
  protected void init(IndexSchema schema, Map<String,String> args) {
  }
  public SortField getSortField(SchemaField field,boolean reverse) {
    return getStringSort(field,reverse);
  }
  public ValueSource getValueSource(SchemaField field) {
    throw new UnsupportedOperationException("ValueSource not implemented");
  }
  public String toInternal(String val) {
    return BCDUtils.base10toBase10kSortableInt(val);
  }
  public String toExternal(Fieldable f) {
    return indexedToReadable(f.stringValue());
  }
  @Override
  public Object toObject(Fieldable f) {
    return Integer.valueOf( toExternal(f) );
  }
  public String indexedToReadable(String indexedForm) {
    return BCDUtils.base10kSortableIntToBase10(indexedForm);
  }
  public void write(XMLWriter xmlWriter, String name, Fieldable f) throws IOException {
    xmlWriter.writeInt(name,toExternal(f));
  }
  public void write(TextResponseWriter writer, String name, Fieldable f) throws IOException {
    writer.writeInt(name,toExternal(f));
  }
}
