package dom.registry;
import org.apache.xerces.dom.CoreDOMImplementationImpl;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import dom.util.Assertion;
public class Test {
    public static void main(String argv[])
    {                                  
        System.out.println("Running dom.registry.Test...");
        System.setProperty(DOMImplementationRegistry.PROPERTY,
                          "org.apache.xerces.dom.DOMImplementationSourceImpl" +
                          " org.apache.xerces.dom.DOMXSImplementationSourceImpl");
        DOMImplementationRegistry registry = null;
        try {
            registry = DOMImplementationRegistry.newInstance();
            Assertion.verify(registry != null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            DOMImplementation i = registry.getDOMImplementation("XML");
            Assertion.verify(i ==
                             CoreDOMImplementationImpl.getDOMImplementation());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            DOMImplementation i =
                registry.getDOMImplementation("XML MutationEvents");
            Assertion.verify(i ==
                             DOMImplementationImpl.getDOMImplementation());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}    
