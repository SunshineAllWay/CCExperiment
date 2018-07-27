package org.apache.lucene.benchmark.byTask.tasks;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.feeds.DocMaker;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
public class WriteLineDocTask extends PerfTask {
  public final static char SEP = '\t';
  private static final Matcher NORMALIZER = Pattern.compile("[\t\r\n]+").matcher("");
  private int docSize = 0;
  private BufferedWriter lineFileOut = null;
  private DocMaker docMaker;
  public WriteLineDocTask(PerfRunData runData) throws Exception {
    super(runData);
    Config config = runData.getConfig();
    String fileName = config.get("line.file.out", null);
    if (fileName == null) {
      throw new IllegalArgumentException("line.file.out must be set");
    }
    OutputStream out = new FileOutputStream(fileName);
    boolean doBzipCompression = false;
    String doBZCompress = config.get("bzip.compression", null);
    if (doBZCompress != null) {
      doBzipCompression = Boolean.valueOf(doBZCompress).booleanValue();
    } else {
      doBzipCompression = fileName.endsWith("bz2");
    }
    if (doBzipCompression) {
      out = new BufferedOutputStream(out, 1 << 16);
      out = new CompressorStreamFactory().createCompressorOutputStream("bzip2", out);
    }
    lineFileOut = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"), 1 << 16);
    docMaker = runData.getDocMaker();
  }
  @Override
  protected String getLogMessage(int recsCount) {
    return "Wrote " + recsCount + " line docs";
  }
  @Override
  public int doLogic() throws Exception {
    Document doc = docSize > 0 ? docMaker.makeDocument(docSize) : docMaker.makeDocument();
    Field f = doc.getField(DocMaker.BODY_FIELD);
    String body = f != null ? NORMALIZER.reset(f.stringValue()).replaceAll(" ") : "";
    f = doc.getField(DocMaker.TITLE_FIELD);
    String title = f != null ? NORMALIZER.reset(f.stringValue()).replaceAll(" ") : "";
    if (body.length() > 0 || title.length() > 0) {
      f = doc.getField(DocMaker.DATE_FIELD);
      String date = f != null ? NORMALIZER.reset(f.stringValue()).replaceAll(" ") : "";
      lineFileOut.write(title, 0, title.length());
      lineFileOut.write(SEP);
      lineFileOut.write(date, 0, date.length());
      lineFileOut.write(SEP);
      lineFileOut.write(body, 0, body.length());
      lineFileOut.newLine();
    }
    return 1;
  }
  @Override
  public void close() throws Exception {
    lineFileOut.close();
    super.close();
  }
  @Override
  public void setParams(String params) {
    if (super.supportsParams()) {
      super.setParams(params);
    }
    docSize = (int) Float.parseFloat(params); 
  }
  @Override
  public boolean supportsParams() {
    return true;
  }
}
