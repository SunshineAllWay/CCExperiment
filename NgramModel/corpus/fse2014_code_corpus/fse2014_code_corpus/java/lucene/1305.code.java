package org.apache.lucene.search.spell;
import junit.framework.TestCase;
public class TestJaroWinklerDistance extends TestCase {
  private StringDistance sd = new JaroWinklerDistance();
  public void testGetDistance() {
    float d = sd.getDistance("al", "al");
    assertTrue(d == 1.0f);
    d = sd.getDistance("martha", "marhta");
    assertTrue(d > 0.961 && d <0.962);
    d = sd.getDistance("jones", "johnson");
    assertTrue(d > 0.832 && d < 0.833);
    d = sd.getDistance("abcvwxyz", "cabvwxyz");
    assertTrue(d > 0.958 && d < 0.959);
    d = sd.getDistance("dwayne", "duane");
    assertTrue(d > 0.84 && d < 0.841);
    d = sd.getDistance("dixon", "dicksonx");
    assertTrue(d > 0.813 && d < 0.814);
    d = sd.getDistance("fvie", "ten");
    assertTrue(d == 0f);
    float d1 = sd.getDistance("zac ephron", "zac efron");
    float d2 = sd.getDistance("zac ephron", "kai ephron");
    assertTrue(d1 > d2);
    d1 = sd.getDistance("brittney spears", "britney spears");
    d2 = sd.getDistance("brittney spears", "brittney startzman");
    assertTrue(d1 > d2);    
  }
}