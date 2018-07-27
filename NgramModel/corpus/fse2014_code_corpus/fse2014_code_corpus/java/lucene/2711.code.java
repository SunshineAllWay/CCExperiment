package org.apache.solr.analysis;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.core.SolrResourceLoader;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import junit.framework.TestCase;
public class TestKeepFilterFactory extends TestCase{
  public void testInform() throws Exception {
    ResourceLoader loader = new SolrResourceLoader(null, null);
    assertTrue("loader is null and it shouldn't be", loader != null);
    KeepWordFilterFactory factory = new KeepWordFilterFactory();
    Map<String, String> args = new HashMap<String, String>();
    args.put("words", "keep-1.txt");
    args.put("ignoreCase", "true");
    factory.init(args);
    factory.inform(loader);
    Set words = factory.getWords();
    assertTrue("words is null and it shouldn't be", words != null);
    assertTrue("words Size: " + words.size() + " is not: " + 2, words.size() == 2);
    factory = new KeepWordFilterFactory();
    args.put("words", "keep-1.txt, keep-2.txt");
    factory.init(args);
    factory.inform(loader);
    words = factory.getWords();
    assertTrue("words is null and it shouldn't be", words != null);
    assertTrue("words Size: " + words.size() + " is not: " + 4, words.size() == 4);
  }
}