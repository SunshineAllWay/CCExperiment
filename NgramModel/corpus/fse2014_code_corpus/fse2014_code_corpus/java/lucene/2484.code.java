package org.apache.solr.search;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.*;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.SolrException;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.TextField;
import org.apache.solr.schema.SchemaField;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
public class FieldQParserPlugin extends QParserPlugin {
  public static String NAME = "field";
  public void init(NamedList args) {
  }
  public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    return new QParser(qstr, localParams, params, req) {
      public Query parse() throws ParseException {
        String field = localParams.get(QueryParsing.F);
        String queryText = localParams.get(QueryParsing.V);
        SchemaField sf = req.getSchema().getField(field);
        FieldType ft = sf.getType();
        return ft.getFieldQuery(this, sf, queryText);
      }
    };
  }
}
