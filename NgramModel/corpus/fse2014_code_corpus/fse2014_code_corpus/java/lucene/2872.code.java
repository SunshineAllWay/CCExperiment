package org.apache.solr.update;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.util.AbstractSolrTestCase;
import org.apache.solr.common.util.StrUtils;
import java.io.IOException;
import java.util.Arrays;
public class TestIndexingPerformance extends AbstractSolrTestCase {
  public String getSchemaFile() { return "schema12.xml"; }
  public String getSolrConfigFile() { return "solrconfig_perf.xml"; }
  public void testIndexingPerf() throws IOException {
    int iter=1000;
    String iterS = System.getProperty("iter");
    if (iterS != null) iter=Integer.parseInt(iterS);
    boolean includeDoc = Boolean.parseBoolean(System.getProperty("includeDoc","true")); 
    boolean overwrite = Boolean.parseBoolean(System.getProperty("overwrite","false"));
    String doc = System.getProperty("doc");
    if (doc != null) {
      StrUtils.splitSmart(doc,",",true);
    }
    SolrQueryRequest req = lrf.makeRequest();
    IndexSchema schema = req.getSchema();
    UpdateHandler updateHandler = req.getCore().getUpdateHandler();
    String[] fields = {"text","simple"
            ,"text","test"
            ,"text","how now brown cow"
            ,"text","what's that?"
            ,"text","radical!"
            ,"text","what's all this about, anyway?"
            ,"text","just how fast is this text indexing?"
    };
    long start = System.currentTimeMillis();
    AddUpdateCommand add = new AddUpdateCommand();
    add.allowDups = !overwrite;
    add.overwriteCommitted = overwrite;
    add.overwritePending = overwrite;
    Field idField=null;
    for (int i=0; i<iter; i++) {
      if (includeDoc || add.doc==null) {
        add.doc = new Document();
        idField = new Field("id","", Field.Store.YES, Field.Index.NOT_ANALYZED);
        add.doc.add(idField);
        for (int j=0; j<fields.length; j+=2) {
          String field = fields[j];
          String val = fields[j+1];
          Field f = schema.getField(field).createField(val, 1.0f);
          add.doc.add(f);
        }
      }
      idField.setValue(Integer.toString(i));
      updateHandler.addDoc(add);
    }
    long end = System.currentTimeMillis();
    System.out.println("includeDoc="+includeDoc+" doc="+ Arrays.toString(fields));
    System.out.println("iter="+iter +" time=" + (end-start) + " throughput=" + ((long)iter*1000)/(end-start));
    updateHandler.rollback(new RollbackUpdateCommand());
    req.close();
  }
}