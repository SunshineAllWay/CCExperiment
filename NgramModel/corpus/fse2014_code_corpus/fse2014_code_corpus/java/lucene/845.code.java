package org.apache.lucene.benchmark.byTask.feeds;
import org.apache.lucene.benchmark.byTask.utils.Config;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Stack;
public class DirContentSource extends ContentSource {
  private static final class DateFormatInfo {
    DateFormat df;
    ParsePosition pos;
  }
  public static class Iterator implements java.util.Iterator<File> {
    static class Comparator implements java.util.Comparator<File> {
      public int compare(File _a, File _b) {
        String a = _a.toString();
        String b = _b.toString();
        int diff = a.length() - b.length();
        if (diff > 0) {
          while (diff-- > 0) {
            b = "0" + b;
          }
        } else if (diff < 0) {
          diff = -diff;
          while (diff-- > 0) {
            a = "0" + a;
          }
        }
        return b.compareTo(a);
      }
    }
    int count = 0;
    Stack<File> stack = new Stack<File>();
    Comparator c = new Comparator();
    public Iterator(File f) {
      push(f);
    }
    void find() {
      if (stack.empty()) {
        return;
      }
      if (!(stack.peek()).isDirectory()) {
        return;
      }
      File f = stack.pop();
      push(f);
    }
    void push(File f) {
      push(f.listFiles(new FileFilter() {
        public boolean accept(File file) {
          return file.isDirectory();
        }
      }));
      push(f.listFiles(new FileFilter() {
        public boolean accept(File file) {
          return file.getName().endsWith(".txt");
        }
      }));
      find();
    }
    void push(File[] files) {
      Arrays.sort(files, c);
      for(int i = 0; i < files.length; i++) {
        stack.push(files[i]);
      }
    }
    public int getCount(){
      return count;
    }
    public boolean hasNext() {
      return stack.size() > 0;
    }
    public File next() {
      assert hasNext();
      count++;
      File object = stack.pop();
      find();
      return object;
    }
    public void remove() {
      throw new RuntimeException("cannot");
    }
  }
  private ThreadLocal<DateFormatInfo> dateFormat = new ThreadLocal<DateFormatInfo>();
  private File dataDir = null;
  private int iteration = 0;
  private Iterator inputFiles = null;
  private DateFormatInfo getDateFormatInfo() {
    DateFormatInfo dfi = dateFormat.get();
    if (dfi == null) {
      dfi = new DateFormatInfo();
      dfi.pos = new ParsePosition(0);
      dfi.df = new SimpleDateFormat("dd-MMM-yyyy kk:mm:ss.SSS", Locale.US);
      dfi.df.setLenient(true);
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
    inputFiles = null;
  }
  @Override
  public DocData getNextDocData(DocData docData) throws NoMoreDataException, IOException {
    File f = null;
    String name = null;
    synchronized (this) {
      if (!inputFiles.hasNext()) { 
        if (!forever) {
          throw new NoMoreDataException();
        }
        inputFiles = new Iterator(dataDir);
        iteration++;
      }
      f = inputFiles.next();
      name = f.getCanonicalPath()+"_"+iteration;
    }
    BufferedReader reader = new BufferedReader(new FileReader(f));
    String line = null;
    String dateStr = reader.readLine();
    reader.readLine();
    String title = reader.readLine();
    reader.readLine();
    StringBuffer bodyBuf = new StringBuffer(1024);
    while ((line = reader.readLine()) != null) {
      bodyBuf.append(line).append(' ');
    }
    reader.close();
    addBytes(f.length());
    Date date = parseDate(dateStr);
    docData.clear();
    docData.setName(name);
    docData.setBody(bodyBuf.toString());
    docData.setTitle(title);
    docData.setDate(date);
    return docData;
  }
  @Override
  public synchronized void resetInputs() throws IOException {
    super.resetInputs();
    inputFiles = new Iterator(dataDir);
    iteration = 0;
  }
  @Override
  public void setConfig(Config config) {
    super.setConfig(config);
    File workDir = new File(config.get("work.dir", "work"));
    String d = config.get("docs.dir", "dir-out");
    dataDir = new File(d);
    if (!dataDir.isAbsolute()) {
      dataDir = new File(workDir, d);
    }
    inputFiles = new Iterator(dataDir);
    if (inputFiles == null) {
      throw new RuntimeException("No txt files in dataDir: " + dataDir.getAbsolutePath());
    }
  }
}
