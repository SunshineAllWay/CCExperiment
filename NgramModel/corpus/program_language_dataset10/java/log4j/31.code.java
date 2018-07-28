package com.psibt.framework.net;
public class UserDialogRequestHandler extends RootRequestHandler {
  private Component parentComponent;
  public UserDialogRequestHandler() {
    this(null);
  }
  public UserDialogRequestHandler(Component parentComponent) {
    this.setTitle("user dialog");
    this.setDescription("show user dialog");
    this.setHandledPath("/userdialog/");
    this.parentComponent = parentComponent;
  }
  public boolean handleRequest(String request, Writer out) {
    String path = "";
    String query = null;
    try {
      URL url = new URL("http://localhost"+request);
      path = url.getPath();
      query = url.getQuery();
      if (path.startsWith(this.getHandledPath()) == false) {
        return false;
      }
      out.write("HTTP/1.0 200 OK\r\n");
      out.write("Content-type: text/html\r\n\r\n");
      out.write("<HTML><HEAD><TITLE>" + this.getTitle() + "</TITLE></HEAD>\r\n");
      out.write("<BODY><H1>" + this.getDescription() + "</H1>\r\n");
      if ((query != null) && (query.length() >= 0)) {
        int idx = query.indexOf("=");
        String message = query.substring(idx+1, query.length());
        message = message.replace('+', ' ');
        idx = message.indexOf("%");
        while (idx >= 0) {
          String sl = message.substring(0, idx);
          String sm = message.substring(idx+1, idx+3);
          String sr = message.substring(idx+3, message.length());
          try {
            int i = Integer.parseInt(sm, 16);
            sm = String.valueOf((char)i);
          }
          catch (Exception ex) {
            sm = "";
          }
          message = sl + sm + sr;
          idx = message.indexOf("%");
        }
        if ((message != null) && (message.length() > 0)) {
          Thread t = new Thread(new DialogThread(parentComponent, message));
          t.start();
        }
      }
      out.write("<form name=\"Formular\" ACTION=\""+this.getHandledPath()+"+\" METHOD=\"PUT\">");
      out.write("<table>\r\n");
      out.write(" <tr><td>Send message to user</td></tr>\r\n");
      out.write(" <tr><td><textarea name=\"message\" rows=10 cols=50></textarea></td></tr>\r\n");
      out.write("</table>\r\n");
      out.write("<input type=submit value=\"Submit\">");
      out.write("</form>");
      out.write("</BODY></HTML>\r\n");
      out.flush();
      return true;
    } catch (Exception ex) {
      return false;
    }
  }
  class DialogThread implements Runnable {
    private Component parentComponent;
    private String message;
    public DialogThread(Component parentComponent, String message) {
      this.parentComponent = parentComponent;
      this.message = message;
    }
    public void run() {
      JOptionPane.showMessageDialog(parentComponent, message);
    }
  }
}