package org.apache.lucene.benchmark.byTask.tasks;
import java.io.Reader;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.feeds.DocMaker;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
public class ReadTokensTask extends PerfTask {
  public ReadTokensTask(PerfRunData runData) {
    super(runData);
  }
  private int totalTokenCount = 0;
  private Document doc = null;
  @Override
  public void setup() throws Exception {
    super.setup();
    DocMaker docMaker = getRunData().getDocMaker();
    doc = docMaker.makeDocument();
  }
  @Override
  protected String getLogMessage(int recsCount) {
    return "read " + recsCount + " docs; " + totalTokenCount + " tokens";
  }
  @Override
  public void tearDown() throws Exception {
    doc = null;
    super.tearDown();
  }
  @Override
  public int doLogic() throws Exception {
    List<Fieldable> fields = doc.getFields();
    Analyzer analyzer = getRunData().getAnalyzer();
    int tokenCount = 0;
    for(final Fieldable field : fields) {
      if (!field.isTokenized()) continue;
      final TokenStream stream;
      final TokenStream streamValue = field.tokenStreamValue();
      if (streamValue != null) 
        stream = streamValue;
      else {
        final Reader reader;			  
        final Reader readerValue = field.readerValue();
        if (readerValue != null)
          reader = readerValue;
        else {
          String stringValue = field.stringValue();
          if (stringValue == null)
            throw new IllegalArgumentException("field must have either TokenStream, String or Reader value");
          stringReader.init(stringValue);
          reader = stringReader;
        }
        stream = analyzer.reusableTokenStream(field.name(), reader);
      }
      stream.reset();
      while(stream.incrementToken())
        tokenCount++;
    }
    totalTokenCount += tokenCount;
    return tokenCount;
  }
  ReusableStringReader stringReader = new ReusableStringReader();
  private final static class ReusableStringReader extends Reader {
    int upto;
    int left;
    String s;
    void init(String s) {
      this.s = s;
      left = s.length();
      this.upto = 0;
    }
    @Override
    public int read(char[] c) {
      return read(c, 0, c.length);
    }
    @Override
    public int read(char[] c, int off, int len) {
      if (left > len) {
        s.getChars(upto, upto+len, c, off);
        upto += len;
        left -= len;
        return len;
      } else if (0 == left) {
        return -1;
      } else {
        s.getChars(upto, upto+left, c, off);
        int r = left;
        left = 0;
        upto = s.length();
        return r;
      }
    }
    @Override
    public void close() {}
  }
}
