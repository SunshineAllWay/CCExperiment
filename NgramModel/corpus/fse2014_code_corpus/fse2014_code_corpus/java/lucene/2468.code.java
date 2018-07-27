package org.apache.solr.schema;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.SortField;
import org.apache.solr.common.SolrException;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
public class UUIDField extends FieldType {
  private static final String NEW = "NEW";
  private static final char DASH='-';
  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);
    restrictProps(TOKENIZED);
  }
  @Override
  public SortField getSortField(SchemaField field, boolean reverse) {
    return getStringSort(field, reverse);
  }
  @Override
  public void write(XMLWriter xmlWriter, String name, Fieldable f)
      throws IOException {
    xmlWriter.writeStr(name, f.stringValue());
  }
  @Override
  public void write(TextResponseWriter writer, String name, Fieldable f)
      throws IOException {
    writer.writeStr(name, f.stringValue(), false);
  }
  @Override
  public String toInternal(String val) {
    if (val == null || 0==val.length() || NEW.equals(val)) {
      return UUID.randomUUID().toString().toLowerCase();
    } else {
      if (val.length() != 36 || val.charAt(8) != DASH || val.charAt(13) != DASH
          || val.charAt(18) != DASH || val.charAt(23) != DASH) {
        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
            "Invalid UUID String: '" + val + "'");
      }
      return val.toLowerCase();
    }
  }
  public String toInternal(UUID uuid) {
    return uuid.toString().toLowerCase();
  }
  @Override
  public UUID toObject(Fieldable f) {
    return UUID.fromString(f.stringValue());
  }
}
