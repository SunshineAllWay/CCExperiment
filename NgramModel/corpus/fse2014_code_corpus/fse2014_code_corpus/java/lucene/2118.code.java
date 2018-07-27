package org.apache.solr.handler.dataimport;
import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class TestEntityProcessorBase {
  @Test
  public void multiTransformer() {
    List<Map<String, String>> fields = new ArrayList<Map<String, String>>();
    Map<String, String> entity = new HashMap<String, String>();
    entity.put("transformer", T1.class.getName() + "," + T2.class.getName()
            + "," + T3.class.getName());
    fields.add(TestRegexTransformer.getField("A", null, null, null, null));
    fields.add(TestRegexTransformer.getField("B", null, null, null, null));
    Context context = AbstractDataImportHandlerTest.getContext(null, null, new MockDataSource(), Context.FULL_DUMP,
            fields, entity);
    Map<String, Object> src = new HashMap<String, Object>();
    src.put("A", "NA");
    src.put("B", "NA");
    EntityProcessorWrapper sep = new EntityProcessorWrapper(new SqlEntityProcessor(), null);
    sep.init(context);
    Map<String, Object> res = sep.applyTransformer(src);
    Assert.assertNotNull(res.get("T1"));
    Assert.assertNotNull(res.get("T2"));
    Assert.assertNotNull(res.get("T3"));
  }
  static class T1 extends Transformer {
    public Object transformRow(Map<String, Object> aRow, Context context) {
      aRow.put("T1", "T1 called");
      return aRow;
    }
  }
  static class T2 extends Transformer {
    public Object transformRow(Map<String, Object> aRow, Context context) {
      aRow.put("T2", "T2 called");
      return aRow;
    }
  }
  static class T3 {
    public Object transformRow(Map<String, Object> aRow) {
      aRow.put("T3", "T3 called");
      return aRow;
    }
  }
}
