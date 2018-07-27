package org.apache.solr.util;
import java.io.IOException;
import java.util.Iterator;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.util.StringHelper;
public class HighFrequencyDictionary implements Dictionary {
  private IndexReader reader;
  private String field;
  private float thresh;
  public HighFrequencyDictionary(IndexReader reader, String field, float thresh) {
    this.reader = reader;
    this.field = StringHelper.intern(field);
    this.thresh = thresh;
  }
  public final Iterator getWordsIterator() {
    return new HighFrequencyIterator();
  }
  final class HighFrequencyIterator implements Iterator {
    private TermEnum termEnum;
    private Term actualTerm;
    private boolean hasNextCalled;
    private int minNumDocs;
    HighFrequencyIterator() {
      try {
        termEnum = reader.terms(new Term(field, ""));
        minNumDocs = (int)(thresh * (float)reader.numDocs());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    private boolean isFrequent(Term term) {
      try {
        return reader.docFreq(term) >= minNumDocs;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    public Object next() {
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
      do {
        actualTerm = termEnum.term();
        if (actualTerm == null) {
          return false;
        }
        String currentField = actualTerm.field();
        if (currentField != field) {   
          actualTerm = null;
          return false;
        }
        if (isFrequent(actualTerm)) {
          return true;
        }
        try {
          termEnum.next();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } while (true);
    }
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
