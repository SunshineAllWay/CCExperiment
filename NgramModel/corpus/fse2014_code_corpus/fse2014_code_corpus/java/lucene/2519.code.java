package org.apache.solr.search.function;
public class DivFloatFunction extends DualFloatFunction {
  public DivFloatFunction(ValueSource a, ValueSource b) {
    super(a,b);
  }
  protected String name() {
    return "div";
  }
  protected float func(int doc, DocValues aVals, DocValues bVals) {
    return aVals.floatVal(doc) / bVals.floatVal(doc);
  }
}
