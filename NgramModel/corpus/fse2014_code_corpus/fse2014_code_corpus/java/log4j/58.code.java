package org.apache.log4j;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.log4j.helpers.LogLog;
public class ConsoleAppender extends WriterAppender {
  public static final String SYSTEM_OUT = "System.out";
  public static final String SYSTEM_ERR = "System.err";
  protected String target = SYSTEM_OUT;
  private boolean follow = false;
  public ConsoleAppender() {
  }
  public ConsoleAppender(Layout layout) {
    this(layout, SYSTEM_OUT);
  }
  public ConsoleAppender(Layout layout, String target) {
    setLayout(layout);
    setTarget(target);
    activateOptions();
  }
  public
  void setTarget(String value) {
    String v = value.trim();
    if (SYSTEM_OUT.equalsIgnoreCase(v)) {
      target = SYSTEM_OUT;
    } else if (SYSTEM_ERR.equalsIgnoreCase(v)) {
      target = SYSTEM_ERR;
    } else {
      targetWarn(value);
    }
  }
  public
  String getTarget() {
    return target;
  }
  public final void setFollow(final boolean newValue) {
     follow = newValue;
  }
  public final boolean getFollow() {
      return follow;
  }
  void targetWarn(String val) {
    LogLog.warn("["+val+"] should be System.out or System.err.");
    LogLog.warn("Using previously set target, System.out by default.");
  }
   public void activateOptions() {
        if (follow) {
            if (target.equals(SYSTEM_ERR)) {
               setWriter(createWriter(new SystemErrStream()));
            } else {
               setWriter(createWriter(new SystemOutStream()));
            }
        } else {
            if (target.equals(SYSTEM_ERR)) {
               setWriter(createWriter(System.err));
            } else {
               setWriter(createWriter(System.out));
            }
        }
        super.activateOptions();
  }
  protected
  final
  void closeWriter() {
     if (follow) {
        super.closeWriter();
     }
  }
    private static class SystemErrStream extends OutputStream {
        public SystemErrStream() {
        }
        public void close() {
        }
        public void flush() {
            System.err.flush();
        }
        public void write(final byte[] b) throws IOException {
            System.err.write(b);
        }
        public void write(final byte[] b, final int off, final int len)
            throws IOException {
            System.err.write(b, off, len);
        }
        public void write(final int b) throws IOException {
            System.err.write(b);
        }
    }
    private static class SystemOutStream extends OutputStream {
        public SystemOutStream() {
        }
        public void close() {
        }
        public void flush() {
            System.out.flush();
        }
        public void write(final byte[] b) throws IOException {
            System.out.write(b);
        }
        public void write(final byte[] b, final int off, final int len)
            throws IOException {
            System.out.write(b, off, len);
        }
        public void write(final int b) throws IOException {
            System.out.write(b);
        }
    }
}
