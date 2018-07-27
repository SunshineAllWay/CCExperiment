package org.apache.lucene.queryParser.ext;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.queryParser.QueryParser;
public class Extensions {
  private final Map<String,ParserExtension> extensions = new HashMap<String,ParserExtension>();
  private final char extensionFieldDelimiter;
  public static final char DEFAULT_EXTENSION_FIELD_DELIMITER = ':';
  public Extensions() {
    this(DEFAULT_EXTENSION_FIELD_DELIMITER);
  }
  public Extensions(char extensionFieldDelimiter) {
    this.extensionFieldDelimiter = extensionFieldDelimiter;
  }
  public void add(String key, ParserExtension extension) {
    this.extensions.put(key, extension);
  }
  public final ParserExtension getExtension(String key) {
    return this.extensions.get(key);
  }
  public char getExtensionFieldDelimiter() {
    return extensionFieldDelimiter;
  }
  public Pair<String,String> splitExtensionField(String defaultField,
      String field) {
    int indexOf = field.indexOf(this.extensionFieldDelimiter);
    if (indexOf < 0)
      return new Pair<String,String>(field, null);
    final String indexField = indexOf == 0 ? defaultField : field.substring(0,
        indexOf);
    final String extensionKey = field.substring(indexOf + 1);
    return new Pair<String,String>(indexField, extensionKey);
  }
  public String escapeExtensionField(String extfield) {
    return QueryParser.escape(extfield);
  }
  public String buildExtensionField(String extensionKey) {
    return buildExtensionField(extensionKey, "");
  }
  public String buildExtensionField(String extensionKey, String field) {
    StringBuilder builder = new StringBuilder(field);
    builder.append(this.extensionFieldDelimiter);
    builder.append(extensionKey);
    return escapeExtensionField(builder.toString());
  }
  public static class Pair<Cur,Cud> {
    public final Cur cur;
    public final Cud cud;
    public Pair(Cur cur, Cud cud) {
      this.cur = cur;
      this.cud = cud;
    }
  }
}
