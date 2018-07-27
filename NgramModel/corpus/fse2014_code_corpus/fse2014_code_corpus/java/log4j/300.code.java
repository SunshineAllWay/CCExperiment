package org.apache.log4j.customLogger;
import org.apache.log4j.*;
import org.apache.log4j.spi.OptionHandler;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.xml.XLevel;
public class XLogger extends Logger implements OptionHandler {
  private static String FQCN = XLogger.class.getName() + ".";
  private static XFactory factory = new XFactory();
  String suffix = "";
  protected XLogger(String name) {
    super(name);
  }
  public
  void activateOptions() {
  }
  public 
  void debug(String message) {
    super.log(FQCN, Level.DEBUG, message + " " + suffix, null);
  }
  public
  void lethal(String message, Throwable t) { 
    if(repository.isDisabled(XLevel.LETHAL_INT)) 
      return;
    if(XLevel.LETHAL.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, XLevel.LETHAL, message, t);
  }
  public
  void lethal(String message) { 
    if(repository.isDisabled(XLevel.LETHAL_INT)) 
      return;
    if(XLevel.LETHAL.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, XLevel.LETHAL, message, null);
  }
  static
  public
  Logger getLogger(String name) {
    return LogManager.getLogger(name, factory);
  }
  static
  public
  Logger getLogger(Class clazz) {
    return XLogger.getLogger(clazz.getName());
  }
  public
  String getSuffix() {
    return suffix;
  }
  public
  void setSuffix(String suffix) {
    this.suffix = suffix;
  }
  public
  void trace(String message, Throwable t) { 
    if(repository.isDisabled(XLevel.TRACE_INT))
      return;   
    if(XLevel.TRACE.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, XLevel.TRACE, message, t);
  }
  public
  void trace(String message) { 
    if(repository.isDisabled(XLevel.TRACE_INT))
      return;   
    if(XLevel.TRACE.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, XLevel.TRACE, message, null);
  }
  public static class XFactory implements LoggerFactory {
    public XFactory() {
    }
    public
    Logger  makeNewLoggerInstance(String name) {
      return new XLogger(name);
    }
  }
}
