package org.apache.solr.common.util;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
public class FileUtilsTest extends TestCase {  
  public void testResolve() throws IOException {
    String cwd = new File(".").getAbsolutePath();
    assertEquals(new File("conf/data"), FileUtils.resolvePath(new File("conf"), "data"));
    assertEquals(new File(cwd+"/conf/data"), FileUtils.resolvePath(new File(cwd+"/conf"), "data"));
    assertEquals(new File(cwd+"/data"), FileUtils.resolvePath(new File("conf"), cwd+"/data"));
  }
}
