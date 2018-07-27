package dom.dom3;
import java.io.Reader;
import java.io.StringReader;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.NodeImpl;
import org.apache.xerces.xs.ElementPSVI;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMLocator;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSResourceResolver;
import org.w3c.dom.ls.LSSerializer;
import dom.util.Assertion;
public class Test implements DOMErrorHandler, LSResourceResolver {
    static int errorCounter = 0;
    static DOMErrorHandler errorHandler = new Test();
    static LSResourceResolver resolver = new Test();
    public static void main( String[] argv) {
        try {
            boolean namespaces = true;
            System.out.println("Running dom.dom3.Test...");
            System.setProperty(DOMImplementationRegistry.PROPERTY,"org.apache.xerces.dom.DOMImplementationSourceImpl org.apache.xerces.dom.DOMXSImplementationSourceImpl");
            DOMImplementationLS impl = (DOMImplementationLS)DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
            Assertion.verify(impl!=null, "domImplementation != null");
            LSParser builder = impl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS,
                                                       null);
            LSSerializer writer = impl.createLSSerializer();
            DOMConfiguration config = writer.getDomConfig();
            config.setParameter("namespaces",(namespaces)?Boolean.TRUE:Boolean.FALSE);
            config.setParameter("validate",Boolean.FALSE);
            {
                Document doc = builder.parseURI("tests/dom/dom3/input.xml");
                NodeList ls = doc.getElementsByTagName("a:elem_a"); 
                NodeImpl elem = (NodeImpl)ls.item(0);
                if (namespaces) {
                    Assertion.verify(elem.lookupPrefix("http://www.example.com").equals("ns1"), 
                                     "[a:elem_a].lookupPrefix(http://www.example.com)==null");
                    Assertion.verify(elem.isDefaultNamespace("http://www.example.com") == true, 
                                     "[a:elem_a].isDefaultNamespace(http://www.example.com)==true");
                    Assertion.verify(elem.lookupPrefix("http://www.example.com").equals("ns1"), 
                                     "[a:elem_a].lookupPrefix(http://www.example.com)==ns1");
                    Assertion.verify(elem.lookupNamespaceURI("xsi").equals("http://www.w3.org/2001/XMLSchema-instance"), 
                                     "[a:elem_a].lookupNamespaceURI('xsi') == 'http://www.w3.org/2001/XMLSchema-instance'" );
                } else {
                    Assertion.verify( elem.lookupPrefix("http://www.example.com") == null,"lookupPrefix(http://www.example.com)==null"); 
                }
                ls = doc.getElementsByTagName("bar:leaf");
                elem = (NodeImpl)ls.item(0);
                Assertion.verify(elem.lookupPrefix("url1:").equals("foo"), 
                                 "[bar:leaf].lookupPrefix('url1:', false) == foo");
                ls = doc.getElementsByTagName("baz");
                elem = (NodeImpl)ls.item(0);
                ls = doc.getElementsByTagName("elem8");
                elem = (NodeImpl)ls.item(0);
                Element e1 = doc.createElementNS("b:","p:baz");
                e1.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:x", "b:");
                elem.appendChild(e1);
                Assertion.verify(((NodeImpl)e1).lookupPrefix("b:").equals("p"), 
                                 "[p:baz].lookupPrefix('b:', false) == p");
                Assertion.verify(elem.lookupNamespaceURI("xsi").equals("http://www.w3.org/2001/XMLSchema-instance"), 
                                 "[bar:leaf].lookupNamespaceURI('xsi') == 'http://www.w3.org/2001/XMLSchema-instance'" );
            }
            {
                errorCounter = 0;
                config = builder.getDomConfig();
                config.setParameter("error-handler",errorHandler);
                config.setParameter("validate", Boolean.TRUE);
                Document core = builder.parseURI("tests/dom/dom3/schema.xml");
                Assertion.verify(errorCounter == 0, "No errors should be reported");
                errorCounter = 0;    
                NodeList ls2 = core.getElementsByTagName("decVal");
                Element testElem = (Element)ls2.item(0);
                testElem.removeAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns");
                ls2 = core.getElementsByTagName("v02:decVal");
                testElem = (Element)ls2.item(0);
                testElem.setPrefix("myPrefix");
                Element root = core.getDocumentElement();
                Element newElem = core.createElementNS(null, "decVal");
                String data="4.5";
                if (true) {
                        data = "string";
                } 
                newElem.appendChild(core.createTextNode(data));
                root.insertBefore(newElem, testElem);
                newElem = core.createElementNS(null,  "notInSchema");
                newElem.appendChild(core.createTextNode("added new element"));
                root.insertBefore(newElem, testElem);
                root.appendChild(core.createElementNS("UndefinedNamespace", "NS1:foo"));
                config = core.getDomConfig();
                config.setParameter("error-handler",errorHandler);
                config.setParameter("validate", Boolean.TRUE);
                config.setParameter("schema-type", "http://www.w3.org/2001/XMLSchema");
                core.normalizeDocument();
                Assertion.verify(errorCounter == 3, "3 errors should be reported");
                errorCounter = 0;
                config.setParameter("validate", Boolean.FALSE);
                config.setParameter("comments", Boolean.FALSE);
                core.normalizeDocument();
                Assertion.verify(errorCounter == 0, "No errors should be reported");
                config = builder.getDomConfig();
                config.setParameter("validate", Boolean.FALSE);
            }
            {
                errorCounter = 0;
                config = builder.getDomConfig();
                config.setParameter("error-handler",errorHandler);
                config.setParameter("validate", Boolean.TRUE);
                config.setParameter("psvi", Boolean.TRUE);
                Document core = builder.parseURI("data/personal-schema.xml");
                Assertion.verify(errorCounter == 0, "No errors should be reported");
                NodeList ls2 = core.getElementsByTagName("person");
                Element testElem = (Element)ls2.item(0);
                Assertion.verify(((ElementPSVI)testElem).getElementDeclaration().getName().equals("person"), "testElem decl");
                Element e1 = core.createElementNS(null, "person");
                core.getDocumentElement().appendChild(e1);
                e1.setAttributeNS(null, "id", "newEmp");
                Element e2 = core.createElementNS(null, "name");
                e2.appendChild(core.createElementNS(null, "family"));
                e2.appendChild(core.createElementNS(null, "given"));
                e1.appendChild(e2);
                e1.appendChild(core.createElementNS(null, "email"));
                Element e3 = core.createElementNS(null, "link");
                e3.setAttributeNS(null, "manager", "Big.Boss");
                e1.appendChild(e3);
                testElem.removeAttributeNode(testElem.getAttributeNodeNS(null, "contr"));
                NamedNodeMap map = testElem.getAttributes();
                config = core.getDomConfig();
                errorCounter = 0;
                config.setParameter("psvi", Boolean.TRUE);
                config.setParameter("error-handler",errorHandler);
                config.setParameter("validate", Boolean.TRUE);
                config.setParameter("schema-type", "http://www.w3.org/2001/XMLSchema");
                core.normalizeDocument();
                Assertion.verify(errorCounter == 0, "No errors should be reported");
                Assertion.verify(((ElementPSVI)e1).getElementDeclaration().getName().equals("person"), "e1 decl");              
                config = builder.getDomConfig();
                config.setParameter("validate", Boolean.FALSE);
            }
            {
                Document doc= new DocumentImpl(); 
                Element root = doc.createElementNS("http://www.w3.org/1999/XSL/Transform", "xsl:stylesheet");
                doc.appendChild(root);
                root.setAttributeNS("http://attr1", "xsl:attr1","");
                Element child1 = doc.createElementNS("http://child1", "NS2:child1");
                child1.setAttributeNS("http://attr2", "NS2:attr2","");
                root.appendChild(child1);
                Element child2 = doc.createElementNS("http://child2","NS4:child2");
                child2.setAttributeNS("http://attr3","attr3", "");
                root.appendChild(child2);
                Element child3 = doc.createElementNS("http://www.w3.org/1999/XSL/Transform","xsl:child3");
                child3.setAttributeNS("http://a1","attr1", "");
                child3.setAttributeNS("http://a2","xsl:attr2", "");
                child3.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:a1", "http://a1");
                child3.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsl", "http://a2");
                Element child4 = doc.createElementNS(null, "child4");
                child4.setAttributeNS("http://a1", "xsl:attr1", "");
                child4.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "default");
                child3.appendChild(child4);
                root.appendChild(child3);
                doc.normalizeDocument();
                String name = root.getNodeName();
                Assertion.verify(name.equals("xsl:stylesheet"), "xsl:stylesheet");
                String value = root.getAttributeNS("http://www.w3.org/2000/xmlns/", "xsl");
                Assertion.verify(value!=null, "xmlns:xsl != null");
                Assertion.verify(value.equals("http://www.w3.org/1999/XSL/Transform"), "xmlns:xsl="+value);
                value = root.getAttributeNS("http://www.w3.org/2000/xmlns/", "NS1");
                Assertion.verify(value!=null && 
                                 value.equals("http://attr1"), "xmlns:NS1="+value);
                Assertion.verify(child1.getNodeName().equals("NS2:child1"), "NS2:child1");
                value = child1.getAttributeNS("http://www.w3.org/2000/xmlns/", "NS2");
                Assertion.verify(value!=null && 
                                 value.equals("http://child1"), "xmlns:NS2="+value);
                value = child1.getAttributeNS("http://www.w3.org/2000/xmlns/", "NS1");
                Assertion.verify(value!=null && 
                                 value.equals("http://attr2"), "xmlns:NS1="+value);
                Assertion.verify(child3.getNodeName().equals("xsl:child3"), "xsl:child3");
                value = child3.getAttributeNS("http://www.w3.org/2000/xmlns/", "NS1");
                Assertion.verify(value!=null && 
                                 value.equals("http://a2"), "xmlns:NS1="+value);
                value = child3.getAttributeNS("http://www.w3.org/2000/xmlns/", "a1");
                Assertion.verify(value!=null && 
                                 value.equals("http://a1"), "xmlns:a1="+value);
                value = child3.getAttributeNS("http://www.w3.org/2000/xmlns/", "xsl");
                Assertion.verify(value!=null && 
                                 value.equals("http://www.w3.org/1999/XSL/Transform"), "xmlns:xsl="+value);
                Attr attr = child3.getAttributeNodeNS("http://a2", "attr2");
                Assertion.verify(attr != null, "NS1:attr2 !=null");
                Assertion.verify(child3.getAttributes().getLength() == 5, "xsl:child3 has 5 attrs");
                Attr temp = child4.getAttributeNodeNS("http://www.w3.org/2000/xmlns/", "xmlns");
                Assertion.verify(temp.getNodeName().equals("xmlns"), "attribute name is xmlns");
                Assertion.verify(temp.getNodeValue().length() == 0, "xmlns=''");                
            }
            {
                Document doc= new DocumentImpl(); 
                Element root = doc.createElementNS("http://www.w3.org/1999/XSL/Transform", "xsl:stylesheet");
                doc.appendChild(root);
                root.setAttributeNS("http://attr1", "xsl:attr1","");
                Element child1 = doc.createElementNS("http://child1", "NS2:child1");
                child1.setAttributeNS("http://attr2", "NS2:attr2","");
                root.appendChild(child1);
                Element child2 = doc.createElementNS("http://child2","NS4:child2");
                child2.setAttributeNS("http://attr3","attr3", "");
                root.appendChild(child2);
                Element child3 = doc.createElementNS("http://www.w3.org/1999/XSL/Transform","xsl:child3");
                child3.setAttributeNS("http://a1","attr1", "");
                child3.setAttributeNS("http://a2","xsl:attr2", "");
                child3.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:a1", "http://a1");
                child3.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsl", "http://a2");
                Element child4 = doc.createElementNS(null, "child4");
                child4.setAttributeNS("http://a1", "xsl:attr1", "");
                child4.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "default");
                child3.appendChild(child4);
                root.appendChild(child3);
                writer.getDomConfig().setParameter("namespaces", Boolean.TRUE);
                String xmlData = writer.writeToString(doc);
                Reader r = new StringReader(xmlData);
                LSInput in = impl.createLSInput();
                in.setCharacterStream(r);
                doc = builder.parse(in);
                root = doc.getDocumentElement();
                child1 = (Element)root.getFirstChild();
                child2 = (Element)child1.getNextSibling();
                child3 = (Element)child2.getNextSibling();
                String name = root.getNodeName();
                Assertion.verify(name.equals("xsl:stylesheet"), "xsl:stylesheet");
                String value = root.getAttributeNS("http://www.w3.org/2000/xmlns/", "xsl");
                Assertion.verify(value!=null, "xmlns:xsl != null");
                Assertion.verify(value.equals("http://www.w3.org/1999/XSL/Transform"), "xmlns:xsl="+value);
                value = root.getAttributeNS("http://www.w3.org/2000/xmlns/", "NS1");
                Assertion.verify(value!=null && 
                                 value.equals("http://attr1"), "xmlns:NS1="+value);
                Assertion.verify(child1.getNodeName().equals("NS2:child1"), "NS2:child1");
                value = child1.getAttributeNS("http://www.w3.org/2000/xmlns/", "NS2");
                Assertion.verify(value!=null && 
                                 value.equals("http://child1"), "xmlns:NS2="+value);
                value = child1.getAttributeNS("http://www.w3.org/2000/xmlns/", "NS1");
                Assertion.verify(value!=null && 
                                 value.equals("http://attr2"), "xmlns:NS1="+value);
                Assertion.verify(child3.getNodeName().equals("xsl:child3"), "xsl:child3");
                value = child3.getAttributeNS("http://www.w3.org/2000/xmlns/", "NS1");
                Assertion.verify(value!=null && 
                                 value.equals("http://a2"), "xmlns:NS1="+value);
                value = child3.getAttributeNS("http://www.w3.org/2000/xmlns/", "a1");
                Assertion.verify(value!=null && 
                                 value.equals("http://a1"), "xmlns:a1="+value);
                value = child3.getAttributeNS("http://www.w3.org/2000/xmlns/", "xsl");
                Assertion.verify(value!=null && 
                                 value.equals("http://www.w3.org/1999/XSL/Transform"), "xmlns:xsl="+value);
                Attr attr = child3.getAttributeNodeNS("http://a2", "attr2");
                Assertion.verify(attr != null, "NS6:attr2 !=null");
                Assertion.verify(child3.getAttributes().getLength() == 5, "xsl:child3 has 5 attrs");
            }
           {
            config = builder.getDomConfig();
            config.setParameter("error-handler",errorHandler);
            config.setParameter("validate", Boolean.FALSE);
            config.setParameter("entities", Boolean.TRUE);
            Document doc = builder.parseURI("tests/dom/dom3/wholeText.xml");
            Element root = doc.getDocumentElement();
            Element test = (Element)doc.getElementsByTagName("elem").item(0);
            test.appendChild(doc.createTextNode("Address: "));
            test.appendChild(doc.createEntityReference("ent2"));
            test.appendChild(doc.createTextNode("City: "));
            test.appendChild(doc.createEntityReference("ent1"));
            DocumentType doctype = doc.getDoctype();
            Node entity = doctype.getEntities().getNamedItem("ent3");
            NodeList ls = test.getChildNodes();
            Assertion.verify(ls.getLength()==5, "List length");
            String compare1 = "Home Address: 1900 Dallas Road (East) City: Dallas. California. USA  PO #5668";
            Assertion.verify(((Text)ls.item(0)).getWholeText().equals(compare1), "Compare1");
            String compare2 = "Home Address: 1900 Dallas Road (East) City: Dallas. California. USA  PO #5668";
            Assertion.verify(((Text)ls.item(1)).getWholeText().equals(compare2), "Compare2");
            ((NodeImpl)ls.item(0)).setReadOnly(true, true);
            Text original = (Text)ls.item(0);
            Node newNode = original.replaceWholeText("Replace with this text");
            ls = test.getChildNodes();
            Assertion.verify(ls.getLength() == 1, "Length == 1");
            Assertion.verify(ls.item(0).getNodeValue().equals("Replace with this text"), "Replacement works");
            Assertion.verify(newNode != original, "New node created");
            Text text = doc.createTextNode("readonly");
            ((NodeImpl)text).setReadOnly(true, true);
            text = text.replaceWholeText("Data");
            Assertion.verify(text.getNodeValue().equals("Data"), "New value 'Data'");
            test = (Element)doc.getElementsByTagName("elem").item(1);
            try {            
                ((Text)test.getFirstChild()).replaceWholeText("can't replace");
            } catch (DOMException e){
               Assertion.verify(e !=null);
            }
            String compare3 = "Test: The Content ends here. ";
           }
            {
                errorCounter = 0;
                config = builder.getDomConfig();
                config.setParameter("error-handler",errorHandler);
                config.setParameter("resource-resolver",resolver);
                config.setParameter("validate", Boolean.TRUE);
                config.setParameter("psvi", Boolean.TRUE);
                errorCounter = 0;
                Document core2 = builder.parseURI("tests/dom/dom3/both-error.xml");
                Assertion.verify(errorCounter == 4, "4 errors should be reported");
                errorCounter = 0;
                config.setParameter("schema-type", "http://www.w3.org/2001/XMLSchema");
                core2 = builder.parseURI("tests/dom/dom3/both.xml");
                Assertion.verify(errorCounter == 0, "No errors should be reported");
                errorCounter = 0;
                config.setParameter("schema-type","http://www.w3.org/TR/REC-xml");
                core2 = builder.parseURI("tests/dom/dom3/both-error.xml");
                Assertion.verify(errorCounter == 3, "3 errors should be reported");
                core2 = builder.parseURI("tests/dom/dom3/both-error.xml");
                errorCounter = 0;
                Element root = core2.getDocumentElement();
                root.removeAttributeNS("http://www.w3.org/2001/XMLSchema", "xsi");               
                root.removeAttributeNS("http://www.w3.org/2001/XMLSchema", "noNamespaceSchemaLocation");
                config = core2.getDomConfig();
                config.setParameter("error-handler",errorHandler);
                config.setParameter("schema-type", "http://www.w3.org/2001/XMLSchema");
                config.setParameter("schema-location","personal.xsd");
                config.setParameter("resource-resolver",resolver);
                config.setParameter("validate", Boolean.TRUE);
                core2.normalizeDocument();
                Assertion.verify(errorCounter == 1, "1 error should be reported: "+errorCounter);
            }
            {
				LSParser parser = impl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS,
														   null);
				Document doc = parser.parseURI("tests/dom/dom3/baseURI.xml");
				Element root = doc.getDocumentElement();
				NodeList ls = doc.getElementsByTagNameNS(null, "streetNum");
				Node e = ls.item(0);
				Assertion.verify(((NodeImpl)e).getBaseURI().endsWith("tests/dom/dom3/baseURI.xml"), 
								 "baseURI=tests/dom/dom3/baseURI.xml");
				ls = root.getElementsByTagNameNS(null, "header");
				Node p2 = ls.item(0);
				Assertion.verify(((NodeImpl)p2).getBaseURI().equals("http://paragraph.com"), 
								 "baseURI=http://paragraph.com");
				p2 = ls.item(1);
				Assertion.verify(((NodeImpl)p2).getBaseURI().equals("http://paragraph.com2"), 
				 "baseURI=http://paragraph.com2");
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
        System.out.println("done!");
    }
    StringBuffer fError = new StringBuffer();
    public boolean handleError(DOMError error){
        fError.setLength(0);
        short severity = error.getSeverity();
        if (severity == DOMError.SEVERITY_ERROR) {
            errorCounter++;
            fError.append("[Error]");
        }
        if (severity == DOMError.SEVERITY_FATAL_ERROR) {
            fError.append("[FatalError]");
        }
        if (severity == DOMError.SEVERITY_WARNING) {
            fError.append("[Warning]");
        }
        DOMLocator locator = error.getLocation();
        if (locator != null) {
            fError.append(locator.getLineNumber());
            fError.append(":");
            fError.append(locator.getColumnNumber());
            fError.append(":");
            fError.append(locator.getByteOffset());
            Node node = locator.getRelatedNode();
            if (node != null) {
                fError.append("[");
                fError.append(locator.getRelatedNode().getNodeName());
                fError.append("]");
            }
            String systemId = locator.getUri();
            if (systemId != null) {
                int index = systemId.lastIndexOf('/');
                if (index != -1)
                    systemId = systemId.substring(index + 1);
                fError.append(":");
                fError.append(systemId);
            }
            fError.append(": ");
            fError.append(error.getMessage());
        }
        return true;
    }
	public LSInput resolveResource(String type, String namespace, String publicId, String systemId, String baseURI) {
		try {
			DOMImplementationLS impl =
				(DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation(
					"LS");
			LSInput source = impl.createLSInput();
			if (systemId.equals("personal.xsd")) {
				source.setSystemId("data/personal.xsd");
			}
			else {
				source.setSystemId("data/personal.dtd");
			}
			return source;
		}
		catch (Exception e) {
			return null;
		}
	}
}
