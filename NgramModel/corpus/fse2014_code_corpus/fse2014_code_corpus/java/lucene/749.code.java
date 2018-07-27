package org.apache.lucene.analysis.br;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
import org.apache.lucene.analysis.LowerCaseTokenizer;
public class TestBrazilianStemmer extends BaseTokenStreamTestCase {
  public void testWithSnowballExamples() throws Exception {
	 check("boa", "boa");
	 check("boainain", "boainain");
	 check("boas", "boas");
	 check("bôas", "boas"); 
	 check("boassu", "boassu");
	 check("boataria", "boat");
	 check("boate", "boat");
	 check("boates", "boat");
	 check("boatos", "boat");
	 check("bob", "bob");
	 check("boba", "bob");
	 check("bobagem", "bobag");
	 check("bobagens", "bobagens");
	 check("bobalhões", "bobalho"); 
	 check("bobear", "bob");
	 check("bobeira", "bobeir");
	 check("bobinho", "bobinh");
	 check("bobinhos", "bobinh");
	 check("bobo", "bob");
	 check("bobs", "bobs");
	 check("boca", "boc");
	 check("bocadas", "boc");
	 check("bocadinho", "bocadinh");
	 check("bocado", "boc");
	 check("bocaiúva", "bocaiuv"); 
	 check("boçal", "bocal"); 
	 check("bocarra", "bocarr");
	 check("bocas", "boc");
	 check("bode", "bod");
	 check("bodoque", "bodoqu");
	 check("body", "body");
	 check("boeing", "boeing");
	 check("boem", "boem");
	 check("boemia", "boem");
	 check("boêmio", "boemi"); 
	 check("bogotá", "bogot");
	 check("boi", "boi");
	 check("bóia", "boi"); 
	 check("boiando", "boi");
	 check("quiabo", "quiab");
	 check("quicaram", "quic");
	 check("quickly", "quickly");
	 check("quieto", "quiet");
	 check("quietos", "quiet");
	 check("quilate", "quilat");
	 check("quilates", "quilat");
	 check("quilinhos", "quilinh");
	 check("quilo", "quil");
	 check("quilombo", "quilomb");
	 check("quilométricas", "quilometr"); 
	 check("quilométricos", "quilometr"); 
	 check("quilômetro", "quilometr"); 
	 check("quilômetros", "quilometr"); 
	 check("quilos", "quil");
	 check("quimica", "quimic");
	 check("quilos", "quil");
	 check("quimica", "quimic");
	 check("quimicas", "quimic");
	 check("quimico", "quimic");
	 check("quimicos", "quimic");
	 check("quimioterapia", "quimioterap");
	 check("quimioterápicos", "quimioterap"); 
	 check("quimono", "quimon");
	 check("quincas", "quinc");
	 check("quinhão", "quinha"); 
	 check("quinhentos", "quinhent");
	 check("quinn", "quinn");
	 check("quino", "quin");
	 check("quinta", "quint");
	 check("quintal", "quintal");
	 check("quintana", "quintan");
	 check("quintanilha", "quintanilh");
	 check("quintão", "quinta"); 
	 check("quintessência", "quintessente"); 
	 check("quintino", "quintin");
	 check("quinto", "quint");
	 check("quintos", "quint");
	 check("quintuplicou", "quintuplic");
	 check("quinze", "quinz");
	 check("quinzena", "quinzen");
	 check("quiosque", "quiosqu");
  }
  public void testNormalization() throws Exception {
    check("Brasil", "brasil"); 
    check("Brasília", "brasil"); 
    check("quimio5terápicos", "quimio5terapicos"); 
    check("áá", "áá"); 
    check("ááá", "aaa"); 
  }
  public void testReusableTokenStream() throws Exception {
    Analyzer a = new BrazilianAnalyzer(TEST_VERSION_CURRENT);
    checkReuse(a, "boa", "boa");
    checkReuse(a, "boainain", "boainain");
    checkReuse(a, "boas", "boas");
    checkReuse(a, "bôas", "boas"); 
  }
  public void testStemExclusionTable() throws Exception {
    BrazilianAnalyzer a = new BrazilianAnalyzer(TEST_VERSION_CURRENT);
    a.setStemExclusionTable(new String[] { "quintessência" });
    checkReuse(a, "quintessência", "quintessência"); 
  }
  public void testStemExclusionTableBWCompat() throws IOException {
    CharArraySet set = new CharArraySet(TEST_VERSION_CURRENT, 1, true);
    set.add("Brasília");
    BrazilianStemFilter filter = new BrazilianStemFilter(
        new LowerCaseTokenizer(TEST_VERSION_CURRENT, new StringReader("Brasília Brasilia")), set);
    assertTokenStreamContents(filter, new String[] { "brasília", "brasil" });
  }
  public void testWithKeywordAttribute() throws IOException {
    CharArraySet set = new CharArraySet(TEST_VERSION_CURRENT, 1, true);
    set.add("Brasília");
    BrazilianStemFilter filter = new BrazilianStemFilter(
        new KeywordMarkerTokenFilter(new LowerCaseTokenizer(TEST_VERSION_CURRENT, new StringReader(
            "Brasília Brasilia")), set));
    assertTokenStreamContents(filter, new String[] { "brasília", "brasil" });
  }
  public void testWithKeywordAttributeAndExclusionTable() throws IOException {
    CharArraySet set = new CharArraySet(TEST_VERSION_CURRENT, 1, true);
    set.add("Brasília");
    CharArraySet set1 = new CharArraySet(TEST_VERSION_CURRENT, 1, true);
    set1.add("Brasilia");
    BrazilianStemFilter filter = new BrazilianStemFilter(
        new KeywordMarkerTokenFilter(new LowerCaseTokenizer(TEST_VERSION_CURRENT, new StringReader(
            "Brasília Brasilia")), set), set1);
    assertTokenStreamContents(filter, new String[] { "brasília", "brasilia" });
  }
  public void testExclusionTableReuse() throws Exception {
    BrazilianAnalyzer a = new BrazilianAnalyzer(TEST_VERSION_CURRENT);
    checkReuse(a, "quintessência", "quintessente");
    a.setStemExclusionTable(new String[] { "quintessência" });
    checkReuse(a, "quintessência", "quintessência");
  }
  private void check(final String input, final String expected) throws Exception {
    checkOneTerm(new BrazilianAnalyzer(TEST_VERSION_CURRENT), input, expected);
  }
  private void checkReuse(Analyzer a, String input, String expected) throws Exception {
    checkOneTermReuse(a, input, expected);
  }
}