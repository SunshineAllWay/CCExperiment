package org.apache.log4j.xml;
import org.w3c.dom.Element;
import java.util.Properties;
public interface UnrecognizedElementHandler {
    boolean parseUnrecognizedElement(Element element, Properties props) throws Exception;
}