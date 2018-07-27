package com.psibt.framework.net;
import java.net.*;
import java.io.*;
import java.util.*;
import org.apache.log4j.*;
public class PluggableHTTPServer implements Runnable {
  public static final int DEFAULT_PORT = 80;
  static Category cat = Category.getInstance("PluggableHTTPServer");
  private int port;
  private Vector handler;
  private ServerSocket server;
  public PluggableHTTPServer(int port) throws IOException {
    this.port = port;
    this.handler = new Vector();
    cat.setPriority(Priority.ERROR);
    server = new ServerSocket(this.port);
  }
  public PluggableHTTPServer() throws IOException {
    this(DEFAULT_PORT);
  }
  public void addRequestHandler(HTTPRequestHandler h) {
    handler.add(h);
  }
  public void removeRequestHandler(HTTPRequestHandler h) {
    handler.remove(h);
  }
  public static void replyNotFound(Writer out) {
    try {
      out.write("HTTP/1.0 404 Not Found\r\n");
      out.write("<HTML><HEAD><TITLE>Not Found</TITLE></HEAD>\r\n");
      out.write("<BODY><H1>Not Found</H1>\r\n");
      out.write("</BODY></HTML>\r\n");
      out.flush();
    }  
    catch (IOException e) {
    }
  }
  public static void replyMethodNotAllowed(Writer out) {
    try {
      out.write("HTTP/1.1 405 Method Not Allowed\r\n");
      out.write("Allow: GET, PUT\r\n");
      out.write("<HTML><HEAD><TITLE>Method Not Allowed</TITLE></HEAD>\r\n");
      out.write("<BODY><H1>Method Not Allowed</H1>\r\n");
      out.write("</BODY></HTML>\r\n");
      out.flush();
    }  
    catch (IOException e) {
    }
  }
  public void autoCreateRootPage(int index) {
    if (handler.get(index) instanceof RootRequestHandler) {
      RootRequestHandler r = (RootRequestHandler)handler.get(index);
      String html = "<HTML><HEAD><TITLE>"+r.getTitle()+"</TITLE></HEAD>\r\n";
      html = html + "<BODY><H1>"+r.getDescription()+"</H1>\r\n";
      for (int i = 0; i < handler.size(); i++) {
        html = html + "<a href=\"" + ((HTTPRequestHandler)handler.get(i)).getHandledPath();
        html = html + "\">" + ((HTTPRequestHandler)handler.get(i)).getDescription() + "</a><br>";
      }
      html = html + "</BODY></HTML>\r\n";
      r.setReplyHTML(html);
    }
  }
  public void run() {
    while (true) {
      try {
        Socket s = server.accept();
        Thread t = new ServerThread(s);
        t.start();
      }
      catch (IOException e) {
      }
    }
  }
  class ServerThread extends Thread {
    private Socket connection;
    ServerThread(Socket s) {
      this.connection = s;
    }
    public void run() {
      try {
        Writer out = new BufferedWriter(
                      new OutputStreamWriter(
                       connection.getOutputStream(), "ASCII"
                      )
                     );
        Reader in = new InputStreamReader(
                     new BufferedInputStream(
                      connection.getInputStream()
                     )
                    );
        StringBuffer req = new StringBuffer(80);
        while (true) {
          int c = in.read();
          if (c == '\r' || c == '\n' || c == -1) break;
          req.append((char) c);
        }
        String get = req.toString();
        cat.debug(get);
        StringTokenizer st = new StringTokenizer(get);
        String method = st.nextToken();
        String request = st.nextToken();
        String version = st.nextToken();
        if (method.equalsIgnoreCase("GET")) {
          boolean served = false;
          for (int i = 0; i < handler.size(); i++) {
            if (handler.get(i) instanceof HTTPRequestHandler) {
              if (((HTTPRequestHandler)handler.get(i)).handleRequest(request, out)) {
                served = true;
                break;
              }
            }
          }
          if (!served)
            PluggableHTTPServer.replyNotFound(out);
        }
        else {
          PluggableHTTPServer.replyMethodNotAllowed(out);
        }
      } 
      catch (IOException e) {
      }
      finally {
        try {
          if (connection != null) connection.close();
        }
        catch (IOException e) {}
      }
    }  
  }  
  public static void main(String[] args) {
    int thePort;
    BasicConfigurator.configure();
    Category cat1 = Category.getInstance("cat1");
    cat1.addAppender(new org.apache.log4j.ConsoleAppender(new PatternLayout("%m%n")));
    Category cat2 = Category.getInstance("cat2");
    cat2.setPriority(Priority.INFO);
    cat2.addAppender(new org.apache.log4j.ConsoleAppender(new PatternLayout("%c - %m%n")));
    try {
      thePort = Integer.parseInt(args[1]);
    }
    catch (Exception e) {
      thePort = PluggableHTTPServer.DEFAULT_PORT;
    }
    PluggableHTTPServer server = null;
    while (server == null) {
      try {
        server = new PluggableHTTPServer(thePort);
        server.addRequestHandler(new RootRequestHandler());
        server.addRequestHandler(new Log4jRequestHandler());
        server.addRequestHandler(new UserDialogRequestHandler());
        server.autoCreateRootPage(0);
        Thread t = new Thread(server);
        t.start();
      } catch (IOException e) {
        server = null;
        thePort++;
      }
    }
  }  
}
