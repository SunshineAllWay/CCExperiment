package org.apache.lucene.benchmark.byTask.feeds;
import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.util.Date;
public interface HTMLParser {
  public DocData parse(DocData docData, String name, Date date, Reader reader, DateFormat dateFormat) throws IOException, InterruptedException;
  public DocData parse(DocData docData, String name, Date date, StringBuffer inputText, DateFormat dateFormat) throws IOException, InterruptedException;
}
