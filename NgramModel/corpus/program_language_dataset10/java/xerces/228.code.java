package org.apache.xerces.dom;
import java.io.OutputStream;
import java.io.Writer;
import org.w3c.dom.ls.LSOutput;
public class DOMOutputImpl implements LSOutput {
        protected Writer fCharStream = null;
        protected OutputStream fByteStream = null;
        protected String fSystemId = null;
        protected String fEncoding = null;
    public DOMOutputImpl() {}
    public Writer getCharacterStream(){
        return fCharStream;
     };
    public void setCharacterStream(Writer characterStream){
        fCharStream = characterStream;
    };
    public OutputStream getByteStream(){
        return fByteStream;
    };
    public void setByteStream(OutputStream byteStream){
        fByteStream = byteStream;
    };
    public String getSystemId(){
        return fSystemId;
    };
    public void setSystemId(String systemId){
        fSystemId = systemId;
    };
    public String getEncoding(){
        return fEncoding;
    };
    public void setEncoding(String encoding){
        fEncoding = encoding;
    };
}
