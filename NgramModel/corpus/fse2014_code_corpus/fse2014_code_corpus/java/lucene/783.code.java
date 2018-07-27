package org.apache.lucene.analysis.nl;
import java.io.File;
import java.io.IOException;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.util.Version;
public class TestDutchStemmer extends BaseTokenStreamTestCase {
  public void testWithSnowballExamples() throws Exception {
	 check("lichaamsziek", "lichaamsziek");
	 check("lichamelijk", "licham");
	 check("lichamelijke", "licham");
	 check("lichamelijkheden", "licham");
	 check("lichamen", "licham");
	 check("lichere", "licher");
	 check("licht", "licht");
	 check("lichtbeeld", "lichtbeeld");
	 check("lichtbruin", "lichtbruin");
	 check("lichtdoorlatende", "lichtdoorlat");
	 check("lichte", "licht");
	 check("lichten", "licht");
	 check("lichtende", "lichtend");
	 check("lichtenvoorde", "lichtenvoord");
	 check("lichter", "lichter");
	 check("lichtere", "lichter");
	 check("lichters", "lichter");
	 check("lichtgevoeligheid", "lichtgevoel");
	 check("lichtgewicht", "lichtgewicht");
	 check("lichtgrijs", "lichtgrijs");
	 check("lichthoeveelheid", "lichthoevel");
	 check("lichtintensiteit", "lichtintensiteit");
	 check("lichtje", "lichtj");
	 check("lichtjes", "lichtjes");
	 check("lichtkranten", "lichtkrant");
	 check("lichtkring", "lichtkring");
	 check("lichtkringen", "lichtkring");
	 check("lichtregelsystemen", "lichtregelsystem");
	 check("lichtste", "lichtst");
	 check("lichtstromende", "lichtstrom");
	 check("lichtte", "licht");
	 check("lichtten", "licht");
	 check("lichttoetreding", "lichttoetred");
	 check("lichtverontreinigde", "lichtverontreinigd");
	 check("lichtzinnige", "lichtzinn");
	 check("lid", "lid");
	 check("lidia", "lidia");
	 check("lidmaatschap", "lidmaatschap");
	 check("lidstaten", "lidstat");
	 check("lidvereniging", "lidveren");
	 check("opgingen", "opging");
	 check("opglanzing", "opglanz");
	 check("opglanzingen", "opglanz");
	 check("opglimlachten", "opglimlacht");
	 check("opglimpen", "opglimp");
	 check("opglimpende", "opglimp");
	 check("opglimping", "opglimp");
	 check("opglimpingen", "opglimp");
	 check("opgraven", "opgrav");
	 check("opgrijnzen", "opgrijnz");
	 check("opgrijzende", "opgrijz");
	 check("opgroeien", "opgroei");
	 check("opgroeiende", "opgroei");
	 check("opgroeiplaats", "opgroeiplat");
	 check("ophaal", "ophal");
	 check("ophaaldienst", "ophaaldienst");
	 check("ophaalkosten", "ophaalkost");
	 check("ophaalsystemen", "ophaalsystem");
	 check("ophaalt", "ophaalt");
	 check("ophaaltruck", "ophaaltruck");
	 check("ophalen", "ophal");
	 check("ophalend", "ophal");
	 check("ophalers", "ophaler");
	 check("ophef", "ophef");
	 check("opheldering", "ophelder");
	 check("ophemelde", "ophemeld");
	 check("ophemelen", "ophemel");
	 check("opheusden", "opheusd");
	 check("ophief", "ophief");
	 check("ophield", "ophield");
	 check("ophieven", "ophiev");
	 check("ophoepelt", "ophoepelt");
	 check("ophoog", "ophog");
	 check("ophoogzand", "ophoogzand");
	 check("ophopen", "ophop");
	 check("ophoping", "ophop");
	 check("ophouden", "ophoud");
  }
  @Deprecated
  public void testOldBuggyStemmer() throws Exception {
    Analyzer a = new DutchAnalyzer(Version.LUCENE_30);
    checkOneTermReuse(a, "opheffen", "ophef"); 
    checkOneTermReuse(a, "opheffende", "ophef"); 
    checkOneTermReuse(a, "opheffing", "ophef"); 
  }
  public void testSnowballCorrectness() throws Exception {
    Analyzer a = new DutchAnalyzer(TEST_VERSION_CURRENT);
    checkOneTermReuse(a, "opheffen", "opheff");
    checkOneTermReuse(a, "opheffende", "opheff");
    checkOneTermReuse(a, "opheffing", "opheff");
  }
  public void testReusableTokenStream() throws Exception {
    Analyzer a = new DutchAnalyzer(TEST_VERSION_CURRENT); 
    checkOneTermReuse(a, "lichaamsziek", "lichaamsziek");
    checkOneTermReuse(a, "lichamelijk", "licham");
    checkOneTermReuse(a, "lichamelijke", "licham");
    checkOneTermReuse(a, "lichamelijkheden", "licham");
  }
  public void testExclusionTableReuse() throws Exception {
    DutchAnalyzer a = new DutchAnalyzer(TEST_VERSION_CURRENT);
    checkOneTermReuse(a, "lichamelijk", "licham");
    a.setStemExclusionTable(new String[] { "lichamelijk" });
    checkOneTermReuse(a, "lichamelijk", "lichamelijk");
  }
  public void testExclusionTableViaCtor() throws IOException {
    CharArraySet set = new CharArraySet(Version.LUCENE_30, 1, true);
    set.add("lichamelijk");
    DutchAnalyzer a = new DutchAnalyzer(TEST_VERSION_CURRENT, CharArraySet.EMPTY_SET, set);
    assertAnalyzesToReuse(a, "lichamelijk lichamelijke", new String[] { "lichamelijk", "licham" });
    a = new DutchAnalyzer(TEST_VERSION_CURRENT, CharArraySet.EMPTY_SET, set);
    assertAnalyzesTo(a, "lichamelijk lichamelijke", new String[] { "lichamelijk", "licham" });
  }
  public void testStemDictionaryReuse() throws Exception {
    DutchAnalyzer a = new DutchAnalyzer(TEST_VERSION_CURRENT);
    checkOneTermReuse(a, "lichamelijk", "licham");
    File customDictFile = getDataFile("customStemDict.txt");
    a.setStemDictionary(customDictFile);
    checkOneTermReuse(a, "lichamelijk", "somethingentirelydifferent");
  }
  @Deprecated
  public void testBuggyStopwordsCasing() throws IOException {
    DutchAnalyzer a = new DutchAnalyzer(Version.LUCENE_30);
    assertAnalyzesTo(a, "Zelf", new String[] { "zelf" });
  }
  public void testStopwordsCasing() throws IOException {
    DutchAnalyzer a = new DutchAnalyzer(Version.LUCENE_31);
    assertAnalyzesTo(a, "Zelf", new String[] { });
  }
  private void check(final String input, final String expected) throws Exception {
    checkOneTerm(new DutchAnalyzer(TEST_VERSION_CURRENT), input, expected); 
  }
}