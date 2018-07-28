package org.apache.log4j.util;
import org.apache.oro.text.perl.Perl5Util;
public class JunitTestRunnerFilter implements Filter {
  Perl5Util util = new Perl5Util();
  public String filter(String in) {
    if (in == null) {
      return null;
    }
    if (
      util.match(
          "/at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner/", in)) {
      return null;
    } else if (
      util.match(
          "/at org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner/",
          in)) {
      return null;
    } else if (
      util.match(
          "/at com.intellij/",
          in)) {
      return null;
    } else if (in.indexOf("at junit.") >= 0 && in.indexOf("ui.TestRunner") >= 0) {
       return null;
    } else if (in.indexOf("org.apache.maven") >= 0) {
       return null;
    } else if(in.indexOf("junit.internal") >= 0) {
        return null;
    } else if(in.indexOf("JUnit4TestAdapter") >= 0) {
        return null;
    } else if (util.match("/\\sat /", in)) {
       return "\t" + in.trim();
    } else {
      return in;
    }
  }
}
