package org.apache.lucene.xmlparser;
import java.io.Reader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
public class DOMUtils
{
    public static Element getChildByTagOrFail(Element e, String name)	throws ParserException
	{
		Element kid = getChildByTagName(e, name);
		if (null == kid)
		{
			throw new ParserException(e.getTagName() + " missing \"" + name
					+ "\" child element");
		}
		return kid;
	}
     public static Element getFirstChildOrFail(Element e) throws ParserException
	{
		Element kid = getFirstChildElement(e);
		if (null == kid)
		{
			throw new ParserException(e.getTagName()
					+ " does not contain a child element");
		}
		return kid;
	}
	public static String getAttributeOrFail(Element e, String name)	throws ParserException
	{
		String v = e.getAttribute(name);
		if (null == v)
		{
			throw new ParserException(e.getTagName() + " missing \"" + name
					+ "\" attribute");
		}
		return v;
	}
    public static String getAttributeWithInheritanceOrFail(Element e,	String name) throws ParserException
	{
		String v = getAttributeWithInheritance(e, name);
		if (null == v)
		{
			throw new ParserException(e.getTagName() + " missing \"" + name
					+ "\" attribute");
		}
		return v;
	}
    public static String getNonBlankTextOrFail(Element e) throws ParserException
	{
		String v = getText(e);
		if (null != v)
			v = v.trim();
		if (null == v || 0 == v.length())
		{
			throw new ParserException(e.getTagName() + " has no text");
		}
		return v;
	}
	public static Element getChildByTagName(Element e, String name)
	{
	       for (Node kid = e.getFirstChild(); kid != null; kid = kid.getNextSibling())
		{
			if( (kid.getNodeType()==Node.ELEMENT_NODE) && (name.equals(kid.getNodeName())) )
			{
				return (Element)kid;
			}
		}
		return null;
	}
	public static String getAttributeWithInheritance(Element element, String attributeName)
	{
		String result=element.getAttribute(attributeName);
		if( (result==null)|| ("".equals(result) ) )
		{
			Node n=element.getParentNode();
			if((n==element)||(n==null))
			{
				return null;
			}
			if(n instanceof Element)
			{
				Element parent=(Element) n;
				return getAttributeWithInheritance(parent,attributeName);
			}
			return null; 
		}
		return result;		
	}
	public static String getChildTextByTagName(Element e, String tagName)
	{
		Element child=getChildByTagName(e,tagName);
		if(child!=null)
		{
			return getText(child);
		}
		return null;
	}
	public static Element insertChild(Element parent, String tagName, String text)
	{
	  	Element child = parent.getOwnerDocument().createElement(tagName);
		parent.appendChild(child);
		if(text!=null)
		{
		  	child.appendChild(child.getOwnerDocument().createTextNode(text));
		}
		return child;
	}
	public static String getAttribute(Element element, String attributeName, String deflt)
	{
		String result=element.getAttribute(attributeName);
		if( (result==null)|| ("".equals(result) ) )
		{
			return deflt;
		}
		return result;
	}
	public static float getAttribute(Element element, String attributeName, float deflt)
	{
		String result=element.getAttribute(attributeName);
		if( (result==null)|| ("".equals(result) ) )
		{
			return deflt;
		}
		return Float.parseFloat(result);
	}	
	public static int getAttribute(Element element, String attributeName, int deflt)
	{
		String result=element.getAttribute(attributeName);
		if( (result==null)|| ("".equals(result) ) )
		{
			return deflt;
		}
		return Integer.parseInt(result);
	}
	public static boolean getAttribute(Element element, String attributeName,
			boolean deflt)
	{
		String result = element.getAttribute(attributeName);
		if ((result == null) || ("".equals(result)))
		{
			return deflt;
		}
		return Boolean.valueOf(result).booleanValue();
	}	
	public static String getText(Node e)
	{
		StringBuilder sb=new StringBuilder();
		getTextBuffer(e, sb);
		return sb.toString();
	}
	public static Element getFirstChildElement(Element element)
	{
		for (Node kid = element.getFirstChild(); kid != null; kid = kid
				.getNextSibling())
		{
			if (kid.getNodeType() == Node.ELEMENT_NODE) 
			{
				return (Element) kid;
			}
		}
		return null;
	}	
	private static void getTextBuffer(Node e, StringBuilder sb)
	{
	    for (Node kid = e.getFirstChild(); kid != null; kid = kid.getNextSibling())
		{
			switch(kid.getNodeType())
			{
				case Node.TEXT_NODE:
				{
					sb.append(kid.getNodeValue());
					break;
				}
				case Node.ELEMENT_NODE:
				{
					getTextBuffer(kid, sb);
					break;
				}
				case Node.ENTITY_REFERENCE_NODE:
				{
					getTextBuffer(kid, sb);
					break;
				}
			}
		}
	}
	public static Document loadXML(Reader is)
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try
		{
			db = dbf.newDocumentBuilder();
		}
		catch (Exception se)
		{
			throw new RuntimeException("Parser configuration error", se);
		}
		org.w3c.dom.Document doc = null;
		try
		{
			doc = db.parse(new InputSource(is));
		}
		catch (Exception se)
		{
			throw new RuntimeException("Error parsing file:" + se, se);
		}
		return doc;
	}	
}
