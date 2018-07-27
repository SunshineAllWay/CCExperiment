package org.apache.lucene.index;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.WordlistLoader;
public class TestWordlistLoader extends LuceneTestCase {
  public void testWordlistLoading() throws IOException {
    String s = "ONE\n  two \nthree";
    HashSet<String> wordSet1 = WordlistLoader.getWordSet(new StringReader(s));
    checkSet(wordSet1);
    HashSet<String> wordSet2 = WordlistLoader.getWordSet(new BufferedReader(new StringReader(s)));
    checkSet(wordSet2);
  }
  public void testComments() throws Exception {
    String s = "ONE\n  two \nthree\n#comment";
    HashSet<String> wordSet1 = WordlistLoader.getWordSet(new StringReader(s), "#");
    checkSet(wordSet1);
    assertFalse(wordSet1.contains("#comment"));
    assertFalse(wordSet1.contains("comment"));
  }
  private void checkSet(HashSet<String> wordset) {
    assertEquals(3, wordset.size());
    assertTrue(wordset.contains("ONE"));		
    assertTrue(wordset.contains("two"));		
    assertTrue(wordset.contains("three"));
    assertFalse(wordset.contains("four"));
  }
  public void testSnowballListLoading() throws IOException {
    String s = 
      "|comment\n" + 
      " |comment\n" + 
      "\n" + 
      "  \t\n" + 
      " |comment | comment\n" + 
      "ONE\n" + 
      "   two   \n" + 
      " three   four five \n" + 
      "six seven | comment\n"; 
    Set<String> wordset = WordlistLoader.getSnowballWordSet(new StringReader(s));
    assertEquals(7, wordset.size());
    assertTrue(wordset.contains("ONE"));
    assertTrue(wordset.contains("two"));
    assertTrue(wordset.contains("three"));
    assertTrue(wordset.contains("four"));
    assertTrue(wordset.contains("five"));
    assertTrue(wordset.contains("six"));
    assertTrue(wordset.contains("seven"));
  }
}
