package org.apache.lucene.queryParser.ext;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
public abstract class ParserExtension {
  public abstract Query parse(final ExtensionQuery query) throws ParseException;
}
