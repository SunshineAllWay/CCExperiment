 package org.apache.xml.serializer;
import java.io.IOException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import org.apache.xml.serializer.utils.MsgKey;
import org.apache.xml.serializer.utils.Utils;
import org.xml.sax.SAXException;
public class ToXMLStream extends ToStream
{
    private CharInfo m_xmlcharInfo =
        CharInfo.getCharInfo(CharInfo.XML_ENTITIES_RESOURCE, Method.XML);
    public ToXMLStream()
    {
        m_charInfo = m_xmlcharInfo;
        initCDATA();
        m_prefixMap = new NamespaceMappings();
    }
    public void CopyFrom(ToXMLStream xmlListener)
    {
        setWriter(xmlListener.m_writer);
        String encoding = xmlListener.getEncoding();
        setEncoding(encoding);
        setOmitXMLDeclaration(xmlListener.getOmitXMLDeclaration());
        m_ispreserve = xmlListener.m_ispreserve;
        m_preserves = xmlListener.m_preserves;
        m_isprevtext = xmlListener.m_isprevtext;
        m_doIndent = xmlListener.m_doIndent;
        setIndentAmount(xmlListener.getIndentAmount());
        m_startNewLine = xmlListener.m_startNewLine;
        m_needToOutputDocTypeDecl = xmlListener.m_needToOutputDocTypeDecl;
        setDoctypeSystem(xmlListener.getDoctypeSystem());
        setDoctypePublic(xmlListener.getDoctypePublic());        
        setStandalone(xmlListener.getStandalone());
        setMediaType(xmlListener.getMediaType());
        m_encodingInfo = xmlListener.m_encodingInfo;
        m_spaceBeforeClose = xmlListener.m_spaceBeforeClose;
        m_cdataStartCalled = xmlListener.m_cdataStartCalled;
    }
    public void startDocumentInternal() throws org.xml.sax.SAXException
    {
        if (m_needToCallStartDocument)
        { 
            super.startDocumentInternal();
            m_needToCallStartDocument = false;
            if (m_inEntityRef)
                return;
            m_needToOutputDocTypeDecl = true;
            m_startNewLine = false;
            final String version = getXMLVersion();
            if (getOmitXMLDeclaration() == false)
            {
                String encoding = Encodings.getMimeEncoding(getEncoding());
                String standalone;
                if (m_standaloneWasSpecified)
                {
                    standalone = " standalone=\"" + getStandalone() + "\"";
                }
                else
                {
                    standalone = "";
                }
                try
                {
                    final java.io.Writer writer = m_writer;
                    writer.write("<?xml version=\"");
                    writer.write(version);
                    writer.write("\" encoding=\"");
                    writer.write(encoding);
                    writer.write('\"');
                    writer.write(standalone);
                    writer.write("?>");
                    if (m_doIndent) {
                        if (m_standaloneWasSpecified
                                || getDoctypePublic() != null
                                || getDoctypeSystem() != null) {
                            writer.write(m_lineSep, 0, m_lineSepLen);
                        }
                    }
                } 
                catch(IOException e)
                {
                    throw new SAXException(e);
                }
            }
        }
    }
    public void endDocument() throws org.xml.sax.SAXException
    {
        flushPending();
        if (m_doIndent && !m_isprevtext)
        {
            try
            {
            outputLineSep();
            }
            catch(IOException e)
            {
                throw new SAXException(e);
            }
        }
        flushWriter();
        if (m_tracer != null)
            super.fireEndDoc();
    }
    public void startPreserving() throws org.xml.sax.SAXException
    {
        m_preserves.push(true);
        m_ispreserve = true;
    }
    public void endPreserving() throws org.xml.sax.SAXException
    {
        m_ispreserve = m_preserves.isEmpty() ? false : m_preserves.pop();
    }
    public void processingInstruction(String target, String data)
        throws org.xml.sax.SAXException
    {
        if (m_inEntityRef)
            return;
        flushPending();   
        if (target.equals(Result.PI_DISABLE_OUTPUT_ESCAPING))
        {
            startNonEscaping();
        }
        else if (target.equals(Result.PI_ENABLE_OUTPUT_ESCAPING))
        {
            endNonEscaping();
        }
        else
        {
            try
            {
                if (m_elemContext.m_startTagOpen)
                {
                    closeStartTag();
                    m_elemContext.m_startTagOpen = false;
                }
                else if (m_needToCallStartDocument)
                    startDocumentInternal();                
                if (shouldIndent())
                    indent();
                final java.io.Writer writer = m_writer;
                writer.write("<?");
                writer.write(target);
                if (data.length() > 0
                    && !Character.isSpaceChar(data.charAt(0)))
                    writer.write(' ');
                int indexOfQLT = data.indexOf("?>");
                if (indexOfQLT >= 0)
                {
                    if (indexOfQLT > 0)
                    {
                        writer.write(data.substring(0, indexOfQLT));
                    }
                    writer.write("? >"); 
                    if ((indexOfQLT + 2) < data.length())
                    {
                        writer.write(data.substring(indexOfQLT + 2));
                    }
                }
                else
                {
                    writer.write(data);
                }
                writer.write('?');
                writer.write('>');
                m_startNewLine = true;
            }
            catch(IOException e)
            {
                throw new SAXException(e);
            }
        }
        if (m_tracer != null)
            super.fireEscapingEvent(target, data);  
    }
    public void entityReference(String name) throws org.xml.sax.SAXException
    {
        if (m_elemContext.m_startTagOpen)
        {
            closeStartTag();
            m_elemContext.m_startTagOpen = false;
        }
        try
        {
            if (shouldIndent())
                indent();
            final java.io.Writer writer = m_writer;
            writer.write('&');
            writer.write(name);
            writer.write(';');
        }
        catch(IOException e)
        {
            throw new SAXException(e);
        }
        if (m_tracer != null)
            super.fireEntityReference(name);            
    }
    public void addUniqueAttribute(String name, String value, int flags)
        throws SAXException
    {
        if (m_elemContext.m_startTagOpen)
        {
            try
            {
                final String patchedName = patchName(name);
                final java.io.Writer writer = m_writer;
                if ((flags & NO_BAD_CHARS) > 0 && m_xmlcharInfo.onlyQuotAmpLtGt)
                {
                    writer.write(' ');
                    writer.write(patchedName);
                    writer.write("=\"");
                    writer.write(value);
                    writer.write('"');
                }
                else
                {
                    writer.write(' ');
                    writer.write(patchedName);
                    writer.write("=\"");
                    writeAttrString(writer, value, this.getEncoding());
                    writer.write('"');
                }
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }
    public void addAttribute(
        String uri,
        String localName,
        String rawName,
        String type,
        String value,
        boolean xslAttribute)
        throws SAXException
    {
        if (m_elemContext.m_startTagOpen)
        {
            boolean was_added = addAttributeAlways(uri, localName, rawName, type, value, xslAttribute);
            if (was_added && !xslAttribute && !rawName.startsWith("xmlns"))
            {
                String prefixUsed =
                    ensureAttributesNamespaceIsDeclared(
                        uri,
                        localName,
                        rawName);
                if (prefixUsed != null
                    && rawName != null
                    && !rawName.startsWith(prefixUsed))
                {
                    rawName = prefixUsed + ":" + localName;
                }
            }
            addAttributeAlways(uri, localName, rawName, type, value, xslAttribute);
        }
        else
        {
            String msg = Utils.messages.createMessage(
                    MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION,new Object[]{ localName });
            try {
                Transformer tran = super.getTransformer();
                ErrorListener errHandler = tran.getErrorListener();
                if (null != errHandler && m_sourceLocator != null)
                  errHandler.warning(new TransformerException(msg, m_sourceLocator));
                else
                  System.out.println(msg);
                }
            catch (TransformerException e){
                SAXException se = new SAXException(e);
                throw se;                
            }             
        }
    }
    public void endElement(String elemName) throws SAXException
    {
        endElement(null, null, elemName);
    }
    public void namespaceAfterStartElement(
        final String prefix,
        final String uri)
        throws SAXException
    {
        if (m_elemContext.m_elementURI == null)
        {
            String prefix1 = getPrefixPart(m_elemContext.m_elementName);
            if (prefix1 == null && EMPTYSTRING.equals(prefix))
            {
                m_elemContext.m_elementURI = uri;
            }
        }            
        startPrefixMapping(prefix,uri,false);
        return;
    }
    protected boolean pushNamespace(String prefix, String uri)
    {
        try
        {
            if (m_prefixMap.pushNamespace(
                prefix, uri, m_elemContext.m_currentElemDepth))
            {
                startPrefixMapping(prefix, uri);
                return true;
            }
        }
        catch (SAXException e)
        {
        }
        return false;
    }
    public boolean reset()
    {
        boolean wasReset = false;
        if (super.reset())
        {
            wasReset = true;
        }
        return wasReset;
    }
    private void resetToXMLStream()
    {
        return;
    }  
    private String getXMLVersion()
    {
        String xmlVersion = getVersion();
        if(xmlVersion == null || xmlVersion.equals(XMLVERSION10))
        {
            xmlVersion = XMLVERSION10;
        }
        else if(xmlVersion.equals(XMLVERSION11))
        {
            xmlVersion = XMLVERSION11;
        }
        else
        {
            String msg = Utils.messages.createMessage(
                               MsgKey.ER_XML_VERSION_NOT_SUPPORTED,new Object[]{ xmlVersion });
            try 
            {
                Transformer tran = super.getTransformer();
                ErrorListener errHandler = tran.getErrorListener();
                if (null != errHandler && m_sourceLocator != null)
                    errHandler.warning(new TransformerException(msg, m_sourceLocator));
                else
                    System.out.println(msg);
            }
            catch (Exception e){}
            xmlVersion = XMLVERSION10;								
        }
        return xmlVersion;
    }
}
