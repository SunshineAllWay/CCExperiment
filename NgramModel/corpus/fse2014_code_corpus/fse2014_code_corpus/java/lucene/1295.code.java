package org.apache.lucene.search.spell;
import java.util.Iterator;
public interface Dictionary {
  Iterator<String> getWordsIterator();
}
