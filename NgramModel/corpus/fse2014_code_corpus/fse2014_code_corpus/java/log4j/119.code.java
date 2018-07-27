package org.apache.log4j.helpers;
import java.io.Writer;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
public class SyslogWriter extends Writer {
  final int SYSLOG_PORT = 514;
  static String syslogHost;
  private InetAddress address;
  private final int port;
  private DatagramSocket ds;
  public
  SyslogWriter(final String syslogHost) {
    SyslogWriter.syslogHost = syslogHost;
    if (syslogHost == null) {
        throw new NullPointerException("syslogHost");
    }
    String host = syslogHost;
    int urlPort = -1;
    if (host.indexOf("[") != -1 || host.indexOf(':') == host.lastIndexOf(':')) {
        try {
            URL url = new URL("http://" + host);
            if (url.getHost() != null) {
                host = url.getHost();
                if(host.startsWith("[") && host.charAt(host.length() - 1) == ']') {
                    host = host.substring(1, host.length() - 1);
                }
                urlPort = url.getPort();
            }
        } catch(MalformedURLException e) {
      		LogLog.error("Malformed URL: will attempt to interpret as InetAddress.", e);
        }
    }
    if (urlPort == -1) {
        urlPort = SYSLOG_PORT;
    }
    port = urlPort;
    try {      
      this.address = InetAddress.getByName(host);
    }
    catch (UnknownHostException e) {
      LogLog.error("Could not find " + host +
			 ". All logging will FAIL.", e);
    }
    try {
      this.ds = new DatagramSocket();
    }
    catch (SocketException e) {
      e.printStackTrace();
      LogLog.error("Could not instantiate DatagramSocket to " + host +
			 ". All logging will FAIL.", e);
    }
  }
  public
  void write(char[] buf, int off, int len) throws IOException {
    this.write(new String(buf, off, len));
  }
  public
  void write(final String string) throws IOException {
    if(this.ds != null && this.address != null) {
        byte[] bytes = string.getBytes();
        int bytesLength = bytes.length;
        if (bytesLength >= 1024) {
            bytesLength = 1024;
        }
        DatagramPacket packet = new DatagramPacket(bytes, bytesLength,
                               address, port);
        ds.send(packet);
    }
  }
  public
  void flush() {}
  public void close() {
      if (ds != null) {
          ds.close();
      }
  }
}
