package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.nodes.RangeQueryNode;
import org.apache.lucene.queryParser.standard.processors.ParametricRangeQueryNodeProcessor;
import org.apache.lucene.util.Attribute;
public interface DateResolutionAttribute extends Attribute {
  public void setDateResolution(DateTools.Resolution dateResolution);
  public DateTools.Resolution getDateResolution();
}
