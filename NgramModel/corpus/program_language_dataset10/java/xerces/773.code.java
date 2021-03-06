package dom.serialize;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xerces.dom.DOMOutputImpl;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.DOMSerializerImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
public class TestXmlns implements DOMErrorHandler{
      public static void main(String[] args) {
            DocumentImpl document = new DocumentImpl();
            document.setXmlEncoding("utf-8");
            Element outerNode = document.createElement("outer");
            outerNode.setAttribute("xmlns", "myuri:");
            document.appendChild(outerNode);
            Element innerNode = document.createElement("inner");
            outerNode.appendChild(innerNode);
            Writer writer = new StringWriter();
            OutputFormat format = new OutputFormat();
            format.setEncoding("utf-8");
            Serializer serializer = SerializerFactory.getSerializerFactory("xml").makeSerializer(writer, format);
            try {
                  serializer.asDOMSerializer().serialize(document);
            } catch (IOException exception) {
                  exception.printStackTrace();
                  System.exit(1);
            }
            System.out.println("\n---XMLSerializer output---");
            System.out.println(writer.toString());
          DOMSerializerImpl s = new DOMSerializerImpl();
                  DOMParser p = new DOMParser();
                  try {
                      p.parse(args[0]);
                  } catch (Exception e){
                  }
                  Document doc = p.getDocument();
            System.out.println("\n---DOMWriter output---");
            LSSerializer domWriter = ((DOMImplementationLS)DOMImplementationImpl.getDOMImplementation()).createLSSerializer();
            DOMConfiguration config = domWriter.getDomConfig();
            config.setParameter("error-handler", new TestXmlns());
            config.setParameter("namespaces", Boolean.FALSE);
            try {
                LSOutput dOut = new DOMOutputImpl();
                dOut.setByteStream(System.out);
                domWriter.write(document,dOut);
            } catch (Exception e){
                e.printStackTrace();
            }
      }
    public boolean handleError(DOMError error){
        short severity = error.getSeverity();
        if (severity == DOMError.SEVERITY_ERROR) {
            System.out.println("[dom3-error]: "+error.getMessage());
        }
        if (severity == DOMError.SEVERITY_FATAL_ERROR) {
                   System.out.println("[dom3-fatal-error]: "+error.getMessage());
               }
        if (severity == DOMError.SEVERITY_WARNING) {
            System.out.println("[dom3-warning]: "+error.getMessage());
        }
        return true;
    }
}
