package org.apache.solr.handler.dataimport;
import org.junit.Assert;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
public class TestTemplateString {
  @Test
  public void testSimple() {
    VariableResolverImpl vri = new VariableResolverImpl();
    Map<String, Object> ns = new HashMap<String, Object>();
    ns.put("last_index_time", Long.valueOf(1199429363730l));
    vri.addNamespace("indexer", ns);
    Assert
            .assertEquals(
                    "select id from subject where last_modified > 1199429363730",
                    new TemplateString()
                            .replaceTokens(
                            "select id from subject where last_modified > ${indexer.last_index_time}",
                            vri));
  }
  private static Properties EMPTY_PROPS = new Properties();
  private static Pattern SELECT_WHERE_PATTERN = Pattern.compile(
          "^\\s*(select\\b.*?\\b)(where).*", Pattern.CASE_INSENSITIVE);
}
