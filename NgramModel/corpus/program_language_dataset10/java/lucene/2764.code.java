package org.apache.solr.client.solrj.response;
import junit.framework.Assert;
import org.apache.solr.client.solrj.SolrExampleTestBase;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SpellingParams;
import java.util.List;
public class TestSpellCheckResponse extends SolrExampleTestBase {
  SolrServer server;
  JettySolrRunner jetty;
  int port = 0;
  static final String context = "/example";
  static String field = "name";
  public void setUp() throws Exception {
    super.setUp();
    jetty = new JettySolrRunner(context, 0);
    jetty.start();
    port = jetty.getLocalPort();
    log.info("Assigned Port: " + port);
    server = this.createNewSolrServer();
  }
  public void testSpellCheckResponse() throws Exception {
    SolrInputDocument doc = new SolrInputDocument();
    doc.setField("id", "111");
    doc.setField(field, "Samsung");
    server.add(doc);
    server.commit(true, true);
    SolrQuery query = new SolrQuery("*:*");
    query.set(CommonParams.QT, "/spell");
    query.set("spellcheck", true);
    query.set(SpellingParams.SPELLCHECK_Q, "samsang");
    query.set(SpellingParams.SPELLCHECK_BUILD, true);
    QueryRequest request = new QueryRequest(query);
    SpellCheckResponse response = request.process(server).getSpellCheckResponse();
    Assert.assertEquals("samsung", response.getFirstSuggestion("samsang"));
  }
  public void testSpellCheckResponse_Extended() throws Exception {
    SolrInputDocument doc = new SolrInputDocument();
    doc.setField("id", "111");
    doc.setField(field, "Samsung");
    server.add(doc);
    server.commit(true, true);
    SolrQuery query = new SolrQuery("*:*");
    query.set(CommonParams.QT, "/spell");
    query.set("spellcheck", true);
    query.set(SpellingParams.SPELLCHECK_Q, "samsang");
    query.set(SpellingParams.SPELLCHECK_BUILD, true);
    query.set(SpellingParams.SPELLCHECK_EXTENDED_RESULTS, true);
    QueryRequest request = new QueryRequest(query);
    SpellCheckResponse response = request.process(server).getSpellCheckResponse();
    assertEquals("samsung", response.getFirstSuggestion("samsang"));
    SpellCheckResponse.Suggestion sug = response.getSuggestion("samsang");
    List<SpellCheckResponse.Suggestion> sugs = response.getSuggestions();
    assertEquals(sug.getAlternatives().size(), sug.getAlternativeFrequencies().size());
    assertEquals(sugs.get(0).getAlternatives().size(), sugs.get(0).getAlternativeFrequencies().size());
    assertEquals("samsung", sug.getAlternatives().get(0));
    assertEquals("samsung", sugs.get(0).getAlternatives().get(0));
    assertTrue(sug.getEndOffset()>0);
    assertTrue(sug.getToken().length() > 0);
    assertTrue(sug.getNumFound() > 0);
    response.getSuggestions().get(0).getAlternatives().get(0);
  }
  protected SolrServer getSolrServer() {
    return server;
  }
  protected SolrServer createNewSolrServer() {
    try {
      String url = "http://localhost:" + port + context;
      CommonsHttpSolrServer s = new CommonsHttpSolrServer(url);
      s.setConnectionTimeout(100); 
      s.setDefaultMaxConnectionsPerHost(100);
      s.setMaxTotalConnections(100);
      return s;
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
