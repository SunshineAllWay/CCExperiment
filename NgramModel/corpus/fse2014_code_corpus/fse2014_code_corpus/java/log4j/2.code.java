package org.apache.log4j;
import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.helpers.QuietWriter;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ErrorCode;
public class DailyFileAppender extends FileAppender {
  static final public String FILE_NAME_PATTERN_OPTION = "FilePattern";
  private String fileNamePattern = null;
  private String currentFileName = null;
  private long nextFilenameComputingMillis = System.currentTimeMillis () - 1;
  public
  DailyFileAppender() {
  }
  public DailyFileAppender (Layout layout,String filename,boolean append) throws IOException {
    super(layout, filename, append);
  }
  public DailyFileAppender (Layout layout,String filename) throws IOException {
    super(layout, filename);
  }
  public
  synchronized
  void setFile(String fileName, boolean append) throws IOException {
    if (fileNamePattern == null) {
      errorHandler.error("Missing file pattern (" + FILE_NAME_PATTERN_OPTION + ") in setFile().");
      return;
    }
    Date now = new Date();
    fileName = new SimpleDateFormat(fileNamePattern).format (now);
    if (fileName.equals(currentFileName))
      return;
    DailyFileAppenderCalendar c = new DailyFileAppenderCalendar();
    c.rollToNextDay ();
    nextFilenameComputingMillis = c.getTimeInMillis ();
    currentFileName = fileName;
    super.setFile(fileName, append);
  }
  protected
  void subAppend(LoggingEvent event) {
     if (System.currentTimeMillis () >= nextFilenameComputingMillis) {
      try {
        setFile (super.fileName, super.fileAppend);
      }
      catch(IOException e) {
        System.err.println("setFile(null, false) call failed.");
        e.printStackTrace();
      }
    }
    super.subAppend(event);
  } 
  public
  String[] getOptionStrings() {
    return OptionConverter.concatanateArrays(super.getOptionStrings(),
		 new String[] {FILE_NAME_PATTERN_OPTION});
  }
  public
  void setOption(String key, String value) {
    super.setOption(key, value);    
    if(key.equalsIgnoreCase(FILE_NAME_PATTERN_OPTION)) {
      fileNamePattern = value;
    }
  }
  public
  void activateOptions() {
    try {
	   setFile(null, super.fileAppend);
    }
    catch(java.io.IOException e) {
	   errorHandler.error("setFile(null,"+fileAppend+") call failed.",
		  	   e, ErrorCode.FILE_OPEN_FAILURE);
    }
  }
}
class DailyFileAppenderCalendar extends java.util.GregorianCalendar
{
  public long getTimeInMillis() {
    return super.getTimeInMillis();
  }
  public void rollToNextDay () {
    this.add(java.util.Calendar.DATE, 0);
    this.add(java.util.Calendar.HOUR, 0);
    this.set(java.util.Calendar.MINUTE, 0);
    this.set(java.util.Calendar.SECOND, 0);
    this.set(java.util.Calendar.MILLISECOND, 0);
  }
}