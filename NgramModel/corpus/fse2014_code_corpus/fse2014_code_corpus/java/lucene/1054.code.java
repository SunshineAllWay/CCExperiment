package org.apache.lucene.queryParser.ext;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.ext.Extensions.Pair;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
public class ExtendableQueryParser extends QueryParser {
  private final String defaultField;
  private final Extensions extensions;
  private static final Extensions DEFAULT_EXTENSION = new Extensions();
  public ExtendableQueryParser(final Version matchVersion, final String f,
      final Analyzer a) {
    this(matchVersion, f, a, DEFAULT_EXTENSION);
  }
  public ExtendableQueryParser(final Version matchVersion, final String f,
      final Analyzer a, final Extensions ext) {
    super(matchVersion, f, a);
    this.defaultField = f;
    this.extensions = ext;
  }
  public char getExtensionFieldDelimiter() {
    return extensions.getExtensionFieldDelimiter();
  }
  @Override
  protected Query getFieldQuery(final String field, final String queryText)
      throws ParseException {
    final Pair<String,String> splitExtensionField = this.extensions
        .splitExtensionField(defaultField, field);
    final ParserExtension extension = this.extensions
        .getExtension(splitExtensionField.cud);
    if (extension != null) {
      return extension.parse(new ExtensionQuery(this, splitExtensionField.cur,
          queryText));
    }
    return super.getFieldQuery(field, queryText);
  }
}
