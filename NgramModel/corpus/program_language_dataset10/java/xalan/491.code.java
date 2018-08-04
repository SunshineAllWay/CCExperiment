package org.apache.xalan.xsltc.dom;
import org.apache.xalan.xsltc.DOM;
import org.apache.xalan.xsltc.TransletException;
import org.apache.xalan.xsltc.StripFilter;
import org.apache.xalan.xsltc.runtime.Hashtable;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.Axis;
import org.apache.xml.dtm.DTMAxisIterator;
import org.apache.xml.dtm.DTMAxisTraverser;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.ref.DTMAxisIteratorBase;
import org.apache.xml.dtm.ref.DTMManagerDefault;
import org.apache.xml.serializer.EmptySerializer;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.utils.XMLString;
import org.apache.xml.utils.XMLStringDefault;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.transform.SourceLocator;
public class SimpleResultTreeImpl extends EmptySerializer implements DOM, DTM
{
    public final class SimpleIterator extends DTMAxisIteratorBase
    {
        static final int DIRECTION_UP = 0;
        static final int DIRECTION_DOWN = 1;
        static final int NO_TYPE = -1;
        int _direction = DIRECTION_DOWN;
        int _type = NO_TYPE;
        int _currentNode;
        public SimpleIterator()
        {
        }
        public SimpleIterator(int direction)
        {
            _direction = direction;
        }
        public SimpleIterator(int direction, int type)
        {
             _direction = direction;
             _type = type;
        }
        public int next()
        {
            if (_direction == DIRECTION_DOWN) {                
                while (_currentNode < NUMBER_OF_NODES) {
                    if (_type != NO_TYPE) {
                        if ((_currentNode == RTF_ROOT && _type == DTM.ROOT_NODE)
                            || (_currentNode == RTF_TEXT && _type == DTM.TEXT_NODE))
                            return returnNode(getNodeHandle(_currentNode++));
                        else
                            _currentNode++;
                    }
                    else
                        return returnNode(getNodeHandle(_currentNode++));
                }
                return END;
            }
            else {                
                while (_currentNode >= 0) {
                    if (_type != NO_TYPE) {
                        if ((_currentNode == RTF_ROOT && _type == DTM.ROOT_NODE)
                            || (_currentNode == RTF_TEXT && _type == DTM.TEXT_NODE))
                            return returnNode(getNodeHandle(_currentNode--));
                        else
                            _currentNode--;
                    }
                    else
                        return returnNode(getNodeHandle(_currentNode--));
                }
                return END;
            }
        }
        public DTMAxisIterator setStartNode(int nodeHandle)
        {
            int nodeID = getNodeIdent(nodeHandle);
            _startNode = nodeID;
            if (!_includeSelf && nodeID != DTM.NULL) {
                if (_direction == DIRECTION_DOWN)
                    nodeID++;
                else if (_direction == DIRECTION_UP)
                    nodeID--;
            }
            _currentNode = nodeID;
            return this;
        }
        public void setMark()
        {
            _markedNode = _currentNode;
        }
        public void gotoMark()
        {
            _currentNode = _markedNode;
        }
    } 
    public final class SingletonIterator extends DTMAxisIteratorBase
    {
        static final int NO_TYPE = -1;
        int _type = NO_TYPE;
        int _currentNode;
        public SingletonIterator()
        {
        }
        public SingletonIterator(int type)
        {
            _type = type;
        }
        public void setMark()
        {
            _markedNode = _currentNode;
        }
        public void gotoMark()
        {
            _currentNode = _markedNode;
        }
        public DTMAxisIterator setStartNode(int nodeHandle)
        {
            _currentNode = _startNode = getNodeIdent(nodeHandle);
            return this;
        }
        public int next()
        {
            if (_currentNode == END)
                return END;
            _currentNode = END;
            if (_type != NO_TYPE) {
                if ((_currentNode == RTF_ROOT && _type == DTM.ROOT_NODE)
                    || (_currentNode == RTF_TEXT && _type == DTM.TEXT_NODE))
                    return getNodeHandle(_currentNode);
            }
            else
                return getNodeHandle(_currentNode);
            return END;                
        }
    }  
    private final static DTMAxisIterator EMPTY_ITERATOR =
        new DTMAxisIteratorBase() {
            public DTMAxisIterator reset() { return this; }
            public DTMAxisIterator setStartNode(int node) { return this; }
            public int next() { return DTM.NULL; }
            public void setMark() {}
            public void gotoMark() {}
            public int getLast() { return 0; }
            public int getPosition() { return 0; }
            public DTMAxisIterator cloneIterator() { return this; }
            public void setRestartable(boolean isRestartable) { }
        };
    public static final int RTF_ROOT = 0;
    public static final int RTF_TEXT = 1;
    public static final int NUMBER_OF_NODES = 2;
    private static int _documentURIIndex = 0;
    private static final String EMPTY_STR = "";
    private String _text;
    protected String[] _textArray;
    protected XSLTCDTMManager _dtmManager;
    protected int _size = 0;
    private int _documentID;
    private BitArray _dontEscape = null;
    private boolean _escaping = true;
    public SimpleResultTreeImpl(XSLTCDTMManager dtmManager, int documentID)
    {
        _dtmManager = dtmManager;
        _documentID = documentID;
        _textArray = new String[4];
    }
    public DTMManagerDefault getDTMManager()
    {
        return _dtmManager;	
    }
    public int getDocument()
    {
        return _documentID;
    }
    public String getStringValue()
    {
        return _text;
    }
    public DTMAxisIterator getIterator()
    {
        return new SingletonIterator(getDocument());
    }
    public DTMAxisIterator getChildren(final int node)
    {
        return new SimpleIterator().setStartNode(node);
    }
    public DTMAxisIterator getTypedChildren(final int type)
    {
        return new SimpleIterator(SimpleIterator.DIRECTION_DOWN, type);
    }
    public DTMAxisIterator getAxisIterator(final int axis)
    {
        switch (axis)
        {
            case Axis.CHILD:
            case Axis.DESCENDANT:
                return new SimpleIterator(SimpleIterator.DIRECTION_DOWN);
            case Axis.PARENT:
            case Axis.ANCESTOR:
                return new SimpleIterator(SimpleIterator.DIRECTION_UP);
            case Axis.ANCESTORORSELF:
                return (new SimpleIterator(SimpleIterator.DIRECTION_UP)).includeSelf();
            case Axis.DESCENDANTORSELF:
                return (new SimpleIterator(SimpleIterator.DIRECTION_DOWN)).includeSelf();
            case Axis.SELF:
                return new SingletonIterator();
            default:
                return EMPTY_ITERATOR;
        }
    }
    public DTMAxisIterator getTypedAxisIterator(final int axis, final int type)
    {
        switch (axis)
        {
            case Axis.CHILD:
            case Axis.DESCENDANT:
                return new SimpleIterator(SimpleIterator.DIRECTION_DOWN, type);
            case Axis.PARENT:
            case Axis.ANCESTOR:
                return new SimpleIterator(SimpleIterator.DIRECTION_UP, type);
            case Axis.ANCESTORORSELF:
                return (new SimpleIterator(SimpleIterator.DIRECTION_UP, type)).includeSelf();
            case Axis.DESCENDANTORSELF:
                return (new SimpleIterator(SimpleIterator.DIRECTION_DOWN, type)).includeSelf();
            case Axis.SELF:
                return new SingletonIterator(type);
            default:
                return EMPTY_ITERATOR;
        }
    }
    public DTMAxisIterator getNthDescendant(int node, int n, boolean includeself)
    {
        return null; 
    }
    public DTMAxisIterator getNamespaceAxisIterator(final int axis, final int ns)
    {
        return null;
    }
    public DTMAxisIterator getNodeValueIterator(DTMAxisIterator iter, int returnType,
					     String value, boolean op)
    {
        return null;
    }
    public DTMAxisIterator orderNodes(DTMAxisIterator source, int node)
    {
        return source;
    }
    public String getNodeName(final int node)
    {
        if (getNodeIdent(node) == RTF_TEXT)
            return "#text";
        else
            return EMPTY_STR;
    }
    public String getNodeNameX(final int node)
    {
        return EMPTY_STR;
    }
    public String getNamespaceName(final int node)
    {
        return EMPTY_STR;
    }
    public int getExpandedTypeID(final int nodeHandle)
    {
        int nodeID = getNodeIdent(nodeHandle);
        if (nodeID == RTF_TEXT)
            return DTM.TEXT_NODE;
        else if (nodeID == RTF_ROOT)
            return DTM.ROOT_NODE;
        else
            return DTM.NULL;
    }
    public int getNamespaceType(final int node)
    {
        return 0;
    }
    public int getParent(final int nodeHandle)
    {
        int nodeID = getNodeIdent(nodeHandle);
        return (nodeID == RTF_TEXT) ? getNodeHandle(RTF_ROOT) : DTM.NULL;            
    }
    public int getAttributeNode(final int gType, final int element)
    {
        return DTM.NULL;
    }
    public String getStringValueX(final int nodeHandle)
    {
        int nodeID = getNodeIdent(nodeHandle);
        if (nodeID == RTF_ROOT || nodeID == RTF_TEXT)
            return _text;
        else
            return EMPTY_STR;
    }
    public void copy(final int node, SerializationHandler handler)
	throws TransletException
    {
        characters(node, handler);
    }
    public void copy(DTMAxisIterator nodes, SerializationHandler handler)
	throws TransletException
    {
        int node;
        while ((node = nodes.next()) != DTM.NULL)
        {
            copy(node, handler);
        }
    }
    public String shallowCopy(final int node, SerializationHandler handler)
	throws TransletException
    {
        characters(node, handler);
        return null;
    }
    public boolean lessThan(final int node1, final int node2)
    {
        if (node1 == DTM.NULL) {
            return false;
        }
        else if (node2 == DTM.NULL) {
            return true;
        }
        else
            return (node1 < node2);
    }
    public void characters(final int node, SerializationHandler handler)
        throws TransletException
    {
        int nodeID = getNodeIdent(node);
        if (nodeID == RTF_ROOT || nodeID == RTF_TEXT) {
            boolean escapeBit = false;
            boolean oldEscapeSetting = false;
            try {
                for (int i = 0; i < _size; i++) {
                    if (_dontEscape != null) {
                        escapeBit = _dontEscape.getBit(i);
                        if (escapeBit) {
                            oldEscapeSetting = handler.setEscaping(false);
                        }
                    }
                    handler.characters(_textArray[i]);
                    if (escapeBit) {
                        handler.setEscaping(oldEscapeSetting);
                    }
                }
            } catch (SAXException e) {
                throw new TransletException(e);
            }
        }
    }
    public Node makeNode(int index)
    {
        return null;
    }
    public Node makeNode(DTMAxisIterator iter)
    {
        return null;
    }
    public NodeList makeNodeList(int index)
    {
        return null;
    }
    public NodeList makeNodeList(DTMAxisIterator iter)
    {
        return null;
    }
    public String getLanguage(int node)
    {
        return null;
    }
    public int getSize()
    {
        return 2;
    }
    public String getDocumentURI(int node)
    {
        return "simple_rtf" + _documentURIIndex++;
    }
    public void setFilter(StripFilter filter)
    {
    }
    public void setupMapping(String[] names, String[] uris, int[] types, String[] namespaces)
    {
    }
    public boolean isElement(final int node)
    {
        return false;
    }
    public boolean isAttribute(final int node)
    {
        return false;
    }
    public String lookupNamespace(int node, String prefix)
	throws TransletException
    {
        return null;
    }
    public int getNodeIdent(final int nodehandle)
    {
        return (nodehandle != DTM.NULL) ? (nodehandle - _documentID) : DTM.NULL;
    }
    public int getNodeHandle(final int nodeId)
    {
        return (nodeId != DTM.NULL) ? (nodeId + _documentID) : DTM.NULL;
    }
    public DOM getResultTreeFrag(int initialSize, int rtfType)
    {
        return null;
    }
    public DOM getResultTreeFrag(int initialSize, int rtfType, boolean addToManager)
    {
        return null;
    }
    public SerializationHandler getOutputDomBuilder()
    {
        return this;
    }
    public int getNSType(int node)
    {
        return 0;
    }
    public String getUnparsedEntityURI(String name)
    {
        return null;
    }
    public Hashtable getElementsWithIDs()
    {
        return null;
    }
    public void startDocument() throws SAXException
    {
    }
    public void endDocument() throws SAXException
    {
        if (_size == 1)
            _text = _textArray[0];
        else {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < _size; i++) {
                buffer.append(_textArray[i]);
            }
            _text = buffer.toString();
        }
    }
    public void characters(String str) throws SAXException
    {
        if (_size >= _textArray.length) {
            String[] newTextArray = new String[_textArray.length * 2];
            System.arraycopy(_textArray, 0, newTextArray, 0, _textArray.length);
            _textArray = newTextArray;
        }
        if (!_escaping) {
            if (_dontEscape == null) {
                _dontEscape = new BitArray(8);
            }
            if (_size >= _dontEscape.size())
                _dontEscape.resize(_dontEscape.size() * 2);
            _dontEscape.setBit(_size);
        }
        _textArray[_size++] = str;
    }
    public void characters(char[] ch, int offset, int length)
	throws SAXException
    {
        if (_size >= _textArray.length) {
            String[] newTextArray = new String[_textArray.length * 2];
            System.arraycopy(_textArray, 0, newTextArray, 0, _textArray.length);
            _textArray = newTextArray;
        }
        if (!_escaping) {
            if (_dontEscape == null) {
                _dontEscape = new BitArray(8);
            }
            if (_size >= _dontEscape.size())
                _dontEscape.resize(_dontEscape.size() * 2);
            _dontEscape.setBit(_size);
        }
        _textArray[_size++] = new String(ch, offset, length);
    }
    public boolean setEscaping(boolean escape) throws SAXException
    {
        final boolean temp = _escaping;
        _escaping = escape; 
        return temp;
    }
    public void setFeature(String featureId, boolean state)
    {
    }
    public void setProperty(String property, Object value)
    {
    }
    public DTMAxisTraverser getAxisTraverser(final int axis)
    {
        return null;
    }
    public boolean hasChildNodes(int nodeHandle)
    {
        return (getNodeIdent(nodeHandle) == RTF_ROOT);
    }
    public int getFirstChild(int nodeHandle)
    {
        int nodeID = getNodeIdent(nodeHandle);
        if (nodeID == RTF_ROOT)
            return getNodeHandle(RTF_TEXT);
        else
            return DTM.NULL;
    }
    public int getLastChild(int nodeHandle)
    {
        return getFirstChild(nodeHandle);
    }
    public int getAttributeNode(int elementHandle, String namespaceURI, String name)
    {
        return DTM.NULL;
    }
    public int getFirstAttribute(int nodeHandle)
    {
        return DTM.NULL;
    }
    public int getFirstNamespaceNode(int nodeHandle, boolean inScope)
    {
        return DTM.NULL;
    }
    public int getNextSibling(int nodeHandle)
    {
        return DTM.NULL;
    }
    public int getPreviousSibling(int nodeHandle)
    {
        return DTM.NULL;
    }
    public int getNextAttribute(int nodeHandle)
    {
        return DTM.NULL;
    }
    public int getNextNamespaceNode(int baseHandle, int namespaceHandle,
                                  boolean inScope)
    {
        return DTM.NULL;
    }
    public int getOwnerDocument(int nodeHandle)
    {
        return getDocument();
    }
    public int getDocumentRoot(int nodeHandle)
    {
        return getDocument();
    }
    public XMLString getStringValue(int nodeHandle)
    {
        return new XMLStringDefault(getStringValueX(nodeHandle));
    }
    public int getStringValueChunkCount(int nodeHandle)
    {
        return 0;
    }
    public char[] getStringValueChunk(int nodeHandle, int chunkIndex,
                                    int[] startAndLen)
    {
        return null;
    }
    public int getExpandedTypeID(String namespace, String localName, int type)
    {
        return DTM.NULL;
    }
    public String getLocalNameFromExpandedNameID(int ExpandedNameID)
    {
        return EMPTY_STR;
    }
    public String getNamespaceFromExpandedNameID(int ExpandedNameID)
    {
        return EMPTY_STR;
    }
    public String getLocalName(int nodeHandle)
    {
        return EMPTY_STR;
    }
    public String getPrefix(int nodeHandle)
    {
        return null;
    }
    public String getNamespaceURI(int nodeHandle)
    {
        return EMPTY_STR;
    }
    public String getNodeValue(int nodeHandle)
    {
        return (getNodeIdent(nodeHandle) == RTF_TEXT) ? _text : null;
    }
    public short getNodeType(int nodeHandle)
    {
        int nodeID = getNodeIdent(nodeHandle);
        if (nodeID == RTF_TEXT)
            return DTM.TEXT_NODE;
        else if (nodeID == RTF_ROOT)
            return DTM.ROOT_NODE;
        else
            return DTM.NULL;
    }
    public short getLevel(int nodeHandle)
    {
        int nodeID = getNodeIdent(nodeHandle);
        if (nodeID == RTF_TEXT)
            return 2;
        else if (nodeID == RTF_ROOT)
            return 1;
        else
            return DTM.NULL;            
    }
    public boolean isSupported(String feature, String version)
    {
        return false;
    }
    public String getDocumentBaseURI()
    {
        return EMPTY_STR;
    }
    public void setDocumentBaseURI(String baseURI)
    {
    }
    public String getDocumentSystemIdentifier(int nodeHandle)
    {
        return null;
    }
    public String getDocumentEncoding(int nodeHandle)
    {
        return null;
    }
    public String getDocumentStandalone(int nodeHandle)
    {
        return null;
    }
    public String getDocumentVersion(int documentHandle)
    {
        return null;
    }
    public boolean getDocumentAllDeclarationsProcessed()
    {
        return false;
    }
    public String getDocumentTypeDeclarationSystemIdentifier()
    {
        return null;
    }
    public String getDocumentTypeDeclarationPublicIdentifier()
    {
        return null;
    }
    public int getElementById(String elementId)
    {
        return DTM.NULL;
    }
    public boolean supportsPreStripping()
    {
        return false;
    }
    public boolean isNodeAfter(int firstNodeHandle, int secondNodeHandle)
    {
        return lessThan(firstNodeHandle, secondNodeHandle);
    }
    public boolean isCharacterElementContentWhitespace(int nodeHandle)
    {
        return false;
    }
    public boolean isDocumentAllDeclarationsProcessed(int documentHandle)
    {
        return false;
    }
    public boolean isAttributeSpecified(int attributeHandle)
    {
        return false;
    }
    public void dispatchCharactersEvents(
        int nodeHandle,
        org.xml.sax.ContentHandler ch,
        boolean normalize)
          throws org.xml.sax.SAXException
    {
    }
    public void dispatchToEvents(int nodeHandle, org.xml.sax.ContentHandler ch)
      throws org.xml.sax.SAXException
    {
    }
    public org.w3c.dom.Node getNode(int nodeHandle)
    {
        return makeNode(nodeHandle);
    }
    public boolean needsTwoThreads()
    {
        return false;
    }
    public org.xml.sax.ContentHandler getContentHandler()
    {
        return null;
    }
    public org.xml.sax.ext.LexicalHandler getLexicalHandler()
    {
        return null;
    }
    public org.xml.sax.EntityResolver getEntityResolver()
    {
        return null;
    }
    public org.xml.sax.DTDHandler getDTDHandler()
    {
        return null;
    }
    public org.xml.sax.ErrorHandler getErrorHandler()
    {
        return null;
    }
    public org.xml.sax.ext.DeclHandler getDeclHandler()
    {
        return null;
    }
    public void appendChild(int newChild, boolean clone, boolean cloneDepth)
    {
    }
    public void appendTextChild(String str)
    {
    }
    public SourceLocator getSourceLocatorFor(int node)
    {
    	return null;
    }
    public void documentRegistration()
    {
    }
    public void documentRelease()
    {
    }
    public void migrateTo(DTMManager manager)
    {
    }
}