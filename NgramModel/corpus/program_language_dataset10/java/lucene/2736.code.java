package org.apache.solr.client.solrj;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
public abstract class LargeVolumeTestBase extends SolrExampleTestBase 
{
  SolrServer gserver = null;
  static final int numdocs = 100; 
  static final int threadCount = 5;
  public void testMultiThreaded() throws Exception {
    gserver = this.getSolrServer();
    gserver.deleteByQuery( "*:*" ); 
    DocThread[] threads = new DocThread[threadCount];
    for (int i=0; i<threadCount; i++) {
      threads[i] = new DocThread( "T"+i+":" );
      threads[i].setName("DocThread-" + i);
      threads[i].start();
      log.info("Started thread: " + i);
    }
    for (int i=0; i<threadCount; i++) {
      threads[i].join();
    }
    gserver.commit();
    query(threadCount * numdocs);
    log.info("done");
  }
  private void query(int count) throws SolrServerException, IOException {
    SolrQuery query = new SolrQuery("*:*");
    QueryResponse response = gserver.query(query);
    assertEquals(0, response.getStatus());
    assertEquals(count, response.getResults().getNumFound());
  }
  public class DocThread extends Thread {
    final SolrServer tserver;
    final String name;
    public DocThread( String name )
    {
      tserver = createNewSolrServer();
      this.name = name;
    }
    @Override
    public void run() {
      try {
        UpdateResponse resp = null;
        List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
        for (int i = 0; i < numdocs; i++) {
          if (i > 0 && i % 200 == 0) {
            resp = tserver.add(docs);
            assertEquals(0, resp.getStatus());
            docs = new ArrayList<SolrInputDocument>();
          }
          if (i > 0 && i % 5000 == 0) {
            log.info(getName() + " - Committing " + i);
            resp = tserver.commit();
            assertEquals(0, resp.getStatus());
          }
          SolrInputDocument doc = new SolrInputDocument();
          doc.addField("id", name+i );
          doc.addField("cat", "foocat");
          docs.add(doc);
        }
        resp = tserver.add(docs);
        assertEquals(0, resp.getStatus());
        resp = tserver.commit();
        assertEquals(0, resp.getStatus());
        resp = tserver.optimize();
        assertEquals(0, resp.getStatus());
      } catch (Exception e) {
        e.printStackTrace();
        fail( getName() + "---" + e.getMessage() );
      }
    }
  }
}
