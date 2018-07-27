package org.apache.lucene.xmlparser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
public class QueryTemplateManager
{
	static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance ();
	static TransformerFactory tFactory = TransformerFactory.newInstance();
	HashMap<String,Templates> compiledTemplatesCache=new HashMap<String,Templates>();
	Templates defaultCompiledTemplates=null;
	public QueryTemplateManager()
	{
	}
	public QueryTemplateManager(InputStream xslIs) throws TransformerConfigurationException, ParserConfigurationException, SAXException, IOException
	{
		addDefaultQueryTemplate(xslIs);
	}
	public void addDefaultQueryTemplate(InputStream xslIs) throws TransformerConfigurationException, ParserConfigurationException, SAXException, IOException
	{
		defaultCompiledTemplates=getTemplates(xslIs);
	}
	public void addQueryTemplate(String name, InputStream xslIs) throws TransformerConfigurationException, ParserConfigurationException, SAXException, IOException
	{
		compiledTemplatesCache.put(name,getTemplates(xslIs));
	}
	public String getQueryAsXmlString(Properties formProperties,String queryTemplateName) throws SAXException, IOException, ParserConfigurationException, TransformerException
	{
		Templates ts= compiledTemplatesCache.get(queryTemplateName);
		return getQueryAsXmlString(formProperties, ts);
	}
	public Document getQueryAsDOM(Properties formProperties,String queryTemplateName) throws SAXException, IOException, ParserConfigurationException, TransformerException
	{
		Templates ts= compiledTemplatesCache.get(queryTemplateName);
		return getQueryAsDOM(formProperties, ts);
	}
	public String getQueryAsXmlString(Properties formProperties) throws SAXException, IOException, ParserConfigurationException, TransformerException
	{
		return getQueryAsXmlString(formProperties, defaultCompiledTemplates);
	}
	public Document getQueryAsDOM(Properties formProperties) throws SAXException, IOException, ParserConfigurationException, TransformerException
	{
		return getQueryAsDOM(formProperties, defaultCompiledTemplates);
	}
	public static String getQueryAsXmlString(Properties formProperties, Templates template) throws SAXException, IOException, ParserConfigurationException, TransformerException 
	{
  		ByteArrayOutputStream baos=new ByteArrayOutputStream();
  		StreamResult result=new StreamResult(baos);
  		transformCriteria(formProperties,template,result);
  		return baos.toString();  		
	}
	public static String getQueryAsXmlString(Properties formProperties, InputStream xslIs) throws SAXException, IOException, ParserConfigurationException, TransformerException 
	{
  		ByteArrayOutputStream baos=new ByteArrayOutputStream();
  		StreamResult result=new StreamResult(baos);
  		transformCriteria(formProperties,xslIs,result);
  		return baos.toString();  		
	}
	public static Document getQueryAsDOM(Properties formProperties, Templates template) throws SAXException, IOException, ParserConfigurationException, TransformerException
	{
  		DOMResult result=new DOMResult();
  		transformCriteria(formProperties,template,result);
  		return (Document)result.getNode();
	}
	public static Document getQueryAsDOM(Properties formProperties, InputStream xslIs) throws SAXException, IOException, ParserConfigurationException, TransformerException
	{
  		DOMResult result=new DOMResult();
  		transformCriteria(formProperties,xslIs,result);
  		return (Document)result.getNode();
	}
	public static void transformCriteria(Properties formProperties, InputStream xslIs, Result result) throws SAXException, IOException, ParserConfigurationException, TransformerException
	{
        dbf.setNamespaceAware(true);	    
		DocumentBuilder builder = dbf.newDocumentBuilder();
		org.w3c.dom.Document xslDoc = builder.parse(xslIs);
		DOMSource ds = new DOMSource(xslDoc);
		Transformer transformer =null;
		synchronized (tFactory)
		{
			transformer = tFactory.newTransformer(ds);			
		}
		transformCriteria(formProperties,transformer,result);
	}
	public static void transformCriteria(Properties formProperties, Templates template, Result result) throws SAXException, IOException, ParserConfigurationException, TransformerException
	{
		transformCriteria(formProperties,template.newTransformer(),result);
	}
	public static void transformCriteria(Properties formProperties, Transformer transformer, Result result) throws SAXException, IOException, ParserConfigurationException, TransformerException
	{
        dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder ();
		org.w3c.dom.Document doc = db.newDocument ();
		Element root = doc.createElement ("Document");
		doc.appendChild (root);
		Enumeration keysEnum = formProperties.keys();
		while(keysEnum.hasMoreElements())
		{
		    String propName=(String) keysEnum.nextElement();
		    String value=formProperties.getProperty(propName);
    		if((value!=null)&&(value.length()>0))
    		{
    		    DOMUtils.insertChild(root,propName,value);    			
    		}
		}		
		DOMSource xml=new DOMSource(doc);
		transformer.transform(xml,result);		
	}
	public static Templates getTemplates(InputStream xslIs) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException  
	{
        dbf.setNamespaceAware(true);	    
		DocumentBuilder builder = dbf.newDocumentBuilder();
		org.w3c.dom.Document xslDoc = builder.parse(xslIs);
		DOMSource ds = new DOMSource(xslDoc);
		return tFactory.newTemplates(ds);
	}
}
