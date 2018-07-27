package org.apache.lucene.demo;
import java.io.*;
import org.apache.lucene.document.*;
import org.apache.lucene.demo.html.HTMLParser;
public class HTMLDocument {
  static char dirSep = System.getProperty("file.separator").charAt(0);
  public static String uid(File f) {
    return f.getPath().replace(dirSep, '\u0000') +
      "\u0000" +
      DateTools.timeToString(f.lastModified(), DateTools.Resolution.SECOND);
  }
  public static String uid2url(String uid) {
    String url = uid.replace('\u0000', '/');	  
    return url.substring(0, url.lastIndexOf('/')); 
  }
  public static Document Document(File f)
       throws IOException, InterruptedException  {
    Document doc = new Document();
    doc.add(new Field("path", f.getPath().replace(dirSep, '/'), Field.Store.YES,
        Field.Index.NOT_ANALYZED));
    doc.add(new Field("modified",
        DateTools.timeToString(f.lastModified(), DateTools.Resolution.MINUTE),
        Field.Store.YES, Field.Index.NOT_ANALYZED));
    doc.add(new Field("uid", uid(f), Field.Store.NO, Field.Index.NOT_ANALYZED));
    FileInputStream fis = new FileInputStream(f);
    HTMLParser parser = new HTMLParser(fis);
    doc.add(new Field("contents", parser.getReader()));
    doc.add(new Field("summary", parser.getSummary(), Field.Store.YES, Field.Index.NO));
    doc.add(new Field("title", parser.getTitle(), Field.Store.YES, Field.Index.ANALYZED));
    return doc;
  }
  private HTMLDocument() {}
}
