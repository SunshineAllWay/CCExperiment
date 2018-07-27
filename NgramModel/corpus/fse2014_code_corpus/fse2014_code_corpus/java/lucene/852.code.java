package org.apache.lucene.benchmark.byTask.feeds;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.lucene.benchmark.byTask.tasks.WriteLineDocTask;
import org.apache.lucene.benchmark.byTask.utils.Config;
public class LineDocSource extends ContentSource {
  private final static char SEP = WriteLineDocTask.SEP;
  private File file;
  private BufferedReader reader;
  private synchronized void openFile() {
    try {
      if (reader != null) {
        reader.close();
      }
      InputStream is = getInputStream(file);
      reader = new BufferedReader(new InputStreamReader(is, encoding), BUFFER_SIZE);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  @Override
  public void close() throws IOException {
    if (reader != null) {
      reader.close();
      reader = null;
    }
  }
  @Override
  public DocData getNextDocData(DocData docData) throws NoMoreDataException, IOException {
    String line;
    synchronized(this) {
      line = reader.readLine();
      if (line == null) {
        if (!forever) {
          throw new NoMoreDataException();
        }
        openFile();
        return getNextDocData(docData);
      }
    }
    int spot = line.indexOf(SEP);
    if (spot == -1) {
      throw new RuntimeException("line: [" + line + "] is in an invalid format !");
    }
    int spot2 = line.indexOf(SEP, 1 + spot);
    if (spot2 == -1) {
      throw new RuntimeException("line: [" + line + "] is in an invalid format !");
    }
    docData.clear();
    docData.setBody(line.substring(1 + spot2, line.length()));
    docData.setTitle(line.substring(0, spot));
    docData.setDate(line.substring(1 + spot, spot2));
    return docData;
  }
  @Override
  public void resetInputs() throws IOException {
    super.resetInputs();
    openFile();
  }
  @Override
  public void setConfig(Config config) {
    super.setConfig(config);
    String fileName = config.get("docs.file", null);
    if (fileName == null) {
      throw new IllegalArgumentException("docs.file must be set");
    }
    file = new File(fileName).getAbsoluteFile();
    if (encoding == null) {
      encoding = "UTF-8";
    }
  }
}
