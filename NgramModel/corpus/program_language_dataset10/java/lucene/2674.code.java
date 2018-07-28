package org.apache.solr;
import java.io.IOException;
import java.io.Writer;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.AbstractSolrTestCase;
public class OutputWriterTest extends AbstractSolrTestCase {
    public static final String USELESS_OUTPUT = "useless output";
    public String getSchemaFile() { return "solr/crazy-path-to-schema.xml"; }
    public String getSolrConfigFile() { return "solr/crazy-path-to-config.xml"; }
    public void testSOLR59responseHeaderVersions() {
        lrf.args.remove("version");
        lrf.args.put("wt", "standard");
        assertQ(req("foo"), "/response/lst[@name='responseHeader']/int[@name='status'][.='0']");
        lrf.args.remove("wt");
        assertQ(req("foo"), "/response/lst[@name='responseHeader']/int[@name='QTime']");
        lrf.args.put("version", "2.1");
        lrf.args.put("wt", "standard");
        assertQ(req("foo"), "/response/responseHeader/status[.='0']");
        lrf.args.remove("wt");
        assertQ(req("foo"), "/response/responseHeader/QTime");
        lrf.args.put("version", "2.2");
        lrf.args.put("wt", "standard");
        assertQ(req("foo"), "/response/lst[@name='responseHeader']/int[@name='status'][.='0']");
        lrf.args.remove("wt");
        assertQ(req("foo"), "/response/lst[@name='responseHeader']/int[@name='QTime']");
    }
    public void testUselessWriter() throws Exception {
        lrf.args.put("wt", "useless");
        String out = h.query(req("foo"));
        assertEquals(USELESS_OUTPUT, out);
    }
    public void testTrivialXsltWriter() throws Exception {
        lrf.args.put("wt", "xslt");
        lrf.args.put("tr", "dummy.xsl");
        String out = h.query(req("foo"));
        assertTrue(out.contains("DUMMY"));
    }
    public static class UselessOutputWriter implements QueryResponseWriter {
        public UselessOutputWriter() {}
        public void init(NamedList n) {}
        public void write(Writer writer, SolrQueryRequest request, SolrQueryResponse response)
        throws IOException {
            writer.write(USELESS_OUTPUT);
        }
      public String getContentType(SolrQueryRequest request, SolrQueryResponse response) {
        return CONTENT_TYPE_TEXT_UTF8;
      }
    }
}
