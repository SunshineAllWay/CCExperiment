package org.apache.batik.dom.util;
import org.xml.sax.SAXException;
import java.io.IOException;
public class SAXIOException extends IOException {
    protected SAXException saxe;
    public SAXIOException( SAXException saxe) {
        super(saxe.getMessage());
        this.saxe = saxe;
    }
    public SAXException getSAXException() { return saxe; }
    public Throwable    getCause() { return saxe; }
}
