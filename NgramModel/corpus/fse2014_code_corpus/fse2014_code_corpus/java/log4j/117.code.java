package org.apache.log4j.helpers;
import java.util.Date;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.DateFormat;
public class RelativeTimeDateFormat extends DateFormat {
  private static final long serialVersionUID = 7055751607085611984L;
  protected final long startTime;
  public
  RelativeTimeDateFormat() {
    this.startTime = System.currentTimeMillis();
  }
  public
  StringBuffer format(Date date, StringBuffer sbuf,
		      FieldPosition fieldPosition) {
    return sbuf.append((date.getTime() - startTime));
  }
  public
  Date parse(java.lang.String s, ParsePosition pos) {
    return null;
  }  
}
