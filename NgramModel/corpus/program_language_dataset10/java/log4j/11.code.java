package org.apache.log4j.net;
import java.io.Writer;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.io.IOException;
import org.apache.log4j.helpers.LogLog;
public class DatagramStringWriter extends Writer {
  static final int SYSLOG_PORT = 514;
  private int port;
  private String host;
  private String encoding;
  private String prefix;
  private InetAddress address;
  private DatagramSocket ds;
  public
  DatagramStringWriter(String host) {
    this(host, SYSLOG_PORT, null, null);
  }
  public
  DatagramStringWriter(String host, int port) {
    this(host, port, null, null);
  }
  public
  DatagramStringWriter(String host, int port, String encoding) {
    this(host, port, null, null);
  }
  public
  DatagramStringWriter(String host, int port, String encoding, String prefix) {
    this.host = host;
    this.port = port;
    this.encoding = encoding;
    this.prefix = prefix;
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
  void write(String string) throws IOException {
    if (prefix != null) {
      string = prefix + string;
    }
    byte[] rawData;
    if (this.encoding == null)
    {
      rawData = string.getBytes();
    }
    else
    {
      rawData = string.getBytes(encoding);
    }
    DatagramPacket packet =
      new DatagramPacket(
                 rawData,
					       rawData.length,
					       address,
                 port);
    if(this.ds != null)
    {
      ds.send(packet);
    }
    else
    {
      LogLog.error(
        "write: failed to create DatagramPacket");
    }
  }
  public
  void flush() {}
  public
  void close() {}
  public
  void setPrefix(String prefix){
    this.prefix = prefix;
  }
}
