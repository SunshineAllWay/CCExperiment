package org.apache.log4j.spi;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.InterruptedIOException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
public class LocationInfo implements java.io.Serializable {
  transient String lineNumber;
  transient String fileName;
  transient String className;
  transient String methodName;
  public String fullInfo;
  private static StringWriter sw = new StringWriter();
  private static PrintWriter pw = new PrintWriter(sw);
  private static Method getStackTraceMethod;
  private static Method getClassNameMethod;
  private static Method getMethodNameMethod;
  private static Method getFileNameMethod;
  private static Method getLineNumberMethod;
  public final static String NA = "?";
  static final long serialVersionUID = -1325822038990805636L;
    public static final LocationInfo NA_LOCATION_INFO =
            new LocationInfo(NA, NA, NA, NA);
  static boolean inVisualAge = false;
  static {
    try {
      inVisualAge = Class.forName("com.ibm.uvm.tools.DebugSupport") != null;
      LogLog.debug("Detected IBM VisualAge environment.");
    } catch(Throwable e) {
    }
      try {
          Class[] noArgs = null;
          getStackTraceMethod = Throwable.class.getMethod("getStackTrace", noArgs);
          Class stackTraceElementClass = Class.forName("java.lang.StackTraceElement");
          getClassNameMethod = stackTraceElementClass.getMethod("getClassName", noArgs);
          getMethodNameMethod = stackTraceElementClass.getMethod("getMethodName", noArgs);
          getFileNameMethod = stackTraceElementClass.getMethod("getFileName", noArgs);
          getLineNumberMethod = stackTraceElementClass.getMethod("getLineNumber", noArgs);
      } catch(ClassNotFoundException ex) {
          LogLog.debug("LocationInfo will use pre-JDK 1.4 methods to determine location.");
      } catch(NoSuchMethodException ex) {
          LogLog.debug("LocationInfo will use pre-JDK 1.4 methods to determine location.");
      }
  }
    public LocationInfo(Throwable t, String fqnOfCallingClass) {
      if(t == null || fqnOfCallingClass == null)
	return;
      if (getLineNumberMethod != null) {
          try {
              Object[] noArgs = null;
              Object[] elements =  (Object[]) getStackTraceMethod.invoke(t, noArgs);
              String prevClass = NA;
              for(int i = elements.length - 1; i >= 0; i--) {
                  String thisClass = (String) getClassNameMethod.invoke(elements[i], noArgs);
                  if(fqnOfCallingClass.equals(thisClass)) {
                      int caller = i + 1;
                      if (caller < elements.length) {
                          className = prevClass;
                          methodName = (String) getMethodNameMethod.invoke(elements[caller], noArgs);
                          fileName = (String) getFileNameMethod.invoke(elements[caller], noArgs);
                          if (fileName == null) {
                              fileName = NA;
                          }
                          int line = ((Integer) getLineNumberMethod.invoke(elements[caller], noArgs)).intValue();
                          if (line < 0) {
                              lineNumber = NA;
                          } else {
                              lineNumber = String.valueOf(line);
                          }
                          StringBuffer buf = new StringBuffer();
                          buf.append(className);
                          buf.append(".");
                          buf.append(methodName);
                          buf.append("(");
                          buf.append(fileName);
                          buf.append(":");
                          buf.append(lineNumber);
                          buf.append(")");
                          this.fullInfo = buf.toString();
                      }
                      return;
                  }
                  prevClass = thisClass;
              }
              return;
          } catch(IllegalAccessException ex) {
              LogLog.debug("LocationInfo failed using JDK 1.4 methods", ex);
          } catch(InvocationTargetException ex) {
              if (ex.getTargetException() instanceof InterruptedException
                      || ex.getTargetException() instanceof InterruptedIOException) {
                  Thread.currentThread().interrupt();
              }
              LogLog.debug("LocationInfo failed using JDK 1.4 methods", ex);
          } catch(RuntimeException ex) {
              LogLog.debug("LocationInfo failed using JDK 1.4 methods", ex);
          }
      }
      String s;
      synchronized(sw) {
	t.printStackTrace(pw);
	s = sw.toString();
	sw.getBuffer().setLength(0);
      }
      int ibegin, iend;
      ibegin = s.lastIndexOf(fqnOfCallingClass);
      if(ibegin == -1)
	return;
      if (ibegin + fqnOfCallingClass.length() < s.length() &&
              s.charAt(ibegin + fqnOfCallingClass.length()) != '.') {
          int i = s.lastIndexOf(fqnOfCallingClass + ".");
          if (i != -1) {
              ibegin = i;
          }
      }
      ibegin = s.indexOf(Layout.LINE_SEP, ibegin);
      if(ibegin == -1)
	return;
      ibegin+= Layout.LINE_SEP_LEN;
      iend = s.indexOf(Layout.LINE_SEP, ibegin);
      if(iend == -1)
	return;
      if(!inVisualAge) {
	ibegin = s.lastIndexOf("at ", iend);
	if(ibegin == -1)
	  return;
	ibegin += 3;
      }
      this.fullInfo = s.substring(ibegin, iend);
    }
    private static final void appendFragment(final StringBuffer buf,
                                             final String fragment) {
          if (fragment == null) {
             buf.append(NA);
          } else {
             buf.append(fragment);
          }
    }
    public LocationInfo(
      final String file,
      final String classname,
      final String method,
      final String line) {
      this.fileName = file;
      this.className = classname;
      this.methodName = method;
      this.lineNumber = line;
      StringBuffer buf = new StringBuffer();
	  appendFragment(buf, classname);
	  buf.append(".");
	  appendFragment(buf, method);
	  buf.append("(");
	  appendFragment(buf, file);
	  buf.append(":");
	  appendFragment(buf, line);
	  buf.append(")");
	  this.fullInfo = buf.toString();
    }
    public
    String getClassName() {
      if(fullInfo == null) return NA;
      if(className == null) {
	int iend = fullInfo.lastIndexOf('(');
	if(iend == -1)
	  className = NA;
	else {
	  iend =fullInfo.lastIndexOf('.', iend);
          int ibegin = 0;
	  if (inVisualAge) {
	    ibegin = fullInfo.lastIndexOf(' ', iend)+1;
          }
	  if(iend == -1)
	    className = NA;
	  else
	    className = this.fullInfo.substring(ibegin, iend);
	}
      }
      return className;
    }
    public
    String getFileName() {
      if(fullInfo == null) return NA;
      if(fileName == null) {
	int iend = fullInfo.lastIndexOf(':');
	if(iend == -1)
	  fileName = NA;
	else {
	  int ibegin = fullInfo.lastIndexOf('(', iend - 1);
	  fileName = this.fullInfo.substring(ibegin + 1, iend);
	}
      }
      return fileName;
    }
    public
    String getLineNumber() {
      if(fullInfo == null) return NA;
      if(lineNumber == null) {
	int iend = fullInfo.lastIndexOf(')');
	int ibegin = fullInfo.lastIndexOf(':', iend -1);
	if(ibegin == -1)
	  lineNumber = NA;
	else
	  lineNumber = this.fullInfo.substring(ibegin + 1, iend);
      }
      return lineNumber;
    }
    public
    String getMethodName() {
      if(fullInfo == null) return NA;
      if(methodName == null) {
	int iend = fullInfo.lastIndexOf('(');
	int ibegin = fullInfo.lastIndexOf('.', iend);
	if(ibegin == -1)
	  methodName = NA;
	else
	  methodName = this.fullInfo.substring(ibegin + 1, iend);
      }
      return methodName;
    }
}
