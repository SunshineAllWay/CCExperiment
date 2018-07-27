package org.apache.xml.serializer;
final class ElemContext
{
    final int m_currentElemDepth;
    ElemDesc m_elementDesc = null;
    String m_elementLocalName = null;
    String m_elementName = null;
    String m_elementURI = null;
    boolean m_isCdataSection;
    boolean m_isRaw = false;
    private ElemContext m_next;
    final ElemContext m_prev;
    boolean m_startTagOpen = false;
    ElemContext()
    {
        m_prev = this;
        m_currentElemDepth = 0;
    }
    private ElemContext(final ElemContext previous)
    {
        m_prev = previous;
        m_currentElemDepth = previous.m_currentElemDepth + 1;
    }
    final ElemContext pop()
    {
        return this.m_prev;
    }
    final ElemContext push()
    {
        ElemContext frame = this.m_next;
        if (frame == null)
        {
            frame = new ElemContext(this);
            this.m_next = frame;
        }
        frame.m_startTagOpen = true;
        return frame;
    }
    final ElemContext push(
        final String uri,
        final String localName,
        final String qName)
    {
        ElemContext frame = this.m_next;
        if (frame == null)
        {
            frame = new ElemContext(this);
            this.m_next = frame;
        }
        frame.m_elementName = qName;
        frame.m_elementLocalName = localName;
        frame.m_elementURI = uri;
        frame.m_isCdataSection = false;
        frame.m_startTagOpen = true;
        return frame;
    }
}
