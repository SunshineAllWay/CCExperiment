package org.apache.solr.search;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.solr.util.AbstractSolrTestCase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessorChain;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.common.SolrInputDocument;
import java.util.*;
import java.io.IOException;
public class TestSearchPerf extends AbstractSolrTestCase {
  public String getSchemaFile() { return "schema11.xml"; }
  public String getSolrConfigFile() { return "solrconfig.xml"; }
  public void setUp() throws Exception {
    super.setUp();
  }
  public void tearDown() throws Exception {
    super.tearDown();
  }
  String t(int tnum) {
    return String.format("%08d", tnum);
  }
  Random r = new Random(0);
  int nDocs;
  void createIndex(int nDocs) {
    this.nDocs = nDocs;
    assertU(delQ("*:*"));
    for (int i=0; i<nDocs; i++) {
      assertU(adoc("id", Float.toString(i)
              ,"foomany_s",t(r.nextInt(nDocs*10))
      ));
    }
    assertU(commit());
  }
  void createIndex2(int nDocs, String... fields) throws IOException {
    Set<String> fieldSet = new HashSet<String>(Arrays.asList(fields));
    SolrQueryRequest req = lrf.makeRequest();
    SolrQueryResponse rsp = new SolrQueryResponse();
    UpdateRequestProcessorChain processorChain = req.getCore().getUpdateProcessingChain(null);
    UpdateRequestProcessor processor = processorChain.createProcessor(req, rsp);
    boolean foomany_s = fieldSet.contains("foomany_s");
    boolean foo1_s = fieldSet.contains("foo1_s");
    boolean foo2_s = fieldSet.contains("foo2_s");
    boolean foo4_s = fieldSet.contains("foo4_s");
    boolean foo8_s = fieldSet.contains("foo8_s");
    boolean t10_100_ws = fieldSet.contains("t10_100_ws");
    for (int i=0; i<nDocs; i++) {
      SolrInputDocument doc = new SolrInputDocument();
      doc.addField("id",Float.toString(i));
      if (foomany_s) {
        doc.addField("foomany_s",t(r.nextInt(nDocs*10)));
      }
      if (foo1_s) {
        doc.addField("foo1_s",t(0));
      }
      if (foo2_s) {
        doc.addField("foo2_s",r.nextInt(2));
      }
      if (foo4_s) {
        doc.addField("foo4_s",r.nextInt(4));
      }
      if (foo8_s) {
        doc.addField("foo8_s",r.nextInt(8));
      }
      if (t10_100_ws) {
        StringBuilder sb = new StringBuilder(9*100);
        for (int j=0; j<100; j++) {
          sb.append(' ');
          sb.append(t(r.nextInt(10)));
        }
        doc.addField("t10_100_ws", sb.toString());
      }
      AddUpdateCommand cmd = new AddUpdateCommand();
      cmd.solrDoc = doc;
      processor.processAdd(cmd);
    }
    processor.finish();
    req.close();
    assertU(commit());
    req = lrf.makeRequest();
    assertEquals(nDocs, req.getSearcher().maxDoc());
    req.close();
  }
  int doSetGen(int iter, Query q) throws Exception {
    SolrQueryRequest req = lrf.makeRequest();
    SolrIndexSearcher searcher = req.getSearcher();
    long start = System.currentTimeMillis();
    int ret = 0;
    for (int i=0; i<iter; i++) {
      DocSet set = searcher.getDocSetNC(q, null);
      ret += set.size();
    }
    long end = System.currentTimeMillis();
    System.out.println("ret="+ret+ " time="+(end-start)+" throughput="+iter*1000/(end-start+1));
    req.close();
    assertTrue(ret>0);  
    return ret;
  }
  int doListGen(int iter, Query q, List<Query> filt, boolean cacheQuery, boolean cacheFilt) throws Exception {
    SolrQueryRequest req = lrf.makeRequest();
    SolrIndexSearcher searcher = req.getSearcher();
    long start = System.currentTimeMillis();
    int NO_CHECK_QCACHE       = 0x80000000;
    int GET_DOCSET            = 0x40000000;
    int NO_CHECK_FILTERCACHE  = 0x20000000;
    int GET_SCORES            = 0x01;
    int ret = 0;
    for (int i=0; i<iter; i++) {
      DocList l = searcher.getDocList(q, filt, (Sort)null, 0, 10, (cacheQuery?0:NO_CHECK_QCACHE)|(cacheFilt?0:NO_CHECK_FILTERCACHE) );
      ret += l.matches();
    }
    long end = System.currentTimeMillis();
    System.out.println("ret="+ret+ " time="+(end-start)+" throughput="+iter*1000/(end-start+1));
    req.close();
    assertTrue(ret>0);  
    return ret;
  }
  public void testEmpty() {
  }
  public void XtestSetGenerationPerformance() throws Exception {
    createIndex(49999);
    doSetGen(10000, new TermQuery(new Term("foo1_s",t(0))) );
    BooleanQuery bq = new BooleanQuery();
    bq.add(new TermQuery(new Term("foo2_s",t(0))), BooleanClause.Occur.SHOULD);
    bq.add(new TermQuery(new Term("foo2_s",t(1))), BooleanClause.Occur.SHOULD);
    doSetGen(5000, bq); 
  }
  public void XtestRangePerformance() throws Exception {
    int indexSize=1999;
    float fractionCovered=1.0f;
    String l=t(0);
    String u=t((int)(indexSize*10*fractionCovered));   
    SolrQueryRequest req = lrf.makeRequest();
    QParser parser = QParser.getParser("foomany_s:[" + l + " TO " + u + "]", null, req);
    Query range = parser.parse();
    QParser parser2 = QParser.getParser("{!frange l="+l+" u="+u+"}foomany_s", null, req);
    Query frange = parser2.parse();
    req.close();
    createIndex2(indexSize,"foomany_s");
    doSetGen(1, range);
    doSetGen(1, frange);   
    doSetGen(100, range);
    doSetGen(10000, frange);
  }
  public void XtestFilteringPerformance() throws Exception {
    int indexSize=19999;
    float fractionCovered=.1f;
    String l=t(0);
    String u=t((int)(indexSize*10*fractionCovered));
    SolrQueryRequest req = lrf.makeRequest();
    QParser parser = QParser.getParser("foomany_s:[" + l + " TO " + u + "]", null, req);
    Query rangeQ = parser.parse();
    List<Query> filters = new ArrayList<Query>();
    filters.add(rangeQ);
    req.close();
    parser = QParser.getParser("{!dismax qf=t10_100_ws pf=t10_100_ws ps=20}"+ t(0) + ' ' + t(1) + ' ' + t(2), null, req);
    Query q= parser.parse();
    createIndex2(indexSize, "foomany_s", "t10_100_ws");
    doListGen(500, q, filters, false, true);
    req.close();
  }  
}