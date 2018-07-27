package org.apache.solr.core;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.solr.common.SolrException;
import org.apache.solr.util.AbstractSolrTestCase;
import org.apache.solr.util.TestHarness;
import org.junit.Test;
import org.xml.sax.SAXException;
public class TestArbitraryIndexDir extends AbstractSolrTestCase{
  public void setUp() throws Exception {
    dataDir = new File(System.getProperty("java.io.tmpdir")
        + System.getProperty("file.separator")
        + getClass().getName() + "-" + System.currentTimeMillis() + System.getProperty("file.separator") + "solr"
        + System.getProperty("file.separator") + "data");
    dataDir.mkdirs();
    solrConfig = h.createConfig(getSolrConfigFile());
    h = new TestHarness( dataDir.getAbsolutePath(),
        solrConfig,
        getSchemaFile());
    lrf = h.getRequestFactory
    ("standard",0,20,"version","2.2");
  }
  public void tearDown() throws Exception {
    super.tearDown();
  }
  @Override
  public String getSchemaFile() {
    return "schema12.xml";
  }
  @Override
  public String getSolrConfigFile() {
    return "solrconfig.xml";
  }
  @Test
  public void testLoadNewIndexDir() throws IOException, ParserConfigurationException, SAXException, ParseException{
    assertU(adoc("id", String.valueOf(1),
        "name", "name"+String.valueOf(1)));
    File idxprops = new File(h.getCore().getDataDir() + "index.properties");
    Properties p = new Properties();
    File newDir = new File(h.getCore().getDataDir() + "index_temp");
    newDir.mkdirs();
    p.put("index", newDir.getName());
    FileOutputStream os = null;
    try {
      os = new FileOutputStream(idxprops);
      p.store(os, "index properties");
    } catch (Exception e) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
          "Unable to write index.properties", e);
    }
    Directory dir = FSDirectory.open(newDir);
    IndexWriter iw = new IndexWriter(dir, new StandardAnalyzer(Version.LUCENE_24), new MaxFieldLength(1000));
    Document doc = new Document();
    doc.add(new Field("id", "2", Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("name", "name2", Field.Store.YES, Field.Index.ANALYZED));
    iw.addDocument(doc);
    iw.commit();
    iw.close();
    assertU(commit());
    assertQ("return doc with id 2",
        req("id:2"),
        "*[count(//doc)=1]"
    );
    newDir.delete();
  }
}
