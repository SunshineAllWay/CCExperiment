package org.apache.lucene;
import org.apache.lucene.util.LuceneTestCase;
public class TestAssertions extends LuceneTestCase {
  public void test() {
    try {
      assert Boolean.FALSE.booleanValue();
      fail("assertions are not enabled!");
    } catch (AssertionError e) {
      assert Boolean.TRUE.booleanValue();
    }
  }
}
