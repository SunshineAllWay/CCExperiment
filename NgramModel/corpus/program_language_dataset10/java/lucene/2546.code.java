package org.apache.solr.search.function;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Searcher;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
public class SumFloatFunction extends MultiFloatFunction {
  public SumFloatFunction(ValueSource[] sources) {
    super(sources);
  }
  @Override  
  protected String name() {
    return "sum";
  }
  protected float func(int doc, DocValues[] valsArr) {
    float val = 0.0f;
    for (DocValues vals : valsArr) {
      val += vals.floatVal(doc);
    }
    return val;
  }
}