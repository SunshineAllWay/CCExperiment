package org.apache.xml.serializer;
import java.util.Vector;
interface XSLOutputAttributes
{
    public String getDoctypePublic();
    public String getDoctypeSystem();
    public String getEncoding();
    public boolean getIndent();
    public int getIndentAmount();
    public String getMediaType();
    public boolean getOmitXMLDeclaration();
    public String getStandalone();
    public String getVersion();
    public void setCdataSectionElements(Vector URI_and_localNames);
    public void setDoctype(String system, String pub);
    public void setDoctypePublic(String doctype);
    public void setDoctypeSystem(String doctype);
    public void setEncoding(String encoding);
    public void setIndent(boolean indent);
    public void setMediaType(String mediatype);
    public void setOmitXMLDeclaration(boolean b);
    public void setStandalone(String standalone);
    public void setVersion(String version);
    public String getOutputProperty(String name);
    public String getOutputPropertyDefault(String name);
    public void   setOutputProperty(String name, String val);
    public void   setOutputPropertyDefault(String name, String val);
}
