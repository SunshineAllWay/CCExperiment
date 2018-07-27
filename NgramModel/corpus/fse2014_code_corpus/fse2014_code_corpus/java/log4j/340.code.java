package org.apache.log4j.util;
import org.apache.oro.text.perl.Perl5Util;
public class SunReflectFilter implements Filter {
  Perl5Util util = new Perl5Util();
  public String filter(String in) {
    if(in == null) {
      return null;
    }
    if (util.match("/at sun.reflect/", in)) {
      return null;
    }
    if (in.indexOf("at java.lang.reflect.") >= 0) {
      return null;
    }
    if (in.indexOf("Compiled Code") >= 0) {
        if(in.indexOf("junit.framework.TestSuite") >= 0) {
            return util.substitute("s/Compiled Code/TestSuite.java:XXX/", in);
        }
    }
    if (util.match("/\\(Method.java:.*\\)/", in)) {
      return util.substitute("s/\\(Method.java:.*\\)/(Native Method)/", in);
    }
    return in;
  }
}
