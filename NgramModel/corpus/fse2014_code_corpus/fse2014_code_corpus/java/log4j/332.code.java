package org.apache.log4j.util;
import org.apache.oro.text.perl.Perl5Util;
public class EnhancedJunitTestRunnerFilter implements Filter {
  private Perl5Util util = new Perl5Util();
  private static final String[] PATTERNS = {
          "at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner",
          "at org.apache.tools.ant",
          "at junit.textui.TestRunner",
          "at com.intellij.rt.execution.junit",
          "at java.lang.reflect.Method.invoke",
          "at org.apache.maven.",
          "at org.codehaus.",
		    "at org.junit.internal.runners.",
		    "at junit.framework.JUnit4TestAdapter"
  };
  public EnhancedJunitTestRunnerFilter() {
  }
  public String filter(String in) {
    if (in == null) {
      return null;
    }
    if (in.indexOf("at junit.framework.TestCase.runTest") != -1) {
        return "\tat java.lang.reflect.Method.invoke(X)\n\t" + in.trim();
    }
    for (int i = 0; i < PATTERNS.length; i++) {
        if(in.indexOf(PATTERNS[i]) != -1) {
            return null;
        }
    }
    if (util.match("/\\sat /", in)) {
       return "\t" + in.trim();
    }
    return in;
  }
}
