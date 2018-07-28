package org.apache.batik.util;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.MissingResourceException;
public class XMLResourceDescriptor {
    public static final String XML_PARSER_CLASS_NAME_KEY =
        "org.xml.sax.driver";
    public static final String CSS_PARSER_CLASS_NAME_KEY =
        "org.w3c.css.sac.driver";
    public static final String RESOURCES =
        "resources/XMLResourceDescriptor.properties";
    protected static Properties parserProps = null;
    protected static String xmlParserClassName;
    protected static String cssParserClassName;
    protected static synchronized Properties getParserProps() {
        if (parserProps != null) return parserProps;
        parserProps = new Properties();
        try {
            Class cls = XMLResourceDescriptor.class;
            InputStream is = cls.getResourceAsStream(RESOURCES);
            parserProps.load(is);
        } catch (IOException ioe) {
            throw new MissingResourceException(ioe.getMessage(),
                                               RESOURCES, null);
        }
        return parserProps;
    }
    public static String getXMLParserClassName() {
        if (xmlParserClassName == null) {
            xmlParserClassName = getParserProps().getProperty
                (XML_PARSER_CLASS_NAME_KEY);
        }
        return xmlParserClassName;
    }
    public static void setXMLParserClassName(String xmlParserClassName) {
        XMLResourceDescriptor.xmlParserClassName = xmlParserClassName;
    }
    public static String getCSSParserClassName() {
        if (cssParserClassName == null) {
            cssParserClassName = getParserProps().getProperty
                (CSS_PARSER_CLASS_NAME_KEY);
        }
        return cssParserClassName;
    }
    public static void setCSSParserClassName(String cssParserClassName) {
        XMLResourceDescriptor.cssParserClassName = cssParserClassName;
    }
}
