package org.apache.solr;
import org.apache.solr.util.AbstractSolrTestCase;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.commons.io.IOUtils;
import java.io.*;
import java.util.Properties;
public class TestSolrCoreProperties extends AbstractSolrTestCase {
  private static final String CONF_DIR = "." + File.separator + "solr" + File.separator + "conf" + File.separator;
  JettySolrRunner solrJetty;
  SolrServer client;
  @Override
  public void setUp() throws Exception {
    setUpMe();
    System.setProperty("solr.solr.home", getHomeDir());
    System.setProperty("solr.data.dir", getDataDir());
    solrJetty = new JettySolrRunner("/solr", 0);
    solrJetty.start();
    String url = "http://localhost:" + solrJetty.getLocalPort() + "/solr";
    client = new CommonsHttpSolrServer(url);
  }
  @Override
  public void tearDown() throws Exception {
    solrJetty.stop();
    AbstractSolrTestCase.recurseDelete(homeDir);
  }
  public void testSimple() throws SolrServerException {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.add("q", "*:*");
    QueryResponse res = client.query(params);
    assertEquals(0, res.getResults().getNumFound());
  }
  File homeDir;
  File confDir;
  public String getHomeDir() {
    return homeDir.toString();
  }
  @Override
  public String getSchemaFile() {
    return CONF_DIR + "schema-replication1.xml";
  }
  public String getConfDir() {
    return confDir.toString();
  }
  public String getDataDir() {
    return dataDir.toString();
  }
  @Override
  public String getSolrConfigFile() {
    return CONF_DIR + "solrconfig-solcoreproperties.xml";
  }
  public void setUpMe() throws Exception {
    String home = System.getProperty("java.io.tmpdir")
            + File.separator
            + getClass().getName() + "-" + System.currentTimeMillis();
    homeDir = new File(home);
    dataDir = new File(home, "data");
    confDir = new File(home, "conf");
    homeDir.mkdirs();
    dataDir.mkdirs();
    confDir.mkdirs();
    File f = new File(confDir, "solrconfig.xml");
    copyFile(new File(getSolrConfigFile()), f);
    f = new File(confDir, "schema.xml");
    copyFile(new File(getSchemaFile()), f);
    Properties p = new Properties();
    p.setProperty("foo.foo1", "f1");
    p.setProperty("foo.foo2", "f2");
    FileOutputStream fos = new FileOutputStream(confDir + File.separator + "solrcore.properties");
    p.store(fos, null);
    fos.close();
    IOUtils.closeQuietly(fos);
  }
  private void copyFile(File src, File dst) throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(src));
    Writer out = new FileWriter(dst);
    for (String line = in.readLine(); null != line; line = in.readLine()) {
      out.write(line);
    }
    in.close();
    out.close();
  }
}
