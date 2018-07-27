package org.apache.solr.analysis;
import org.apache.lucene.analysis.shingle.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Iterator;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Token;
import java.util.Map;
public class ShingleFilterFactory extends BaseTokenFilterFactory {
  private int maxShingleSize;
  private boolean outputUnigrams;
  public void init(Map<String, String> args) {
    super.init(args);
    maxShingleSize = getInt("maxShingleSize", 
                            ShingleFilter.DEFAULT_MAX_SHINGLE_SIZE);
    outputUnigrams = getBoolean("outputUnigrams", true);
  }
  public ShingleFilter create(TokenStream input) {
    ShingleFilter r = new ShingleFilter(input,maxShingleSize);
    r.setOutputUnigrams(outputUnigrams);
    return r;
  }
}
