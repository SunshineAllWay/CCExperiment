package org.apache.xalan.processor;
import java.util.Vector;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.templates.WhiteSpaceInfo;
public class WhitespaceInfoPaths extends WhiteSpaceInfo
{
    static final long serialVersionUID = 5954766719577516723L;
  private Vector m_elements;
  public void setElements(Vector elems)
  {
    m_elements = elems;
  }
  Vector getElements()
  {
    return m_elements;
  }
  public void clearElements()
  {
  	m_elements = null;
  }
  public WhitespaceInfoPaths(Stylesheet thisSheet)
  {
  	super(thisSheet);
  	setStylesheet(thisSheet);
  }
}
