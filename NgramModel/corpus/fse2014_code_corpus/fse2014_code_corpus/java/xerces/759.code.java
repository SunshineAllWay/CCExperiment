package dom;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Notation;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import dom.util.Assertion;
public class DTest {
	public static Element 		testElementNode;
	public static Attr 		testAttributeNode;
	public static Text 		testTextNode;
	public static CDATASection 	testCDATASectionNode;
	public static EntityReference 	testEntityReferenceNode;
	public static Entity 		testEntityNode;
	public static ProcessingInstruction testProcessingInstructionNode;
	public static Comment 		testCommentNode;
	public static Document 		testDocumentNode;
	public static DocumentType 	testDocumentTypeNode;
	public static DocumentFragment 	testDocumentFragmentNode;
	public static Notation 		testNotationNode;
public DTest() {
	super();
}
public Document createDocument() {
	return new org.apache.xerces.dom.DocumentImpl();	
}
public DocumentType createDocumentType(Document doc, String name) {
	return ((org.apache.xerces.dom.DocumentImpl) doc).createDocumentType(name, null, null);	
}
public Entity createEntity(Document doc, String name) {
	return new org.apache.xerces.dom.EntityImpl((org.apache.xerces.dom.DocumentImpl)doc, name);	
}
public Notation createNotation(Document doc, String name) {
	return new org.apache.xerces.dom.NotationImpl((org.apache.xerces.dom.DocumentImpl) doc, name);	
}
public void docBuilder(org.w3c.dom.Document document, String name)
{
	Document doc = document;
	boolean OK = true;
	Element docFirstElement = doc.createElement(name + "FirstElement");
	doc.appendChild(docFirstElement);
	docFirstElement.setAttribute(name + "FirstElement", name + "firstElement");
	ProcessingInstruction docProcessingInstruction = doc.createProcessingInstruction(name +
					"TargetProcessorChannel", "This is " + doc + "'s processing instruction");
	docFirstElement.appendChild(docProcessingInstruction);
	Element docBody = doc.createElement(name + "TestBody");
	docFirstElement.appendChild(docBody);
	Element docBodyLevel21 = doc.createElement(name + "BodyLevel21");
	Element docBodyLevel22 = doc.createElement(name + "BodyLevel22");
	Element docBodyLevel23 = doc.createElement(name + "BodyLevel23");
	Element docBodyLevel24 = doc.createElement(name + "BodyLevel24");
	docBody.appendChild(docBodyLevel21);
	docBody.appendChild(docBodyLevel22);
	docBody.appendChild(docBodyLevel23);
	docBody.appendChild(docBodyLevel24);
	Element docBodyLevel31 = doc.createElement(name + "BodyLevel31");
	Element docBodyLevel32 = doc.createElement(name + "BodyLevel32");
	Element docBodyLevel33 = doc.createElement(name + "BodyLevel33");
	Element docBodyLevel34 = doc.createElement(name + "BodyLevel34");
	docBodyLevel21.appendChild(docBodyLevel31);
	docBodyLevel21.appendChild(docBodyLevel32);
	docBodyLevel22.appendChild(docBodyLevel33);
	docBodyLevel22.appendChild(docBodyLevel34);
	Text docTextNode11 = doc.createTextNode(name + "BodyLevel31'sChildTextNode11");
	Text docTextNode12 = doc.createTextNode(name + "BodyLevel31'sChildTextNode12");
	Text docTextNode13 = doc.createTextNode(name + "BodyLevel31'sChildTextNode13");
	Text docTextNode2 = doc.createTextNode(name + "TextNode2");
	Text docTextNode3 = doc.createTextNode(name + "TextNode3");
	Text docTextNode4 = doc.createTextNode(name + "TextNode4");
	docBodyLevel31.appendChild(docTextNode11);
	docBodyLevel31.appendChild(docTextNode12);
	docBodyLevel31.appendChild(docTextNode13);
	docBodyLevel32.appendChild(docTextNode2);
	docBodyLevel33.appendChild(docTextNode3);
	docBodyLevel34.appendChild(docTextNode4);
	CDATASection docCDATASection = doc.createCDATASection("<![CDATA[<greeting>Hello, world!</greeting>]]>");
	docBodyLevel23.appendChild(docCDATASection);
	Comment docComment = doc.createComment("This should be a comment of some kind ");
	docBodyLevel23.appendChild(docComment);
	EntityReference docReferenceEntity = doc.createEntityReference("ourEntityNode");
	docBodyLevel24.appendChild(docReferenceEntity);
	DTest make = new DTest();
	Notation docNotation = make.createNotation(doc, "ourNotationNode");
	DocumentType docType = (DocumentType)doc.getFirstChild();
	docType.getNotations().setNamedItem(docNotation);
	DocumentFragment docDocFragment = doc.createDocumentFragment();
	Text docNode3 = doc.createTextNode(name + "docTextNode3");
	Text docNode4 = doc.createTextNode(name + "docTextNode4");
	Entity docEntity = (Entity) doc.getDoctype().getEntities().getNamedItem("ourEntityNode"); 
	DocumentType docDocType = (DocumentType) doc.getFirstChild();	
	EntityReference entityReferenceText = (EntityReference) doc.getLastChild().getLastChild().getLastChild().getFirstChild();
	Text entityReferenceText2 = doc.createTextNode("entityReferenceText information");
	DTest tests = new DTest();
	OK &= Assertion.verify(DTest.DOMExceptionsTest(document, "appendChild", new Class[]{Node.class}, new Object[]{docBody}, DOMException.HIERARCHY_REQUEST_ERR )); 
	OK &= Assertion.verify(DTest.DOMExceptionsTest(docNode3, "appendChild", new Class[]{Node.class}, new Object[]{docNode4}, DOMException.HIERARCHY_REQUEST_ERR )); 
	OK &= Assertion.verify(DTest.DOMExceptionsTest(doc, "insertBefore", new Class[]{Node.class, Node.class}, new Object[]{docEntity, docFirstElement}, DOMException.HIERARCHY_REQUEST_ERR )); 
	OK &= Assertion.verify(DTest.DOMExceptionsTest(doc, "replaceChild", new Class[]{Node.class, Node.class}, new Object[]{docCDATASection, docFirstElement}, DOMException.HIERARCHY_REQUEST_ERR )); 
        docFirstElement.setNodeValue("This shouldn't do anything!");
	OK &= Assertion.verify(docFirstElement.getNodeValue() == null);
        docReferenceEntity.setNodeValue("This shouldn't do anything!");
	OK &= Assertion.verify(docReferenceEntity.getNodeValue() == null);
        docEntity.setNodeValue("This shouldn't do anything!");
	OK &= Assertion.verify(docEntity.getNodeValue() == null);
        doc.setNodeValue("This shouldn't do anything!");
	OK &= Assertion.verify(doc.getNodeValue() == null);
        docType.setNodeValue("This shouldn't do anything!");
	OK &= Assertion.verify(docType.getNodeValue() == null);
        docDocFragment.setNodeValue("This shouldn't do anything!");
	OK &= Assertion.verify(docDocFragment.getNodeValue() == null);
        docNotation.setNodeValue("This shouldn't do anything!");
	OK &= Assertion.verify(docNotation.getNodeValue() == null);
	OK &= Assertion.verify(DTest.DOMExceptionsTest(docReferenceEntity, "appendChild", new Class[]{Node.class}, new Object[]{entityReferenceText2 }, DOMException.NO_MODIFICATION_ALLOWED_ERR ));
	OK &= Assertion.verify(DTest.DOMExceptionsTest(docBodyLevel32, "insertBefore", new Class[]{Node.class, Node.class}, new Object[]{docTextNode11,docBody }, DOMException.NOT_FOUND_ERR ));
	OK &= Assertion.verify(DTest.DOMExceptionsTest(docBodyLevel32, "removeChild", new Class[]{Node.class}, new Object[]{docFirstElement}, DOMException.NOT_FOUND_ERR ));
	OK &= Assertion.verify(DTest.DOMExceptionsTest(docBodyLevel32, "replaceChild", new Class[]{Node.class, Node.class}, new Object[]{docTextNode11,docFirstElement }, DOMException.NOT_FOUND_ERR ));
}
public static boolean DOMExceptionsTest(Object node, String methodName, Class[] methodSignature, Object[] parameters, short code) {
	boolean asExpected = false;
	Method method;
	try
	{
		method = node.getClass().getMethod(methodName,methodSignature);
		method.invoke(node, parameters);
	}catch(InvocationTargetException exc)
	{
		Throwable realE = exc.getTargetException(); 
		if(realE instanceof DOMException)
		{
			asExpected = (((DOMException)realE).code== code);
			if(!asExpected)
				System.out.println("Wrong DOMException(" + ((DOMException)realE).code + ")");
		}
		else
			System.out.println("Wrong Exception (" + code + ")");
		if(!asExpected)
		{
			System.out.println("Expected DOMException (" + code + ") not thrown");			
		}
	}catch(Exception exc)
	{
		System.out.println("test invocation failure (" + exc + ")");
	}
	return (asExpected);
}
public void findTestNodes(Document document) {
	Node node = document;
	int nodeCount = 0;
	while (node != null && nodeCount < 12)
	{
		switch (node.getNodeType())
	{
		case org.w3c.dom.Node.ELEMENT_NODE :
			if (testElementNode == null) {testElementNode = (Element)node; nodeCount++;}
			break;
		case org.w3c.dom.Node.ATTRIBUTE_NODE :
			if (testAttributeNode == null) {testAttributeNode = (Attr)node; nodeCount++;}
			break;
		case org.w3c.dom.Node.TEXT_NODE :
			if (testTextNode == null) {testTextNode = (Text)node; nodeCount++;}
			break;
		case org.w3c.dom.Node.CDATA_SECTION_NODE :
			if (testCDATASectionNode == null) {testCDATASectionNode = (CDATASection)node; nodeCount++;}
			break;
		case org.w3c.dom.Node.ENTITY_REFERENCE_NODE :
			if (testEntityReferenceNode == null) {testEntityReferenceNode = (EntityReference)node; nodeCount++;}
			break;
		case org.w3c.dom.Node.ENTITY_NODE :
			if (testEntityNode == null) {testEntityNode = (Entity)node; nodeCount++;}
			break;
		case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE :
			if (testProcessingInstructionNode == null) {testProcessingInstructionNode = (ProcessingInstruction)node; nodeCount++;}
			break;
		case org.w3c.dom.Node.COMMENT_NODE :
			if (testCommentNode == null) {testCommentNode = (Comment)node; nodeCount++;}
			break;
		case org.w3c.dom.Node.DOCUMENT_TYPE_NODE :
			if (testDocumentTypeNode == null) {testDocumentTypeNode = (DocumentType)node; nodeCount++;}
			break;
		case org.w3c.dom.Node.DOCUMENT_FRAGMENT_NODE :
			if (testDocumentFragmentNode == null) {testDocumentFragmentNode = (DocumentFragment)node; nodeCount++;}
			break;
		case org.w3c.dom.Node.NOTATION_NODE :
			if (testNotationNode == null) {testNotationNode = (Notation)node; nodeCount++;}
			break;
		case org.w3c.dom.Node.DOCUMENT_NODE :
			if (testDocumentNode == null) {testDocumentNode = (Document)node; nodeCount++;}
			break;
		default:
	}
	}
}
public void findTestNodes(Node node) {
	DTest test = new DTest();
	Node kid;
	if (node.getFirstChild() != null)
	{
		kid = node.getFirstChild();
		test.findTestNodes(kid);
	}
	if (node.getNextSibling() != null)
	{
		kid = node.getNextSibling();
		test.findTestNodes(kid);
	}
	switch (node.getNodeType())
	{
		case org.w3c.dom.Node.ELEMENT_NODE :
			if (testElementNode == null) {testElementNode = (Element)node; }
			break;
		case org.w3c.dom.Node.ATTRIBUTE_NODE :
			if (testAttributeNode == null) {testAttributeNode = (Attr)node; }
			break;
		case org.w3c.dom.Node.TEXT_NODE :
			if (testTextNode == null) {testTextNode = (Text)node; }
			break;
		case org.w3c.dom.Node.CDATA_SECTION_NODE :
			if (testCDATASectionNode == null) {testCDATASectionNode = (CDATASection)node; }
			break;
		case org.w3c.dom.Node.ENTITY_REFERENCE_NODE :
			if (testEntityReferenceNode == null) {testEntityReferenceNode = (EntityReference)node;}
			break;
		case org.w3c.dom.Node.ENTITY_NODE :
			if (testEntityNode == null) {testEntityNode = (Entity)node;}
			break;
		case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE :
			if (testProcessingInstructionNode == null) {testProcessingInstructionNode = (ProcessingInstruction)node;}
			break;
		case org.w3c.dom.Node.COMMENT_NODE :
			if (testCommentNode == null) {testCommentNode = (Comment)node;}
			break;
		case org.w3c.dom.Node.DOCUMENT_TYPE_NODE :
			if (testDocumentTypeNode == null) {testDocumentTypeNode = (DocumentType)node; }
			break;
		case org.w3c.dom.Node.DOCUMENT_FRAGMENT_NODE :
			if (testDocumentFragmentNode == null) {testDocumentFragmentNode = (DocumentFragment)node;}
			break;
		case org.w3c.dom.Node.NOTATION_NODE :
			if (testNotationNode == null) {testNotationNode = (Notation)node;}
			break;
		case org.w3c.dom.Node.DOCUMENT_NODE :
			if (testDocumentNode == null) {testDocumentNode = (Document)node;}
			break;
		default:
	}
}
public static void main(String args[]) {
    System.out.println("# main()");
	DTest test = new DTest();
	long avgTime = 0;
	boolean OK = true;
	long startTime = 0;
		startTime = System.currentTimeMillis();
	Document d = test.createDocument();
	DocumentType docDocType = test.createDocumentType(d,"testDocument1");
	d.appendChild(docDocType);
	Entity docEntity = test.createEntity( d, "ourEntityNode");
	Text entityChildText = d.createTextNode("entityChildText information"); 
        ((org.apache.xerces.dom.NodeImpl)docEntity).setReadOnly(false, true);
	docEntity.appendChild(entityChildText);					  
        ((org.apache.xerces.dom.NodeImpl)docEntity).setReadOnly(true, true);
	docDocType.getEntities().setNamedItem(docEntity);
	test.docBuilder(d, "d");
	test.findTestNodes((Node)d);
	try {
		test.testAttr(d);
		test.testCDATASection(d);
		test.testCharacterData(d);
		test.testChildNodeList(d);
		test.testComment(d);
		test.testDeepNodeList(d);
		test.testDocument(d);
		test.testDocumentFragment(d);
		test.testDocumentType(d);
		test.testDOMImplementation(d);
		test.testElement(d);
		test.testEntity(d);
		test.testEntityReference(d);
		test.testNode(d);
		test.testNotation(d);
		test.testPI(d);
		test.testText(d);
		test.testDOMerrors(d);
	} catch (Exception e) {
		System.out.println("Exception is: ");
		e.printStackTrace();
		OK = false;
	}
	avgTime += System.currentTimeMillis() - startTime;
}
public void testAttr(org.w3c.dom.Document document)
{
	Node node;	
	Attr attributeNode, attribute2;
	String compare;
	boolean T = true;
	boolean F = false;
	boolean OK = true;
	Attr testAttribute = document.createAttribute("testAttribute");
	testAttribute.setValue("testAttribute's value");
	node = document.getDocumentElement(); 
	((Element)node).setAttributeNode(testAttribute);
	attributeNode = ((Element)node).getAttributeNode("testAttribute");
	compare = "testAttribute";
	if (!compare.equals(attributeNode.getName()))
	{
		System.out.println("Warning!!! Attr's 'getName' method failed to work properly!");
		OK = false;
	}
	compare = "testAttribute's value";
	if (!compare.equals(attributeNode.getNodeValue()))
	{
		System.out.println("Warning!!! Attr's 'getNodeValue' method failed to work properly!");
		OK = false;
	}
	if (! T ==attributeNode.getSpecified())
	{
		System.out.println("Warning!!! Attr's 'getSpecified' method failed to work properly!");
		OK = false;
	}
	if (!compare.equals(attributeNode.getValue()))
	{
		System.out.println("Warning!!! Attr's 'getValue' method failed to work properly!");
		OK = false;
	}
	attributeNode.setNodeValue("Reset Value");
	compare = "Reset Value";
	if (!compare.equals(attributeNode.getNodeValue()))
	{
		System.out.println("Warning!!! Attr's 'setNodeValue' method failed to work properly!");
		OK = false;
	}
	((org.apache.xerces.dom.AttrImpl)attributeNode).setSpecified(F);
	if (! F ==attributeNode.getSpecified())
	{
		System.out.println("Warning!!! Attr's 'setSpecified' method failed to work properly!");
		OK = false;
	}
	attributeNode.setValue(null);
	if (attributeNode.getValue().length() != 0)
	{
		System.out.println("Warning!!! Attr's 'setValue' to 'null' method failed to work properly!");
		OK = false;
	}
	attributeNode.setValue("Another value ");
	compare = "Another value ";
	if (!compare.equals(attributeNode.getValue()))
	{
		System.out.println("Warning!!! Attr's 'setValue' method failed to work properly!");
		OK = false;
	}
	node = attributeNode.cloneNode(T);
	if (! (node.getNodeName().equals(attributeNode.getNodeName()) &&	     
	      (node.getNodeValue() != null && attributeNode.getNodeValue() != null)  
	    ?  node.getNodeValue().equals(attributeNode.getNodeValue()) 	     
	    : (node.getNodeValue() == null && attributeNode.getNodeValue() == null)))
		{	
			System.out.println("'cloneNode' did not clone the Attribute node correctly");
			OK = false;
		}
	DTest tests = new DTest();
        Assertion.verify(
          DTest.DOMExceptionsTest(document.getDocumentElement(),
                                  "appendChild",
                                  new Class[]{Node.class},
                                  new Object[]{attributeNode},
                                  DOMException.HIERARCHY_REQUEST_ERR));
	attribute2 = document.createAttribute("testAttribute2");
        Assertion.verify(
          DTest.DOMExceptionsTest(document.getDocumentElement(),
                                  "removeAttributeNode",
                                  new Class[]{Attr.class},
                                  new Object[]{attribute2},
                                  DOMException.NOT_FOUND_ERR));
        Element element = (Element)document.getLastChild().getLastChild();
        Assertion.verify(
          DTest.DOMExceptionsTest(element,
                                  "setAttributeNode",
                                  new Class[]{Attr.class},
                                  new Object[]{testAttribute},
                                  DOMException.INUSE_ATTRIBUTE_ERR));
	if (! OK)
		System.out.println("\n*****The Attr method calls listed above failed, all others worked correctly.*****");
}
public void testCDATASection(org.w3c.dom.Document document)
{
	Node node, node2;
	boolean T = true;
	boolean OK = true;
	node = document.getDocumentElement().getElementsByTagName("dBodyLevel23").item(0).getFirstChild(); 
	node2 = node.cloneNode(T);
	if (! (node.getNodeName().equals(node2.getNodeName()) && 		
	      (node.getNodeValue() != null && node2.getNodeValue() != null)     
	    ?  node.getNodeValue().equals(node2.getNodeValue()) 		
	    : (node.getNodeValue() == null && node2.getNodeValue() == null)))	
	{
		System.out.println("'cloneNode' did not clone the CDATASection node correctly");
		OK = false;
	}
	if (! OK)
		System.out.println("\n*****The CDATASection method calls listed above failed, all others worked correctly.*****");
}
public void testCharacterData(org.w3c.dom.Document document)
{
	CharacterData charData;
	String compareData, newData, resetData;
	boolean OK = true;
	charData = (CharacterData) document.getDocumentElement().getElementsByTagName("dBodyLevel31").item(0).getFirstChild(); 
	compareData = "dBodyLevel31'sChildTextNode11";
	if (!compareData.equals(charData.getData()))
	{
		System.out.println("Warning!!! CharacterData's 'getData' failed to work properly!\n This may corrupt other CharacterData tests!!!*****");
		OK = false;
	}	
	resetData = charData.getData();
	newData = " This is new data for this node";
	compareData = charData.getData() + newData;
	charData.appendData(newData);
	if (!compareData.equals(charData.getData()))
	{
		System.out.println("Warning!!! CharacterData's 'appendData' failed to work properly!");
		OK = false;
	}
	compareData = "dBodyLevel";
	charData.deleteData(10, 100);
	if (!compareData.equals(charData.getData()))
	{
		System.out.println("Warning!!! CharacterData's 'deleteData' failed to work properly!");
		OK = false;
	}
	int length = 10;
	if (!(length == charData.getLength()))
	{
		System.out.println("Warning!!! CharacterData's 'getLength' failed to work properly!");
		OK = false;
	}
	compareData = "dBody' This is data inserted into this node'Level";
	charData.insertData(5, "' This is data inserted into this node'");
	if (!compareData.equals(charData.getData()))
	{
		System.out.println("Warning!!! CharacterData's 'insertData' failed to work properly!");
		OK = false;
	}
	compareData = "dBody' This is ' replacement data'ted into this node'Level";
	charData.replaceData(15, 10, "' replacement data'");
	if (!compareData.equals(charData.getData()))
	{
		System.out.println("Warning!!! CharacterData's 'replaceData' failed to work properly!");
		OK = false;
	}
	compareData = "New data A123456789B123456789C123456789D123456789E123456789";
	charData.setData("New data A123456789B123456789C123456789D123456789E123456789");
	if (!compareData.equals(charData.getData()))
	{
		System.out.println("Warning!!! CharacterData's 'setData' failed to work properly!");
		OK = false;
	}
	compareData = "123456789D123456789E123456789";
	if (!compareData.equals(charData.substringData(30, 30)))
	{
		System.out.println("Warning!!! CharacterData's 'substringData' failed to work properly!");
		OK = false;
	}
	compareData = "New data A123456789B12345";
	if (!compareData.equals(charData.substringData(0, 25)))
	{
		System.out.println("Warning!!! CharacterData's 'substringData' failed to work properly!");
		OK = false;
	}
	DTest tests = new DTest();
	OK &= Assertion.verify(DTest.DOMExceptionsTest(charData, "deleteData", new Class[]{int.class, int.class}, 
			new Object[]{new Integer(-1),new Integer(5) }, DOMException.INDEX_SIZE_ERR ));
	OK &= Assertion.verify(DTest.DOMExceptionsTest(charData, "deleteData", new Class[]{int.class, int.class}, 
			new Object[]{new Integer(2),new Integer(-1) }, DOMException.INDEX_SIZE_ERR ));
	OK &= Assertion.verify(DTest.DOMExceptionsTest(charData, "deleteData", new Class[]{int.class, int.class}, 
			new Object[]{new Integer(100),new Integer(5) }, DOMException.INDEX_SIZE_ERR ));
	OK &= Assertion.verify(DTest.DOMExceptionsTest(charData, "insertData", new Class[]{int.class, String.class}, 
			new Object[]{new Integer(-1),"Stuff inserted" }, DOMException.INDEX_SIZE_ERR ));
	OK &= Assertion.verify(DTest.DOMExceptionsTest(charData, "insertData", new Class[]{int.class, String.class}, 
			new Object[]{new Integer(100),"Stuff inserted" }, DOMException.INDEX_SIZE_ERR ));
	OK &= Assertion.verify(DTest.DOMExceptionsTest(charData, "replaceData", new Class[]{int.class, int.class, String.class}, 
			new Object[]{new Integer(-1),new Integer(5),"Replacement stuff" }, DOMException.INDEX_SIZE_ERR ));
	OK &= Assertion.verify(DTest.DOMExceptionsTest(charData, "replaceData", new Class[]{int.class, int.class, String.class}, 
			new Object[]{new Integer(100),new Integer(5),"Replacement stuff" }, DOMException.INDEX_SIZE_ERR ));
	OK &= Assertion.verify(DTest.DOMExceptionsTest(charData, "replaceData", new Class[]{int.class, int.class, String.class}, 
			new Object[]{new Integer(2),new Integer(-1),"Replacement stuff" }, DOMException.INDEX_SIZE_ERR ));
	OK &= Assertion.verify(DTest.DOMExceptionsTest(charData, "substringData", new Class[]{int.class, int.class}, 
			new Object[]{new Integer(-1),new Integer(5) }, DOMException.INDEX_SIZE_ERR ));
	OK &= Assertion.verify(DTest.DOMExceptionsTest(charData, "substringData", new Class[]{int.class, int.class}, 
			new Object[]{new Integer(100),new Integer(5) }, DOMException.INDEX_SIZE_ERR ));
	OK &= Assertion.verify(DTest.DOMExceptionsTest(charData, "substringData", new Class[]{int.class, int.class}, 
			new Object[]{new Integer(2),new Integer(-1) }, DOMException.INDEX_SIZE_ERR ));
	Node node = document.getDocumentElement().getElementsByTagName("dBodyLevel24").item(0).getFirstChild().getChildNodes().item(0); 
	OK &= Assertion.verify(DTest.DOMExceptionsTest(node, "appendData", new Class[]{String.class}, 
			new Object[]{"new data" }, DOMException.NO_MODIFICATION_ALLOWED_ERR ));
	OK &= Assertion.verify(DTest.DOMExceptionsTest(node, "deleteData", new Class[]{int.class, int.class}, 
			new Object[]{new Integer(5),new Integer(10) }, DOMException.NO_MODIFICATION_ALLOWED_ERR ));
	OK &= Assertion.verify(DTest.DOMExceptionsTest(node, "insertData", new Class[]{int.class, String.class}, 
			new Object[]{new Integer(5),"Stuff inserted" }, DOMException.NO_MODIFICATION_ALLOWED_ERR ));
	OK &= Assertion.verify(DTest.DOMExceptionsTest(node, "replaceData", new Class[]{int.class, int.class, String.class}, 
			new Object[]{new Integer(5),new Integer(10),"Replacementstuff" }, DOMException.NO_MODIFICATION_ALLOWED_ERR ));
	OK &= Assertion.verify(DTest.DOMExceptionsTest(node, "setData", new Class[]{String.class}, 
			new Object[]{"New setdata stuff"}, DOMException.NO_MODIFICATION_ALLOWED_ERR ));
	if (!OK)
		System.out.println("\n*****The CharacterData method calls listed above failed, all others worked correctly.*****");
	charData.setData(resetData); 
}
public void testChildNodeList(org.w3c.dom.Document document)
{
	Node node, node2;
	boolean OK = true;
	node = document.getDocumentElement().getLastChild(); 
	if (!(node.getChildNodes().getLength()== 4))
		OK = false;
	node2 = node.getChildNodes().item(2);
	if (! node2.getNodeName().equals("dBodyLevel23"))
		OK = false;
	if (!OK)
		System.out.println("\n*****The ChildNodeList method calls listed above failed, all others worked correctly.*****");		
}
public void testComment(org.w3c.dom.Document document)
{
	Node node, node2;
	boolean T = true;
	boolean OK = true;
	node = document.getDocumentElement().getElementsByTagName("dBodyLevel31").item(0).getFirstChild(); 
	node2 = node.cloneNode(T);
	if (!(node.getNodeName().equals(node2.getNodeName()) && 		
	      (node.getNodeValue() != null && node2.getNodeValue() != null)     
	    ?  node.getNodeValue().equals(node2.getNodeValue()) 		
	    : (node.getNodeValue() == null && node2.getNodeValue() == null)))	
		OK = false;
	if (OK)
	if (!OK)
		System.out.println("\n*****The Comment method calls listed above failed, all others worked correctly.*****");
}
public void testDeepNodeList(org.w3c.dom.Document document)
{
	Node node, node2;
	boolean OK = true;
	node = document.getLastChild().getLastChild(); 
	if (!(8 == ((Element) node).getElementsByTagName("*").getLength()))
		{
			System.out.println ("Warning!!! DeepNodeList's 'getLength' failed to work properly!");
			OK = false;		
		}
	node2 = ((Element) node).getElementsByTagName("*").item(2); 
	if (! node2.getNodeName().equals("dBodyLevel32"))
		{
			System.out.println ("Warning!!! DeepNodeList's 'item' (or Element's 'getElementsBy TagName)failed to work properly!");
			OK = false;		
		}
	node2 = document.getLastChild();
	if (! ((Element) node2).getElementsByTagName("dTestBody").item(0).getNodeName().equals("dTestBody"))
		{
			System.out.println ("Warning!!! DeepNodeList's 'item' (or Element's 'getElementsBy TagName)failed to work properly!");
			OK = false;		
		}
	if (!OK)
		System.out.println("\n*****The DeepNodeList method calls listed above failed, all others worked correctly.*****");
}
public void testDocument(org.w3c.dom.Document document)
{
	DTest make = new DTest();
	DocumentFragment docFragment, docFragment2;
	Element newElement;
	Node node, node2;
	String[] elementNames =  {"dFirstElement", "dTestBody", "dBodyLevel21","dBodyLevel31","dBodyLevel32",
				   "dBodyLevel22","dBodyLevel33","dBodyLevel34","dBodyLevel23","dBodyLevel24"};
	String[] newElementNames = {"dFirstElement", "dTestBody", "dBodyLevel22","dBodyLevel33","dBodyLevel34","dBodyLevel23"};
	boolean result;
	boolean OK = true;
	DocumentType checkDocType =  make.createDocumentType(document,"testDocument1");
	DocumentType docType = document.getDoctype();
	if (! (checkDocType.getNodeName().equals(docType.getNodeName()) && 		
	      (checkDocType.getNodeValue() != null && docType.getNodeValue() != null)   
	    ?  checkDocType.getNodeValue().equals(docType.getNodeValue()) 		
	    : (checkDocType.getNodeValue() == null && docType.getNodeValue() == null)))	
	{
		System.out.println("Warning!!! Document's 'getDocType method failed!" );
		OK = false;
	}
	Node rootElement = document.getLastChild();
	if (! (rootElement.getNodeName().equals(document.getDocumentElement().getNodeName()) && 		
	      (rootElement.getNodeValue() != null && document.getDocumentElement().getNodeValue() != null)   
	    ?  rootElement.getNodeValue().equals(document.getDocumentElement().getNodeValue()) 		
	    : (rootElement.getNodeValue() == null && document.getDocumentElement().getNodeValue() == null)))	
	{
		System.out.println("Warning!!! Document's 'getDocumentElement' method failed!" );
		OK = false;
	}
	NodeList docElements = document.getElementsByTagName("*");
	int docSize = docElements.getLength();
	int i;
	for (i = 0; i < docSize; i++)
	{
		Node n = (Node) docElements.item(i);
		if (! (elementNames[i].equals(n.getNodeName())))
		{
			System.out.println("Comparison of this document's elements failed at element number " + i + " : " + n.getNodeName());
			OK = false;
			break;
		}
	}
	if (document.equals(document.getImplementation()))
	{
		System.out.println("Warning!!! Document's 'getImplementation' method failed!" );
		OK = false;		
	}
	newElement = document.createElement("NewElementTestsInsertBefore");
	docFragment = document.createDocumentFragment();
	docFragment.appendChild(docElements.item(1).removeChild(docElements.item(9)));
	docFragment2 = document.createDocumentFragment();
	docFragment2.appendChild(docElements.item(1).removeChild(docElements.item(2)));
	docSize = docElements.getLength();
	for (i = 0; i < docSize; i++)
	{
		Node n = (Node) docElements.item(i);
		if (! (newElementNames[i].equals(n.getNodeName())))
		{
			System.out.println("Comparison of new document's elements failed at element number " + i + " : " + n.getNodeName());
			OK = false;
			break;
		}
	}
	docElements.item(1).insertBefore(docFragment, null); 
	docElements.item(1).insertBefore(docFragment2, docElements.item(2)); 
	docSize = docElements.getLength();
	for (i = 0; i < docSize; i++)
	{
		Node n = (Node) docElements.item(i);
		if (! (elementNames[i].equals(n.getNodeName())))
		{
			System.out.println("Comparison of restored document's elements failed at element number " + i + " : " + n.getNodeName());
			OK = false;
			break;
		}
	}
	DTest tests = new DTest();
	node = document;
	node2 = document.cloneNode(true);
	result = treeCompare(node, node2); 
	if (!result)
	{
		System.out.println("Warning!!! Deep clone of the document failed!");
		OK = false;
	}
	Document doc2 = (Document) node2;
	Assertion.verify(doc2.getDocumentElement().getOwnerDocument() == doc2);
	node2 = doc2.createElement("foo");
	doc2.getDocumentElement().appendChild(node2);
	if (!OK)
		System.out.println("\n*****The Document method calls listed above failed, all others worked correctly.*****");
}
public void testDocumentFragment(org.w3c.dom.Document document)
{
	boolean OK = true;
	DocumentFragment testDocFragment = document.createDocumentFragment();
	if (!OK)
		System.out.println("\n*****The DocumentFragment method calls listed above failed, all others worked correctly.*****");
}
public void testDocumentType(org.w3c.dom.Document document)
{
	DTest test = new DTest();
	DocumentType docType, holdDocType;
	NamedNodeMap docEntityMap, docNotationMap;
	Node node, node2;
	String compare;
	boolean OK = true;
	DocumentType newDocumentType =  test.createDocumentType(document, "TestDocument");
	node = document.getFirstChild(); 
	node2 = node.cloneNode(true);
	if (! (node.getNodeName().equals(node2.getNodeName()) && 	     
	      (node.getNodeValue() != null && node2.getNodeValue() != null)  
	    ?  node.getNodeValue().equals(node2.getNodeValue()) 	     
	    : (node.getNodeValue() == null && node2.getNodeValue() == null)))
	{	
		System.out.println("'cloneNode' did not clone the DocumentType node correctly");
		OK = false;
	}
	docType = (DocumentType) document.getFirstChild();
	compare = "ourEntityNode";
	docEntityMap = docType.getEntities();
	if (! compare.equals(docEntityMap.item(0).getNodeName()))
	{
		System.out.println("Warning!!! DocumentType's 'getEntities' failed!" );
		OK = false;
	}
	docNotationMap = docType.getNotations();
	compare = "ourNotationNode";
	if (! compare.equals(docNotationMap.item(0).getNodeName()))
	{
		System.out.println("Warning!!! DocumentType's 'getNotations' failed!");
		OK = false;
	}
	holdDocType = (DocumentType) document.removeChild(document.getFirstChild()); 
	document.insertBefore(newDocumentType, document.getDocumentElement());
	document.removeChild(document.getFirstChild()); 
	document.insertBefore(holdDocType, document.getFirstChild()); 
	if (!OK)
		System.out.println("\n*****The DocumentType method calls listed above failed, all others worked correctly.*****");
}
public void testDOMerrors(Document document) {
	boolean OK = true;
	DTest tests = new DTest();
	OK &= Assertion.verify(DTest.DOMExceptionsTest(document, "appendChild", new Class[]{Node.class}, new Object[]{testElementNode}, DOMException.HIERARCHY_REQUEST_ERR )); 
	OK &= Assertion.verify(DTest.DOMExceptionsTest(testTextNode, "appendChild", new Class[]{Node.class}, new Object[]{testTextNode}, DOMException.HIERARCHY_REQUEST_ERR )); 
}
public void testDOMImplementation(org.w3c.dom.Document document)
{
	DOMImplementation implementation;
	boolean result = false;
	boolean OK = true;
	implementation = document.getImplementation(); 
	result = implementation.hasFeature("XML", "1.0");
	if(!result)
	{
		System.out.println("Warning!!! DOMImplementation's 'hasFeature' that should be 'true' failed!");
		OK = false;
	}
	result = implementation.hasFeature("HTML", "4.0");
	if(result)
	{
		System.out.println("Warning!!! DOMImplementation's 'hasFeature' that should be 'false' failed!");
		OK = false;
	}
	if (!OK)
		System.out.println("\n*****The DOMImplementation method calls listed above failed, all others worked correctly.*****");
}
public void testElement(org.w3c.dom.Document document)
{
	Attr attributeNode, newAttributeNode;
	Element element, element2;
	Node node, node2;
	String attribute, compare;
	String[] attributeCompare = {"AnotherFirstElementAttribute", "dFirstElement", "testAttribute"};
	String[] elementNames =  {"dFirstElement", "dTestBody", "dBodyLevel21","dBodyLevel31","dBodyLevel32",
				   "dBodyLevel22","dBodyLevel33","dBodyLevel34","dBodyLevel23","dBodyLevel24"};
	String[] textCompare = {"dBodyLevel31'sChildTextNode11", "dBodyLevel31'sChildTextNode12", "dBodyLevel31'sChildTextNode13"};
	NamedNodeMap nodeMap;
	boolean OK = true;
	node = document.getDocumentElement(); 
	node2 = node.cloneNode(true);
	if (!(node.getNodeName().equals(node2.getNodeName()) &&		    
	     (node.getNodeValue() != null && node2.getNodeValue() != null)  
	    ? node.getNodeValue().equals(node2.getNodeValue())  	    
	    :(node.getNodeValue() == null && node2.getNodeValue() == null)))
	{	
		System.out.println("'cloneNode' did not clone the Element node correctly");
		OK = false;
	}
	element = document.getDocumentElement(); 
	compare = "";
	attribute = element.getAttribute(document + "'s test attribute");
	if (! compare.equals(element.getAttribute(document + "'s test attribute")))
	{
		System.out.println("Warning!!! Element's 'getAttribute' failed!");
		OK = false;
	}
	attributeNode = element.getAttributeNode(document + "FirstElement");
	if(! (attributeNode == null))
	{
		System.out.println("Warning!!! Element's 'getAttributeNode' failed! It should have returned 'null' here!");
		OK = false;
	}
	newAttributeNode = document.createAttribute("AnotherFirstElementAttribute");
	newAttributeNode.setValue("A new attribute which helps test calls in Element");
	element.setAttributeNode(newAttributeNode);
	nodeMap = element.getAttributes();
	int size = nodeMap.getLength();
	int k;
	for (k = 0; k < size; k++)
	{
		Node n = (Node) nodeMap.item(k);
		if (! (attributeCompare[k].equals(n.getNodeName())))
		{
			System.out.println("Warning!!! Comparison of firstElement's attributes failed at attribute #"+ (k+1) +" " + n.getNodeValue());
			System.out.println("This failure can be a result of Element's 'setValue' and/or 'setAttributeNode' and/or 'getAttributes' failing.");
			OK = false;
			break;
		}
	}
	NodeList docElements = document.getElementsByTagName("*");
	int docSize = docElements.getLength();
	int i;
	for (i = 0; i < docSize; i++)
	{
		Node n = (Node) docElements.item(i);
		if (! (elementNames[i].equals(n.getNodeName())))
		{
			System.out.println("Warning!!! Comparison of Element's 'getElementsByTagName' and/or 'item' failed at element number " 
						+ i + " : " + n.getNodeName());
			OK = false;
			break;
		}		
	}
	element = (Element) document.getElementsByTagName("dBodyLevel21").item(0); 
	element2 = (Element) document.getElementsByTagName("dBodyLevel31").item(0); 
	NodeList text = ((Node) element2).getChildNodes();
	int textSize = text.getLength();
	int j;
	for (j = 0; j < textSize; j++)
	{
		Node n = (Node) text.item(j);
		if (! (textCompare[j].equals(n.getNodeValue())))
		{
			System.out.println("Warning!!! Comparison of original text nodes via Node 'getChildNodes' & NodeList 'item'"
						+ "failed at text node: #" + j +" " + n.getNodeValue());
			OK = false;
			break;
		}
	}
	element = document.getDocumentElement(); 
	element.normalize();		
	NodeList text2 = ((Node) element2).getChildNodes();
	compare = "dBodyLevel31'sChildTextNode11dBodyLevel31'sChildTextNode12dBodyLevel31'sChildTextNode13";
	Node n = (Node) text2.item(0);
		if (! (compare.equals(n.getNodeValue())))
		{
			System.out.println("Warning!!! Comparison of concatenated text nodes created by Element's 'normalize' failed!");
			OK = false;
		}
	element.setAttribute("FirstElementLastAttribute", "More attribute stuff for firstElement!!");
	element.removeAttribute("FirstElementLastAttribute");
	element.removeAttributeNode(newAttributeNode);
	if (!OK)
		System.out.println("\n*****The Element method calls listed above failed, all others worked correctly.*****");
}
public void testEntity(org.w3c.dom.Document document)
{
	Entity entity;
	Node node, node2;
	boolean OK = true;
	String compare;
	entity = (Entity) document.getDoctype().getEntities().getNamedItem("ourEntityNode");
	node = entity;
	node2 = entity.cloneNode(true);
	if (!(node.getNodeName().equals(node2.getNodeName()) && 		
	     (node.getNodeValue() != null && node2.getNodeValue() != null) ?    
	      node.getNodeValue().equals(node2.getNodeValue()) :		
	     (node.getNodeValue() == null && node2.getNodeValue() == null)))	
	{	
		System.out.println("Warning!!! 'cloneNode' did not clone the Entity node correctly");
		OK = false;
	}
 	((org.apache.xerces.dom.EntityImpl) entity).setNotationName("testNotationName");
	compare = "testNotationName";
 	if(! compare.equals(entity.getNotationName()))
	{
		System.out.println("Warning!!! Entity's 'setNotationName' and/or getNotationName' failed!");
		OK = false;
	}
 	((org.apache.xerces.dom.EntityImpl) entity).setPublicId("testPublicId");
	compare = "testPublicId";
 	if(! compare.equals(entity.getPublicId()))
	{
		System.out.println("Warning!!! Entity's 'setPublicId' and/or getPublicId' failed!");
		OK = false;
	}	
 	((org.apache.xerces.dom.EntityImpl) entity).setSystemId("testSystemId");
	compare = "testSystemId";
 	if(! compare.equals(entity.getSystemId()))
	{
		System.out.println("Warning!!! Entity's 'setSystemId' and/or getSystemId' failed!");
		OK = false;
	}		
	if (!OK)
		System.out.println("\n*****The Entity method calls listed above failed, all others worked correctly.*****");
}
public void testEntityReference(org.w3c.dom.Document document)
{
	EntityReference entityReference;
	Node node, node2;
	boolean OK = true;
	entityReference = (EntityReference) document.getLastChild().getLastChild().getLastChild().getFirstChild();
	node = entityReference;
	node2 = node.cloneNode(true);
	if (!(node.getNodeName().equals(node2.getNodeName()) && 	    
	     (node.getNodeValue() != null && node2.getNodeValue() != null)  
	    ? node.getNodeValue().equals(node2.getNodeValue()) 		    
	    :(node.getNodeValue() == null && node2.getNodeValue() == null)))
	{	
		System.out.println("'cloneNode' did not clone the EntityReference node correctly");
		OK = false;
	}
	if (!OK)
		System.out.println("\n*****The EntityReference method calls listed above failed, all others worked correctly.*****");
}
public void testNode(org.w3c.dom.Document document)
{
	Node node, node2;
	boolean result;
	boolean OK = true;
	node = document.getDocumentElement();
	node2 = node.cloneNode(true);
	result = treeCompare(node, node2); 
	if (result)
	{
	}
	else
	{
		System.out.println("'cloneNode' did not successfully clone this whole node tree (deep)!");
		OK = false;	
	}
	node = document.getDocumentElement();
	node2 = node.getFirstChild();
	result = treeCompare(node, node2);
	if (!result)
	{
	}
	else
	{
		System.out.println("'cloneNode' was supposed to fail here, either it or 'treeCompare' failed!!!");
		OK = false;
	}
	if (!OK)
		System.out.println("\n*****The Node method calls listed above failed, all others worked correctly.*****");	
}
public void testNotation(org.w3c.dom.Document document)
{
	Node node, node2;
	Notation notation;
	boolean OK = true;
	String compare;
	notation = (Notation) document.getDoctype().getNotations().getNamedItem("ourNotationNode");
	node = notation;
	node2 = notation.cloneNode(true);
	if (!(node.getNodeName().equals(node2.getNodeName()) && 	    
	     (node.getNodeValue() != null && node2.getNodeValue() != null)  
	    ? node.getNodeValue().equals(node2.getNodeValue()) 		    
	    :(node.getNodeValue() == null && node2.getNodeValue() == null)))
	{	
		System.out.println("'cloneNode' did not clone the Notation node correctly");
		OK = false;
	}
 	((org.apache.xerces.dom.NotationImpl) notation).setPublicId("testPublicId");
	compare = "testPublicId";
	if (!compare.equals(notation.getPublicId()))
	{
		System.out.println("Warning!!! Notation's 'getPublicId' failed!");
		OK = false;
	}
 	((org.apache.xerces.dom.NotationImpl) notation).setSystemId("testSystemId");
	compare = "testSystemId";
	if (! compare.equals(notation.getSystemId()))
	{
		System.out.println("Warning!!! Notation's 'getSystemId' failed!");
		OK = false;
	}
	if (!OK)
		System.out.println("\n*****The Notation method calls listed above failed, all others worked correctly.*****");
}
public void testPI(org.w3c.dom.Document document)
{
	Node node, node2;
	ProcessingInstruction pI, pI2;
	String compare;
	boolean OK = true;
	pI = (ProcessingInstruction) document.getDocumentElement().getFirstChild();
	pI2 = (org.apache.xerces.dom.ProcessingInstructionImpl) pI.cloneNode(true);
	if (!(pI.getNodeName().equals(pI2.getNodeName()) && 		
	     (pI.getNodeValue() != null && pI2.getNodeValue() != null)  
	    ? pI.getNodeValue().equals(pI2.getNodeValue()) 		
	    :(pI.getNodeValue() == null && pI2.getNodeValue() == null)))
	{	
		System.out.println("'cloneNode' did not clone the Entity node correctly");
		OK = false;
	}
	compare = "This is [#document: null]'s processing instruction";
	if (! compare.equals(pI.getData()))
	{
		System.out.println("Warning!!! PI's 'getData' failed!");
		OK = false;
	}
	pI.setData("PI's reset data");
	compare = "PI's reset data";
	if (! compare.equals(pI.getData()))
	{
		System.out.println("Warning!!! PI's 'setData' failed!");
		OK = false;
	}	
	compare = "dTargetProcessorChannel";
	if (! compare.equals(pI.getTarget()))
	{
		System.out.println("Warning!!! PI's 'getTarget' failed!");
		OK = false;
	}	
	if (!OK)
		System.out.println("\n*****The PI method calls listed above failed, all others worked correctly.*****");
}
public void testText(org.w3c.dom.Document document)
{
	Node node, node2;
	Text text;
	String compare;
	boolean OK = true;
	node = document.getDocumentElement().getElementsByTagName("dBodyLevel31").item(0).getFirstChild(); 
	text = (Text) node;
	node2 = node.cloneNode(true);
	if (!(node.getNodeName().equals(node2.getNodeName()) && 	    
	     (node.getNodeValue() != null && node2.getNodeValue() != null)  
	    ? node.getNodeValue().equals(node2.getNodeValue()) 		    
	    :(node.getNodeValue() == null && node2.getNodeValue() == null)))
	{	
		System.out.println("'cloneNode' did not clone the Text node correctly");
		OK = false;
	}
	text.splitText(25);
	compare = "dBodyLevel31'sChildTextNo";	
	if (! compare.equals(text.getNodeValue()))
		{
			System.out.println("First part of Text's split text failed!" );
			OK = false;
		}
	compare = "de11dBodyLevel31'sChildTextNode12dBodyLevel31'sChildTextNode13";
	if (! compare.equals(text.getNextSibling().getNodeValue()))
		{
			System.out.println("The second part of Text's split text failed!") ;
			OK = false;	
		}
	DTest tests = new DTest();		
	if (!OK)
		System.out.println("\n*****The Text method calls listed above failed, all others worked correctly.*****");
}
public boolean treeCompare(Node node, Node node2)
{
	boolean answer = true;
	Node kid, kid2;			
	kid = node.getFirstChild();
	kid2 = node2.getFirstChild();
	if (kid != null && kid2 != null)
	{
		answer = treeCompare(kid, kid2);
		if (!answer)
			return answer;
		else
			if (kid.getNextSibling() != null && kid2.getNextSibling() != null)
			{
				while (kid.getNextSibling() != null && kid2.getNextSibling() != null)
				{
					answer = treeCompare(kid.getNextSibling(), kid2.getNextSibling());
					if (!answer)
						return answer;
					else
					{
						kid = kid.getNextSibling();
						kid2 = kid2.getNextSibling();
					}
				}
			} else
				if (!(kid.getNextSibling() == null && kid2.getNextSibling() == null))
				{
					return false;
				}
	} else
		if (kid != kid2)
		{
			return false;
		}
	if (!(node.getNodeName().equals(node2.getNodeName()) &&		    
	     (node.getNodeValue() != null && node2.getNodeValue() != null)  
	    ? node.getNodeValue().equals(node2.getNodeValue()) 		    
	    :(node.getNodeValue() == null && node2.getNodeValue() == null)))
	{
		return false;	
	}
	return answer;
}
}
