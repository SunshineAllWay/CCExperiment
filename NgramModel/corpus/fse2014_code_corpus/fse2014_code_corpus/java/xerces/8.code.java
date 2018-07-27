package dom;
import org.w3c.dom.Document;
import org.w3c.dom.Text;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
public interface ParserWrapper {
    public Document parse(String uri) throws Exception;
    public void setFeature(String featureId, boolean state)
        throws  SAXNotRecognizedException, SAXNotSupportedException; 
    public DocumentInfo getDocumentInfo();
    public interface DocumentInfo {
        public boolean isIgnorableWhitespace(Text text);
    } 
} 
