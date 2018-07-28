package org.apache.batik.dom.svg;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import org.apache.batik.dom.util.DocumentFactory;
import org.w3c.dom.svg.SVGDocument;
public interface SVGDocumentFactory extends DocumentFactory {
    SVGDocument createSVGDocument(String uri) throws IOException;
    SVGDocument createSVGDocument(String uri, InputStream is) 
        throws IOException;
    SVGDocument createSVGDocument(String uri, Reader r) throws IOException;
}
