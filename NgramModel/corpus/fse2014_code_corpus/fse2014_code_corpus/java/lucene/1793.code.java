package org.apache.lucene.util;
import org.apache.lucene.LucenePackage;
public final class Constants {
  private Constants() {}			  
  public static final String JAVA_VERSION = System.getProperty("java.version");
  public static final boolean JAVA_1_1 = JAVA_VERSION.startsWith("1.1.");
  public static final boolean JAVA_1_2 = JAVA_VERSION.startsWith("1.2.");
  public static final boolean JAVA_1_3 = JAVA_VERSION.startsWith("1.3.");
  public static final String OS_NAME = System.getProperty("os.name");
  public static final boolean LINUX = OS_NAME.startsWith("Linux");
  public static final boolean WINDOWS = OS_NAME.startsWith("Windows");
  public static final boolean SUN_OS = OS_NAME.startsWith("SunOS");
  public static final String OS_ARCH = System.getProperty("os.arch");
  public static final String OS_VERSION = System.getProperty("os.version");
  public static final String JAVA_VENDOR = System.getProperty("java.vendor");
  public static final boolean JRE_IS_64BIT;
  static {
    String x = System.getProperty("sun.arch.data.model");
    if (x != null) {
      JRE_IS_64BIT = x.indexOf("64") != -1;
    } else {
      if (OS_ARCH != null && OS_ARCH.indexOf("64") != -1) {
        JRE_IS_64BIT = true;
      } else {
        JRE_IS_64BIT = false;
      }
    }
  }
  private static String ident(final String s) {
    return s.toString();
  }
  public static final String LUCENE_MAIN_VERSION = ident("3.1");
  public static final String LUCENE_VERSION;
  static {
    Package pkg = LucenePackage.get();
    String v = (pkg == null) ? null : pkg.getImplementationVersion();
    if (v == null) {
      v = LUCENE_MAIN_VERSION + "-dev";
    } else if (!v.startsWith(LUCENE_MAIN_VERSION)) {
      v = LUCENE_MAIN_VERSION + "-dev " + v;
    }
    LUCENE_VERSION = ident(v);
  }
}
