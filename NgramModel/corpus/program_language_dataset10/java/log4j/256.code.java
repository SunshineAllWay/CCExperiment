package org.apache.log4j.xml;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.apache.log4j.helpers.LogLog;
public class SAXErrorHandler implements ErrorHandler {
  public
  void error(final SAXParseException ex) {
    emitMessage("Continuable parsing error ", ex);
  }
  public
  void fatalError(final SAXParseException ex) {
    emitMessage("Fatal parsing error ", ex);
  }
  public
  void warning(final SAXParseException ex) {
    emitMessage("Parsing warning ", ex);
  }
  private static void emitMessage(final String msg, final SAXParseException ex) {
    LogLog.warn(msg +ex.getLineNumber()+" and column "
		 +ex.getColumnNumber());
    LogLog.warn(ex.getMessage(), ex.getException());
  }
}
