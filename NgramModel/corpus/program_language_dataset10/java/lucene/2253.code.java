package org.apache.solr.analysis;
import org.apache.lucene.analysis.payloads.*;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Payload;
import java.io.IOException;
import java.util.Map;
public class NumericPayloadTokenFilterFactory extends BaseTokenFilterFactory {
  private float payload;
  private String typeMatch;
  public void init(Map<String, String> args) {
    super.init(args);
    payload = Float.parseFloat(args.get("payload"));
    typeMatch = args.get("typeMatch");
  }
  public NumericPayloadTokenFilter create(TokenStream input) {
    return new NumericPayloadTokenFilter(input,payload,typeMatch);
  }
}
