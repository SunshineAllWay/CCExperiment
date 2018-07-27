package org.apache.xerces.xni;
import java.util.Enumeration;
public interface NamespaceContext {
    public final static String XML_URI = "http://www.w3.org/XML/1998/namespace".intern();
    public final static String XMLNS_URI = "http://www.w3.org/2000/xmlns/".intern();
    public void pushContext();
    public void popContext();
    public boolean declarePrefix(String prefix, String uri);
    public String getURI(String prefix);
    public String getPrefix(String uri);
    public int getDeclaredPrefixCount();
    public String getDeclaredPrefixAt(int index);
    public Enumeration getAllPrefixes();
    public void reset();
} 
