package org.apache.solr.analysis;
import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.solr.common.SolrException;
public class TrimFilterFactory extends BaseTokenFilterFactory {
  protected boolean updateOffsets = false;
  @Override
  public void init(Map<String,String> args) {
    super.init( args );
    String v = args.get( "updateOffsets" );
    if( v != null ) {
      try {
        updateOffsets = Boolean.valueOf( v );
      }
      catch( Exception ex ) {
        throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "Error reading updateOffsets value.  Must be true or false.", ex );
      }
    }
  }
  public TrimFilter create(TokenStream input) {
    return new TrimFilter(input, updateOffsets);
  }
}
