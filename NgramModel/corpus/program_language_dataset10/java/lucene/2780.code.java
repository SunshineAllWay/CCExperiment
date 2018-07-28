package org.apache.solr.core;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.ValueSourceParser;
import org.apache.solr.search.function.DocValues;
import org.apache.solr.search.function.SimpleFloatFunction;
import org.apache.solr.search.function.ValueSource;
public class DummyValueSourceParser extends ValueSourceParser {
  private NamedList args;
  public void init(NamedList args) {
    this.args = args;
  }
  public ValueSource parse(FunctionQParser fp) throws ParseException {
    ValueSource source = fp.parseValueSource();
    ValueSource result = new SimpleFloatFunction(source) {
      protected String name() {
        return "foo";
      }
      protected float func(int doc, DocValues vals) {
        float result = 0;
        return result;
      }
    };
    return result;
  }
}
