package org.apache.xalan.client;
import java.applet.Applet;
import java.awt.Graphics;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
public class XSLTProcessorApplet extends Applet
{
  transient TransformerFactory m_tfactory = null;
  private String m_styleURL;
  private String m_documentURL;
  private final String PARAM_styleURL = "styleURL";
  private final String PARAM_documentURL = "documentURL";
  private String m_styleURLOfCached = null;
  private String m_documentURLOfCached = null;
  private URL m_codeBase = null;
  private String m_treeURL = null;
  private URL m_documentBase = null;
  transient private Thread m_callThread = null;
  transient private TrustedAgent m_trustedAgent = null;
  transient private Thread m_trustedWorker = null;
  transient private String m_htmlText = null;
  transient private String m_sourceText = null;
  transient private String m_nameOfIDAttrOfElemToModify = null;
  transient private String m_elemIdToModify = null;
  transient private String m_attrNameToSet = null;
  transient private String m_attrValueToSet = null;
  public XSLTProcessorApplet(){}
  public String getAppletInfo()
  {
    return "Name: XSLTProcessorApplet\r\n" + "Author: Scott Boag";
  }
  public String[][] getParameterInfo()
  {
    String[][] info =
    {
      { PARAM_styleURL, "String", "URL to an XSL stylesheet" },
      { PARAM_documentURL, "String", "URL to an XML document" },
    };
    return info;
  }
  public void init()
  {
    String param;
    param = getParameter(PARAM_styleURL);
    m_parameters = new Hashtable();
    if (param != null)
      setStyleURL(param);
    param = getParameter(PARAM_documentURL);
    if (param != null)
      setDocumentURL(param);
    m_codeBase = this.getCodeBase();
    m_documentBase = this.getDocumentBase();
    resize(320, 240);
  }
  public void start()
  {
    m_trustedAgent = new TrustedAgent();
    Thread currentThread = Thread.currentThread();
    m_trustedWorker = new Thread(currentThread.getThreadGroup(),
                                 m_trustedAgent);
    m_trustedWorker.start();
    try
    {
      m_tfactory = TransformerFactory.newInstance();
      this.showStatus("Causing Transformer and Parser to Load and JIT...");
      StringReader xmlbuf = new StringReader("<?xml version='1.0'?><foo/>");
      StringReader xslbuf = new StringReader(
        "<?xml version='1.0'?><xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'><xsl:template match='foo'><out/></xsl:template></xsl:stylesheet>");
      PrintWriter pw = new PrintWriter(new StringWriter());
      synchronized (m_tfactory)
      {
        Templates templates = m_tfactory.newTemplates(new StreamSource(xslbuf));
        Transformer transformer = templates.newTransformer();
        transformer.transform(new StreamSource(xmlbuf), new StreamResult(pw));
      }
      System.out.println("Primed the pump!");
      this.showStatus("Ready to go!");
    }
    catch (Exception e)
    {
      this.showStatus("Could not prime the pump!");
      System.out.println("Could not prime the pump!");
      e.printStackTrace();
    }
  }
  public void paint(Graphics g){}  
  public void stop()
  {
    if (null != m_trustedWorker)
    {
      m_trustedWorker.stop();
      m_trustedWorker = null;
    }
    m_styleURLOfCached = null;
    m_documentURLOfCached = null;
  }   
  public void destroy()
  {
    if (null != m_trustedWorker)
    {
      m_trustedWorker.stop();
      m_trustedWorker = null;
    }
    m_styleURLOfCached = null;
    m_documentURLOfCached = null;
  }
  public void setStyleURL(String urlString)
  {
    m_styleURL = urlString;
  }
  public void setDocumentURL(String urlString)
  {
    m_documentURL = urlString;
  }
  public void freeCache()
  {
    m_styleURLOfCached = null;
    m_documentURLOfCached = null;
  }
  public void setStyleSheetAttribute(String nameOfIDAttrOfElemToModify,
                                     String elemId, String attrName,
                                     String value)
  {
    m_nameOfIDAttrOfElemToModify = nameOfIDAttrOfElemToModify;
    m_elemIdToModify = elemId;
    m_attrNameToSet = attrName;
    m_attrValueToSet = value;
  }
  transient Hashtable m_parameters;  
  public void setStylesheetParam(String key, String expr)
  {
    m_parameters.put(key, expr);
  }
  public String escapeString(String s)
  {
    StringBuffer sb = new StringBuffer();
    int length = s.length();
    for (int i = 0; i < length; i++)
    {
      char ch = s.charAt(i);
      if ('<' == ch)
      {
        sb.append("&lt;");
      }
      else if ('>' == ch)
      {
        sb.append("&gt;");
      }
      else if ('&' == ch)
      {
        sb.append("&amp;");
      }
      else if (0xd800 <= ch && ch < 0xdc00)
      {
        int next;
        if (i + 1 >= length)
        {
          throw new RuntimeException(
            XSLMessages.createMessage(
              XSLTErrorResources.ER_INVALID_UTF16_SURROGATE,
              new Object[]{ Integer.toHexString(ch) }));  
        }
        else
        {
          next = s.charAt(++i);
          if (!(0xdc00 <= next && next < 0xe000))
            throw new RuntimeException(
              XSLMessages.createMessage(
                XSLTErrorResources.ER_INVALID_UTF16_SURROGATE,
                new Object[]{
                  Integer.toHexString(ch) + " "
                  + Integer.toHexString(next) }));  
          next = ((ch - 0xd800) << 10) + next - 0xdc00 + 0x00010000;
        }
        sb.append("&#x");
        sb.append(Integer.toHexString(next));
        sb.append(";");
      }
      else
      {
        sb.append(ch);
      }
    }
    return sb.toString();
  }
  public String getHtmlText()
  {
    m_trustedAgent.m_getData = true;
    m_callThread = Thread.currentThread();
    try
    {
      synchronized (m_callThread)
      {
        m_callThread.wait();
      }
    }
    catch (InterruptedException ie)
    {
      System.out.println(ie.getMessage());
    }
    return m_htmlText;
  }
  public String getTreeAsText(String treeURL) throws IOException
  {
    m_treeURL = treeURL;
    m_trustedAgent.m_getData = true;
    m_trustedAgent.m_getSource = true;
    m_callThread = Thread.currentThread();
    try
    {
      synchronized (m_callThread)
      {
        m_callThread.wait();
      }
    }
    catch (InterruptedException ie)
    {
      System.out.println(ie.getMessage());
    }
    return m_sourceText;
  }
  private String getSource() throws TransformerException
  {
    StringWriter osw = new StringWriter();
    PrintWriter pw = new PrintWriter(osw, false);
    String text = "";
    try
    {
      URL docURL = new URL(m_documentBase, m_treeURL);
      synchronized (m_tfactory)
      {
        Transformer transformer = m_tfactory.newTransformer();
        StreamSource source = new StreamSource(docURL.toString());    
        StreamResult result = new StreamResult(pw);
        transformer.transform(source, result);
        text = osw.toString();
      }
    }
    catch (MalformedURLException e)
    {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }      
    catch (Exception any_error)
    {
      any_error.printStackTrace();
    }
    return text;
  }
  public String getSourceTreeAsText() throws Exception
  {
    return getTreeAsText(m_documentURL);
  }
  public String getStyleTreeAsText() throws Exception
  {
    return getTreeAsText(m_styleURL);
  }
  public String getResultTreeAsText() throws Exception
  {
    return escapeString(getHtmlText());
  }
  public String transformToHtml(String doc, String style)
  {
    if (null != doc)
    {
      m_documentURL = doc;
    }
    if (null != style)
    {
      m_styleURL = style;
    }
    return getHtmlText();
  }
  public String transformToHtml(String doc)
  {
    if (null != doc)
    {
      m_documentURL = doc;
    }
    m_styleURL = null;
    return getHtmlText();
  }
  private String processTransformation() throws TransformerException
  {
    String htmlData = null;
    this.showStatus("Waiting for Transformer and Parser to finish loading and JITing...");
    synchronized (m_tfactory)
    {
     URL documentURL = null;
      URL styleURL = null;
      StringWriter osw = new StringWriter();
      PrintWriter pw = new PrintWriter(osw, false);
      StreamResult result = new StreamResult(pw);
      this.showStatus("Begin Transformation...");
      try
      {
        documentURL = new URL(m_codeBase, m_documentURL);
        StreamSource xmlSource = new StreamSource(documentURL.toString());
        styleURL = new URL(m_codeBase, m_styleURL);
        StreamSource xslSource = new StreamSource(styleURL.toString());
        Transformer transformer = m_tfactory.newTransformer(xslSource);
        Enumeration m_keys = m_parameters.keys();
        while (m_keys.hasMoreElements()){
          Object key = m_keys.nextElement();
          Object expression = m_parameters.get(key);
          transformer.setParameter((String) key, expression);
        }
        transformer.transform(xmlSource, result);
      }
      catch (TransformerConfigurationException tfe)
      {
        tfe.printStackTrace();
        throw new RuntimeException(tfe.getMessage());
      }
      catch (MalformedURLException e)
      {
        e.printStackTrace();
        throw new RuntimeException(e.getMessage());
      }
      this.showStatus("Transformation Done!");
      htmlData = osw.toString();
    }
    return htmlData;
  }
  class TrustedAgent implements Runnable
  {
    public boolean m_getData = false;
    public boolean m_getSource = false;
    public void run()
    {
      while (true)
      {
        Thread.yield();
        if (m_getData)  
        {
          try
          {
            m_getData = false;
            m_htmlText = null;
            m_sourceText = null;
            if (m_getSource)  
            {
              m_getSource = false;
              m_sourceText = getSource();
            }
            else              
              m_htmlText = processTransformation();
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
          finally
          {
            synchronized (m_callThread)
            {
              m_callThread.notify();
            }
          }
        }
        else
        {
          try
          {
            Thread.sleep(50);
          }
          catch (InterruptedException ie)
          {
            ie.printStackTrace();
          }
        }
      }
    }
  }
  private static final long serialVersionUID=4618876841979251422L;
  private void readObject(java.io.ObjectInputStream inStream) throws IOException, ClassNotFoundException 
  {
      inStream.defaultReadObject();
      m_tfactory = TransformerFactory.newInstance();
  }      
}
