package org.apache.lucene.benchmark.byTask.feeds;
import java.util.Date;
import java.util.Properties;
import org.apache.lucene.document.DateTools;
public class DocData {
  private String name;
  private String body;
  private String title;
  private String date;
  private Properties props;
  public void clear() {
    name = null;
    body = null;
    title = null;
    date = null;
    props = null;
  }
  public String getBody() {
    return body;
  }
  public String getDate() {
    return date;
  }
  public String getName() {
    return name;
  }
  public Properties getProps() {
    return props;
  }
  public String getTitle() {
    return title;
  }
  public void setBody(String body) {
    this.body = body;
  }
  public void setDate(Date date) {
    if (date != null) {
      setDate(DateTools.dateToString(date, DateTools.Resolution.SECOND));
    } else {
      this.date = null;
    }
  }
  public void setDate(String date) {
    this.date = date;
  }
  public void setName(String name) {
    this.name = name;
  }
  public void setProps(Properties props) {
    this.props = props;
  }
  public void setTitle(String title) {
    this.title = title;
  }
}
