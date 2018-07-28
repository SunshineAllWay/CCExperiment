package org.apache.solr.search.function;
public class ProductFloatFunction extends MultiFloatFunction {
  public ProductFloatFunction(ValueSource[] sources) {
    super(sources);
  }
  protected String name() {
    return "product";
  }
  protected float func(int doc, DocValues[] valsArr) {
    float val = 1.0f;
    for (DocValues vals : valsArr) {
      val *= vals.floatVal(doc);
    }
    return val;
  }
}
