package org.apache.solr.analysis;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.core.SolrResourceLoader;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import junit.framework.TestCase;
public class TestStopFilterFactory extends TestCase {
  public void testInform() throws Exception {
    ResourceLoader loader = new SolrResourceLoader(null, null);
    assertTrue("loader is null and it shouldn't be", loader != null);
    StopFilterFactory factory = new StopFilterFactory();
    Map<String, String> args = new HashMap<String, String>();
    args.put("words", "stop-1.txt");
    args.put("ignoreCase", "true");
    factory.init(args);
    factory.inform(loader);
    Set words = factory.getStopWords();
    assertTrue("words is null and it shouldn't be", words != null);
    assertTrue("words Size: " + words.size() + " is not: " + 2, words.size() == 2);
    assertTrue(factory.isIgnoreCase() + " does not equal: " + true, factory.isIgnoreCase() == true);
    factory = new StopFilterFactory();
    args.put("words", "stop-1.txt, stop-2.txt");
    factory.init(args);
    factory.inform(loader);
    words = factory.getStopWords();
    assertTrue("words is null and it shouldn't be", words != null);
    assertTrue("words Size: " + words.size() + " is not: " + 4, words.size() == 4);
    assertTrue(factory.isIgnoreCase() + " does not equal: " + true, factory.isIgnoreCase() == true);
  }
}
