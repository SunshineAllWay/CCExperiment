import java.io.OutputStreamWriter;
import javax.xml.namespace.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import org.xml.sax.*;
import org.w3c.dom.*;
public class ApplyXPathJAXP
{
    public static void main(String[] args)
    {
    	QName returnType = null;
        if (args.length != 3)
        {
            System.err.println("Usage: java ApplyXPathAPI xml_file xpath_expression type");
        }
        InputSource xml = new InputSource(args[0]);
        String expr = args[1];
        if (args[2].equals("num")) returnType = XPathConstants.NUMBER;
        else if (args[2].equals("bool")) returnType = XPathConstants.BOOLEAN;
        else if (args[2].equals("str")) returnType = XPathConstants.STRING;
        else if (args[2].equals("node")) returnType = XPathConstants.NODE;
        else if (args[2].equals("nodeset")) returnType = XPathConstants.NODESET;
        else
          System.err.println("Invalid return type: " + args[2]);
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        Object result = null;
        try {
          XPathExpression xpathExpr = xpath.compile(expr);
          result = xpathExpr.evaluate(xml, returnType);
          printResult(result);
        }
        catch (Exception e) {
          e.printStackTrace();
        }        
    }
    static void printResult(Object result)
      throws Exception
    {
        if (result instanceof Double) {
            System.out.println("Result type: double");
            System.out.println("Value: " + result);
        }
        else if (result instanceof Boolean) {
            System.out.println("Result type: boolean");
            System.out.println("Value: " + ((Boolean)result).booleanValue());
        }	
        else if (result instanceof String) {
            System.out.println("Result type: String");
             System.out.println("Value: " + result);
        }
        else if (result instanceof Node) {
            Node node = (Node)result;
            System.out.println("Result type: Node");
            System.out.println("<output>");
            printNode(node);
            System.out.println("</output>");
        }
        else if (result instanceof NodeList) {
            NodeList nodelist = (NodeList)result;
            System.out.println("Result type: NodeList");
            System.out.println("<output>");
            printNodeList(nodelist);
            System.out.println("</output>");
        }
    }
    static boolean isTextNode(Node n) 
    {
      if (n == null)
        return false;
      short nodeType = n.getNodeType();
      return nodeType == Node.CDATA_SECTION_NODE || nodeType == Node.TEXT_NODE;
    }
    static void printNode(Node node) 
      throws Exception
    {
      if (isTextNode(node)) {
        System.out.println(node.getNodeValue());       
      }
      else {
        Transformer serializer = TransformerFactory.newInstance().newTransformer();
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        serializer.transform(new DOMSource(node), new StreamResult(new OutputStreamWriter(System.out)));
      }        
    }
    static void printNodeList(NodeList nodelist) 
      throws Exception
    {
      Node n;
      Transformer serializer = TransformerFactory.newInstance().newTransformer();
      serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      for (int i = 0; i < nodelist.getLength(); i++)
      {         
	n = nodelist.item(i);
	if (isTextNode(n)) {
	    StringBuffer sb = new StringBuffer(n.getNodeValue());
	    for (
	      Node nn = n.getNextSibling(); 
	      isTextNode(nn);
	      nn = nn.getNextSibling()
	    ) {
	      sb.append(nn.getNodeValue());
	    }
	    System.out.print(sb);
	}
	else {
         serializer.transform(new DOMSource(n), new StreamResult(new OutputStreamWriter(System.out)));
	}
        System.out.println();
      }
    }
}
