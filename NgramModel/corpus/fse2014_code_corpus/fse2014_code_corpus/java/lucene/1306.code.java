package org.apache.lucene.search.spell;
import junit.framework.TestCase;
public class TestLevenshteinDistance extends TestCase {
  private StringDistance sd = new LevensteinDistance();
  public void testGetDistance() {
    float d = sd.getDistance("al", "al");
    assertEquals(d,1.0f,0.001);
    d = sd.getDistance("martha", "marhta");
    assertEquals(d,0.6666,0.001);
    d = sd.getDistance("jones", "johnson");
    assertEquals(d,0.4285,0.001);
    d = sd.getDistance("abcvwxyz", "cabvwxyz");
    assertEquals(d,0.75,0.001);    
    d = sd.getDistance("dwayne", "duane");
    assertEquals(d,0.666,0.001);
    d = sd.getDistance("dixon", "dicksonx");
    assertEquals(d,0.5,0.001);
    d = sd.getDistance("six", "ten");
    assertEquals(d,0,0.001);
    float d1 = sd.getDistance("zac ephron", "zac efron");
    float d2 = sd.getDistance("zac ephron", "kai ephron");
    assertEquals(d1,d2,0.001);
    d1 = sd.getDistance("brittney spears", "britney spears");
    d2 = sd.getDistance("brittney spears", "brittney startzman");
    assertTrue(d1 > d2);
  }
  public void testEmpty() throws Exception {
    float d = sd.getDistance("", "al");
    assertEquals(d,0.0f,0.001);
  }
}
