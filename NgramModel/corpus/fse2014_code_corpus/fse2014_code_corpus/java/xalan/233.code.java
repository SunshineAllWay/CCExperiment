package org.apache.xalan.transformer;
public class ResultNameSpace
{
  public ResultNameSpace m_next = null;
  public String m_prefix;
  public String m_uri;  
  public ResultNameSpace(String prefix, String uri)
  {
    m_prefix = prefix;
    m_uri = uri;
  }
}
