package org.apache.log4j.xml;
import org.apache.log4j.helpers.LogLog;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
public class Log4jEntityResolver implements EntityResolver {
  private static final String PUBLIC_ID = "-//APACHE//DTD LOG4J 1.2//EN";
  public InputSource resolveEntity (String publicId, String systemId) {
    if (systemId.endsWith("log4j.dtd")  || PUBLIC_ID.equals(publicId)) {
      Class clazz = getClass();
      InputStream in = clazz.getResourceAsStream("/org/apache/log4j/xml/log4j.dtd");
      if (in == null) {
	    LogLog.warn("Could not find [log4j.dtd] using [" + clazz.getClassLoader()
		     + "] class loader, parsed without DTD.");
        in = new ByteArrayInputStream(new byte[0]);
      }
	  return new InputSource(in);
    } else {
      return null;
    }
  }
}
