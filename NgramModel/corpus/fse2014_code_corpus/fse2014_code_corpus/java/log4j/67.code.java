package org.apache.log4j;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.helpers.Transform;
public class HTMLLayout extends Layout {
  protected final int BUF_SIZE = 256;
  protected final int MAX_CAPACITY = 1024;
  static String TRACE_PREFIX = "<br>&nbsp;&nbsp;&nbsp;&nbsp;";
  private StringBuffer sbuf = new StringBuffer(BUF_SIZE);
  public static final String LOCATION_INFO_OPTION = "LocationInfo";
  public static final String TITLE_OPTION = "Title";
  boolean locationInfo = false;
  String title = "Log4J Log Messages";
  public
  void setLocationInfo(boolean flag) {
    locationInfo = flag;
  }
  public
  boolean getLocationInfo() {
    return locationInfo;
  }
  public
  void setTitle(String title) {
    this.title = title;
  }
  public
  String getTitle() {
    return title;
  }
  public
  String getContentType() {
    return "text/html";
  }
  public
  void activateOptions() {
  }
  public
  String format(LoggingEvent event) {
    if(sbuf.capacity() > MAX_CAPACITY) {
      sbuf = new StringBuffer(BUF_SIZE);
    } else {
      sbuf.setLength(0);
    }
    sbuf.append(Layout.LINE_SEP + "<tr>" + Layout.LINE_SEP);
    sbuf.append("<td>");
    sbuf.append(event.timeStamp - LoggingEvent.getStartTime());
    sbuf.append("</td>" + Layout.LINE_SEP);
    String escapedThread = Transform.escapeTags(event.getThreadName());
    sbuf.append("<td title=\"" + escapedThread + " thread\">");
    sbuf.append(escapedThread);
    sbuf.append("</td>" + Layout.LINE_SEP);
    sbuf.append("<td title=\"Level\">");
    if (event.getLevel().equals(Level.DEBUG)) {
      sbuf.append("<font color=\"#339933\">");
      sbuf.append(Transform.escapeTags(String.valueOf(event.getLevel())));
      sbuf.append("</font>");
    }
    else if(event.getLevel().isGreaterOrEqual(Level.WARN)) {
      sbuf.append("<font color=\"#993300\"><strong>");
      sbuf.append(Transform.escapeTags(String.valueOf(event.getLevel())));
      sbuf.append("</strong></font>");
    } else {
      sbuf.append(Transform.escapeTags(String.valueOf(event.getLevel())));
    }
    sbuf.append("</td>" + Layout.LINE_SEP);
    String escapedLogger = Transform.escapeTags(event.getLoggerName());
    sbuf.append("<td title=\"" + escapedLogger + " category\">");
    sbuf.append(escapedLogger);
    sbuf.append("</td>" + Layout.LINE_SEP);
    if(locationInfo) {
      LocationInfo locInfo = event.getLocationInformation();
      sbuf.append("<td>");
      sbuf.append(Transform.escapeTags(locInfo.getFileName()));
      sbuf.append(':');
      sbuf.append(locInfo.getLineNumber());
      sbuf.append("</td>" + Layout.LINE_SEP);
    }
    sbuf.append("<td title=\"Message\">");
    sbuf.append(Transform.escapeTags(event.getRenderedMessage()));
    sbuf.append("</td>" + Layout.LINE_SEP);
    sbuf.append("</tr>" + Layout.LINE_SEP);
    if (event.getNDC() != null) {
      sbuf.append("<tr><td bgcolor=\"#EEEEEE\" style=\"font-size : xx-small;\" colspan=\"6\" title=\"Nested Diagnostic Context\">");
      sbuf.append("NDC: " + Transform.escapeTags(event.getNDC()));
      sbuf.append("</td></tr>" + Layout.LINE_SEP);
    }
    String[] s = event.getThrowableStrRep();
    if(s != null) {
      sbuf.append("<tr><td bgcolor=\"#993300\" style=\"color:White; font-size : xx-small;\" colspan=\"6\">");
      appendThrowableAsHTML(s, sbuf);
      sbuf.append("</td></tr>" + Layout.LINE_SEP);
    }
    return sbuf.toString();
  }
  void appendThrowableAsHTML(String[] s, StringBuffer sbuf) {
    if(s != null) {
      int len = s.length;
      if(len == 0)
	return;
      sbuf.append(Transform.escapeTags(s[0]));
      sbuf.append(Layout.LINE_SEP);
      for(int i = 1; i < len; i++) {
	sbuf.append(TRACE_PREFIX);
	sbuf.append(Transform.escapeTags(s[i]));
	sbuf.append(Layout.LINE_SEP);
      }
    }
  }
  public
  String getHeader() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"  + Layout.LINE_SEP);
    sbuf.append("<html>" + Layout.LINE_SEP);
    sbuf.append("<head>" + Layout.LINE_SEP);
    sbuf.append("<title>" + title + "</title>" + Layout.LINE_SEP);
    sbuf.append("<style type=\"text/css\">"  + Layout.LINE_SEP);
    sbuf.append("<!--"  + Layout.LINE_SEP);
    sbuf.append("body, table {font-family: arial,sans-serif; font-size: x-small;}" + Layout.LINE_SEP);
    sbuf.append("th {background: #336699; color: #FFFFFF; text-align: left;}" + Layout.LINE_SEP);
    sbuf.append("-->" + Layout.LINE_SEP);
    sbuf.append("</style>" + Layout.LINE_SEP);
    sbuf.append("</head>" + Layout.LINE_SEP);
    sbuf.append("<body bgcolor=\"#FFFFFF\" topmargin=\"6\" leftmargin=\"6\">" + Layout.LINE_SEP);
    sbuf.append("<hr size=\"1\" noshade>" + Layout.LINE_SEP);
    sbuf.append("Log session start time " + new java.util.Date() + "<br>" + Layout.LINE_SEP);
    sbuf.append("<br>" + Layout.LINE_SEP);
    sbuf.append("<table cellspacing=\"0\" cellpadding=\"4\" border=\"1\" bordercolor=\"#224466\" width=\"100%\">" + Layout.LINE_SEP);
    sbuf.append("<tr>" + Layout.LINE_SEP);
    sbuf.append("<th>Time</th>" + Layout.LINE_SEP);
    sbuf.append("<th>Thread</th>" + Layout.LINE_SEP);
    sbuf.append("<th>Level</th>" + Layout.LINE_SEP);
    sbuf.append("<th>Category</th>" + Layout.LINE_SEP);
    if(locationInfo) {
      sbuf.append("<th>File:Line</th>" + Layout.LINE_SEP);
    }
    sbuf.append("<th>Message</th>" + Layout.LINE_SEP);
    sbuf.append("</tr>" + Layout.LINE_SEP);
    return sbuf.toString();
  }
  public
  String getFooter() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append("</table>" + Layout.LINE_SEP);
    sbuf.append("<br>" + Layout.LINE_SEP);
    sbuf.append("</body></html>");
    return sbuf.toString();
  }
  public
  boolean ignoresThrowable() {
    return false;
  }
}
