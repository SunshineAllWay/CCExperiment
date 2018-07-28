package org.apache.lucene.benchmark.byTask.feeds;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import org.apache.lucene.benchmark.byTask.utils.Config;
public class ReutersContentSource extends ContentSource {
  private static final class DateFormatInfo {
    DateFormat df;
    ParsePosition pos;
  }
  private ThreadLocal<DateFormatInfo> dateFormat = new ThreadLocal<DateFormatInfo>();
  private File dataDir = null;
  private ArrayList<File> inputFiles = new ArrayList<File>();
  private int nextFile = 0;
  private int iteration = 0;
  @Override
  public void setConfig(Config config) {
    super.setConfig(config);
    File workDir = new File(config.get("work.dir", "work"));
    String d = config.get("docs.dir", "reuters-out");
    dataDir = new File(d);
    if (!dataDir.isAbsolute()) {
      dataDir = new File(workDir, d);
    }
    inputFiles.clear();
    collectFiles(dataDir, inputFiles);
    if (inputFiles.size() == 0) {
      throw new RuntimeException("No txt files in dataDir: "+dataDir.getAbsolutePath());
    }
  }
  private synchronized DateFormatInfo getDateFormatInfo() {
    DateFormatInfo dfi = dateFormat.get();
    if (dfi == null) {
      dfi = new DateFormatInfo();
      dfi.df = new SimpleDateFormat("dd-MMM-yyyy kk:mm:ss.SSS",Locale.US);
      dfi.df.setLenient(true);
      dfi.pos = new ParsePosition(0);
      dateFormat.set(dfi);
    }
    return dfi;
  }
  private Date parseDate(String dateStr) {
    DateFormatInfo dfi = getDateFormatInfo();
    dfi.pos.setIndex(0);
    dfi.pos.setErrorIndex(-1);
    return dfi.df.parse(dateStr.trim(), dfi.pos);
  }
  @Override
  public void close() throws IOException {
  }
  @Override
  public DocData getNextDocData(DocData docData) throws NoMoreDataException, IOException {
    File f = null;
    String name = null;
    synchronized (this) {
      if (nextFile >= inputFiles.size()) {
        if (!forever) {
          throw new NoMoreDataException();
        }
        nextFile = 0;
        iteration++;
      }
      f = inputFiles.get(nextFile++);
      name = f.getCanonicalPath() + "_" + iteration;
    }
    BufferedReader reader = new BufferedReader(new FileReader(f));
    try {
      String dateStr = reader.readLine();
      reader.readLine();
      String title = reader.readLine();
      reader.readLine();
      StringBuffer bodyBuf = new StringBuffer(1024);
      String line = null;
      while ((line = reader.readLine()) != null) {
        bodyBuf.append(line).append(' ');
      }
      reader.close();
      addBytes(f.length());
      Date date = parseDate(dateStr.trim());
      docData.clear();
      docData.setName(name);
      docData.setBody(bodyBuf.toString());
      docData.setTitle(title);
      docData.setDate(date);
      return docData;
    } finally {
      reader.close();
    }
  }
  @Override
  public synchronized void resetInputs() throws IOException {
    super.resetInputs();
    nextFile = 0;
    iteration = 0;
  }
}
