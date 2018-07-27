package org.apache.xml.serializer;
interface SerializerConstants
{
    static final String CDATA_CONTINUE = "]]]]><![CDATA[>";
    static final String CDATA_DELIMITER_CLOSE = "]]>";
    static final String CDATA_DELIMITER_OPEN = "<![CDATA[";
    static final String EMPTYSTRING = "";
    static final String ENTITY_AMP = "&amp;";
    static final String ENTITY_CRLF = "&#xA;";
    static final String ENTITY_GT = "&gt;";
    static final String ENTITY_LT = "&lt;";
    static final String ENTITY_QUOT = "&quot;";
    static final String XML_PREFIX = "xml";
    static final String XMLNS_PREFIX = "xmlns";
    static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";
    public static final String DEFAULT_SAX_SERIALIZER=SerializerBase.PKG_NAME+".ToXMLSAXHandler";
    static final String XMLVERSION11 = "1.1";
    static final String XMLVERSION10 = "1.0";
}
