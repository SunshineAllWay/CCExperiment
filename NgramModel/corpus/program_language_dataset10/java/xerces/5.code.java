package dom;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
public class DOMGenerate {
    public static void main( String[] argv ) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            Element root = doc.createElementNS(null, "person"); 
            Element item = doc.createElementNS(null, "name");   
            item.appendChild( doc.createTextNode("Jeff") );
            root.appendChild( item );                           
            item = doc.createElementNS(null, "age");            
            item.appendChild( doc.createTextNode("28" ) );       
            root.appendChild( item );                           
            item = doc.createElementNS(null, "height");            
            item.appendChild( doc.createTextNode("1.80" ) );
            root.appendChild( item );                           
            doc.appendChild( root );                            
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS domImplLS = (DOMImplementationLS)registry.getDOMImplementation("LS");
            LSSerializer ser = domImplLS.createLSSerializer();  
            LSOutput out = domImplLS.createLSOutput();
            StringWriter stringOut = new StringWriter();        
            out.setCharacterStream(stringOut);
            ser.write(doc, out);                                
            System.out.println( "STRXML = " 
                    + stringOut.toString() );                   
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }
}
