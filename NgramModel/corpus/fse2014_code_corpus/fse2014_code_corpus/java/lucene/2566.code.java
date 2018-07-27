package org.apache.solr.tst;
import org.apache.lucene.search.*;
import org.apache.lucene.document.Document;
import java.util.List;
import java.io.IOException;
import java.net.URL;
import org.apache.solr.search.DocSlice;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.StrUtils;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
@Deprecated
public class OldRequestHandler implements SolrRequestHandler {
  long numRequests;
  long numErrors;
  public void init(NamedList args) {
    SolrCore.log.info( "Unused request handler arguments:" + args);
  }
  public void handleRequest(SolrQueryRequest req, SolrQueryResponse rsp) {
    numRequests++;
    Query query = null;
    Filter filter = null;
    List<String> commands = StrUtils.splitSmart(req.getQueryString(),';');
    String qs = commands.size() >= 1 ? commands.get(0) : "";
    query = QueryParsing.parseQuery(qs, req.getSchema());
    Sort sort = null;
    if (commands.size() >= 2) {
      sort = QueryParsing.parseSort(commands.get(1), req.getSchema());
    }
    try {
      int numHits;
      ScoreDoc[] scoreDocs;
      if (sort != null) {
        TopFieldDocs hits = req.getSearcher().search(query, filter,
            req.getStart() + req.getLimit(), sort);
        scoreDocs = hits.scoreDocs;
        numHits = hits.totalHits;
      } else {
        TopDocs hits = req.getSearcher().search(query, filter,
            req.getStart() + req.getLimit());
        scoreDocs = hits.scoreDocs;
        numHits = hits.totalHits;
      }
      int startRow = Math.min(numHits, req.getStart());
      int endRow = Math.min(numHits,req.getStart()+req.getLimit());
      int numRows = endRow-startRow;
      int[] ids = new int[numRows];
      Document[] data = new Document[numRows];
      for (int i=startRow; i<endRow; i++) {
        ids[i] = scoreDocs[i].doc;
        data[i] = req.getSearcher().doc(ids[i]);
      }
      rsp.add(null, new DocSlice(0,numRows,ids,null,numHits,0.0f));
    } catch (IOException e) {
      rsp.setException(e);
      numErrors++;
      return;
    }
  }
  public String getName() {
    return OldRequestHandler.class.getName();
  }
  public String getVersion() {
    return SolrCore.version;
  }
  public String getDescription() {
    return "The original Hits based request handler";
  }
  public Category getCategory() {
    return Category.QUERYHANDLER;
  }
  public String getSourceId() {
    return "$Id: OldRequestHandler.java 922984 2010-03-14 22:24:06Z markrmiller $";
  }
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/tst/OldRequestHandler.java $";
  }
  public URL[] getDocs() {
    return null;
  }
  public NamedList getStatistics() {
    NamedList lst = new NamedList();
    lst.add("requests", numRequests);
    lst.add("errors", numErrors);
    return lst;
  }
}
