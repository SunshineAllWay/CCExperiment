package org.apache.tools.mail;
import java.io.IOException;
import java.io.PrintStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.InetAddress;
import java.util.Vector;
import java.util.Enumeration;
public class MailMessage {
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 25;
    private String host;
    private int port = DEFAULT_PORT;
    private String from;
    private Vector replyto;
    private Vector to;
    private Vector cc;
    private Vector headersKeys;
    private Vector headersValues;
    private MailPrintStream out;
    private SmtpResponseReader in;
    private Socket socket;
    private static final int OK_READY = 220;
    private static final int OK_HELO = 250;
    private static final int OK_FROM = 250;
    private static final int OK_RCPT_1 = 250;
    private static final int OK_RCPT_2 = 251;
    private static final int OK_DATA = 354;
    private static final int OK_DOT = 250;
    private static final int OK_QUIT = 221;
  public MailMessage() throws IOException {
    this(DEFAULT_HOST, DEFAULT_PORT);
  }
  public MailMessage(String host) throws IOException {
    this(host, DEFAULT_PORT);
  }
  public MailMessage(String host, int port) throws IOException {
    this.port = port;
    this.host = host;
    replyto = new Vector();
    to = new Vector();
    cc = new Vector();
    headersKeys = new Vector();
    headersValues = new Vector();
    connect();
    sendHelo();
  }
    public void setPort(int port) {
        this.port = port;
    }
    public void from(String from) throws IOException {
        sendFrom(from);
        this.from = from;
    }
    public void replyto(String rto) {
      this.replyto.addElement(rto);
    }
  public void to(String to) throws IOException {
    sendRcpt(to);
    this.to.addElement(to);
  }
  public void cc(String cc) throws IOException {
    sendRcpt(cc);
    this.cc.addElement(cc);
  }
  public void bcc(String bcc) throws IOException {
    sendRcpt(bcc);
  }
  public void setSubject(String subj) {
    setHeader("Subject", subj);
  }
  public void setHeader(String name, String value) {
    headersKeys.add(name);
    headersValues.add(value);
  }
  public PrintStream getPrintStream() throws IOException {
    setFromHeader();
    setReplyToHeader();
    setToHeader();
    setCcHeader();
    setHeader("X-Mailer", "org.apache.tools.mail.MailMessage (ant.apache.org)");
    sendData();
    flushHeaders();
    return out;
  }
  void setFromHeader() {
    setHeader("From", from);
  }
  void setReplyToHeader() {
    if (!replyto.isEmpty()) {
      setHeader("Reply-To", vectorToList(replyto));
    }
  }
  void setToHeader() {
    if (!to.isEmpty()) {
      setHeader("To", vectorToList(to));
    }
  }
  void setCcHeader() {
    if (!cc.isEmpty()) {
      setHeader("Cc", vectorToList(cc));
    }
  }
  String vectorToList(Vector v) {
    StringBuffer buf = new StringBuffer();
    Enumeration e = v.elements();
    while (e.hasMoreElements()) {
      buf.append(e.nextElement());
      if (e.hasMoreElements()) {
        buf.append(", ");
      }
    }
    return buf.toString();
  }
  void flushHeaders() throws IOException {
   for (int i = 0; i < headersKeys.size(); i++) {
      String name = (String) headersKeys.elementAt(i);
      String value = (String) headersValues.elementAt(i);
      out.println(name + ": " + value);
    }
    out.println();
    out.flush();
  }
  public void sendAndClose() throws IOException {
      try {
          sendDot();
          sendQuit();
      } finally {
          disconnect();
      }
  }
  static String sanitizeAddress(String s) {
    int paramDepth = 0;
    int start = 0;
    int end = 0;
    int len = s.length();
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      if (c == '(') {
        paramDepth++;
        if (start == 0) {
          end = i;  
        }
      } else if (c == ')') {
        paramDepth--;
        if (end == 0) {
          start = i + 1;  
        }
      } else if (paramDepth == 0 && c == '<') {
        start = i + 1;
      } else if (paramDepth == 0 && c == '>') {
        end = i;
      }
    }
    if (end == 0) {
      end = len;
    }
    return s.substring(start, end);
  }
  void connect() throws IOException {
    socket = new Socket(host, port);
    out = new MailPrintStream(
          new BufferedOutputStream(
          socket.getOutputStream()));
    in = new SmtpResponseReader(socket.getInputStream());
    getReady();
  }
  void getReady() throws IOException {
    String response = in.getResponse();
    int[] ok = {OK_READY};
    if (!isResponseOK(response, ok)) {
      throw new IOException(
        "Didn't get introduction from server: " + response);
    }
  }
  void sendHelo() throws IOException {
    String local = InetAddress.getLocalHost().getHostName();
    int[] ok = {OK_HELO};
    send("HELO " + local, ok);
  }
  void sendFrom(String from) throws IOException {
    int[] ok = {OK_FROM};
    send("MAIL FROM: " + "<" + sanitizeAddress(from) + ">", ok);
  }
  void sendRcpt(String rcpt) throws IOException {
    int[] ok = {OK_RCPT_1, OK_RCPT_2};
    send("RCPT TO: " + "<" + sanitizeAddress(rcpt) + ">", ok);
  }
  void sendData() throws IOException {
    int[] ok = {OK_DATA};
    send("DATA", ok);
  }
  void sendDot() throws IOException {
    int[] ok = {OK_DOT};
    send("\r\n.", ok);  
  }
    void sendQuit() throws IOException {
        int[] ok = {OK_QUIT};
        try {
            send("QUIT", ok);
        } catch (IOException e) {
            throw new ErrorInQuitException(e);
        }
    }
    void send(String msg, int[] ok) throws IOException {
        out.rawPrint(msg + "\r\n");  
        String response = in.getResponse();
        if (!isResponseOK(response, ok)) {
            throw new IOException("Unexpected reply to command: "
                                  + msg + ": " + response);
        }
    }
  boolean isResponseOK(String response, int[] ok) {
    for (int i = 0; i < ok.length; i++) {
      if (response.startsWith("" + ok[i])) {
        return true;
      }
    }
    return false;
  }
    void disconnect() throws IOException {
        if (out != null) {
            out.close();
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }
}
class MailPrintStream extends PrintStream {
  private int lastChar;
  public MailPrintStream(OutputStream out) {
    super(out, true);  
  }
  public void write(int b) {
    if (b == '\n' && lastChar != '\r') {
      rawWrite('\r');  
      rawWrite(b);
    } else if (b == '.' && lastChar == '\n') {
      rawWrite('.');  
      rawWrite(b);
    } else {
      rawWrite(b);
    }
    lastChar = b;
  }
  public void write(byte[] buf, int off, int len) {
    for (int i = 0; i < len; i++) {
      write(buf[off + i]);
    }
  }
  void rawWrite(int b) {
    super.write(b);
  }
  void rawPrint(String s) {
    int len = s.length();
    for (int i = 0; i < len; i++) {
      rawWrite(s.charAt(i));
    }
  }
}
