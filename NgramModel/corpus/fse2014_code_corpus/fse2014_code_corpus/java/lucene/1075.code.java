package org.apache.lucene.queryParser.ext;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
class ExtensionStub extends ParserExtension {
  @Override
  public Query parse(ExtensionQuery components) throws ParseException {
    return new TermQuery(new Term(components.getField(), components
        .getRawQueryString()));
  }
}