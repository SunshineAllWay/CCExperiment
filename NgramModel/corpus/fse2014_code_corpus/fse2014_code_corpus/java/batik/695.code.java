package org.apache.batik.dom.util;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import org.w3c.dom.Document;
import org.xml.sax.XMLReader;
public interface DocumentFactory {
    void setValidating(boolean isValidating);
    boolean isValidating();
    Document createDocument(String ns, String root, String uri) throws IOException;
    Document createDocument(String ns, String root, String uri, InputStream is)
        throws IOException;
    Document createDocument(String ns, String root, String uri, XMLReader r)
        throws IOException;
    Document createDocument(String ns, String root, String uri, Reader r)
        throws IOException;
    DocumentDescriptor getDocumentDescriptor();
}
