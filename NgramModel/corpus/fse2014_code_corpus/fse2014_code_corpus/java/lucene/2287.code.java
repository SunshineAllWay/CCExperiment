package org.apache.solr.analysis;
import org.apache.lucene.analysis.payloads.*;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Payload;
import java.io.IOException;
import java.util.Map;
public class TokenOffsetPayloadTokenFilterFactory extends BaseTokenFilterFactory {
  public TokenOffsetPayloadTokenFilter create(TokenStream input) {
    return new TokenOffsetPayloadTokenFilter(input);
  }
}
