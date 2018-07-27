package org.apache.solr.analysis;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.core.SolrResourceLoader;
public class TestDictionaryCompoundWordTokenFilterFactory extends BaseTokenTestCase {
  public void testDecompounding() throws Exception {
    Reader reader = new StringReader("I like to play softball");
    Tokenizer tokenizer = new WhitespaceTokenizer(reader);
    DictionaryCompoundWordTokenFilterFactory factory = new DictionaryCompoundWordTokenFilterFactory();
    ResourceLoader loader = new SolrResourceLoader(null, null);
    Map<String,String> args = new HashMap<String,String>();
    args.put("dictionary", "compoundDictionary.txt");
    factory.init(args);
    factory.inform(loader);
    TokenStream stream = factory.create(tokenizer);
    assertTokenStreamContents(stream, 
        new String[] { "I", "like", "to", "play", "softball", "soft", "ball" });
  }
}
