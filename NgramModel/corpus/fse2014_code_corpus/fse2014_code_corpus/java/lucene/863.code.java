package org.apache.lucene.benchmark.byTask.feeds;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.benchmark.byTask.utils.StringBufferReader;
import org.apache.lucene.util.ThreadInterruptedException;
public class TrecContentSource extends ContentSource {
  private static final class DateFormatInfo {
    DateFormat[] dfs;
    ParsePosition pos;
  }
  private static final String DATE = "Date: ";
  private static final String DOCHDR = "<DOCHDR>";
  private static final String TERMINATING_DOCHDR = "</DOCHDR>";
  private static final String DOCNO = "<DOCNO>";
  private static final String TERMINATING_DOCNO = "</DOCNO>";
  private static final String DOC = "<DOC>";
  private static final String TERMINATING_DOC = "</DOC>";
  private static final String NEW_LINE = System.getProperty("line.separator");
  private static final String DATE_FORMATS [] = {
       "EEE, dd MMM yyyy kk:mm:ss z",	  
       "EEE MMM dd kk:mm:ss yyyy z",  	
       "EEE, dd-MMM-':'y kk:mm:ss z", 	
       "EEE, dd-MMM-yyy kk:mm:ss z", 	  
       "EEE MMM dd kk:mm:ss yyyy",  	  
  };
  private ThreadLocal<DateFormatInfo> dateFormats = new ThreadLocal<DateFormatInfo>();
  private ThreadLocal<StringBufferReader> trecDocReader = new ThreadLocal<StringBufferReader>();
  private ThreadLocal<StringBuffer> trecDocBuffer = new ThreadLocal<StringBuffer>();
  private File dataDir = null;
  private ArrayList<File> inputFiles = new ArrayList<File>();
  private int nextFile = 0;
  private int rawDocSize;
  private Object lock = new Object();
  BufferedReader reader;
  int iteration = 0;
  HTMLParser htmlParser;
  private boolean excludeDocnameIteration;
  private DateFormatInfo getDateFormatInfo() {
    DateFormatInfo dfi = dateFormats.get();
    if (dfi == null) {
      dfi = new DateFormatInfo();
      dfi.dfs = new SimpleDateFormat[DATE_FORMATS.length];
      for (int i = 0; i < dfi.dfs.length; i++) {
        dfi.dfs[i] = new SimpleDateFormat(DATE_FORMATS[i], Locale.US);
        dfi.dfs[i].setLenient(true);
      }
      dfi.pos = new ParsePosition(0);
      dateFormats.set(dfi);
    }
    return dfi;
  }
  private StringBuffer getDocBuffer() {
    StringBuffer sb = trecDocBuffer.get();
    if (sb == null) {
      sb = new StringBuffer();
      trecDocBuffer.set(sb);
    }
    return sb;
  }
  private Reader getTrecDocReader(StringBuffer docBuffer) {
    StringBufferReader r = trecDocReader.get();
    if (r == null) {
      r = new StringBufferReader(docBuffer);
      trecDocReader.set(r);
    } else {
      r.set(docBuffer);
    }
    return r;
  }
  private void read(StringBuffer buf, String prefix, boolean collectMatchLine,
                    boolean collectAll, String terminatingTag)
      throws IOException, NoMoreDataException {
    String sep = "";
    while (true) {
      String line = reader.readLine();
      if (line == null) {
        openNextFile();
        continue;
      }
      rawDocSize += line.length();
      if (line.startsWith(prefix)) {
        if (collectMatchLine) {
          buf.append(sep).append(line);
          sep = NEW_LINE;
        }
        break;
      }
      if (terminatingTag != null && line.startsWith(terminatingTag)) {
        buf.setLength(0);
        break;
      }
      if (collectAll) {
        buf.append(sep).append(line);
        sep = NEW_LINE;
      }
    }
  }
  void openNextFile() throws NoMoreDataException, IOException {
    close();
    int retries = 0;
    while (true) {
      if (nextFile >= inputFiles.size()) { 
        if (!forever) {
          throw new NoMoreDataException();
        }
        nextFile = 0;
        iteration++;
      }
      File f = inputFiles.get(nextFile++);
      if (verbose) {
        System.out.println("opening: " + f + " length: " + f.length());
      }
      try {
        GZIPInputStream zis = new GZIPInputStream(new FileInputStream(f), BUFFER_SIZE);
        reader = new BufferedReader(new InputStreamReader(zis, encoding), BUFFER_SIZE);
        return;
      } catch (Exception e) {
        retries++;
        if (retries < 20 && verbose) {
          System.out.println("Skipping 'bad' file " + f.getAbsolutePath() + "  #retries=" + retries);
          continue;
        }
        throw new NoMoreDataException();
      }
    }
  }
  Date parseDate(String dateStr) {
    dateStr = dateStr.trim();
    DateFormatInfo dfi = getDateFormatInfo();
    for (int i = 0; i < dfi.dfs.length; i++) {
      DateFormat df = dfi.dfs[i];
      dfi.pos.setIndex(0);
      dfi.pos.setErrorIndex(-1);
      Date d = df.parse(dateStr, dfi.pos);
      if (d != null) {
        return d;
      }
    }
    if (verbose) {
      System.out.println("failed to parse date (assigning 'now') for: " + dateStr);
    }
    return null; 
  }
  @Override
  public void close() throws IOException {
    if (reader == null) {
      return;
    }
    try {
      reader.close();
    } catch (IOException e) {
      if (verbose) {
        System.out.println("failed to close reader !");
        e.printStackTrace(System.out);
      }
    }
    reader = null;
  }
  @Override
  public DocData getNextDocData(DocData docData) throws NoMoreDataException, IOException {
    String dateStr = null, name = null;
    Reader r = null;
    synchronized (lock) {
      if (reader == null) {
        openNextFile();
      }
      StringBuffer docBuf = getDocBuffer();
      docBuf.setLength(0);
      read(docBuf, DOC, false, false, null);
      docBuf.setLength(0);
      read(docBuf, DOCNO, true, false, null);
      name = docBuf.substring(DOCNO.length(), docBuf.indexOf(TERMINATING_DOCNO,
          DOCNO.length()));
      if (!excludeDocnameIteration)
        name = name + "_" + iteration;
      docBuf.setLength(0);
      read(docBuf, DOCHDR, false, false, null);
      boolean findTerminatingDocHdr = false;
      docBuf.setLength(0);
      read(docBuf, DATE, true, false, TERMINATING_DOCHDR);
      if (docBuf.length() != 0) {
        dateStr = docBuf.substring(DATE.length());
        findTerminatingDocHdr = true;
      }
      if (findTerminatingDocHdr) {
        docBuf.setLength(0);
        read(docBuf, TERMINATING_DOCHDR, false, false, null);
      }
      docBuf.setLength(0);
      read(docBuf, TERMINATING_DOC, false, true, null);
      r = getTrecDocReader(docBuf);
      r.reset();
      addBytes(docBuf.length()); 
    }
    Date date = dateStr != null ? parseDate(dateStr) : null;
    try {
      docData = htmlParser.parse(docData, name, date, r, null);
      addDoc();
    } catch (InterruptedException ie) {
      throw new ThreadInterruptedException(ie);
    }
    return docData;
  }
  @Override
  public void resetInputs() throws IOException {
    synchronized (lock) {
      super.resetInputs();
      close();
      nextFile = 0;
      iteration = 0;
    }
  }
  @Override
  public void setConfig(Config config) {
    super.setConfig(config);
    File workDir = new File(config.get("work.dir", "work"));
    String d = config.get("docs.dir", "trec");
    dataDir = new File(d);
    if (!dataDir.isAbsolute()) {
      dataDir = new File(workDir, d);
    }
    collectFiles(dataDir, inputFiles);
    if (inputFiles.size() == 0) {
      throw new IllegalArgumentException("No files in dataDir: " + dataDir);
    }
    try {
      String parserClassName = config.get("html.parser",
          "org.apache.lucene.benchmark.byTask.feeds.DemoHTMLParser");
      htmlParser = Class.forName(parserClassName).asSubclass(HTMLParser.class).newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    if (encoding == null) {
      encoding = "ISO-8859-1";
    }
    excludeDocnameIteration = config.get("content.source.excludeIteration", false);
  }
}
