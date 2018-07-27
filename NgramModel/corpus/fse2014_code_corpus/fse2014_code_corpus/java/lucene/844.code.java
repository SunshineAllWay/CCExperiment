package org.apache.lucene.benchmark.byTask.feeds;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;
public class DemoHTMLParser implements org.apache.lucene.benchmark.byTask.feeds.HTMLParser {
  public DocData parse(DocData docData, String name, Date date, Reader reader, DateFormat dateFormat) throws IOException, InterruptedException {
    org.apache.lucene.demo.html.HTMLParser p = new org.apache.lucene.demo.html.HTMLParser(reader);
    String title = p.getTitle();
    Properties props = p.getMetaTags(); 
    Reader r = p.getReader();
    char c[] = new char[1024];
    StringBuffer bodyBuf = new StringBuffer();
    int n;
    while ((n = r.read(c)) >= 0) {
      if (n>0) {
        bodyBuf.append(c,0,n);
      }
    }
    r.close();
    if (date == null && props.getProperty("date")!=null) {
      try {
        date = dateFormat.parse(props.getProperty("date").trim());
      } catch (ParseException e) {
        System.out.println("ignoring date parse exception (assigning 'now') for: "+props.getProperty("date"));
        date = new Date(); 
      }
    }
    docData.clear();
    docData.setName(name);
    docData.setBody(bodyBuf.toString());
    docData.setTitle(title);
    docData.setProps(props);
    docData.setDate(date);
    return docData;
  }
  public DocData parse(DocData docData, String name, Date date, StringBuffer inputText, DateFormat dateFormat) throws IOException, InterruptedException {
    return parse(docData, name, date, new StringReader(inputText.toString()), dateFormat);
  }
}
