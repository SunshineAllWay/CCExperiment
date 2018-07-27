package org.apache.solr.common.util;
import junit.framework.TestCase;
public class NamedListTest extends TestCase {
  public void testRemove() {
    NamedList<String> nl = new NamedList<String>();
    nl.add("key1", "value1");
    nl.add("key2", "value2");
    assertEquals(2, nl.size());
    String value = nl.remove(0);
    assertEquals("value1", value);
    assertEquals(1, nl.size());
  }
}
