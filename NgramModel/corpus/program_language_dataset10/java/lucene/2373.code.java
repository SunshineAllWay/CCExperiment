package org.apache.solr.highlight;
import org.apache.lucene.search.vectorhighlight.BaseFragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.FragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.ScoreOrderFragmentsBuilder;
import org.apache.solr.common.params.DefaultSolrParams;
import org.apache.solr.common.params.SolrParams;
public class MultiColoredScoreOrderFragmentsBuilder extends
    HighlightingPluginBase implements SolrFragmentsBuilder {
  public FragmentsBuilder getFragmentsBuilder(SolrParams params) {
    numRequests++;
    if( defaults != null ) {
      params = new DefaultSolrParams( params, defaults );
    }
    return new ScoreOrderFragmentsBuilder(
        BaseFragmentsBuilder.COLORED_PRE_TAGS, BaseFragmentsBuilder.COLORED_POST_TAGS );
  }
  @Override
  public String getDescription() {
    return "MultiColoredScoreOrderFragmentsBuilder";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/highlight/MultiColoredScoreOrderFragmentsBuilder.java $";
  }
  @Override
  public String getSourceId() {
    return "$Id: MultiColoredScoreOrderFragmentsBuilder.java 897383 2010-01-09 04:57:20Z koji $";
  }
  @Override
  public String getVersion() {
    return "$Revision: 897383 $";
  }
}
