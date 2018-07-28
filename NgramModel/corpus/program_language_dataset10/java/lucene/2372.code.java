package org.apache.solr.highlight;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.solr.common.params.DefaultSolrParams;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.SolrParams;
public class HtmlFormatter extends HighlightingPluginBase implements SolrFormatter 
{
  public Formatter getFormatter(String fieldName, SolrParams params ) 
  {
    numRequests++;
    if( defaults != null ) {
      params = new DefaultSolrParams( params, defaults );
    }
    return new SimpleHTMLFormatter(
        params.getFieldParam(fieldName, HighlightParams.SIMPLE_PRE,  "<em>" ), 
        params.getFieldParam(fieldName, HighlightParams.SIMPLE_POST, "</em>"));
  }
  @Override
  public String getDescription() {
    return "HtmlFormatter";
  }
  @Override
  public String getVersion() {
      return "$Revision: 557874 $";
  }
  @Override
  public String getSourceId() {
    return "$Id: HtmlFormatter.java 557874 2007-07-20 05:39:15Z klaas $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/highlight/HtmlFormatter.java $";
  }
}
