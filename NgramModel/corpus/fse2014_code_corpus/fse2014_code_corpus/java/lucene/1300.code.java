package org.apache.lucene.search.spell;
import java.util.Iterator;
import java.io.*;
public class PlainTextDictionary implements Dictionary {
  private BufferedReader in;
  private String line;
  private boolean hasNextCalled;
  public PlainTextDictionary(File file) throws FileNotFoundException {
    in = new BufferedReader(new FileReader(file));
  }
  public PlainTextDictionary(InputStream dictFile) {
    in = new BufferedReader(new InputStreamReader(dictFile));
  }
  public PlainTextDictionary(Reader reader) {
    in = new BufferedReader(reader);
  }
  public Iterator<String> getWordsIterator() {
    return new fileIterator();
  }
  final class fileIterator implements Iterator<String> {
    public String next() {
      if (!hasNextCalled) {
        hasNext();
      }
      hasNextCalled = false;
      return line;
    }
    public boolean hasNext() {
      hasNextCalled = true;
      try {
        line = in.readLine();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
      return (line != null) ? true : false;
    }
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
