package org.apache.solr.handler.component;
import java.io.IOException;
import java.net.URL;
import org.apache.solr.common.params.MoreLikeThisParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.MoreLikeThisHandler;
import org.apache.solr.search.DocList;
import org.apache.solr.search.SolrIndexSearcher;
public class MoreLikeThisComponent extends SearchComponent
{
  public static final String COMPONENT_NAME = "mlt";
  @Override
  public void prepare(ResponseBuilder rb) throws IOException
  {
  }
  @Override
  public void process(ResponseBuilder rb) throws IOException
  {
    SolrParams p = rb.req.getParams();
    if( p.getBool( MoreLikeThisParams.MLT, false ) ) {
      SolrIndexSearcher searcher = rb.req.getSearcher();
      MoreLikeThisHandler.MoreLikeThisHelper mlt 
        = new MoreLikeThisHandler.MoreLikeThisHelper( p, searcher );
      int mltcount = p.getInt( MoreLikeThisParams.DOC_COUNT, 5 );
      NamedList<DocList> sim = mlt.getMoreLikeThese(
          rb.getResults().docList, mltcount, rb.getFieldFlags() );
      rb.rsp.add( "moreLikeThis", sim );
    }
  }
  @Override
  public String getDescription() {
    return "More Like This";
  }
  @Override
  public String getVersion() {
    return "$Revision: 631357 $";
  }
  @Override
  public String getSourceId() {
    return "$Id: MoreLikeThisComponent.java 631357 2008-02-26 19:47:07Z yonik $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/component/MoreLikeThisComponent.java $";
  }
  @Override
  public URL[] getDocs() {
    return null;
  }
}
