package org.apache.solr.schema;
import org.apache.lucene.search.SortField;
import org.apache.solr.search.function.ValueSource;
import org.apache.solr.search.function.FieldCacheSource;
import org.apache.solr.search.function.DocValues;
import org.apache.solr.search.function.StringIndexDocValues;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.solr.util.NumberUtils;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import java.util.Map;
import java.io.IOException;
public class SortableFloatField extends FieldType {
  protected void init(IndexSchema schema, Map<String,String> args) {
  }
  public SortField getSortField(SchemaField field,boolean reverse) {
    return getStringSort(field,reverse);
  }
  public ValueSource getValueSource(SchemaField field) {
    return new SortableFloatFieldSource(field.name);
  }
  public String toInternal(String val) {
    return NumberUtils.float2sortableStr(val);
  }
  public String toExternal(Fieldable f) {
    return indexedToReadable(f.stringValue());
  }
  @Override
  public Float toObject(Fieldable f) {
    return NumberUtils.SortableStr2float(f.stringValue());
  }
  public String indexedToReadable(String indexedForm) {
    return NumberUtils.SortableStr2floatStr(indexedForm);
  }
  public void write(XMLWriter xmlWriter, String name, Fieldable f) throws IOException {
    String sval = f.stringValue();
    xmlWriter.writeFloat(name, NumberUtils.SortableStr2float(sval));
  }
  public void write(TextResponseWriter writer, String name, Fieldable f) throws IOException {
    String sval = f.stringValue();
    writer.writeFloat(name, NumberUtils.SortableStr2float(sval));
  }
}
class SortableFloatFieldSource extends FieldCacheSource {
  protected float defVal;
  public SortableFloatFieldSource(String field) {
    this(field, 0.0f);
  }
  public SortableFloatFieldSource(String field, float defVal) {
    super(field);
    this.defVal = defVal;
  }
    public String description() {
    return "sfloat(" + field + ')';
  }
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    final float def = defVal;
    return new StringIndexDocValues(this, reader, field) {
      protected String toTerm(String readableValue) {
        return NumberUtils.float2sortableStr(readableValue);
      }
      public float floatVal(int doc) {
        int ord=order[doc];
        return ord==0 ? def  : NumberUtils.SortableStr2float(lookup[ord]);
      }
      public int intVal(int doc) {
        return (int)floatVal(doc);
      }
      public long longVal(int doc) {
        return (long)floatVal(doc);
      }
      public double doubleVal(int doc) {
        return (double)floatVal(doc);
      }
      public String strVal(int doc) {
        return Float.toString(floatVal(doc));
      }
      public String toString(int doc) {
        return description() + '=' + floatVal(doc);
      }
    };
  }
  public boolean equals(Object o) {
    return o instanceof SortableFloatFieldSource
            && super.equals(o)
            && defVal == ((SortableFloatFieldSource)o).defVal;
  }
  private static int hcode = SortableFloatFieldSource.class.hashCode();
  public int hashCode() {
    return hcode + super.hashCode() + Float.floatToIntBits(defVal);
  };
}
