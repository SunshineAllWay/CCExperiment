package org.apache.lucene.analysis.fr;
import java.io.IOException;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.util.Version;
public class TestFrenchAnalyzer extends BaseTokenStreamTestCase {
	public void testAnalyzer() throws Exception {
		FrenchAnalyzer fa = new FrenchAnalyzer(TEST_VERSION_CURRENT);
		assertAnalyzesTo(fa, "", new String[] {
		});
		assertAnalyzesTo(
			fa,
			"chien chat cheval",
			new String[] { "chien", "chat", "cheval" });
		assertAnalyzesTo(
			fa,
			"chien CHAT CHEVAL",
			new String[] { "chien", "chat", "cheval" });
		assertAnalyzesTo(
			fa,
			"  chien  ,? + = -  CHAT /: > CHEVAL",
			new String[] { "chien", "chat", "cheval" });
		assertAnalyzesTo(fa, "chien++", new String[] { "chien" });
		assertAnalyzesTo(
			fa,
			"mot \"entreguillemet\"",
			new String[] { "mot", "entreguillemet" });
		assertAnalyzesTo(
			fa,
			"Jean-François",
			new String[] { "jean", "françois" });
		assertAnalyzesTo(
			fa,
			"le la chien les aux chat du des à cheval",
			new String[] { "chien", "chat", "cheval" });
		assertAnalyzesTo(
			fa,
			"lances chismes habitable chiste éléments captifs",
			new String[] {
				"lanc",
				"chism",
				"habit",
				"chist",
				"élément",
				"captif" });
		assertAnalyzesTo(
			fa,
			"finissions souffrirent rugissante",
			new String[] { "fin", "souffr", "rug" });
		assertAnalyzesTo(
			fa,
			"C3PO aujourd'hui oeuf ïâöûàä anticonstitutionnellement Java++ ",
			new String[] {
				"c3po",
				"aujourd'hui",
				"oeuf",
				"ïâöûàä",
				"anticonstitutionnel",
				"jav" });
		assertAnalyzesTo(
			fa,
			"33Bis 1940-1945 1940:1945 (---i+++)*",
			new String[] { "33bis", "1940-1945", "1940", "1945", "i" });
	}
	@Deprecated
	public void testAnalyzer30() throws Exception {
	    FrenchAnalyzer fa = new FrenchAnalyzer(Version.LUCENE_30);
	    assertAnalyzesTo(fa, "", new String[] {
	    });
	    assertAnalyzesTo(
	      fa,
	      "chien chat cheval",
	      new String[] { "chien", "chat", "cheval" });
	    assertAnalyzesTo(
	      fa,
	      "chien CHAT CHEVAL",
	      new String[] { "chien", "chat", "cheval" });
	    assertAnalyzesTo(
	      fa,
	      "  chien  ,? + = -  CHAT /: > CHEVAL",
	      new String[] { "chien", "chat", "cheval" });
	    assertAnalyzesTo(fa, "chien++", new String[] { "chien" });
	    assertAnalyzesTo(
	      fa,
	      "mot \"entreguillemet\"",
	      new String[] { "mot", "entreguillemet" });
	    assertAnalyzesTo(
	      fa,
	      "Jean-François",
	      new String[] { "jean", "françois" });
	    assertAnalyzesTo(
	      fa,
	      "le la chien les aux chat du des à cheval",
	      new String[] { "chien", "chat", "cheval" });
	    assertAnalyzesTo(
	      fa,
	      "lances chismes habitable chiste éléments captifs",
	      new String[] {
	        "lanc",
	        "chism",
	        "habit",
	        "chist",
	        "élément",
	        "captif" });
	    assertAnalyzesTo(
	      fa,
	      "finissions souffrirent rugissante",
	      new String[] { "fin", "souffr", "rug" });
	    assertAnalyzesTo(
	      fa,
	      "C3PO aujourd'hui oeuf ïâöûàä anticonstitutionnellement Java++ ",
	      new String[] {
	        "c3po",
	        "aujourd'hui",
	        "oeuf",
	        "ïâöûàä",
	        "anticonstitutionnel",
	        "jav" });
	    assertAnalyzesTo(
	      fa,
	      "33Bis 1940-1945 1940:1945 (---i+++)*",
	      new String[] { "33bis", "1940-1945", "1940", "1945", "i" });
	  }
	public void testReusableTokenStream() throws Exception {
	  FrenchAnalyzer fa = new FrenchAnalyzer(TEST_VERSION_CURRENT);
      assertAnalyzesToReuse(
          fa,
          "le la chien les aux chat du des à cheval",
          new String[] { "chien", "chat", "cheval" });
      assertAnalyzesToReuse(
          fa,
          "lances chismes habitable chiste éléments captifs",
          new String[] {
              "lanc",
              "chism",
              "habit",
              "chist",
              "élément",
              "captif" });
	}
	public void testExclusionTableReuse() throws Exception {
	  FrenchAnalyzer fa = new FrenchAnalyzer(TEST_VERSION_CURRENT);
	  assertAnalyzesToReuse(fa, "habitable", new String[] { "habit" });
	  fa.setStemExclusionTable(new String[] { "habitable" });
	  assertAnalyzesToReuse(fa, "habitable", new String[] { "habitable" });
	}
  public void testExclusionTableViaCtor() throws Exception {
    CharArraySet set = new CharArraySet(TEST_VERSION_CURRENT, 1, true);
    set.add("habitable");
    FrenchAnalyzer fa = new FrenchAnalyzer(TEST_VERSION_CURRENT,
        CharArraySet.EMPTY_SET, set);
    assertAnalyzesToReuse(fa, "habitable chiste", new String[] { "habitable",
        "chist" });
    fa = new FrenchAnalyzer(TEST_VERSION_CURRENT, CharArraySet.EMPTY_SET, set);
    assertAnalyzesTo(fa, "habitable chiste", new String[] { "habitable",
        "chist" });
  }
  public void testElision() throws Exception {
    FrenchAnalyzer fa = new FrenchAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(fa, "voir l'embrouille", new String[] { "voir", "embrouill" });
  }
  @Deprecated
  public void testBuggyStopwordsCasing() throws IOException {
    FrenchAnalyzer a = new FrenchAnalyzer(Version.LUCENE_30);
    assertAnalyzesTo(a, "Votre", new String[] { "votr" });
  }
  public void testStopwordsCasing() throws IOException {
    FrenchAnalyzer a = new FrenchAnalyzer(Version.LUCENE_31);
    assertAnalyzesTo(a, "Votre", new String[] { });
  }
}
