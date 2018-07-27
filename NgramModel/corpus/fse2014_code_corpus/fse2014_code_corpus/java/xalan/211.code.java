package org.apache.xalan.trace;
import org.apache.xalan.transformer.TransformerImpl;
import org.xml.sax.Attributes;
public class GenerateEvent implements java.util.EventListener
{
  public TransformerImpl m_processor;
  public int m_eventtype;
  public char m_characters[];
  public int m_start;
  public int m_length;
  public String m_name;
  public String m_data;
  public Attributes m_atts;
  public GenerateEvent(TransformerImpl processor, int eventType)
  {
    m_processor = processor;
    m_eventtype = eventType;
  }
  public GenerateEvent(TransformerImpl processor, int eventType, String name,
                       Attributes atts)
  {
    m_name = name;
    m_atts = atts;
    m_processor = processor;
    m_eventtype = eventType;
  }
  public GenerateEvent(TransformerImpl processor, int eventType, char ch[],
                       int start, int length)
  {
    m_characters = ch;
    m_start = start;
    m_length = length;
    m_processor = processor;
    m_eventtype = eventType;
  }
  public GenerateEvent(TransformerImpl processor, int eventType, String name,
                       String data)
  {
    m_name = name;
    m_data = data;
    m_processor = processor;
    m_eventtype = eventType;
  }
  public GenerateEvent(TransformerImpl processor, int eventType, String data)
  {
    m_data = data;
    m_processor = processor;
    m_eventtype = eventType;
  }
}
