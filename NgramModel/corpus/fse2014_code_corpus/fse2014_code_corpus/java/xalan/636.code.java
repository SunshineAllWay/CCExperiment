package org.apache.xml.serializer;
import org.xml.sax.SAXException;
public interface ExtendedLexicalHandler extends org.xml.sax.ext.LexicalHandler
{
    public void comment(String comment) throws SAXException;
}
