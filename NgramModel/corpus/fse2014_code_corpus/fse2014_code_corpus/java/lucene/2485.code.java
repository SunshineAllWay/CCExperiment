package org.apache.solr.search;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.function.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class FunctionQParser extends QParser {
  protected QueryParsing.StrParser sp;
  public FunctionQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    super(qstr, localParams, params, req);
  }
  public Query parse() throws ParseException {
    sp = new QueryParsing.StrParser(getString());
    ValueSource vs = parseValueSource();
    return new FunctionQuery(vs);
  }
  public boolean hasMoreArguments() throws ParseException {
    int ch = sp.peek();
    return (! (ch == 0 || ch == ')') );
  }
  public String parseId() throws ParseException {
    String value = sp.getId();
    consumeArgumentDelimiter();
    return value;
  }
  public Float parseFloat() throws ParseException {
    float value = sp.getFloat();
    consumeArgumentDelimiter();
    return value;
  }
  public double parseDouble() throws ParseException {
    double value = sp.getDouble();
    consumeArgumentDelimiter();
    return value;
  }
  public int parseInt() throws ParseException {
    int value = sp.getInt();
    consumeArgumentDelimiter();
    return value;
  }
  public String parseArg() throws ParseException {
    sp.eatws();
    char ch = sp.peek();
    String val = null;
    switch (ch) {
      case ')': return null;
      case '$':
        sp.pos++;
        String param = sp.getId();
        val = getParam(param);
        break;
      case '\'':
      case '"':
        val = sp.getQuotedString();
        break;
      default:
        int valStart = sp.pos;
        for (;;) {
          if (sp.pos >= sp.end) {
            throw new ParseException("Missing end to unquoted value starting at " + valStart + " str='" + sp.val +"'");
          }
          char c = sp.val.charAt(sp.pos);
          if (c==')' || c==',' || Character.isWhitespace(c)) {
            val = sp.val.substring(valStart, sp.pos);
            break;
          }
          sp.pos++;
        }
    }
    sp.eatws();
    consumeArgumentDelimiter();
    return val;
  }
  public List<ValueSource> parseValueSourceList() throws ParseException {
    List<ValueSource> sources = new ArrayList<ValueSource>(3);
    for (;;) {
      sources.add(parseValueSource(false));
      if (! consumeArgumentDelimiter()) break;
    }
    return sources;
  }
  public ValueSource parseValueSource() throws ParseException {
    return parseValueSource(true);
  }
  public Query parseNestedQuery() throws ParseException {
    Query nestedQuery;
    if (sp.opt("$")) {
      String param = sp.getId();
      String qstr = getParam(param);
      qstr = qstr==null ? "" : qstr;
      nestedQuery = subQuery(qstr, null).getQuery();
    }
    else {
      int start = sp.pos;
      String v = sp.val;
      String qs = v;
      HashMap nestedLocalParams = new HashMap<String,String>();
      int end = QueryParsing.parseLocalParams(qs, start, nestedLocalParams, getParams());
      QParser sub;
      if (end>start) {
        if (nestedLocalParams.get(QueryParsing.V) != null) {
          sub = subQuery(qs.substring(start, end), null);
        } else {
          sub = subQuery(qs, null);
          throw new ParseException("Nested local params must have value in v parameter.  got '" + qs + "'");
        }
      } else {
        throw new ParseException("Nested function query must use $param or {!v=value} forms. got '" + qs + "'");
      }
      sp.pos += end-start;  
      nestedQuery = sub.getQuery();
    }
    consumeArgumentDelimiter();
    return nestedQuery;
  }
  protected ValueSource parseValueSource(boolean doConsumeDelimiter) throws ParseException {
    ValueSource valueSource;
    int ch = sp.peek();
    if (ch>='0' && ch<='9'  || ch=='.' || ch=='+' || ch=='-') {
      valueSource = new ConstValueSource(sp.getFloat());
    } else if (ch == '"' || ch == '\''){
      valueSource = new LiteralValueSource(sp.getQuotedString());
    }
    else {
      String id = sp.getId();
      if (sp.opt("(")) {
        ValueSourceParser argParser = req.getCore().getValueSourceParser(id);
        if (argParser==null) {
          throw new ParseException("Unknown function " + id + " in FunctionQuery(" + sp + ")");
        }
        valueSource = argParser.parse(this);
        sp.expect(")");
      }
      else {
        SchemaField f = req.getSchema().getField(id);
        valueSource = f.getType().getValueSource(f, this);
      }
    }
    if (doConsumeDelimiter)
      consumeArgumentDelimiter();
    return valueSource;
  }
  protected boolean consumeArgumentDelimiter() throws ParseException {
    if (hasMoreArguments()) {
      sp.expect(",");
      return true;
    }
    return false;
  }
}
