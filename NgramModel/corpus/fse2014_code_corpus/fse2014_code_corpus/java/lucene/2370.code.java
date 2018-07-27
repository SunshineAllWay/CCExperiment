package org.apache.solr.highlight;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.NullFragmenter;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.solr.common.params.DefaultSolrParams;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.SolrParams;
public class GapFragmenter extends HighlightingPluginBase implements SolrFragmenter
{
  public Fragmenter getFragmenter(String fieldName, SolrParams params )
  {
    numRequests++;
    if( defaults != null ) {
      params = new DefaultSolrParams( params, defaults );
    }
    int fragsize = params.getFieldInt( fieldName, HighlightParams.FRAGSIZE, 100 );
    return (fragsize <= 0) ? new NullFragmenter() : new LuceneGapFragmenter(fragsize);
  }
  @Override
  public String getDescription() {
    return "GapFragmenter";
  }
  @Override
  public String getVersion() {
      return "$Revision: 897383 $";
  }
  @Override
  public String getSourceId() {
    return "$Id: GapFragmenter.java 897383 2010-01-09 04:57:20Z koji $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/highlight/GapFragmenter.java $";
  }
}
class LuceneGapFragmenter extends SimpleFragmenter {
  public static final int INCREMENT_THRESHOLD = 50;
  protected int fragOffset = 0;
  private OffsetAttribute offsetAtt;
  private PositionIncrementAttribute posIncAtt;
  public LuceneGapFragmenter() {
  }
  public LuceneGapFragmenter(int fragsize) {
     super(fragsize);
  }
  public void start(String originalText, TokenStream tokenStream) {
    offsetAtt = (OffsetAttribute) tokenStream.getAttribute(OffsetAttribute.class);
    posIncAtt = (PositionIncrementAttribute) tokenStream.getAttribute(PositionIncrementAttribute.class);
    fragOffset = 0;
  }
  public boolean isNewFragment() {
    int endOffset = offsetAtt.endOffset();
    boolean isNewFrag = 
      endOffset >= fragOffset + getFragmentSize() ||
      posIncAtt.getPositionIncrement() > INCREMENT_THRESHOLD;
    if(isNewFrag) {
        fragOffset = endOffset;
    }
    return isNewFrag;
  }
}
