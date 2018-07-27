package org.apache.xerces.util;
import java.io.InputStream;
import java.io.Reader;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
public final class SAXInputSource extends XMLInputSource {
    private XMLReader fXMLReader;
    private InputSource fInputSource;
    public SAXInputSource() {
        this(null);
    }
    public SAXInputSource(InputSource inputSource) {
        this(null, inputSource);
    }
    public SAXInputSource(XMLReader reader, InputSource inputSource) {
        super(inputSource != null ? inputSource.getPublicId() : null, 
                inputSource != null ? inputSource.getSystemId() : null, null);
        if (inputSource != null) {
            setByteStream(inputSource.getByteStream());
            setCharacterStream(inputSource.getCharacterStream());
            setEncoding(inputSource.getEncoding());
        }
        fInputSource = inputSource;
        fXMLReader = reader;
    }
    public void setXMLReader(XMLReader reader) {
        fXMLReader = reader;
    }
    public XMLReader getXMLReader() {
        return fXMLReader;
    }
    public void setInputSource(InputSource inputSource) {
        if (inputSource != null) {
            setPublicId(inputSource.getPublicId());
            setSystemId(inputSource.getSystemId());
            setByteStream(inputSource.getByteStream());
            setCharacterStream(inputSource.getCharacterStream());
            setEncoding(inputSource.getEncoding());
        }
        else {
            setPublicId(null);
            setSystemId(null);
            setByteStream(null);
            setCharacterStream(null);
            setEncoding(null);
        }
        fInputSource = inputSource;
    }
    public InputSource getInputSource() {
        return fInputSource;
    }
    public void setPublicId(String publicId) {
        super.setPublicId(publicId);
        if (fInputSource == null) {
            fInputSource = new InputSource();
        }
        fInputSource.setPublicId(publicId);
    } 
    public void setSystemId(String systemId) {
        super.setSystemId(systemId);
        if (fInputSource == null) {
            fInputSource = new InputSource();
        }
        fInputSource.setSystemId(systemId);
    } 
    public void setByteStream(InputStream byteStream) {
        super.setByteStream(byteStream);
        if (fInputSource == null) {
            fInputSource = new InputSource();
        }
        fInputSource.setByteStream(byteStream);
    } 
    public void setCharacterStream(Reader charStream) {
        super.setCharacterStream(charStream);
        if (fInputSource == null) {
            fInputSource = new InputSource();
        }
        fInputSource.setCharacterStream(charStream);
    } 
    public void setEncoding(String encoding) {
        super.setEncoding(encoding);
        if (fInputSource == null) {
            fInputSource = new InputSource();
        }
        fInputSource.setEncoding(encoding);
    } 
} 
