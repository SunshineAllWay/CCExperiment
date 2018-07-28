package org.apache.solr.highlight;
import org.apache.lucene.search.vectorhighlight.BaseFragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.FragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.SimpleFragmentsBuilder;
import org.apache.solr.common.params.DefaultSolrParams;
import org.apache.solr.common.params.SolrParams;
public class MultiColoredSimpleFragmentsBuilder extends HighlightingPluginBase
    implements SolrFragmentsBuilder {
  public FragmentsBuilder getFragmentsBuilder(SolrParams params) {
    numRequests++;
    if( defaults != null ) {
      params = new DefaultSolrParams( params, defaults );
    }
    return new SimpleFragmentsBuilder(
        BaseFragmentsBuilder.COLORED_PRE_TAGS, BaseFragmentsBuilder.COLORED_POST_TAGS );
  }
  @Override
  public String getDescription() {
    return "MultiColoredSimpleFragmentsBuilder";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/highlight/MultiColoredSimpleFragmentsBuilder.java $";
  }
  @Override
  public String getSourceId() {
    return "$Id: MultiColoredSimpleFragmentsBuilder.java 897383 2010-01-09 04:57:20Z koji $";
  }
  @Override
  public String getVersion() {
    return "$Revision: 897383 $";
  }
}
