package org.apache.lucene.search.spell;
import org.apache.lucene.index.IndexReader;
import java.util.Iterator;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.StringHelper;
import java.io.*;
public class LuceneDictionary implements Dictionary {
  private IndexReader reader;
  private String field;
  public LuceneDictionary(IndexReader reader, String field) {
    this.reader = reader;
    this.field = StringHelper.intern(field);
  }
  public final Iterator<String> getWordsIterator() {
    return new LuceneIterator();
  }
  final class LuceneIterator implements Iterator<String> {
    private TermEnum termEnum;
    private Term actualTerm;
    private boolean hasNextCalled;
    LuceneIterator() {
      try {
        termEnum = reader.terms(new Term(field));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    public String next() {
      if (!hasNextCalled) {
        hasNext();
      }
      hasNextCalled = false;
      try {
        termEnum.next();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return (actualTerm != null) ? actualTerm.text() : null;
    }
    public boolean hasNext() {
      if (hasNextCalled) {
        return actualTerm != null;
      }
      hasNextCalled = true;
      actualTerm = termEnum.term();
      if (actualTerm == null) {
        return false;
      }
      String currentField = actualTerm.field();
      if (currentField != field) {
        actualTerm = null;
        return false;
      }
      return true;
    }
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
