package org.apache.lucene.queryParser.spans;
import org.apache.lucene.queryParser.core.config.FieldConfig;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
public class SpansQueryConfigHandler extends QueryConfigHandler {
  public SpansQueryConfigHandler() {
    addAttribute(UniqueFieldAttribute.class);
  }
  @Override
  public FieldConfig getFieldConfig(CharSequence fieldName) {
    return null;
  }
}
