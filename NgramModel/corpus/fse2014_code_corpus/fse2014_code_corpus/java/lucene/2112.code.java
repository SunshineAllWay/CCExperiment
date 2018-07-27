package org.apache.solr.handler.dataimport;
import junit.framework.Assert;
import org.junit.Test;
import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Clob;
import java.util.*;
public class TestClobTransformer {
  @Test
  public void simple() throws Exception {
    List<Map<String, String>> flds = new ArrayList<Map<String, String>>();
    Map<String, String> f = new HashMap<String, String>();
    f.put(DataImporter.COLUMN, "dsc");
    f.put(ClobTransformer.CLOB, "true");
    f.put(DataImporter.NAME, "description");
    flds.add(f);
    Context ctx = AbstractDataImportHandlerTest.getContext(null, new VariableResolverImpl(), null, Context.FULL_DUMP, flds, Collections.EMPTY_MAP);
    Transformer t = new ClobTransformer();
    Map<String, Object> row = new HashMap<String, Object>();
    Clob clob = (Clob) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Clob.class}, new InvocationHandler() {
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("getCharacterStream")) {
          return new StringReader("hello!");
        }
        return null;
      }
    });
    row.put("dsc", clob);
    t.transformRow(row, ctx);
    Assert.assertEquals("hello!", row.get("dsc"));
  }
}
