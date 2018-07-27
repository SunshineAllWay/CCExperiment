package org.apache.lucene.search.spell;
import java.io.IOException;
import java.io.StringReader;
import junit.framework.TestCase;
import org.apache.lucene.store.RAMDirectory;
public class TestPlainTextDictionary extends TestCase {
  public void testBuild() throws IOException {
    final String LF = System.getProperty("line.separator");
    String input = "oneword" + LF + "twoword" + LF + "threeword";
    PlainTextDictionary ptd = new PlainTextDictionary(new StringReader(input));
    RAMDirectory ramDir = new RAMDirectory();
    SpellChecker spellChecker = new SpellChecker(ramDir);
    spellChecker.indexDictionary(ptd);
    String[] similar = spellChecker.suggestSimilar("treeword", 2);
    assertEquals(2, similar.length);
    assertEquals(similar[0], "threeword");
    assertEquals(similar[1], "twoword");
  }
}
