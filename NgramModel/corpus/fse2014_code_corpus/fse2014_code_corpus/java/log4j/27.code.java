package com.psibt.framework.net;
import java.io.*;
import java.net.*;
public interface HTTPRequestHandler {
  public String getTitle();
  public void setTitle(String title);
  public String getDescription();
  public void setDescription(String description);
  public String getHandledPath();
  public void setHandledPath(String path);
  public boolean handleRequest(String request, Writer out);
}