package xs;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObject;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.LSParser;
public class QueryXS implements DOMErrorHandler {
    protected static final boolean DEFAULT_NAMESPACES = true;
    protected static final boolean DEFAULT_VALIDATION = false;
    protected static final boolean DEFAULT_SCHEMA_VALIDATION = false;
    static LSParser builder;
    public static void main(String[] argv) {
        if (argv.length == 0) {
            printUsage();
            System.exit(1);
        }
        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            XSImplementation impl = (XSImplementation) registry.getDOMImplementation("XS-Loader");
            XSLoader schemaLoader = impl.createXSLoader(null);
            DOMConfiguration config = schemaLoader.getConfig();
            DOMErrorHandler errorHandler = new QueryXS();
            config.setParameter("error-handler", errorHandler);
            config.setParameter("validate", Boolean.TRUE);
            System.out.println("Parsing " + argv[0] + "...");
            XSModel model = schemaLoader.loadURI(argv[0]);
            if (model != null) {
                XSNamedMap map = model.getComponents(XSConstants.ELEMENT_DECLARATION);
                if (map.getLength() != 0) {
					System.out.println("*************************************************");
					System.out.println("* Global element declarations: {namespace} name ");
					System.out.println("*************************************************");
                    for (int i = 0; i < map.getLength(); i++) {
                        XSObject item = map.item(i);
                        System.out.println("{" + item.getNamespace() + "}" + item.getName());
                    }
                }
                map = model.getComponents(XSConstants.ATTRIBUTE_DECLARATION);
                if (map.getLength() != 0) {
					System.out.println("*************************************************");
                    System.out.println("* Global attribute declarations: {namespace} name");
					System.out.println("*************************************************");
                    for (int i = 0; i < map.getLength(); i++) {
                        XSObject item = map.item(i);
                        System.out.println("{" + item.getNamespace() + "}" + item.getName());
                    }
                }
				map = model.getComponents(XSConstants.TYPE_DEFINITION);
				if (map.getLength() != 0) {
					System.out.println("*************************************************");
					System.out.println("* Global type declarations: {namespace} name");
					System.out.println("*************************************************");
					for (int i = 0; i < map.getLength(); i++) {
						XSObject item = map.item(i);
						System.out.println("{" + item.getNamespace() + "}" + item.getName());
					}
				}
				map = model.getComponents(XSConstants.NOTATION_DECLARATION);
				if (map.getLength() != 0) {
					System.out.println("*************************************************");
					System.out.println("* Global notation declarations: {namespace} name");
					System.out.println("*************************************************");
					for (int i = 0; i < map.getLength(); i++) {
						XSObject item = map.item(i);
						System.out.println("{" + item.getNamespace() + "}" + item.getName());
					}
				}
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private static void printUsage() {
        System.err.println("usage: java dom.QueryXS uri ...");
        System.err.println();
    } 
    public boolean handleError(DOMError error){
        short severity = error.getSeverity();
        if (severity == DOMError.SEVERITY_ERROR) {
            System.out.println("[xs-error]: "+error.getMessage());
        }
        if (severity == DOMError.SEVERITY_WARNING) {
            System.out.println("[xs-warning]: "+error.getMessage());
        }
        return true;
    }
}
