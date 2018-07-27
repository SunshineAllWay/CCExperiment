package org.apache.xalan.trace;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.xml.transform.SourceLocator;
import org.apache.xalan.templates.Constants;
import org.apache.xalan.templates.ElemTemplate;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.ElemTextLiteral;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.ref.DTMNodeProxy;
import org.apache.xml.serializer.SerializerTrace;
import org.w3c.dom.Node;
public class PrintTraceListener implements TraceListenerEx3
{
  public PrintTraceListener(java.io.PrintWriter pw)
  {
    m_pw = pw;
  }
  java.io.PrintWriter m_pw;
  public boolean m_traceTemplates = false;
  public boolean m_traceElements = false;
  public boolean m_traceGeneration = false;
  public boolean m_traceSelection = false;
  public boolean m_traceExtension = false;
  public void _trace(TracerEvent ev)
  {
    switch (ev.m_styleNode.getXSLToken())
    {
    case Constants.ELEMNAME_TEXTLITERALRESULT :
      if (m_traceElements)
      {
        m_pw.print(ev.m_styleNode.getSystemId()+ " Line #" + ev.m_styleNode.getLineNumber() + ", "
                   + "Column #" + ev.m_styleNode.getColumnNumber() + " -- "
                   + ev.m_styleNode.getNodeName() + ": ");
        ElemTextLiteral etl = (ElemTextLiteral) ev.m_styleNode;
        String chars = new String(etl.getChars(), 0, etl.getChars().length);
        m_pw.println("    " + chars.trim());
      }
      break;
    case Constants.ELEMNAME_TEMPLATE :
      if (m_traceTemplates || m_traceElements)
      {
        ElemTemplate et = (ElemTemplate) ev.m_styleNode;
        m_pw.print(et.getSystemId()+ " Line #" + et.getLineNumber() + ", " + "Column #"
                   + et.getColumnNumber() + ": " + et.getNodeName() + " ");
        if (null != et.getMatch())
        {
          m_pw.print("match='" + et.getMatch().getPatternString() + "' ");
        }
        if (null != et.getName())
        {
          m_pw.print("name='" + et.getName() + "' ");
        }
        m_pw.println();
      }
      break;
    default :
      if (m_traceElements)
      {
        m_pw.println(ev.m_styleNode.getSystemId()+ " Line #" + ev.m_styleNode.getLineNumber() + ", "
                     + "Column #" + ev.m_styleNode.getColumnNumber() + ": "
                     + ev.m_styleNode.getNodeName());
      }
    }
  }
  int m_indent = 0;
  public void trace(TracerEvent ev)
  {
	_trace(ev);
  }
  public void traceEnd(TracerEvent ev)
  {
  }
public void selected(SelectionEvent ev)
    throws javax.xml.transform.TransformerException {
    if (m_traceSelection) {
        ElemTemplateElement ete = (ElemTemplateElement) ev.m_styleNode;
        Node sourceNode = ev.m_sourceNode;
        SourceLocator locator = null;
        if (sourceNode instanceof DTMNodeProxy) {
            int nodeHandler = ((DTMNodeProxy) sourceNode).getDTMNodeNumber();
            locator =
                ((DTMNodeProxy) sourceNode).getDTM().getSourceLocatorFor(
                    nodeHandler);
        }
        if (locator != null)
            m_pw.println(
                "Selected source node '"
                    + sourceNode.getNodeName()
                    + "', at "
                    + locator);
        else
            m_pw.println(
                "Selected source node '" + sourceNode.getNodeName() + "'");
        if (ev.m_styleNode.getLineNumber() == 0) {
            ElemTemplateElement parent =
                (ElemTemplateElement) ete.getParentElem();
            if (parent == ete.getStylesheetRoot().getDefaultRootRule()) {
                m_pw.print("(default root rule) ");
            } else if (
                parent == ete.getStylesheetRoot().getDefaultTextRule()) {
                m_pw.print("(default text rule) ");
            } else if (parent == ete.getStylesheetRoot().getDefaultRule()) {
                m_pw.print("(default rule) ");
            }
            m_pw.print(
                ete.getNodeName()
                    + ", "
                    + ev.m_attributeName
                    + "='"
                    + ev.m_xpath.getPatternString()
                    + "': ");
        } else {
            m_pw.print(
                ev.m_styleNode.getSystemId()
                    + " Line #"
                    + ev.m_styleNode.getLineNumber()
                    + ", "
                    + "Column #"
                    + ev.m_styleNode.getColumnNumber()
                    + ": "
                    + ete.getNodeName()
                    + ", "
                    + ev.m_attributeName
                    + "='"
                    + ev.m_xpath.getPatternString()
                    + "': ");
        }
        if (ev.m_selection.getType() == ev.m_selection.CLASS_NODESET) {
            m_pw.println();
            org.apache.xml.dtm.DTMIterator nl = ev.m_selection.iter();
            int currentPos = DTM.NULL;
            currentPos = nl.getCurrentPos();
            nl.setShouldCacheNodes(true); 
            org.apache.xml.dtm.DTMIterator clone = null;
            try {
                clone = nl.cloneWithReset();
            } catch (CloneNotSupportedException cnse) {
                m_pw.println(
                    "     [Can't trace nodelist because it it threw a CloneNotSupportedException]");
                return;
            }
            int pos = clone.nextNode();
            if (DTM.NULL == pos) {
                m_pw.println("     [empty node list]");
            } else {
                while (DTM.NULL != pos) {
                    DTM dtm = ev.m_processor.getXPathContext().getDTM(pos);
                    m_pw.print("     ");
                    m_pw.print(Integer.toHexString(pos));
                    m_pw.print(": ");
                    m_pw.println(dtm.getNodeName(pos));
                    pos = clone.nextNode();
                }
            }
            nl.runTo(-1);
            nl.setCurrentPos(currentPos);
        } else {
            m_pw.println(ev.m_selection.str());
        }
    }
}
  public void selectEnd(EndSelectionEvent ev) 
     throws javax.xml.transform.TransformerException
  {
  }
  public void generated(GenerateEvent ev)
  {
    if (m_traceGeneration)
    {
      switch (ev.m_eventtype)
      {
      case SerializerTrace.EVENTTYPE_STARTDOCUMENT :
        m_pw.println("STARTDOCUMENT");
        break;
      case SerializerTrace.EVENTTYPE_ENDDOCUMENT :
        m_pw.println("ENDDOCUMENT");
        break;
      case SerializerTrace.EVENTTYPE_STARTELEMENT :
        m_pw.println("STARTELEMENT: " + ev.m_name);
        break;
      case SerializerTrace.EVENTTYPE_ENDELEMENT :
        m_pw.println("ENDELEMENT: " + ev.m_name);
        break;
      case SerializerTrace.EVENTTYPE_CHARACTERS :
      {
        String chars = new String(ev.m_characters, ev.m_start, ev.m_length);
        m_pw.println("CHARACTERS: " + chars);
      }
      break;
      case SerializerTrace.EVENTTYPE_CDATA :
      {
        String chars = new String(ev.m_characters, ev.m_start, ev.m_length);
        m_pw.println("CDATA: " + chars);
      }
      break;
      case SerializerTrace.EVENTTYPE_COMMENT :
        m_pw.println("COMMENT: " + ev.m_data);
        break;
      case SerializerTrace.EVENTTYPE_PI :
        m_pw.println("PI: " + ev.m_name + ", " + ev.m_data);
        break;
      case SerializerTrace.EVENTTYPE_ENTITYREF :
        m_pw.println("ENTITYREF: " + ev.m_name);
        break;
      case SerializerTrace.EVENTTYPE_IGNORABLEWHITESPACE :
        m_pw.println("IGNORABLEWHITESPACE");
        break;
      }
    }
  }
  public void extension(ExtensionEvent ev) {
    if (m_traceExtension) {
      switch (ev.m_callType) {
        case ExtensionEvent.DEFAULT_CONSTRUCTOR:
          m_pw.println("EXTENSION: " + ((Class)ev.m_method).getName() + "#<init>");
          break;
        case ExtensionEvent.METHOD:
          m_pw.println("EXTENSION: " + ((Method)ev.m_method).getDeclaringClass().getName() + "#" + ((Method)ev.m_method).getName());
          break;
        case ExtensionEvent.CONSTRUCTOR:
          m_pw.println("EXTENSION: " + ((Constructor)ev.m_method).getDeclaringClass().getName() + "#<init>");
          break;
      }
    }
  }
  public void extensionEnd(ExtensionEvent ev) {
  }
}
