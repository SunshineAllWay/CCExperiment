package org.apache.solr.highlight;
import org.apache.lucene.search.vectorhighlight.FragmentsBuilder;
import org.apache.solr.common.params.DefaultSolrParams;
import org.apache.solr.common.params.SolrParams;
public class ScoreOrderFragmentsBuilder extends HighlightingPluginBase
    implements SolrFragmentsBuilder {
  public FragmentsBuilder getFragmentsBuilder(SolrParams params) {
    numRequests++;
    if( defaults != null ) {
      params = new DefaultSolrParams( params, defaults );
    }
    return new org.apache.lucene.search.vectorhighlight.ScoreOrderFragmentsBuilder();
  }
  @Override
  public String getDescription() {
    return "ScoreOrderFragmentsBuilder";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/highlight/ScoreOrderFragmentsBuilder.java $";
  }
  @Override
  public String getSourceId() {
    return "$Id: ScoreOrderFragmentsBuilder.java 897383 2010-01-09 04:57:20Z koji $";
  }
  @Override
  public String getVersion() {
    return "$Revision: 897383 $";
  }
}
