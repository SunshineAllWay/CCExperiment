package org.apache.solr.analysis;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.core.SolrResourceLoader;
public class TestKeepWordFilter extends BaseTokenTestCase {
  public void testStopAndGo() throws Exception 
  {  
    Set<String> words = new HashSet<String>();
    words.add( "aaa" );
    words.add( "bbb" );
    String input = "aaa BBB ccc ddd EEE";
    Map<String,String> args = new HashMap<String, String>();
    ResourceLoader loader = new SolrResourceLoader(null, null);
    KeepWordFilterFactory factory = new KeepWordFilterFactory();
    args.put( "ignoreCase", "true" );
    factory.init( args );
    factory.inform( loader );
    factory.setWords( words );
    assertTrue(factory.isIgnoreCase());
    TokenStream stream = factory.create(new WhitespaceTokenizer(new StringReader(input)));
    assertTokenStreamContents(stream, new String[] { "aaa", "BBB" });
    factory = new KeepWordFilterFactory();
    args = new HashMap<String, String>();
    factory.init( args );
    factory.inform( loader );
    factory.setIgnoreCase(true);
    factory.setWords( words );
    assertTrue(factory.isIgnoreCase());
    stream = factory.create(new WhitespaceTokenizer(new StringReader(input)));
    assertTokenStreamContents(stream, new String[] { "aaa", "BBB" });
    args = new HashMap<String, String>();
    args.put( "ignoreCase", "false" );
    factory.init( args );
    factory.inform( loader );
    assertFalse(factory.isIgnoreCase());
    stream = factory.create(new WhitespaceTokenizer(new StringReader(input)));
    assertTokenStreamContents(stream, new String[] { "aaa" });
  }
}
