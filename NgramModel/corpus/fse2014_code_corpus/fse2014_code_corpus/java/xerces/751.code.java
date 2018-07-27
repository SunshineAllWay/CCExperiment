package org.apache.xml.serialize;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.DocumentHandler;
public interface Serializer
{
    public void setOutputByteStream(OutputStream output);
    public void setOutputCharStream( Writer output );
    public void setOutputFormat( OutputFormat format );
    public DocumentHandler asDocumentHandler()
        throws IOException;
    public ContentHandler asContentHandler()
        throws IOException;
    public DOMSerializer asDOMSerializer()
        throws IOException;
}
