package org.apache.xalan.templates;
public class XMLNSDecl
        implements java.io.Serializable 
{
    static final long serialVersionUID = 6710237366877605097L;
  public XMLNSDecl(String prefix, String uri, boolean isExcluded)
  {
    m_prefix = prefix;
    m_uri = uri;
    m_isExcluded = isExcluded;
  }
  private String m_prefix;
  public String getPrefix()
  {
    return m_prefix;
  }
  private String m_uri;
  public String getURI()
  {
    return m_uri;
  }
  private boolean m_isExcluded;
  public boolean getIsExcluded()
  {
    return m_isExcluded;
  }
}
