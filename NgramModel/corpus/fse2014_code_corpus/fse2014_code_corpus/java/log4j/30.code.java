package com.psibt.framework.net;
import java.io.*;
import java.net.*;
import java.util.*;
public class RootRequestHandler implements HTTPRequestHandler {
  private String title;
  private String description;
  private String handledPath;
  private String ReplyType = "Content-type: text/html\r\n\r\n";
  private String ReplyHTML = "<HTML><HEAD><TITLE>Root</TITLE></HEAD>\r\n"
                           + "<BODY><H1>Root</H1>\r\n"
                           + "</BODY></HTML>\r\n";
  public RootRequestHandler() {
    this.setTitle("root page");
    this.setDescription("root page");
    this.setHandledPath("/");
  }
  public String getReplyType() {
    return this.ReplyType;
  }
  public void setReplyType(String ReplyType) {
    this.ReplyType = ReplyType;
  }
  public String getReplyHTML() {
    return this.ReplyHTML;
  }
  public void setReplyHTML(String ReplyHTML) {
    this.ReplyHTML = ReplyHTML;
  }
  public String getTitle() {
    return this.title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public String getDescription() {
    return this.description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public String getHandledPath() {
    return this.handledPath;
  }
  public void setHandledPath(String path) {
    this.handledPath = path;
  }
  public boolean handleRequest(String request, Writer out) {
    String path = "";
    String query = null;
    try {
      URL url = new URL("http://localhost"+request);
      path = url.getPath();
      query = url.getPath();
      if (path.equals(handledPath) == false) {
        return false;
      }
      out.write("HTTP/1.0 200 OK\r\n");
      if (ReplyType != null)
        out.write(ReplyType);
      if (ReplyHTML != null)
        out.write(ReplyHTML);
      out.flush();
      return true;
    } catch (Exception ex) {
      return false;
    }
  }
}