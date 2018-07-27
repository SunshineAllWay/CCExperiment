package org.apache.xalan.templates;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTM;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.StringVector;
import org.apache.xml.utils.SystemIDResolver;
public class Stylesheet extends ElemTemplateElement
        implements java.io.Serializable 
{
    static final long serialVersionUID = 2085337282743043776L;
  public Stylesheet(Stylesheet parent)
  {
    if (null != parent)
    {
      m_stylesheetParent = parent;
      m_stylesheetRoot = parent.getStylesheetRoot();
    }
  }
  public Stylesheet getStylesheet()
  {
    return this;
  }
  public boolean isAggregatedType()
  {
    return false;
  }
  public boolean isRoot()
  {
    return false;
  }
  public static final String STYLESHEET_EXT = ".lxc";
  private void readObject(ObjectInputStream stream)
          throws IOException, TransformerException
  {
    try
    {
      stream.defaultReadObject();
    }
    catch (ClassNotFoundException cnfe)
    {
      throw new TransformerException(cnfe);
    }
  }
  private void writeObject(ObjectOutputStream stream) throws IOException
  {
    stream.defaultWriteObject();
  }
  private String m_XmlnsXsl;
  public void setXmlnsXsl(String v)
  {
    m_XmlnsXsl = v;
  }
  public String getXmlnsXsl()
  {
    return m_XmlnsXsl;
  }
  private StringVector m_ExtensionElementURIs;
  public void setExtensionElementPrefixes(StringVector v)
  {
    m_ExtensionElementURIs = v;
  }
  public String getExtensionElementPrefix(int i)
          throws ArrayIndexOutOfBoundsException
  {
    if (null == m_ExtensionElementURIs)
      throw new ArrayIndexOutOfBoundsException();
    return m_ExtensionElementURIs.elementAt(i);
  }
  public int getExtensionElementPrefixCount()
  {
    return (null != m_ExtensionElementURIs)
           ? m_ExtensionElementURIs.size() : 0;
  }
  public boolean containsExtensionElementURI(String uri)
  {
    if (null == m_ExtensionElementURIs)
      return false;
    return m_ExtensionElementURIs.contains(uri);
  }
  private StringVector m_ExcludeResultPrefixs;
  public void setExcludeResultPrefixes(StringVector v)
  {
    m_ExcludeResultPrefixs = v;
  }
  public String getExcludeResultPrefix(int i)
          throws ArrayIndexOutOfBoundsException
  {
    if (null == m_ExcludeResultPrefixs)
      throw new ArrayIndexOutOfBoundsException();
    return m_ExcludeResultPrefixs.elementAt(i);
  }
  public int getExcludeResultPrefixCount()
  {
    return (null != m_ExcludeResultPrefixs)
           ? m_ExcludeResultPrefixs.size() : 0;
  }
  public boolean containsExcludeResultPrefix(String prefix, String uri) 
  {
    if (null == m_ExcludeResultPrefixs || uri == null )
      return false;
    for (int i =0; i< m_ExcludeResultPrefixs.size(); i++)
    {
      if (uri.equals(getNamespaceForPrefix(m_ExcludeResultPrefixs.elementAt(i))))
        return true;
    }
    return false;
  }
  private String m_Id;
  public void setId(String v)
  {
    m_Id = v;
  }
  public String getId()
  {
    return m_Id;
  }
  private String m_Version;
  private boolean m_isCompatibleMode = false;
  public void setVersion(String v)
  {
    m_Version = v;
    m_isCompatibleMode = (Double.valueOf(v).doubleValue() > Constants.XSLTVERSUPPORTED);
  }
  public boolean getCompatibleMode()
  {
  	return m_isCompatibleMode;
  }
  public String getVersion()
  {
    return m_Version;
  }
  private Vector m_imports;
  public void setImport(StylesheetComposed v)
  {
    if (null == m_imports)
      m_imports = new Vector();
    m_imports.addElement(v);
  }
  public StylesheetComposed getImport(int i)
          throws ArrayIndexOutOfBoundsException
  {
    if (null == m_imports)
      throw new ArrayIndexOutOfBoundsException();
    return (StylesheetComposed) m_imports.elementAt(i);
  }
  public int getImportCount()
  {
    return (null != m_imports) ? m_imports.size() : 0;
  }
  private Vector m_includes;
  public void setInclude(Stylesheet v)
  {
    if (null == m_includes)
      m_includes = new Vector();
    m_includes.addElement(v);
  }
  public Stylesheet getInclude(int i) throws ArrayIndexOutOfBoundsException
  {
    if (null == m_includes)
      throw new ArrayIndexOutOfBoundsException();
    return (Stylesheet) m_includes.elementAt(i);
  }
  public int getIncludeCount()
  {
    return (null != m_includes) ? m_includes.size() : 0;
  }
  Stack m_DecimalFormatDeclarations;
  public void setDecimalFormat(DecimalFormatProperties edf)
  {
    if (null == m_DecimalFormatDeclarations)
      m_DecimalFormatDeclarations = new Stack();
    m_DecimalFormatDeclarations.push(edf);
  }
  public DecimalFormatProperties getDecimalFormat(QName name)
  {
    if (null == m_DecimalFormatDeclarations)
      return null;
    int n = getDecimalFormatCount();
    for (int i = (n - 1); i >= 0; i++)
    {
      DecimalFormatProperties dfp = getDecimalFormat(i);
      if (dfp.getName().equals(name))
        return dfp;
    }
    return null;
  }
  public DecimalFormatProperties getDecimalFormat(int i)
          throws ArrayIndexOutOfBoundsException
  {
    if (null == m_DecimalFormatDeclarations)
      throw new ArrayIndexOutOfBoundsException();
    return (DecimalFormatProperties) m_DecimalFormatDeclarations.elementAt(i);
  }
  public int getDecimalFormatCount()
  {
    return (null != m_DecimalFormatDeclarations)
           ? m_DecimalFormatDeclarations.size() : 0;
  }
  private Vector m_whitespaceStrippingElements;
  public void setStripSpaces(WhiteSpaceInfo wsi)
  {
    if (null == m_whitespaceStrippingElements)
    {
      m_whitespaceStrippingElements = new Vector();
    }
    m_whitespaceStrippingElements.addElement(wsi);
  }
  public WhiteSpaceInfo getStripSpace(int i) throws ArrayIndexOutOfBoundsException
  {
    if (null == m_whitespaceStrippingElements)
      throw new ArrayIndexOutOfBoundsException();
    return (WhiteSpaceInfo) m_whitespaceStrippingElements.elementAt(i);
  }
  public int getStripSpaceCount()
  {
    return (null != m_whitespaceStrippingElements)
           ? m_whitespaceStrippingElements.size() : 0;
  }
  private Vector m_whitespacePreservingElements;
  public void setPreserveSpaces(WhiteSpaceInfo wsi)
  {
    if (null == m_whitespacePreservingElements)
    {
      m_whitespacePreservingElements = new Vector();
    }
    m_whitespacePreservingElements.addElement(wsi);
  }
  public WhiteSpaceInfo getPreserveSpace(int i) throws ArrayIndexOutOfBoundsException
  {
    if (null == m_whitespacePreservingElements)
      throw new ArrayIndexOutOfBoundsException();
    return (WhiteSpaceInfo) m_whitespacePreservingElements.elementAt(i);
  }
  public int getPreserveSpaceCount()
  {
    return (null != m_whitespacePreservingElements)
           ? m_whitespacePreservingElements.size() : 0;
  }
  private Vector m_output;
  public void setOutput(OutputProperties v)
  {
    if (null == m_output)
    {
      m_output = new Vector();
    }
    m_output.addElement(v);
  }
  public OutputProperties getOutput(int i) throws ArrayIndexOutOfBoundsException
  {
    if (null == m_output)
      throw new ArrayIndexOutOfBoundsException();
    return (OutputProperties) m_output.elementAt(i);
  }
  public int getOutputCount()
  {
    return (null != m_output)
           ? m_output.size() : 0;
  }
  private Vector m_keyDeclarations;
  public void setKey(KeyDeclaration v)
  {
    if (null == m_keyDeclarations)
      m_keyDeclarations = new Vector();
    m_keyDeclarations.addElement(v);
  }
  public KeyDeclaration getKey(int i) throws ArrayIndexOutOfBoundsException
  {
    if (null == m_keyDeclarations)
      throw new ArrayIndexOutOfBoundsException();
    return (KeyDeclaration) m_keyDeclarations.elementAt(i);
  }
  public int getKeyCount()
  {
    return (null != m_keyDeclarations) ? m_keyDeclarations.size() : 0;
  }
  private Vector m_attributeSets;
  public void setAttributeSet(ElemAttributeSet attrSet)
  {
    if (null == m_attributeSets)
    {
      m_attributeSets = new Vector();
    }
    m_attributeSets.addElement(attrSet);
  }
  public ElemAttributeSet getAttributeSet(int i)
          throws ArrayIndexOutOfBoundsException
  {
    if (null == m_attributeSets)
      throw new ArrayIndexOutOfBoundsException();
    return (ElemAttributeSet) m_attributeSets.elementAt(i);
  }
  public int getAttributeSetCount()
  {
    return (null != m_attributeSets) ? m_attributeSets.size() : 0;
  }
  private Vector m_topLevelVariables;
  public void setVariable(ElemVariable v)
  {
    if (null == m_topLevelVariables)
      m_topLevelVariables = new Vector();
    m_topLevelVariables.addElement(v);
  }
  public ElemVariable getVariableOrParam(QName qname)
  {
    if (null != m_topLevelVariables)
    {
      int n = getVariableOrParamCount();
      for (int i = 0; i < n; i++)
      {
        ElemVariable var = (ElemVariable) getVariableOrParam(i);
        if (var.getName().equals(qname))
          return var;
      }
    }
    return null;
  }
  public ElemVariable getVariable(QName qname)
  {
    if (null != m_topLevelVariables)
    {
      int n = getVariableOrParamCount();
      for (int i = 0; i < n; i++)
      {
        ElemVariable var = getVariableOrParam(i);
        if((var.getXSLToken() == Constants.ELEMNAME_VARIABLE) &&
           (var.getName().equals(qname)))
          return var;
      }
    }
    return null;
  }
  public ElemVariable getVariableOrParam(int i) throws ArrayIndexOutOfBoundsException
  {
    if (null == m_topLevelVariables)
      throw new ArrayIndexOutOfBoundsException();
    return (ElemVariable) m_topLevelVariables.elementAt(i);
  }
  public int getVariableOrParamCount()
  {
    return (null != m_topLevelVariables) ? m_topLevelVariables.size() : 0;
  }
  public void setParam(ElemParam v)
  {
    setVariable(v);
  }
  public ElemParam getParam(QName qname)
  {
    if (null != m_topLevelVariables)
    {
      int n = getVariableOrParamCount();
      for (int i = 0; i < n; i++)
      {
        ElemVariable var = getVariableOrParam(i);
        if((var.getXSLToken() == Constants.ELEMNAME_PARAMVARIABLE) &&
           (var.getName().equals(qname)))
          return (ElemParam)var;
      }
    }
    return null;
  }
  private Vector m_templates;
  public void setTemplate(ElemTemplate v)
  {
    if (null == m_templates)
      m_templates = new Vector();
    m_templates.addElement(v);
    v.setStylesheet(this);
  }
  public ElemTemplate getTemplate(int i) throws TransformerException
  {
    if (null == m_templates)
      throw new ArrayIndexOutOfBoundsException();
    return (ElemTemplate) m_templates.elementAt(i);
  }
  public int getTemplateCount()
  {
    return (null != m_templates) ? m_templates.size() : 0;
  }
  private Vector m_prefix_aliases;
  public void setNamespaceAlias(NamespaceAlias na)
  {
    if (m_prefix_aliases == null)
      m_prefix_aliases = new Vector();
    m_prefix_aliases.addElement(na);
  }
  public NamespaceAlias getNamespaceAlias(int i)
          throws ArrayIndexOutOfBoundsException
  {
    if (null == m_prefix_aliases)
      throw new ArrayIndexOutOfBoundsException();
    return (NamespaceAlias) m_prefix_aliases.elementAt(i);
  }
  public int getNamespaceAliasCount()
  {
    return (null != m_prefix_aliases) ? m_prefix_aliases.size() : 0;
  }
  private Hashtable m_NonXslTopLevel;
  public void setNonXslTopLevel(QName name, Object obj)
  {
    if (null == m_NonXslTopLevel)
      m_NonXslTopLevel = new Hashtable();
    m_NonXslTopLevel.put(name, obj);
  }
  public Object getNonXslTopLevel(QName name)
  {
    return (null != m_NonXslTopLevel) ? m_NonXslTopLevel.get(name) : null;
  }
  private String m_href = null;
  private String m_publicId;
  private String m_systemId;
  public String getHref()
  {
    return m_href;
  }
  public void setHref(String baseIdent)
  {
    m_href = baseIdent;
  }
  public void setLocaterInfo(SourceLocator locator)
  {
    if (null != locator)
    {
      m_publicId = locator.getPublicId();
      m_systemId = locator.getSystemId();
      if (null != m_systemId)
      {
        try
        {
          m_href = SystemIDResolver.getAbsoluteURI(m_systemId, null);
        }
        catch (TransformerException se)
        {
        }
      }
      super.setLocaterInfo(locator);
    }
  }
  private StylesheetRoot m_stylesheetRoot;
  public StylesheetRoot getStylesheetRoot()
  {
    return m_stylesheetRoot;
  }
  public void setStylesheetRoot(StylesheetRoot v)
  {
    m_stylesheetRoot = v;
  }
  private Stylesheet m_stylesheetParent;
  public Stylesheet getStylesheetParent()
  {
    return m_stylesheetParent;
  }
  public void setStylesheetParent(Stylesheet v)
  {
    m_stylesheetParent = v;
  }
  public StylesheetComposed getStylesheetComposed()
  {
    Stylesheet sheet = this;
    while (!sheet.isAggregatedType())
    {
      sheet = sheet.getStylesheetParent();
    }
    return (StylesheetComposed) sheet;
  }
  public short getNodeType()
  {
    return DTM.DOCUMENT_NODE;
  }
  public int getXSLToken()
  {
    return Constants.ELEMNAME_STYLESHEET;
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_STYLESHEET_STRING;
  }
  public void replaceTemplate(ElemTemplate v, int i) throws TransformerException
  {
    if (null == m_templates)
      throw new ArrayIndexOutOfBoundsException();
    replaceChild(v, (ElemTemplateElement)m_templates.elementAt(i));
    m_templates.setElementAt(v, i);
    v.setStylesheet(this);
  }
    protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs)
    {
      int s = getImportCount();
      for (int j = 0; j < s; j++)
      {
      	getImport(j).callVisitors(visitor);
      }
      s = getIncludeCount();
      for (int j = 0; j < s; j++)
      {
      	getInclude(j).callVisitors(visitor);
      }
      s = getOutputCount();
      for (int j = 0; j < s; j++)
      {
        visitor.visitTopLevelInstruction(getOutput(j));
      }
      s = getAttributeSetCount();
      for (int j = 0; j < s; j++)
      {
      	ElemAttributeSet attrSet = getAttributeSet(j);
        if (visitor.visitTopLevelInstruction(attrSet))
        {
          attrSet.callChildVisitors(visitor);
        }
      }
      s = getDecimalFormatCount();
      for (int j = 0; j < s; j++)
      {
        visitor.visitTopLevelInstruction(getDecimalFormat(j));
      }
      s = getKeyCount();
      for (int j = 0; j < s; j++)
      {
        visitor.visitTopLevelInstruction(getKey(j));
      }
      s = getNamespaceAliasCount();
      for (int j = 0; j < s; j++)
      {
        visitor.visitTopLevelInstruction(getNamespaceAlias(j));
      }
      s = getTemplateCount();
      for (int j = 0; j < s; j++)
      {
        try
        {
          ElemTemplate template = getTemplate(j);
          if (visitor.visitTopLevelInstruction(template))
          {
            template.callChildVisitors(visitor);
          }
        }
        catch (TransformerException te)
        {
          throw new org.apache.xml.utils.WrappedRuntimeException(te);
        }
      }
      s = getVariableOrParamCount();
      for (int j = 0; j < s; j++)
      {
      	ElemVariable var = getVariableOrParam(j);
        if (visitor.visitTopLevelVariableOrParamDecl(var))
        {
          var.callChildVisitors(visitor);
        }
      }
      s = getStripSpaceCount();
      for (int j = 0; j < s; j++)
      {
        visitor.visitTopLevelInstruction(getStripSpace(j));
      }
      s = getPreserveSpaceCount();
      for (int j = 0; j < s; j++)
      {
        visitor.visitTopLevelInstruction(getPreserveSpace(j));
      }
      if(null != m_NonXslTopLevel)
      {
      	java.util.Enumeration elements = m_NonXslTopLevel.elements();
      	while(elements.hasMoreElements())
      	{
      	  ElemTemplateElement elem = (ElemTemplateElement)elements.nextElement();
          if (visitor.visitTopLevelInstruction(elem))
          {
            elem.callChildVisitors(visitor);
          }
      	}
      }
    }
  protected boolean accept(XSLTVisitor visitor)
  {
  	return visitor.visitStylesheet(this);
  }
}
