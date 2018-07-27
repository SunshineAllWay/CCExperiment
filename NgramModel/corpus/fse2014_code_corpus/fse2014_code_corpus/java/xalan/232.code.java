package org.apache.xalan.transformer;
import java.util.Vector;
import org.apache.xml.utils.MutableAttrListImpl;
public abstract class QueuedEvents
{
  protected int m_eventCount = 0;
  public boolean m_docPending = false;
  protected boolean m_docEnded = false;
  public boolean m_elemIsPending = false;
  public boolean m_elemIsEnded = false;
  protected MutableAttrListImpl m_attributes = new MutableAttrListImpl();
  protected boolean m_nsDeclsHaveBeenAdded = false;
  protected String m_name;
  protected String m_url;
  protected String m_localName;
  protected Vector m_namespaces = null;
  protected void reInitEvents()
  {
  }
  public void reset()
  {
    pushDocumentEvent();
    reInitEvents();
  }
  void pushDocumentEvent()
  {
    m_docPending = true;
    m_eventCount++;
  }
  void popEvent()
  {
    m_elemIsPending = false;
    m_attributes.clear();
    m_nsDeclsHaveBeenAdded = false;
    m_name = null;
    m_url = null;
    m_localName = null;
    m_namespaces = null;
    m_eventCount--;
  }
  private org.apache.xml.serializer.Serializer m_serializer;
  void setSerializer(org.apache.xml.serializer.Serializer s)
  {
    m_serializer = s;
  }
  org.apache.xml.serializer.Serializer getSerializer()
  {
    return m_serializer;
  }
}
