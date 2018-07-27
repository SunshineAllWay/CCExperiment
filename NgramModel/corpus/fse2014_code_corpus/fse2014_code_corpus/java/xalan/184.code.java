package org.apache.xalan.templates;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.QName;
public class ElemUse extends ElemTemplateElement
{
    static final long serialVersionUID = 5830057200289299736L;
  private QName m_attributeSetsNames[] = null;
  public void setUseAttributeSets(Vector v)
  {
    int n = v.size();
    m_attributeSetsNames = new QName[n];
    for (int i = 0; i < n; i++)
    {
      m_attributeSetsNames[i] = (QName) v.elementAt(i);
    }
  }
  public void setUseAttributeSets(QName[] v)
  {
    m_attributeSetsNames = v;
  }
  public QName[] getUseAttributeSets()
  {
    return m_attributeSetsNames;
  }
  public void applyAttrSets(
          TransformerImpl transformer, StylesheetRoot stylesheet)
            throws TransformerException
  {
    applyAttrSets(transformer, stylesheet, m_attributeSetsNames);
  }
  private void applyAttrSets(
          TransformerImpl transformer, StylesheetRoot stylesheet, QName attributeSetsNames[])
            throws TransformerException
  {
    if (null != attributeSetsNames)
    {
      int nNames = attributeSetsNames.length;
      for (int i = 0; i < nNames; i++)
      {
        QName qname = attributeSetsNames[i];
        java.util.List attrSets = stylesheet.getAttributeSetComposed(qname);
        if (null != attrSets)
        {
          int nSets = attrSets.size();
          for (int k = nSets-1; k >= 0 ; k--)
          {
            ElemAttributeSet attrSet =
              (ElemAttributeSet) attrSets.get(k);
            attrSet.execute(transformer);
          }
        } 
        else 
        {
          throw new TransformerException(
              XSLMessages.createMessage(XSLTErrorResources.ER_NO_ATTRIB_SET, 
                  new Object[] {qname}),this); 
        }
      }
    }
  }
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {
    if (null != m_attributeSetsNames)
    {
      applyAttrSets(transformer, getStylesheetRoot(),
                    m_attributeSetsNames);
    }
  }
}
