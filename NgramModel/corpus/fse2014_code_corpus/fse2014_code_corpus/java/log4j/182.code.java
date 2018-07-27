package org.apache.log4j.net;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.SyslogQuietWriter;
import org.apache.log4j.helpers.SyslogWriter;
import org.apache.log4j.spi.LoggingEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;
public class SyslogAppender extends AppenderSkeleton {
  final static public int LOG_KERN     = 0;
  final static public int LOG_USER     = 1<<3;
  final static public int LOG_MAIL     = 2<<3;
  final static public int LOG_DAEMON   = 3<<3;
  final static public int LOG_AUTH     = 4<<3;
  final static public int LOG_SYSLOG   = 5<<3;
  final static public int LOG_LPR      = 6<<3;
  final static public int LOG_NEWS     = 7<<3;
  final static public int LOG_UUCP     = 8<<3;
  final static public int LOG_CRON     = 9<<3;
  final static public int LOG_AUTHPRIV = 10<<3;
  final static public int LOG_FTP      = 11<<3;
  final static public int LOG_LOCAL0 = 16<<3;
  final static public int LOG_LOCAL1 = 17<<3;
  final static public int LOG_LOCAL2 = 18<<3;
  final static public int LOG_LOCAL3 = 19<<3;
  final static public int LOG_LOCAL4 = 20<<3;
  final static public int LOG_LOCAL5 = 21<<3;
  final static public int LOG_LOCAL6 = 22<<3;
  final static public int LOG_LOCAL7 = 23<<3;
  protected static final int SYSLOG_HOST_OI = 0;
  protected static final int FACILITY_OI = 1;
  static final String TAB = "    ";
  int syslogFacility = LOG_USER;
  String facilityStr;
  boolean facilityPrinting = false;
  SyslogQuietWriter sqw;
  String syslogHost;
  private boolean header = false;
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss ", Locale.ENGLISH);
  private String localHostname;
  private boolean layoutHeaderChecked = false;
  public
  SyslogAppender() {
    this.initSyslogFacilityStr();
  }
  public
  SyslogAppender(Layout layout, int syslogFacility) {
    this.layout = layout;
    this.syslogFacility = syslogFacility;
    this.initSyslogFacilityStr();
  }
  public
  SyslogAppender(Layout layout, String syslogHost, int syslogFacility) {
    this(layout, syslogFacility);
    setSyslogHost(syslogHost);
  }
  synchronized
  public
  void close() {
    closed = true;
    if (sqw != null) {
        try {
            if (layoutHeaderChecked && layout != null && layout.getFooter() != null) {
                sendLayoutMessage(layout.getFooter());
            }
            sqw.close();
            sqw = null;
        } catch(java.io.InterruptedIOException e) {
            Thread.currentThread().interrupt();
            sqw = null;
        } catch(IOException e) {
            sqw = null;
        }
    }
  }
  private
  void initSyslogFacilityStr() {
    facilityStr = getFacilityString(this.syslogFacility);
    if (facilityStr == null) {
      System.err.println("\"" + syslogFacility +
                  "\" is an unknown syslog facility. Defaulting to \"USER\".");
      this.syslogFacility = LOG_USER;
      facilityStr = "user:";
    } else {
      facilityStr += ":";
    }
  }
  public
  static
  String getFacilityString(int syslogFacility) {
    switch(syslogFacility) {
    case LOG_KERN:      return "kern";
    case LOG_USER:      return "user";
    case LOG_MAIL:      return "mail";
    case LOG_DAEMON:    return "daemon";
    case LOG_AUTH:      return "auth";
    case LOG_SYSLOG:    return "syslog";
    case LOG_LPR:       return "lpr";
    case LOG_NEWS:      return "news";
    case LOG_UUCP:      return "uucp";
    case LOG_CRON:      return "cron";
    case LOG_AUTHPRIV:  return "authpriv";
    case LOG_FTP:       return "ftp";
    case LOG_LOCAL0:    return "local0";
    case LOG_LOCAL1:    return "local1";
    case LOG_LOCAL2:    return "local2";
    case LOG_LOCAL3:    return "local3";
    case LOG_LOCAL4:    return "local4";
    case LOG_LOCAL5:    return "local5";
    case LOG_LOCAL6:    return "local6";
    case LOG_LOCAL7:    return "local7";
    default:            return null;
    }
  }
  public
  static
  int getFacility(String facilityName) {
    if(facilityName != null) {
      facilityName = facilityName.trim();
    }
    if("KERN".equalsIgnoreCase(facilityName)) {
      return LOG_KERN;
    } else if("USER".equalsIgnoreCase(facilityName)) {
      return LOG_USER;
    } else if("MAIL".equalsIgnoreCase(facilityName)) {
      return LOG_MAIL;
    } else if("DAEMON".equalsIgnoreCase(facilityName)) {
      return LOG_DAEMON;
    } else if("AUTH".equalsIgnoreCase(facilityName)) {
      return LOG_AUTH;
    } else if("SYSLOG".equalsIgnoreCase(facilityName)) {
      return LOG_SYSLOG;
    } else if("LPR".equalsIgnoreCase(facilityName)) {
      return LOG_LPR;
    } else if("NEWS".equalsIgnoreCase(facilityName)) {
      return LOG_NEWS;
    } else if("UUCP".equalsIgnoreCase(facilityName)) {
      return LOG_UUCP;
    } else if("CRON".equalsIgnoreCase(facilityName)) {
      return LOG_CRON;
    } else if("AUTHPRIV".equalsIgnoreCase(facilityName)) {
      return LOG_AUTHPRIV;
    } else if("FTP".equalsIgnoreCase(facilityName)) {
      return LOG_FTP;
    } else if("LOCAL0".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL0;
    } else if("LOCAL1".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL1;
    } else if("LOCAL2".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL2;
    } else if("LOCAL3".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL3;
    } else if("LOCAL4".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL4;
    } else if("LOCAL5".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL5;
    } else if("LOCAL6".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL6;
    } else if("LOCAL7".equalsIgnoreCase(facilityName)) {
      return LOG_LOCAL7;
    } else {
      return -1;
    }
  }
  private void splitPacket(final String header, final String packet) {
      int byteCount = packet.getBytes().length;
      if (byteCount <= 1019) {
          sqw.write(packet);
      } else {
          int split = header.length() + (packet.length() - header.length())/2;
          splitPacket(header, packet.substring(0, split) + "...");
          splitPacket(header, header + "..." + packet.substring(split));
      }      
  }
  public
  void append(LoggingEvent event) {
    if(!isAsSevereAsThreshold(event.getLevel()))
      return;
    if(sqw == null) {
      errorHandler.error("No syslog host is set for SyslogAppedender named \""+
			this.name+"\".");
      return;
    }
    if (!layoutHeaderChecked) {
        if (layout != null && layout.getHeader() != null) {
            sendLayoutMessage(layout.getHeader());
        }
        layoutHeaderChecked = true;
    }
    String hdr = getPacketHeader(event.timeStamp);
    String packet;
    if (layout == null) {
        packet = String.valueOf(event.getMessage());
    } else {
        packet = layout.format(event);
    }
    if(facilityPrinting || hdr.length() > 0) {
        StringBuffer buf = new StringBuffer(hdr);
        if(facilityPrinting) {
            buf.append(facilityStr);
        }
        buf.append(packet);
        packet = buf.toString();
    }
    sqw.setLevel(event.getLevel().getSyslogEquivalent());
    if (packet.length() > 256) {
        splitPacket(hdr, packet);
    } else {
        sqw.write(packet);
    }
    if (layout == null || layout.ignoresThrowable()) {
      String[] s = event.getThrowableStrRep();
      if (s != null) {
        for(int i = 0; i < s.length; i++) {
            if (s[i].startsWith("\t")) {
               sqw.write(hdr+TAB+s[i].substring(1));
            } else {
               sqw.write(hdr+s[i]);
            }
        }
      }
    }
  }
  public
  void activateOptions() {
      if (header) {
        getLocalHostname();
      }
      if (layout != null && layout.getHeader() != null) {
          sendLayoutMessage(layout.getHeader());
      }
      layoutHeaderChecked = true;
  }
  public
  boolean requiresLayout() {
    return true;
  }
  public
  void setSyslogHost(final String syslogHost) {
    this.sqw = new SyslogQuietWriter(new SyslogWriter(syslogHost),
				     syslogFacility, errorHandler);
    this.syslogHost = syslogHost;
  }
  public
  String getSyslogHost() {
    return syslogHost;
  }
  public
  void setFacility(String facilityName) {
    if(facilityName == null)
      return;
    syslogFacility = getFacility(facilityName);
    if (syslogFacility == -1) {
      System.err.println("["+facilityName +
                  "] is an unknown syslog facility. Defaulting to [USER].");
      syslogFacility = LOG_USER;
    }
    this.initSyslogFacilityStr();
    if(sqw != null) {
      sqw.setSyslogFacility(this.syslogFacility);
    }
  }
  public
  String getFacility() {
    return getFacilityString(syslogFacility);
  }
  public
  void setFacilityPrinting(boolean on) {
    facilityPrinting = on;
  }
  public
  boolean getFacilityPrinting() {
    return facilityPrinting;
  }
  public final boolean getHeader() {
      return header;
  }
  public final void setHeader(final boolean val) {
      header = val;
  }
  private String getLocalHostname() {
      if (localHostname == null) {
          try {
            InetAddress addr = InetAddress.getLocalHost();
            localHostname = addr.getHostName();
          } catch (UnknownHostException uhe) {
            localHostname = "UNKNOWN_HOST";
          }
      }
      return localHostname;
  }
  private String getPacketHeader(final long timeStamp) {
      if (header) {
        StringBuffer buf = new StringBuffer(dateFormat.format(new Date(timeStamp)));
        if (buf.charAt(4) == '0') {
          buf.setCharAt(4, ' ');
        }
        buf.append(getLocalHostname());
        buf.append(' ');
        return buf.toString();
      }
      return "";
  }
  private void sendLayoutMessage(final String msg) {
      if (sqw != null) {
          String packet = msg;
          String hdr = getPacketHeader(new Date().getTime());
          if(facilityPrinting || hdr.length() > 0) {
              StringBuffer buf = new StringBuffer(hdr);
              if(facilityPrinting) {
                  buf.append(facilityStr);
              }
              buf.append(msg);
              packet = buf.toString();
          }
          sqw.setLevel(6);
          sqw.write(packet);
      }
  }
}
