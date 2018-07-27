package dom.mem;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.NodeImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Notation;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;
import dom.util.Assertion;
public class Test {
    public static boolean DOMExceptionsTest(Object node,
					    String methodName,
					    Class[] methodSignature,
					    Object[] parameters,
					    short code)
    {
	boolean asExpected = false;
	Method method;
	try {
	    method = node.getClass().getMethod(methodName,methodSignature);
	    method.invoke(node, parameters);
	} catch(InvocationTargetException exc) {
	    Throwable realE = exc.getTargetException(); 
	    if(realE instanceof DOMException) {
		asExpected = (((DOMException)realE).code== code);
		if(!asExpected)
		    System.out.println("Wrong DOMException(" +
				       ((DOMException)realE).code + ")");
	    } else {
		System.out.println("Wrong Exception (" + code + ")");
	    }
	    if(!asExpected) {
		System.out.println("Expected DOMException (" +
				   code + ") not thrown");
	    }
	} catch(Exception exc) {
	    System.out.println("test invocation failure (" + exc + ")");
	}
	return (asExpected);
    }
    public static void main(String argv[])
    {
    System.out.println("DOM Memory Test...");
    {
        Document    doc;
        doc = new DocumentImpl();
    }
    {
        Document doc = new DocumentImpl();
        Element     el = doc.createElement("Doc02Element");
        DocumentFragment frag = doc.createDocumentFragment ();
        Text  text = doc.createTextNode("Doc02TextNode");
        Comment comment = doc.createComment("Doc02Comment");
        CDATASection  cdataSec = doc.createCDATASection("Doc02CDataSection");
        DocumentType  docType = doc.getImplementation().createDocumentType("Doc02DocumentType", null, null);
        Notation notation = ((DocumentImpl) doc).createNotation("Doc02Notation");
        ProcessingInstruction pi = doc.createProcessingInstruction("Doc02PITarget",
                                    "Doc02PIData");
        NodeList    nodeList = doc.getElementsByTagName("*");
    }
    {
        Document doc = new DocumentImpl();
        Element     el = doc.createElement("Doc02Element");
    }
    {
        Document    doc = new DocumentImpl();
        DocumentFragment frag = doc.createDocumentFragment ();
    };
    {
        Document doc = new DocumentImpl();
        Element     el = doc.createElement("Doc02Element");
    }
    {
        Document doc = new DocumentImpl();
        Text  text = doc.createTextNode("Doc02TextNode");
    }
    {
        Document doc = new DocumentImpl();
        Comment comment = doc.createComment("Doc02Comment");
    }
    {
        Document doc = new DocumentImpl();
        CDATASection  cdataSec = doc.createCDATASection("Doc02CDataSection");
    }
    {
        Document doc = new DocumentImpl();
        DocumentType  docType = doc.getImplementation().createDocumentType("Doc02DocumentType", null, null);
    }
    {
        Document doc = new DocumentImpl();
        Notation notation = ((DocumentImpl)doc).createNotation("Doc02Notation");
    }
    {
        Document doc = new DocumentImpl();
        ProcessingInstruction pi = doc.createProcessingInstruction("Doc02PITarget",
                                    "Doc02PIData");
    }
    {
        Document doc = new DocumentImpl();
        Attr  attribute = doc.createAttribute("Doc02Attribute");
    }
    {
        Document doc = new DocumentImpl();
        EntityReference  er = doc.createEntityReference("Doc02EntityReference");
    }
    {
        Document doc = new DocumentImpl();
        NodeList    nodeList = doc.getElementsByTagName("*");
    }
    {
        Document    doc = new DocumentImpl();
        Element     rootEl = doc.createElement("Doc03RootElement");
        doc.appendChild(rootEl);
        Text        textNode = doc.createTextNode("Doc03 text stuff");
        Assertion.verify(rootEl.getFirstChild() == null);
        Assertion.verify(rootEl.getLastChild() == null);
        rootEl.appendChild(textNode);
        Assertion.verify(rootEl.getFirstChild() == textNode);
        Assertion.verify(rootEl.getLastChild() == textNode);
        Assertion.verify(textNode.getNextSibling() == null);
        Assertion.verify(textNode.getPreviousSibling() == null);
        Text        textNode2 = doc.createTextNode("Doc03 text stuff");
        rootEl.appendChild(textNode2);
        Assertion.verify(textNode.getNextSibling() == textNode2);
        Assertion.verify(textNode2.getNextSibling() == null);
        Assertion.verify(textNode.getPreviousSibling() == null);
        Assertion.verify(textNode2.getPreviousSibling() == textNode);
        Assertion.verify(rootEl.getFirstChild() == textNode);
        Assertion.verify(rootEl.getLastChild() == textNode2);
        NodeList    nodeList = doc.getElementsByTagName("*");
    };
    {
        Document    doc = new DocumentImpl();
        Element     rootEl  = doc.createElement("RootElement");
        doc.appendChild(rootEl);
        {
            Attr        attr01  = doc.createAttribute("Attr01");
            rootEl.setAttributeNode(attr01);
        }
        {
            Attr attr02 = doc.createAttribute("Attr01");
            rootEl.setAttributeNode(attr02);  
        }
    };
    {
        Document    doc = new DocumentImpl();
        Element     rootEl  = doc.createElement("RootElement");
        doc.appendChild(rootEl);
        Attr        attr01  = doc.createAttribute("Attr02");
        rootEl.setAttributeNode(attr01);
        Attr        attr02 = doc.createAttribute("Attr02");
        rootEl.setAttributeNode(attr02);  
    }
    {
        Document    doc = new DocumentImpl();
        Element     rootEl  = doc.createElement("RootElement");
        doc.appendChild(rootEl);
        Attr        attr01  = doc.createAttribute("Attr03");
        rootEl.setAttributeNode(attr01);
        attr01.setValue("Attr03Value1");
        attr01.setValue("Attr03Value2");
    }
    {
        Document    doc = new DocumentImpl();
        Element     rootEl  = doc.createElement("RootElement");
        doc.appendChild(rootEl);
        Text        txt1 = doc.createTextNode("Hello Goodbye");
        rootEl.appendChild(txt1);
        txt1.splitText(6);
        rootEl.normalize();
    }
    { 
    }
    {
        NamedNodeMap    nnm = null;
        Assertion.verify(nnm == null);
        Document        doc = new DocumentImpl();
        nnm = doc.getAttributes();    
        Assertion.verify(nnm == null);
        Assertion.verify(!(nnm != null));
        Element el = doc.createElement("NamedNodeMap01");
        NamedNodeMap nnm2 = el.getAttributes();    
        Assertion.verify(nnm2 != null);
        Assertion.verify(nnm != nnm2);
        nnm = nnm2;
        Assertion.verify(nnm == nnm2);
    }
    {
        Document    doc1 = new DocumentImpl();
        Document    doc2 = new DocumentImpl();
        Element     el1  = doc1.createElement("abc");
        doc1.appendChild(el1);
        Assertion.verify(el1.getParentNode() != null);
        el1.setAttribute("foo", "foovalue");
        Node        el2  = doc2.importNode(el1, true);
        Assertion.verify(el2.getParentNode() == null);
        String       tagName = el2.getNodeName();
        Assertion.equals(tagName, "abc");
        Assertion.verify(el2.getOwnerDocument() == doc2);
        Assertion.equals(((Element) el2).getAttribute("foo"), "foovalue");
        Assertion.verify(doc1 != doc2);
    }
    {
        Document     doc = new DocumentImpl();
        Text          tx = doc.createTextNode("Hello");
        Element       el = doc.createElement("abc");
        el.appendChild(tx);
        int     textLength = tx.getLength();
        Assertion.verify(textLength == 5);
        NodeList      nl = tx.getChildNodes();
        int      nodeListLen = nl.getLength();
        Assertion.verify(nodeListLen == 0);
        nl = el.getChildNodes();
        nodeListLen = nl.getLength();
        Assertion.verify(nodeListLen == 1);
    }
    {
        NodeList    nl = null;
        NodeList    nl2 = null;
        Assertion.verify(nl == null);
        Assertion.verify(!(nl != null));
        Assertion.verify(nl == nl2);
        Document        doc = new DocumentImpl();
        nl = doc.getChildNodes();    
        Assertion.verify(nl != null);
        int len = nl.getLength();
        Assertion.verify(len == 0);
        Element el = doc.createElement("NodeList01");
        doc.appendChild(el);
        len = nl.getLength();
        Assertion.verify(len == 1);
        Assertion.verify(nl != nl2);
        nl2 = nl;
        Assertion.verify(nl == nl2);
    }
    {
         Document        doc = new DocumentImpl();
         Assertion.verify(DOMExceptionsTest(doc, "createElement",
					  new Class[]{String.class},
					  new Object[]{"!@@ bad element name"},
					  DOMException.INVALID_CHARACTER_ERR));
    }
    {
        Document        doc = new DocumentImpl();
        Element el = doc.createElement("NodeList01");
        doc.appendChild(el);
        Element n1, n2, n3;
        n1 = n2 = n3 = el;
        Assertion.verify(n1 == n2);
        Assertion.verify(n1 == n3);
        Assertion.verify(n1 == el);
        Assertion.verify(n1 != null);
        n1 = n2 = n3 = null;
        Assertion.verify(n1 == null);
    }
    {
        Document    doc = new DocumentImpl();
        Element     root = doc.createElement("CTestRoot");
        root.setAttribute("CTestAttr", "CTestAttrValue");
        String s = root.getAttribute("CTestAttr");
        Assertion.equals(s, "CTestAttrValue");
        Element     cloned = (Element)root.cloneNode(true);
        Attr a = cloned.getAttributeNode("CTestAttr");
        Assertion.verify(a != null);
        s = a.getValue();
        Assertion.equals(s, "CTestAttrValue");
        a = null;
        a = cloned.getAttributeNode("CTestAttr");
        Assertion.verify(a != null);
        s = a.getValue();
        Assertion.equals(s, "CTestAttrValue");
    }
    {
        Document    doc = new DocumentImpl();
        Element     root = doc.createElement("CTestRoot");
        root.setAttribute("attr", "attrValue");
        Attr attr = root.getAttributeNode("attr");
        ((org.apache.xerces.dom.AttrImpl)attr).setSpecified(false);
        root.setAttribute("attr2", "attr2Value");
        Element     cloned = (Element)root.cloneNode(true);
        Attr a = cloned.getAttributeNode("attr");
        Assertion.verify(a.getSpecified() == false);
        a = cloned.getAttributeNode("attr2");
        Assertion.verify(a.getSpecified() == true);
        a = (Attr)attr.cloneNode(true);
        Assertion.verify(a.getSpecified() == true);
    }
    {
        DOMImplementation  impl = DOMImplementationImpl.getDOMImplementation();
        Assertion.verify(impl.hasFeature("XML", "2.0")    == true);
        Assertion.verify(impl.hasFeature("XML", null)       == true);
        Assertion.verify(impl.hasFeature("XML", "1.0")    == true);
        Assertion.verify(impl.hasFeature("Traversal", null) == true);
        Assertion.verify(impl.hasFeature("HTML", null)           == false);
        Assertion.verify(impl.hasFeature("Views", null)          == false);
        Assertion.verify(impl.hasFeature("StyleSheets", null)    == false);
        Assertion.verify(impl.hasFeature("CSS", null)            == false);
        Assertion.verify(impl.hasFeature("CSS2", null)           == false);
        Assertion.verify(impl.hasFeature("Events", null)         == true);
        Assertion.verify(impl.hasFeature("UIEvents", null)       == false);
        Assertion.verify(impl.hasFeature("MouseEvents", null)    == false);
        Assertion.verify(impl.hasFeature("MutationEvents", null) == true);
        Assertion.verify(impl.hasFeature("HTMLEvents", null)     == false);
        Assertion.verify(impl.hasFeature("Range", null)          == true);
    }
    {
        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
        String qName = "foo:docName";
        String pubId = "pubId";
        String sysId = "http://sysId";
        DocumentType dt = impl.createDocumentType(qName, pubId, sysId);
        Assertion.verify(dt != null);
        Assertion.verify(dt.getNodeType() == Node.DOCUMENT_TYPE_NODE);
        Assertion.equals(dt.getNodeName(), qName);
        Assertion.verify(dt.getNamespaceURI() == null);
        Assertion.verify(dt.getPrefix() == null);
        Assertion.verify(dt.getLocalName() == null);
        Assertion.equals(dt.getPublicId(), pubId);
        Assertion.equals(dt.getSystemId(), sysId);
        Assertion.verify(dt.getInternalSubset() == null);
        Assertion.verify(dt.getOwnerDocument() == null);
        NamedNodeMap nnm = dt.getEntities();
        Assertion.verify(nnm.getLength() == 0);
        nnm = dt.getNotations();
        Assertion.verify(nnm.getLength() == 0);
        qName = "docName";
        dt = impl.createDocumentType(qName, pubId, sysId);
        Assertion.verify(dt != null);
        Assertion.verify(dt.getNodeType() == Node.DOCUMENT_TYPE_NODE);
        Assertion.equals(dt.getNodeName(), qName);
        Assertion.verify(dt.getNamespaceURI() == null);
        Assertion.verify(dt.getPrefix() == null);
        Assertion.verify(dt.getLocalName() == null);
        Assertion.equals(dt.getPublicId(), pubId);
        Assertion.equals(dt.getSystemId(), sysId);
        Assertion.verify(dt.getInternalSubset() == null);
        Assertion.verify(dt.getOwnerDocument() == null);
        Assertion.verify(DOMExceptionsTest(impl, "createDocumentType",
			new Class[]{String.class, String.class, String.class},
			new Object[]{"<docName", pubId, sysId},
			DOMException.INVALID_CHARACTER_ERR));     
        Assertion.verify(DOMExceptionsTest(impl, "createDocumentType",
			new Class[]{String.class, String.class, String.class},
			new Object[]{":docName", pubId, sysId},
			DOMException.NAMESPACE_ERR));     
        Assertion.verify(DOMExceptionsTest(impl, "createDocumentType",
			new Class[]{String.class, String.class, String.class},
			new Object[]{"docName:", pubId, sysId},
			DOMException.NAMESPACE_ERR));     
        Assertion.verify(DOMExceptionsTest(impl, "createDocumentType",
			new Class[]{String.class, String.class, String.class},
			new Object[]{"<doc::Name", pubId, sysId},
			DOMException.NAMESPACE_ERR));     
        Assertion.verify(DOMExceptionsTest(impl, "createDocumentType",
			new Class[]{String.class, String.class, String.class},
			new Object[]{"<doc:N:ame", pubId, sysId},
			DOMException.NAMESPACE_ERR));     
    }
    {
        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
        String qName = "foo:docName";
        String pubId = "pubId";
        String sysId = "http://sysId";
        DocumentType dt = impl.createDocumentType(qName, pubId, sysId);
        String docNSURI = "http://document.namespace";
        Document doc = impl.createDocument(docNSURI, qName, dt);
        Assertion.verify(dt.getOwnerDocument() == doc);
        Assertion.verify(doc.getOwnerDocument() == null);
        Assertion.verify(doc.getNodeType() == Node.DOCUMENT_NODE);
        Assertion.verify(doc.getDoctype() == dt);
        Assertion.equals(doc.getNodeName(), "#document");
        Assertion.verify(doc.getNodeValue() == null);
        Element el = doc.getDocumentElement();
        Assertion.equals(el.getLocalName(), "docName");
        Assertion.equals(el.getNamespaceURI(), docNSURI);
        Assertion.equals(el.getNodeName(), qName);
        Assertion.verify(el.getOwnerDocument() == doc);
        Assertion.verify(el.getParentNode() == doc);
        Assertion.equals(el.getPrefix(), "foo");
        Assertion.equals(el.getTagName(), qName);
        Assertion.verify(el.hasChildNodes() == false);
        Assertion.verify(DOMExceptionsTest(impl, "createDocument",
					   new Class[]{String.class,
						       String.class,
						       DocumentType.class},
					   new Object[]{docNSURI, qName, dt},
					   DOMException.WRONG_DOCUMENT_ERR));
    }
    {
        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
        String qName = "foo:docName";
        String pubId = "pubId";
        String sysId = "http://sysId";
        DocumentType dt = impl.createDocumentType(qName, pubId, sysId);
        String docNSURI = "http://document.namespace";
	Document doc = impl.createDocument(docNSURI, qName, dt);
        Element rootEl = doc.getDocumentElement();
        Element ela = doc.createElementNS("http://nsa", "a:ela");  
        Element elb = doc.createElementNS("http://nsb", "elb");    
        Element elc = doc.createElementNS(null, "elc");              
        rootEl.appendChild(ela);
        rootEl.appendChild(elb);
        rootEl.appendChild(elc);
        Assertion.equals(ela.getNodeName(), "a:ela");
        Assertion.equals(ela.getNamespaceURI(), "http://nsa");
        Assertion.equals(ela.getPrefix(), "a");
        Assertion.equals(ela.getLocalName(), "ela");
        Assertion.equals(ela.getTagName(), "a:ela");
        Assertion.equals(elb.getNodeName(), "elb");
        Assertion.equals(elb.getNamespaceURI(), "http://nsb");
        Assertion.verify(elb.getPrefix() == null);
        Assertion.equals(elb.getLocalName(), "elb");
        Assertion.equals(elb.getTagName(), "elb");
        Assertion.equals(elc.getNodeName(), "elc");
        Assertion.verify(elc.getNamespaceURI() == null);
        Assertion.verify(elc.getPrefix() ==  null);
        Assertion.equals(elc.getLocalName(), "elc");
        Assertion.equals(elc.getTagName(), "elc");
	Assertion.verify(DOMExceptionsTest(doc, "createElementNS",
				      new Class[]{String.class, String.class},
				      new Object[]{"http://nsa", "<a"},
				      DOMException.INVALID_CHARACTER_ERR));
	Assertion.verify(DOMExceptionsTest(doc, "createElementNS",
				      new Class[]{String.class, String.class},
				      new Object[]{"http://nsa", ":a"},
				      DOMException.NAMESPACE_ERR));
	Assertion.verify(DOMExceptionsTest(doc, "createElementNS",
				      new Class[]{String.class, String.class},
				      new Object[]{"http://nsa", "a:"},
				      DOMException.NAMESPACE_ERR));
	Assertion.verify(DOMExceptionsTest(doc, "createElementNS",
				      new Class[]{String.class, String.class},
				      new Object[]{"http://nsa", "a::a"},
				      DOMException.NAMESPACE_ERR));
	Assertion.verify(DOMExceptionsTest(doc, "createElementNS",
				      new Class[]{String.class, String.class},
				      new Object[]{"http://nsa", "a:a:a"},
				      DOMException.NAMESPACE_ERR));
	String xmlURI = "http://www.w3.org/XML/1998/namespace";
	Assertion.equals(doc.createElementNS(xmlURI, "xml:a").getNamespaceURI(), xmlURI);
        Assertion.verify(DOMExceptionsTest(doc, "createElementNS",
				      new Class[]{String.class, String.class},
				      new Object[]{"http://nsa", "xml:a"},
				      DOMException.NAMESPACE_ERR));
        Assertion.verify(DOMExceptionsTest(doc, "createElementNS",
				      new Class[]{String.class, String.class},
				      new Object[]{"", "xml:a"},
				      DOMException.NAMESPACE_ERR));
	Assertion.verify(DOMExceptionsTest(doc, "createElementNS",
				      new Class[]{String.class, String.class},
				      new Object[]{null, "xml:a"},
				      DOMException.NAMESPACE_ERR));
        Assertion.verify(DOMExceptionsTest(doc, "createElementNS",
                                           new Class[]{String.class, String.class},
				           new Object[]{"http://nsa", "xmlns"},
				           DOMException.NAMESPACE_ERR));
        Assertion.verify(DOMExceptionsTest(doc, "createElementNS",
                                           new Class[]{String.class, String.class},
				           new Object[]{xmlURI, "xmlns"},
				           DOMException.NAMESPACE_ERR));
    Assertion.verify(doc.createElementNS(null, "noNamespace").getNamespaceURI() == null);
	Assertion.verify(DOMExceptionsTest(doc, "createElementNS",
				      new Class[]{String.class, String.class},
				      new Object[]{null, "xmlns:a"},
				      DOMException.NAMESPACE_ERR));
        Assertion.equals(doc.createElementNS("http://nsa", "foo:a").getNamespaceURI(), "http://nsa");
	Assertion.verify(DOMExceptionsTest(doc, "createElementNS",
				      new Class[]{String.class, String.class},
				      new Object[]{null, "foo:a"},
				      DOMException.NAMESPACE_ERR));
        Element elem = doc.createElementNS("http://nsa", "foo:a");
        elem.setPrefix("bar");
        Assertion.equals(elem.getNodeName(), "bar:a");
        Assertion.equals(elem.getNamespaceURI(), "http://nsa");
        Assertion.equals(elem.getPrefix(), "bar");
        Assertion.equals(elem.getLocalName(), "a");
        Assertion.equals(elem.getTagName(), "bar:a");
        elem = doc.createElementNS("http://nsa", "a");
        Assertion.equals(elem.getPrefix(), null);
        elem.setPrefix("bar");
        Assertion.equals(elem.getNodeName(), "bar:a");
        Assertion.equals(elem.getNamespaceURI(), "http://nsa");
        Assertion.equals(elem.getPrefix(), "bar");
        Assertion.equals(elem.getLocalName(), "a");
        Assertion.equals(elem.getTagName(), "bar:a");
        elem = doc.createElementNS(xmlURI, "foo:a");
        elem.setPrefix("xml");
        elem = doc.createElementNS("http://nsa", "foo:a");
        Assertion.verify(DOMExceptionsTest(elem, "setPrefix",
					  new Class[]{String.class},
					  new Object[]{"xml"},
					  DOMException.NAMESPACE_ERR));
        elem.setPrefix("xmlns");
        elem = doc.createElementNS(null, "a");
        Assertion.verify(DOMExceptionsTest(elem, "setPrefix",
					  new Class[]{String.class},
					  new Object[]{"foo"},
					  DOMException.NAMESPACE_ERR));
        Assertion.verify(DOMExceptionsTest(doc, "setPrefix",
					  new Class[]{String.class},
					  new Object[]{"foo"},
					  DOMException.NAMESPACE_ERR));
    }
    {
        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
        String qName = "foo:docName";
        String pubId = "pubId";
        String sysId = "http://sysId";
        DocumentType dt = impl.createDocumentType(qName, pubId, sysId);
        String docNSURI = "http://document.namespace";
        Document doc = impl.createDocument(docNSURI, qName, dt);
        Element rootEl = doc.getDocumentElement();
        Attr attra = doc.createAttributeNS("http://nsa", "a:attra");       
        Attr attrb = doc.createAttributeNS("http://nsb", "attrb");         
        Attr attrc = doc.createAttributeNS(null, "attrc");    
        Assertion.equals(attra.getNodeName(), "a:attra");
        Assertion.equals(attra.getNamespaceURI(), "http://nsa");
        Assertion.equals(attra.getPrefix(), "a");
        Assertion.equals(attra.getLocalName(), "attra");
        Assertion.equals(attra.getName(), "a:attra");
        Assertion.verify(attra.getOwnerElement() == null);
        Assertion.equals(attrb.getNodeName(), "attrb");
        Assertion.equals(attrb.getNamespaceURI(), "http://nsb");
        Assertion.equals(attrb.getPrefix(), null);
        Assertion.equals(attrb.getLocalName(), "attrb");
        Assertion.equals(attrb.getName(), "attrb");
        Assertion.verify(attrb.getOwnerElement() == null);
        Assertion.equals(attrc.getNodeName(), "attrc");
        Assertion.verify(attrc.getNamespaceURI() == null);
        Assertion.verify(attrc.getPrefix() == null);
        Assertion.equals(attrc.getLocalName(), "attrc");
        Assertion.equals(attrc.getName(), "attrc");
        Assertion.verify(attrc.getOwnerElement() == null);
        Assertion.verify(DOMExceptionsTest(doc, "createAttributeNS",
				      new Class[]{String.class, String.class},
				      new Object[]{"http://nsa", "<a"},
				      DOMException.INVALID_CHARACTER_ERR));
	Assertion.verify(DOMExceptionsTest(doc, "createAttributeNS",
				      new Class[]{String.class, String.class},
				      new Object[]{"http://nsa", ":a"},
				      DOMException.NAMESPACE_ERR));
        Assertion.verify(DOMExceptionsTest(doc, "createAttributeNS",
				      new Class[]{String.class, String.class},
				      new Object[]{"http://nsa", "a:"},
				      DOMException.NAMESPACE_ERR));
        Assertion.verify(DOMExceptionsTest(doc, "createAttributeNS",
				      new Class[]{String.class, String.class},
				      new Object[]{"http://nsa", "a::a"},
				      DOMException.NAMESPACE_ERR));
        Assertion.verify(DOMExceptionsTest(doc, "createAttributeNS",
				      new Class[]{String.class, String.class},
				      new Object[]{"http://nsa", "a:a:a"},
				      DOMException.NAMESPACE_ERR));
        String xmlURI = "http://www.w3.org/XML/1998/namespace";
        Assertion.equals(doc.createAttributeNS(xmlURI, "xml:a").getNamespaceURI(), xmlURI);
        Assertion.verify(DOMExceptionsTest(doc, "createAttributeNS",
				      new Class[]{String.class, String.class},
				      new Object[]{"http://nsa", "xml:a"},
				      DOMException.NAMESPACE_ERR));
        Assertion.verify(DOMExceptionsTest(doc, "createAttributeNS",
				      new Class[]{String.class, String.class},
				      new Object[]{"", "xml:a"},
				      DOMException.NAMESPACE_ERR));
        Assertion.verify(DOMExceptionsTest(doc, "createAttributeNS",
				      new Class[]{String.class, String.class},
				      new Object[]{null,  "xml:a"},
				      DOMException.NAMESPACE_ERR));
        String xmlnsURI = "http://www.w3.org/2000/xmlns/";
        Assertion.equals(doc.createAttributeNS(xmlnsURI, "xmlns").getNamespaceURI(), xmlnsURI);
        Assertion.verify(DOMExceptionsTest(doc, "createAttributeNS",
				      new Class[]{String.class, String.class},
				      new Object[]{"http://nsa", "xmlns"},
				      DOMException.NAMESPACE_ERR));
        Assertion.verify(DOMExceptionsTest(doc, "createAttributeNS",
				      new Class[]{String.class, String.class},
				      new Object[]{xmlURI, "xmlns"},
				      DOMException.NAMESPACE_ERR));
        Assertion.verify(DOMExceptionsTest(doc, "createAttributeNS",
				      new Class[]{String.class, String.class},
				      new Object[]{"", "xmlns"},
				      DOMException.NAMESPACE_ERR));
        Assertion.verify(DOMExceptionsTest(doc, "createAttributeNS",
				      new Class[]{String.class, String.class},
				      new Object[]{null,  "xmlns"},
				      DOMException.NAMESPACE_ERR));
        Assertion.equals(doc.createAttributeNS(xmlnsURI, "xmlns:a").getNamespaceURI(), xmlnsURI);
        Assertion.verify(DOMExceptionsTest(doc, "createAttributeNS",
				      new Class[]{String.class, String.class},
				      new Object[]{"http://nsa", "xmlns:a"},
				      DOMException.NAMESPACE_ERR));
        Assertion.verify(DOMExceptionsTest(doc, "createAttributeNS",
				      new Class[]{String.class, String.class},
				      new Object[]{xmlURI, "xmlns:a"},
				      DOMException.NAMESPACE_ERR));
        Assertion.verify(DOMExceptionsTest(doc, "createAttributeNS",
				      new Class[]{String.class, String.class},
				      new Object[]{"", "xmlns:a"},
				      DOMException.NAMESPACE_ERR));
        Assertion.verify(DOMExceptionsTest(doc, "createAttributeNS",
				      new Class[]{String.class, String.class},
				      new Object[]{null,  "xmlns:a"},
				      DOMException.NAMESPACE_ERR));
        Assertion.equals(doc.createAttributeNS("http://nsa", "foo:a").getNamespaceURI(), "http://nsa");
        Assertion.verify(DOMExceptionsTest(doc, "createAttributeNS",
				      new Class[]{String.class, String.class},
				      new Object[]{null,  "foo:a"},
				      DOMException.NAMESPACE_ERR));
        Attr attr = doc.createAttributeNS("http://nsa", "foo:a");
        attr.setPrefix("bar");
        Assertion.equals(attr.getNodeName(), "bar:a");
        Assertion.equals(attr.getNamespaceURI(), "http://nsa");
        Assertion.equals(attr.getPrefix(), "bar");
        Assertion.equals(attr.getLocalName(), "a");
        Assertion.equals(attr.getName(), "bar:a");
        attr = doc.createAttributeNS("http://nsa", "a");
        Assertion.verify(attr.getPrefix() == null);
        attr.setPrefix("bar");
        Assertion.equals(attr.getNodeName(), "bar:a");
        Assertion.equals(attr.getNamespaceURI(), "http://nsa");
        Assertion.equals(attr.getPrefix(), "bar");
        Assertion.equals(attr.getLocalName(), "a");
        Assertion.equals(attr.getName(), "bar:a");
        attr = doc.createAttributeNS(xmlURI, "foo:a");
        attr.setPrefix("xml");
        attr = doc.createAttributeNS("http://nsa", "foo:a");
        Assertion.verify(DOMExceptionsTest(attr, "setPrefix",
					   new Class[]{String.class},
					   new Object[]{"xml"},
					   DOMException.NAMESPACE_ERR));
        attr = doc.createAttributeNS("http://nsa", "foo:a");
        Assertion.verify(DOMExceptionsTest(attr, "setPrefix",
					   new Class[]{String.class},
					   new Object[]{"xmlns"},
					   DOMException.NAMESPACE_ERR));
        attr = doc.createAttributeNS(xmlnsURI, "xmlns");
        Assertion.verify(DOMExceptionsTest(attr, "setPrefix",
					   new Class[]{String.class},
					   new Object[]{"xml"},
					   DOMException.NAMESPACE_ERR));
        attr = doc.createAttributeNS(null, "a");
        Assertion.verify(DOMExceptionsTest(attr, "setPrefix",
					   new Class[]{String.class},
					   new Object[]{"foo"},
					   DOMException.NAMESPACE_ERR));
        Assertion.verify(DOMExceptionsTest(attr, "setPrefix",
					   new Class[]{String.class},
					   new Object[]{"foo"},
					   DOMException.NAMESPACE_ERR));
    }
    {
        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
        String qName = "foo:docName";
        String pubId = "pubId";
        String sysId = "http://sysId";
        DocumentType dt = impl.createDocumentType(qName, pubId, sysId);
        String docNSURI = "http://document.namespace";
	Document doc = impl.createDocument(docNSURI, qName, dt);
        Element rootEl = doc.getDocumentElement();
        Element ela = doc.createElementNS("http://nsa", "a:ela");  
        rootEl.appendChild(ela);
        Element elb = doc.createElementNS("http://nsb", "elb");   
        rootEl.appendChild(elb);
        Element elc = doc.createElementNS(null,           "elc");  
        rootEl.appendChild(elc);
        Element eld = doc.createElementNS("http://nsa", "d:ela");
        rootEl.appendChild(eld);
        Element ele = doc.createElementNS("http://nse", "elb");   
        rootEl.appendChild(ele);
        NodeList nl = doc.getElementsByTagName("a:ela");
        Assertion.verify(nl.getLength() == 1);
        Assertion.verify(nl.item(0) == ela);
        nl = doc.getElementsByTagName("elb");
        Assertion.verify(nl.getLength() == 2);
        Assertion.verify(nl.item(0) == elb);
        Assertion.verify(nl.item(1) == ele);
        nl = doc.getElementsByTagName("d:ela");
        Assertion.verify(nl.getLength() == 1);
        Assertion.verify(nl.item(0) == eld);
        nl = doc.getElementsByTagNameNS(null, "elc");
        Assertion.verify(nl.getLength() == 1);
        Assertion.verify(nl.item(0) == elc);
        nl = doc.getElementsByTagNameNS("http://nsa", "ela");
        Assertion.verify(nl.getLength() == 2);
        Assertion.verify(nl.item(0) == ela);
        Assertion.verify(nl.item(1) == eld);
        nl = doc.getElementsByTagNameNS(null, "elb");
        Assertion.verify(nl.getLength() == 0);
        nl = doc.getElementsByTagNameNS("http://nsb", "elb");
        Assertion.verify(nl.getLength() == 1);
        Assertion.verify(nl.item(0) == elb);
        nl = doc.getElementsByTagNameNS("*", "elb");
        Assertion.verify(nl.getLength() == 2);
        Assertion.verify(nl.item(0) == elb);
        Assertion.verify(nl.item(1) == ele);
        nl = doc.getElementsByTagNameNS("http://nsa", "*");
        Assertion.verify(nl.getLength() == 2);
        Assertion.verify(nl.item(0) == ela);
        Assertion.verify(nl.item(1) == eld);
        nl = doc.getElementsByTagNameNS("*", "*");
        Assertion.verify(nl.getLength() == 6);     
        Assertion.verify(nl.item(6) == null);
        nl = rootEl.getElementsByTagNameNS("*", "*");
        Assertion.verify(nl.getLength() == 5);
        nl = doc.getElementsByTagNameNS("http://nsa", "d:ela");
        Assertion.verify(nl.getLength() == 0);
        nl = doc.getElementsByTagNameNS("*", "*");
        NodeList nla = ela.getElementsByTagNameNS("*", "*");
        Assertion.verify(nl.getLength() == 6); 
        Assertion.verify(nla.getLength() == 0);
        rootEl.removeChild(elc);
        Assertion.verify(nl.getLength() == 5);
        Assertion.verify(nla.getLength() == 0);
        ela.appendChild(elc);
        Assertion.verify(nl.getLength() == 6);
        Assertion.verify(nla.getLength() == 1);
    }
    {
        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
        String qName = "foo:docName";
        String pubId = "pubId";
        String sysId = "http://sysId";
        DocumentType dt = impl.createDocumentType(qName, pubId, sysId);
        String docNSURI = "http://document.namespace";
        Document doc = impl.createDocument(docNSURI, qName, dt);
        Element rootEl = doc.getDocumentElement();
        Attr attra = doc.createAttributeNS("http://nsa", "a:attra");  
        rootEl.setAttributeNodeNS(attra);
        Attr attrb = doc.createAttributeNS("http://nsb", "attrb");   
        rootEl.setAttributeNodeNS(attrb);
        Attr attrc = doc.createAttributeNS(null,           "attrc");  
        rootEl.setAttributeNodeNS(attrc);
        Attr attrd = doc.createAttributeNS("http://nsa", "d:attra");
        rootEl.setAttributeNodeNS(attrd);
        Attr attre = doc.createAttributeNS("http://nse", "attrb");   
        rootEl.setAttributeNodeNS(attre);
        Assertion.equals(attra.getNodeName(), "a:attra");
        Assertion.equals(attra.getNamespaceURI(), "http://nsa");
        Assertion.equals(attra.getLocalName(), "attra");
        Assertion.equals(attra.getName(), "a:attra");
        Assertion.verify(attra.getNodeType() == Node.ATTRIBUTE_NODE);
        Assertion.equals(attra.getNodeValue(), "");
        Assertion.equals(attra.getPrefix(), "a");
        Assertion.verify(attra.getSpecified() == true);
        Assertion.equals(attra.getValue(), "");
        Assertion.verify(attra.getOwnerElement() == null);
        NamedNodeMap nnm = rootEl.getAttributes();
        Assertion.verify(nnm.getLength() == 4);
        Assertion.verify(nnm.getNamedItemNS("http://nsa", "attra") == attrd);
        Assertion.verify(nnm.getNamedItemNS("http://nsb", "attrb") == attrb);
        Assertion.verify(nnm.getNamedItemNS("http://nse", "attrb") == attre);
        Assertion.verify(nnm.getNamedItemNS(null, "attrc") == attrc);
        Assertion.verify(nnm.getNamedItemNS(null, "attra") == null);
        Assertion.verify(nnm.getNamedItemNS("http://nsa", "attrb") == null);
    }
    {
        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
        DocumentType dt = impl.createDocumentType("foo", "PubId", "SysId");
        Document doc = impl.createDocument(null, "foo", dt);
        Assertion.verify(((NodeImpl) doc).getTextContent() == null);
        Assertion.verify(((NodeImpl) dt).getTextContent() == null);
        ((NodeImpl) doc).setTextContent("foo");
        ((NodeImpl) dt).setTextContent("foo");
        NodeImpl el = (NodeImpl) doc.getDocumentElement();
        Assertion.equals(((NodeImpl) el).getTextContent(), "");
        el.setTextContent("yo!");
        Node t = el.getFirstChild();
        Assertion.verify(t != null && t.getNodeType() == Node.TEXT_NODE &&
                         t.getNodeValue().equals("yo!"));
        Assertion.equals(el.getTextContent(), "yo!");
        Comment c = doc.createComment("dummy");
        el.appendChild(c);
        NodeImpl el2 = (NodeImpl) doc.createElement("bar");
        el2.setTextContent("bye now");
        el.appendChild(el2);
        Assertion.equals(el.getTextContent(), "yo!bye now");
		NodeImpl el3 = (NodeImpl) doc.createElement("test");
		el.appendChild(el3);
		NodeImpl empty = (NodeImpl) doc.createElement("empty");
		el3.appendChild(empty);
		Assertion.verify(el3.getTextContent() != null);
		empty.setTextContent("hello");
		Assertion.verify(empty.getChildNodes().getLength() == 1);
		empty.setTextContent(null);
		Assertion.verify(empty.getChildNodes().getLength() == 0);
		empty.setTextContent("");
		Assertion.verify(empty.getChildNodes().getLength() == 0);
        class MyHandler implements UserDataHandler {
            boolean fCalled;
            Node fNode;
            String fKey;
            Object fData;
            MyHandler(String key, Object data, Node node) {
                fCalled = false;
                fKey = key;
                fData = data;
                fNode = node;
            }
            public void handle(short operation, String key,
                               Object data, Node src, Node dst) {
                Assertion.verify(operation == UserDataHandler.NODE_CLONED);
                Assertion.verify(key == fKey && data == fData && src == fNode);
                Assertion.verify(dst != null &&
                                 dst.getNodeType() == fNode.getNodeType());
                fCalled = true;
            }
        }
        el.setUserData("mykey", c, null);
        el.setUserData("mykey2", el2, null);
        Assertion.verify(el.getUserData("mykey") == c);
        Assertion.verify(el.getUserData("mykey2") == el2);
        el.setUserData("mykey", null, null);
        Assertion.verify(el.getUserData("mykey") == null);
        el.setUserData("mykey2", null, null);
        Assertion.verify(el.getUserData("mykey2") == null);
        MyHandler h = new MyHandler("mykey", c, el);
        el.setUserData("mykey", c, h);
        MyHandler h2 = new MyHandler("mykey2", el2, el);
        el.setUserData("mykey2", el2, h2);
        Node cl = el.cloneNode(false);
        Assertion.verify(h.fCalled == true);
        Assertion.verify(h2.fCalled == true);
        el.setTextContent("zapped!");
        Node t2 = el.getFirstChild();
        Assertion.verify(t2.getNodeValue().equals("zapped!"));
        Assertion.verify(t2.getNextSibling() == null);
    }
    {
        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
        Document doc = impl.createDocument(null, "root", null);
        NodeImpl root = (NodeImpl) doc.getDocumentElement();
        NodeImpl n1 = (NodeImpl) doc.createElement("el");
        n1.setTextContent("yo!");
        NodeImpl n2 = (NodeImpl) doc.createElement("el");
        n2.setTextContent("yo!");
        Assertion.verify(n1.isEqualNode(n2) == true);
        n2.setTextContent("yoyo!");
        Assertion.verify(n1.isEqualNode(n2) == false);
        n1.setTextContent("yoyo!");
        ((Element) n1).setAttribute("a1", "v1");
        ((Element) n1).setAttributeNS("uri", "a2", "v2");
        ((Element) n2).setAttribute("a1", "v1");
        ((Element) n2).setAttributeNS("uri", "a2", "v2");
        Assertion.verify(n1.isEqualNode(n2) == true);
        Element elem = doc.createElementNS(null, "e2");
        root.appendChild(elem);
        Attr attr = doc.createAttributeNS("http://attr", "attr1");
        elem.setAttributeNode(attr);
        elem.setAttributeNS("http://attr","p:attr1","v2");
        Attr attr2 = elem.getAttributeNodeNS("http://attr", "attr1");
        Assertion.verify(attr2.getNodeName().equals("p:attr1"), "p:attr1");
        Assertion.verify(attr2.getNodeValue().equals("v2"), "value v2");
        elem.setAttributeNS("http://attr","attr1","v2");
        attr2 = elem.getAttributeNodeNS("http://attr", "attr1");
        Assertion.verify(attr2.getNodeName().equals("attr1"), "attr1");
        ((Element) n2).setAttribute("a1", "v2");
        Assertion.verify(n1.isEqualNode(n2) == false);
        root.appendChild(n1);
        root.appendChild(n2);
        NodeImpl clone = (NodeImpl) root.cloneNode(true);
        Assertion.verify(clone.isEqualNode(root) == true);
    }
    System.out.println("done.");
    };
}    
