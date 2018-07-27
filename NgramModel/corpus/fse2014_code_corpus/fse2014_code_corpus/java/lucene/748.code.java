package org.apache.lucene.analysis.bg;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.util.Version;
public class TestBulgarianStemmer extends BaseTokenStreamTestCase {
  public void testMasculineNouns() throws IOException {
    BulgarianAnalyzer a = new BulgarianAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(a, "град", new String[] {"град"});
    assertAnalyzesTo(a, "града", new String[] {"град"});
    assertAnalyzesTo(a, "градът", new String[] {"град"});
    assertAnalyzesTo(a, "градове", new String[] {"град"});
    assertAnalyzesTo(a, "градовете", new String[] {"град"});
    assertAnalyzesTo(a, "народ", new String[] {"народ"});
    assertAnalyzesTo(a, "народа", new String[] {"народ"});
    assertAnalyzesTo(a, "народът", new String[] {"народ"});
    assertAnalyzesTo(a, "народи", new String[] {"народ"});
    assertAnalyzesTo(a, "народите", new String[] {"народ"});
    assertAnalyzesTo(a, "народе", new String[] {"народ"});
    assertAnalyzesTo(a, "път", new String[] {"път"});
    assertAnalyzesTo(a, "пътя", new String[] {"път"});
    assertAnalyzesTo(a, "пътят", new String[] {"път"});
    assertAnalyzesTo(a, "пътища", new String[] {"път"});
    assertAnalyzesTo(a, "пътищата", new String[] {"път"});
    assertAnalyzesTo(a, "градец", new String[] {"градец"});
    assertAnalyzesTo(a, "градеца", new String[] {"градец"});
    assertAnalyzesTo(a, "градецът", new String[] {"градец"});
    assertAnalyzesTo(a, "градовце", new String[] {"градовц"});
    assertAnalyzesTo(a, "градовцете", new String[] {"градовц"});
    assertAnalyzesTo(a, "дядо", new String[] {"дяд"});
    assertAnalyzesTo(a, "дядото", new String[] {"дяд"});
    assertAnalyzesTo(a, "дядовци", new String[] {"дяд"});
    assertAnalyzesTo(a, "дядовците", new String[] {"дяд"});
    assertAnalyzesTo(a, "мъж", new String[] {"мъж"});
    assertAnalyzesTo(a, "мъжа", new String[] {"мъж"});
    assertAnalyzesTo(a, "мъже", new String[] {"мъж"});
    assertAnalyzesTo(a, "мъжете", new String[] {"мъж"});
    assertAnalyzesTo(a, "мъжо", new String[] {"мъж"});
    assertAnalyzesTo(a, "мъжът", new String[] {"мъжът"});
    assertAnalyzesTo(a, "крак", new String[] {"крак"});
    assertAnalyzesTo(a, "крака", new String[] {"крак"});
    assertAnalyzesTo(a, "кракът", new String[] {"крак"});
    assertAnalyzesTo(a, "краката", new String[] {"крак"});
    assertAnalyzesTo(a, "брат", new String[] {"брат"});
    assertAnalyzesTo(a, "брата", new String[] {"брат"});
    assertAnalyzesTo(a, "братът", new String[] {"брат"});
    assertAnalyzesTo(a, "братя", new String[] {"брат"});
    assertAnalyzesTo(a, "братята", new String[] {"брат"});
    assertAnalyzesTo(a, "брате", new String[] {"брат"});
  }
  public void testFeminineNouns() throws IOException {
    BulgarianAnalyzer a = new BulgarianAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(a, "вест", new String[] {"вест"});
    assertAnalyzesTo(a, "вестта", new String[] {"вест"});
    assertAnalyzesTo(a, "вести", new String[] {"вест"});
    assertAnalyzesTo(a, "вестите", new String[] {"вест"});
  }
  public void testNeuterNouns() throws IOException {
    BulgarianAnalyzer a = new BulgarianAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(a, "дърво", new String[] {"дърв"});
    assertAnalyzesTo(a, "дървото", new String[] {"дърв"});
    assertAnalyzesTo(a, "дърва", new String[] {"дърв"});
    assertAnalyzesTo(a, "дървета", new String[] {"дърв"});
    assertAnalyzesTo(a, "дървата", new String[] {"дърв"});
    assertAnalyzesTo(a, "дърветата", new String[] {"дърв"});
    assertAnalyzesTo(a, "море", new String[] {"мор"});
    assertAnalyzesTo(a, "морето", new String[] {"мор"});
    assertAnalyzesTo(a, "морета", new String[] {"мор"});
    assertAnalyzesTo(a, "моретата", new String[] {"мор"});
    assertAnalyzesTo(a, "изключение", new String[] {"изключени"});
    assertAnalyzesTo(a, "изключението", new String[] {"изключени"});
    assertAnalyzesTo(a, "изключенията", new String[] {"изключени"});
    assertAnalyzesTo(a, "изключения", new String[] {"изключн"});
  }
  public void testAdjectives() throws IOException {
    BulgarianAnalyzer a = new BulgarianAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(a, "красив", new String[] {"красив"});
    assertAnalyzesTo(a, "красивия", new String[] {"красив"});
    assertAnalyzesTo(a, "красивият", new String[] {"красив"});
    assertAnalyzesTo(a, "красива", new String[] {"красив"});
    assertAnalyzesTo(a, "красивата", new String[] {"красив"});
    assertAnalyzesTo(a, "красиво", new String[] {"красив"});
    assertAnalyzesTo(a, "красивото", new String[] {"красив"});
    assertAnalyzesTo(a, "красиви", new String[] {"красив"});
    assertAnalyzesTo(a, "красивите", new String[] {"красив"});
  }
  public void testExceptions() throws IOException {
    BulgarianAnalyzer a = new BulgarianAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(a, "собственик", new String[] {"собственик"});
    assertAnalyzesTo(a, "собственика", new String[] {"собственик"});
    assertAnalyzesTo(a, "собственикът", new String[] {"собственик"});
    assertAnalyzesTo(a, "собственици", new String[] {"собственик"});
    assertAnalyzesTo(a, "собствениците", new String[] {"собственик"});
    assertAnalyzesTo(a, "подлог", new String[] {"подлог"});
    assertAnalyzesTo(a, "подлога", new String[] {"подлог"});
    assertAnalyzesTo(a, "подлогът", new String[] {"подлог"});
    assertAnalyzesTo(a, "подлози", new String[] {"подлог"});
    assertAnalyzesTo(a, "подлозите", new String[] {"подлог"});
    assertAnalyzesTo(a, "кожух", new String[] {"кожух"});
    assertAnalyzesTo(a, "кожуха", new String[] {"кожух"});
    assertAnalyzesTo(a, "кожухът", new String[] {"кожух"});
    assertAnalyzesTo(a, "кожуси", new String[] {"кожух"});
    assertAnalyzesTo(a, "кожусите", new String[] {"кожух"});
    assertAnalyzesTo(a, "център", new String[] {"центр"});
    assertAnalyzesTo(a, "центъра", new String[] {"центр"});
    assertAnalyzesTo(a, "центърът", new String[] {"центр"});
    assertAnalyzesTo(a, "центрове", new String[] {"центр"});
    assertAnalyzesTo(a, "центровете", new String[] {"центр"});
    assertAnalyzesTo(a, "промяна", new String[] {"промян"});
    assertAnalyzesTo(a, "промяната", new String[] {"промян"});
    assertAnalyzesTo(a, "промени", new String[] {"промян"});
    assertAnalyzesTo(a, "промените", new String[] {"промян"});
    assertAnalyzesTo(a, "песен", new String[] {"песн"});
    assertAnalyzesTo(a, "песента", new String[] {"песн"});
    assertAnalyzesTo(a, "песни", new String[] {"песн"});
    assertAnalyzesTo(a, "песните", new String[] {"песн"});
    assertAnalyzesTo(a, "строй", new String[] {"строй"});
    assertAnalyzesTo(a, "строеве", new String[] {"строй"});
    assertAnalyzesTo(a, "строевете", new String[] {"строй"});
    assertAnalyzesTo(a, "строя", new String[] {"стр"});
    assertAnalyzesTo(a, "строят", new String[] {"стр"});
  }
  public void testWithKeywordAttribute() throws IOException {
    CharArraySet set = new CharArraySet(Version.LUCENE_31, 1, true);
    set.add("строеве");
    WhitespaceTokenizer tokenStream = new WhitespaceTokenizer(TEST_VERSION_CURRENT, 
        new StringReader("строевете строеве"));
    BulgarianStemFilter filter = new BulgarianStemFilter(
        new KeywordMarkerTokenFilter(tokenStream, set));
    assertTokenStreamContents(filter, new String[] { "строй", "строеве" });
  }
}
