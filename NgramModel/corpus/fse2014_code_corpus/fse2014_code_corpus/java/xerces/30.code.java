package ui;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.util.EncodingMap;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
public class DOMParserSaveEncoding extends DOMParser {
    String _mimeEncoding = "UTF-8";
    private void setMimeEncoding( String encoding ) {
        _mimeEncoding = encoding;
    }
    private String getMimeEncoding() {
        return(_mimeEncoding);
    }
    public String getJavaEncoding() {
        String javaEncoding = null;
        String mimeEncoding = getMimeEncoding();
        if (mimeEncoding != null) {
            if (mimeEncoding.equals( "DEFAULT" ))
                javaEncoding =  "UTF8";
            else if (mimeEncoding.equalsIgnoreCase( "UTF-16" ))
                javaEncoding = "Unicode";
            else
                javaEncoding = EncodingMap.getIANA2JavaMapping( mimeEncoding );    
        } 
        if(javaEncoding == null)   
            javaEncoding = "UTF8";
        return(javaEncoding);
    }
    public void startGeneralEntity(String name, 
                            XMLResourceIdentifier identifier,
                            String encoding, Augmentations augs) throws XNIException {
        if( encoding != null){
            setMimeEncoding( encoding);
        }
        super.startGeneralEntity(name, identifier, encoding, augs);
    }
}
