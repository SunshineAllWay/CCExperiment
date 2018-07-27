package org.apache.solr.handler.dataimport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import junit.framework.Assert;
import org.junit.Test;
public class TestURLDataSource {
  private List<Map<String, String>> fields = new ArrayList<Map<String, String>>();
  private URLDataSource dataSource = new URLDataSource();
  private VariableResolverImpl variableResolver = new VariableResolverImpl();
  private Context context = AbstractDataImportHandlerTest.getContext(null, variableResolver,
      dataSource, Context.FULL_DUMP, fields, null);
  private Properties initProps = new Properties();
  @Test
  public void substitutionsOnBaseUrl() throws Exception {
    String url = "http://example.com/";
    variableResolver.addNamespace("dataimporter.request", Collections.<String,Object>singletonMap("baseurl", url));
    initProps.setProperty(URLDataSource.BASE_URL, "${dataimporter.request.baseurl}");
    dataSource.init(context, initProps);
    Assert.assertEquals(url, dataSource.getBaseUrl());
  }
}
