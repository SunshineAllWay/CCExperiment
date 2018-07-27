package org.apache.xalan.xsltc.dom;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Enumeration;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import org.apache.xalan.xsltc.DOM;
import org.apache.xalan.xsltc.DOMEnhancedForDTM;
import org.apache.xalan.xsltc.StripFilter;
import org.apache.xalan.xsltc.TransletException;
import org.apache.xalan.xsltc.runtime.BasisLibrary;
import org.apache.xalan.xsltc.runtime.Hashtable;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.Axis;
import org.apache.xml.dtm.DTMAxisIterator;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.DTMWSFilter;
import org.apache.xml.dtm.ref.DTMAxisIterNodeList;
import org.apache.xml.dtm.ref.DTMDefaultBase;
import org.apache.xml.dtm.ref.EmptyIterator;
import org.apache.xml.dtm.ref.DTMNodeProxy;
import org.apache.xml.dtm.ref.sax2dtm.SAX2DTM2;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.serializer.ToXMLSAXHandler;
import org.apache.xml.utils.XMLStringFactory;
import org.apache.xml.utils.SystemIDResolver;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Entity;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
public final class SAXImpl extends SAX2DTM2
                           implements DOMEnhancedForDTM, DOMBuilder
{
    private int       _uriCount     = 0;
    private int       _prefixCount  = 0;
    private int[]   _xmlSpaceStack;
    private int     _idx = 1;
    private boolean _preserve = false;
    private static final String XML_STRING = "xml:";
    private static final String XML_PREFIX   = "xml";   
    private static final String XMLSPACE_STRING = "xml:space";
    private static final String PRESERVE_STRING = "preserve";
    private static final String XMLNS_PREFIX = "xmlns";
    private static final String XML_URI = "http://www.w3.org/XML/1998/namespace";
    private boolean _escaping = true;
    private boolean _disableEscaping = false;
    private int _textNodeToProcess = DTM.NULL;
    private final static String EMPTYSTRING = "";
    private final static DTMAxisIterator EMPTYITERATOR = EmptyIterator.getInstance();
    private int _namesSize = -1;
    private Hashtable _nsIndex = new Hashtable();
    private int _size = 0;
    private BitArray  _dontEscape = null;
    private String    _documentURI = null;
    static private int _documentURIIndex = 0;
    private Document _document;
    private Hashtable _node2Ids = null;
    private boolean _hasDOMSource = false;
    private XSLTCDTMManager _dtmManager;
    private Node[] _nodes;
    private NodeList[] _nodeLists;
    private final static String XML_LANG_ATTRIBUTE =
        "http://www.w3.org/XML/1998/namespace:@lang";
    public void setDocumentURI(String uri) {
        if (uri != null) {
            setDocumentBaseURI(SystemIDResolver.getAbsoluteURI(uri));
        }
    }
    public String getDocumentURI() {
        String baseURI = getDocumentBaseURI();
        return (baseURI != null) ? baseURI : "rtf" + _documentURIIndex++;
    }
    public String getDocumentURI(int node) {
        return getDocumentURI();
    }
    public void setupMapping(String[] names, String[] urisArray,
                             int[] typesArray, String[] namespaces) {
    }
    public String lookupNamespace(int node, String prefix)
        throws TransletException
    {
        int anode, nsnode;
        final AncestorIterator ancestors = new AncestorIterator();
        if (isElement(node)) {
            ancestors.includeSelf();
        }
        ancestors.setStartNode(node);
        while ((anode = ancestors.next()) != DTM.NULL) {
            final NamespaceIterator namespaces = new NamespaceIterator();
            namespaces.setStartNode(anode);
            while ((nsnode = namespaces.next()) != DTM.NULL) {
                if (getLocalName(nsnode).equals(prefix)) {
                    return getNodeValue(nsnode);
                }
            }
        }
        BasisLibrary.runTimeError(BasisLibrary.NAMESPACE_PREFIX_ERR, prefix);
        return null;
    }
    public boolean isElement(final int node) {
        return getNodeType(node) == DTM.ELEMENT_NODE;
    }
    public boolean isAttribute(final int node) {
        return getNodeType(node) == DTM.ATTRIBUTE_NODE;
    }
    public int getSize() {
        return getNumberOfNodes();
    }
    public void setFilter(StripFilter filter) {
    }
    public boolean lessThan(int node1, int node2) {
        if (node1 == DTM.NULL) {
            return false;
        }
        if (node2 == DTM.NULL) {
            return true;
        }
        return (node1 < node2);
    }
    public Node makeNode(int index) {
        if (_nodes == null) {
            _nodes = new Node[_namesSize];
        }
        int nodeID = makeNodeIdentity(index);
        if (nodeID < 0) {
            return null;
        }
        else if (nodeID < _nodes.length) {
            return (_nodes[nodeID] != null) ? _nodes[nodeID] 
                : (_nodes[nodeID] = new DTMNodeProxy((DTM)this, index));
        }
        else {
            return new DTMNodeProxy((DTM)this, index);
        }
    }
    public Node makeNode(DTMAxisIterator iter) {
        return makeNode(iter.next());
    }
    public NodeList makeNodeList(int index) {
        if (_nodeLists == null) {
            _nodeLists = new NodeList[_namesSize];
        }
        int nodeID = makeNodeIdentity(index);
        if (nodeID < 0) {
            return null;
        }
        else if (nodeID < _nodeLists.length) {
            return (_nodeLists[nodeID] != null) ? _nodeLists[nodeID]
                   : (_nodeLists[nodeID] = new DTMAxisIterNodeList(this,
                                                 new SingletonIterator(index)));
    }
        else {
            return new DTMAxisIterNodeList(this, new SingletonIterator(index));
        }
    }
    public NodeList makeNodeList(DTMAxisIterator iter) {
        return new DTMAxisIterNodeList(this, iter);
    }
    public class TypedNamespaceIterator extends NamespaceIterator {
        private  String _nsPrefix;
        public TypedNamespaceIterator(int nodeType) { 
            super();
            if(m_expandedNameTable != null){
                _nsPrefix = m_expandedNameTable.getLocalName(nodeType);
            }
        }
        public int next() {
            if ((_nsPrefix == null) ||(_nsPrefix.length() == 0) ){
                return (END);
            }          
            int node = END;
            for (node = super.next(); node != END; node = super.next()) {
                if (_nsPrefix.compareTo(getLocalName(node))== 0) {
                    return returnNode(node);
                }
            }
            return (END);
        }
    }  
    private final class NodeValueIterator extends InternalAxisIteratorBase
    {
	private DTMAxisIterator _source;
	private String _value;
	private boolean _op;
	private final boolean _isReverse;
	private int _returnType = RETURN_PARENT;
	public NodeValueIterator(DTMAxisIterator source, int returnType,
				 String value, boolean op)
        {
	    _source = source;
	    _returnType = returnType;
	    _value = value;
	    _op = op;
	    _isReverse = source.isReverse();
	}
	public boolean isReverse()
        {
	    return _isReverse;
	}
        public DTMAxisIterator cloneIterator()
        {
            try {
                NodeValueIterator clone = (NodeValueIterator)super.clone();
                clone._isRestartable = false;
                clone._source = _source.cloneIterator();
                clone._value = _value;
                clone._op = _op;
                return clone.reset();
            }
            catch (CloneNotSupportedException e) {
                BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR,
                                          e.toString());
                return null;
            }
        }
        public void setRestartable(boolean isRestartable)
        {
	    _isRestartable = isRestartable;
	    _source.setRestartable(isRestartable);
	}
	public DTMAxisIterator reset()
        {
	    _source.reset();
	    return resetPosition();
	}
	public int next()
        {
            int node;
            while ((node = _source.next()) != END) {
                String val = getStringValueX(node);
                if (_value.equals(val) == _op) {
                    if (_returnType == RETURN_CURRENT) {
                        return returnNode(node);
                    }
                    else {
                        return returnNode(getParent(node));
                    }
                }
            }
            return END;
        }
	public DTMAxisIterator setStartNode(int node)
        {
            if (_isRestartable) {
                _source.setStartNode(_startNode = node);
                return resetPosition();
            }
            return this;
        }
	public void setMark()
        {
	    _source.setMark();
	}
	public void gotoMark()
        {
	    _source.gotoMark();
	}
    } 
    public DTMAxisIterator getNodeValueIterator(DTMAxisIterator iterator, int type,
					     String value, boolean op)
    {
        return(DTMAxisIterator)(new NodeValueIterator(iterator, type, value, op));
    }
    public DTMAxisIterator orderNodes(DTMAxisIterator source, int node)
    {
        return new DupFilterIterator(source);
    }
    public DTMAxisIterator getIterator()
    {
        return new SingletonIterator(getDocument(), true);
    }
    public int getNSType(int node)
    {
    	String s = getNamespaceURI(node);
    	if (s == null) {
    	    return 0;
    	}
    	int eType = getIdForNamespace(s);
    	return ((Integer)_nsIndex.get(new Integer(eType))).intValue();        
    }
    public int getNamespaceType(final int node)
    {
    	return super.getNamespaceType(node);
    }
    private int[] setupMapping(String[] names, String[] uris, int[] types, int nNames) {
        final int[] result = new int[m_expandedNameTable.getSize()];
        for (int i = 0; i < nNames; i++)      {
            int type = m_expandedNameTable.getExpandedTypeID(uris[i], names[i], types[i], false);
            result[type] = type;
        }
        return result;
    }
    public int getGeneralizedType(final String name) {
        return getGeneralizedType(name, true);
    }
    public int getGeneralizedType(final String name, boolean searchOnly) {
        String lName, ns = null;
        int index = -1;
        int code;
        if ((index = name.lastIndexOf(":"))> -1) {
            ns = name.substring(0, index);
        }
        int lNameStartIdx = index+1;
        if (name.charAt(lNameStartIdx) == '@') {
            code = DTM.ATTRIBUTE_NODE;
            lNameStartIdx++;
        }
        else {
            code = DTM.ELEMENT_NODE;
        }
        lName = (lNameStartIdx == 0) ? name : name.substring(lNameStartIdx);
        return m_expandedNameTable.getExpandedTypeID(ns, lName, code, searchOnly);
    }
    public short[] getMapping(String[] names, String[] uris, int[] types)
    {
        if (_namesSize < 0) {
            return getMapping2(names, uris, types);
        }
        int i;
        final int namesLength = names.length;
        final int exLength = m_expandedNameTable.getSize();
        final short[] result = new short[exLength];
        for (i = 0; i < DTM.NTYPES; i++) {
            result[i] = (short)i;
        }
        for (i = NTYPES; i < exLength; i++) { 
      	    result[i] = m_expandedNameTable.getType(i);
      	}
        for (i = 0; i < namesLength; i++) {
            int genType = m_expandedNameTable.getExpandedTypeID(uris[i],
                                                                names[i],
                                                                types[i],
                                                                true);
            if (genType >= 0 && genType < exLength) {
                result[genType] = (short)(i + DTM.NTYPES);
            }
        }
        return result;
    }
    public int[] getReverseMapping(String[] names, String[] uris, int[] types)
    {
        int i;
        final int[] result = new int[names.length + DTM.NTYPES];
        for (i = 0; i < DTM.NTYPES; i++) {
            result[i] = i;
        }
        for (i = 0; i < names.length; i++) {
            int type = m_expandedNameTable.getExpandedTypeID(uris[i], names[i], types[i], true);
            result[i+DTM.NTYPES] = type;
        }
        return(result);
    }
    private short[] getMapping2(String[] names, String[] uris, int[] types)
    {
        int i;
        final int namesLength = names.length;
        final int exLength = m_expandedNameTable.getSize();
        int[] generalizedTypes = null;
        if (namesLength > 0) {
            generalizedTypes = new int[namesLength];
        }
        int resultLength = exLength;
        for (i = 0; i < namesLength; i++) {
            generalizedTypes[i] =
                m_expandedNameTable.getExpandedTypeID(uris[i],
                                                      names[i],
                                                      types[i],
                                                      false);
            if (_namesSize < 0 && generalizedTypes[i] >= resultLength) {
                resultLength = generalizedTypes[i] + 1;
            }
        }
        final short[] result = new short[resultLength];
        for (i = 0; i < DTM.NTYPES; i++) {
            result[i] = (short)i;
        }
        for (i = NTYPES; i < exLength; i++) {
            result[i] = m_expandedNameTable.getType(i);
        }
        for (i = 0; i < namesLength; i++) {
            int genType = generalizedTypes[i];
            if (genType >= 0 && genType < resultLength) {
                result[genType] = (short)(i + DTM.NTYPES);
            }
        }
        return(result);
    }
    public short[] getNamespaceMapping(String[] namespaces)
    {
        int i;
        final int nsLength = namespaces.length;
        final int mappingLength = _uriCount;
        final short[] result = new short[mappingLength];
        for (i=0; i<mappingLength; i++) {
            result[i] = (short)(-1);
        }
        for (i=0; i<nsLength; i++) {
            int eType = getIdForNamespace(namespaces[i]); 
            Integer type = (Integer)_nsIndex.get(new Integer(eType));
            if (type != null) {
                result[type.intValue()] = (short)i;
            }
        }
        return(result);
    }
    public short[] getReverseNamespaceMapping(String[] namespaces)
    {
        int i;
        final int length = namespaces.length;
        final short[] result = new short[length];
        for (i = 0; i < length; i++) {
            int eType = getIdForNamespace(namespaces[i]);
            Integer type = (Integer)_nsIndex.get(new Integer(eType));
            result[i] = (type == null) ? -1 : type.shortValue();
        }
        return result;
    }
    public SAXImpl(XSLTCDTMManager mgr, Source source,
                   int dtmIdentity, DTMWSFilter whiteSpaceFilter,
                   XMLStringFactory xstringfactory,
                   boolean doIndexing, boolean buildIdIndex)
    {
        this(mgr, source, dtmIdentity, whiteSpaceFilter, xstringfactory,
            doIndexing, DEFAULT_BLOCKSIZE, buildIdIndex, false);
    }
    public SAXImpl(XSLTCDTMManager mgr, Source source,
                   int dtmIdentity, DTMWSFilter whiteSpaceFilter,
                   XMLStringFactory xstringfactory,
                   boolean doIndexing, int blocksize, 
                   boolean buildIdIndex,
                   boolean newNameTable)
    {
        super(mgr, source, dtmIdentity, whiteSpaceFilter, xstringfactory,
            doIndexing, blocksize, false, buildIdIndex, newNameTable);
        _dtmManager = mgr;      
        _size = blocksize;
        _xmlSpaceStack = new int[blocksize <= 64 ? 4 : 64];
        _xmlSpaceStack[0] = DTMDefaultBase.ROOTNODE;
        if (source instanceof DOMSource) {
            _hasDOMSource = true;
            DOMSource domsrc = (DOMSource)source;
            Node node = domsrc.getNode();
            if (node instanceof Document) {
                _document = (Document)node;
            }
            else {
                _document = node.getOwnerDocument();
            }
            _node2Ids = new Hashtable();
        }                          
    }
    public void migrateTo(DTMManager manager) {
    	super.migrateTo(manager);
    	if (manager instanceof XSLTCDTMManager) {
    	    _dtmManager = (XSLTCDTMManager)manager;
    	}
    }
    public int getElementById(String idString)
    {
        Node node = _document.getElementById(idString);
        if (node != null) {
            Integer id = (Integer)_node2Ids.get(node);
            return (id != null) ? id.intValue() : DTM.NULL;
        }
        else {
            return DTM.NULL;
        }
    }
    public boolean hasDOMSource()
    {
        return _hasDOMSource;	
    }
    private void xmlSpaceDefine(String val, final int node)
    {
        final boolean setting = val.equals(PRESERVE_STRING);
        if (setting != _preserve) {
            _xmlSpaceStack[_idx++] = node;
            _preserve = setting;
        }
    }
    private void xmlSpaceRevert(final int node)
    {
        if (node == _xmlSpaceStack[_idx - 1]) {
            _idx--;
            _preserve = !_preserve;
        }
    }
    protected boolean getShouldStripWhitespace()
    {
        return _preserve ? false : super.getShouldStripWhitespace();
    }
    private void handleTextEscaping() {
        if (_disableEscaping && _textNodeToProcess != DTM.NULL
            && _type(_textNodeToProcess) == DTM.TEXT_NODE) {
            if (_dontEscape == null) {
                _dontEscape = new BitArray(_size);
            }
            if (_textNodeToProcess >= _dontEscape.size()) {
                _dontEscape.resize(_dontEscape.size() * 2);
            }
            _dontEscape.setBit(_textNodeToProcess);
            _disableEscaping = false;
        }
        _textNodeToProcess = DTM.NULL;
    }
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        super.characters(ch, start, length);
        _disableEscaping = !_escaping;  
        _textNodeToProcess = getNumberOfNodes();
    }
    public void startDocument() throws SAXException
    {
        super.startDocument();
        _nsIndex.put(new Integer(0), new Integer(_uriCount++));
        definePrefixAndUri(XML_PREFIX, XML_URI);
    }
    public void endDocument() throws SAXException
    {
        super.endDocument();
        handleTextEscaping();
        _namesSize = m_expandedNameTable.getSize();
    }
    public void startElement(String uri, String localName,
                             String qname, Attributes attributes,
                             Node node)
        throws SAXException
    {
    	this.startElement(uri, localName, qname, attributes);
    	if (m_buildIdIndex) {
    	    _node2Ids.put(node, new Integer(m_parents.peek()));
    	}
    }
    public void startElement(String uri, String localName,
                 String qname, Attributes attributes)
        throws SAXException
    {
        super.startElement(uri, localName, qname, attributes);
        handleTextEscaping();
        if (m_wsfilter != null) {
            final int index = attributes.getIndex(XMLSPACE_STRING);
            if (index >= 0) {
                xmlSpaceDefine(attributes.getValue(index), m_parents.peek());
            }
        }
    }
    public void endElement(String namespaceURI, String localName, String qname)
        throws SAXException
    {
        super.endElement(namespaceURI, localName, qname);
        handleTextEscaping();
        if (m_wsfilter != null) {
            xmlSpaceRevert(m_previous);
        }
    }
    public void processingInstruction(String target, String data)
        throws SAXException
    {
        super.processingInstruction(target, data);
        handleTextEscaping();
    }
    public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException
    {
        super.ignorableWhitespace(ch, start, length);
        _textNodeToProcess = getNumberOfNodes();
    }
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException
    {
        super.startPrefixMapping(prefix, uri);
        handleTextEscaping();
        definePrefixAndUri(prefix, uri);
    }
    private void definePrefixAndUri(String prefix, String uri) 
        throws SAXException 
    {
        Integer eType = new Integer(getIdForNamespace(uri));
        if ((Integer)_nsIndex.get(eType) == null) {
            _nsIndex.put(eType, new Integer(_uriCount++));
        }
    }
    public void comment(char[] ch, int start, int length)
        throws SAXException
    {
        super.comment(ch, start, length);
        handleTextEscaping();
    }
    public boolean setEscaping(boolean value) {
        final boolean temp = _escaping;
        _escaping = value; 
        return temp;
    }
    public void print(int node, int level)
    {
        switch(getNodeType(node))
        {
	    case DTM.ROOT_NODE:
	    case DTM.DOCUMENT_NODE:
	        print(getFirstChild(node), level);
	        break;
	    case DTM.TEXT_NODE:
	    case DTM.COMMENT_NODE:
	    case DTM.PROCESSING_INSTRUCTION_NODE:
	        System.out.print(getStringValueX(node));
	        break;
	    default:
	        final String name = getNodeName(node);
	        System.out.print("<" + name);
	        for (int a = getFirstAttribute(node); a != DTM.NULL; a = getNextAttribute(a))
                {
		    System.out.print("\n" + getNodeName(a) + "=\"" + getStringValueX(a) + "\"");
	        }
	        System.out.print('>');
	        for (int child = getFirstChild(node); child != DTM.NULL;
		    child = getNextSibling(child)) {
		    print(child, level + 1);
	        }
	        System.out.println("</" + name + '>');
	        break;
	}
    }
    public String getNodeName(final int node)
    {
	int nodeh = node;
	final short type = getNodeType(nodeh);
	switch(type)
        {
	    case DTM.ROOT_NODE:
	    case DTM.DOCUMENT_NODE:
	    case DTM.TEXT_NODE:
	    case DTM.COMMENT_NODE:
	        return EMPTYSTRING;
	    case DTM.NAMESPACE_NODE:
		return this.getLocalName(nodeh);
	    default:
	        return super.getNodeName(nodeh);
	}
    }    
    public String getNamespaceName(final int node)
    {
    	if (node == DTM.NULL) {
    	    return "";
    	}
        String s;
        return (s = getNamespaceURI(node)) == null ? EMPTYSTRING : s;
    }
    public int getAttributeNode(final int type, final int element)
    {
        for (int attr = getFirstAttribute(element);
           attr != DTM.NULL;
           attr = getNextAttribute(attr))
        {
            if (getExpandedTypeID(attr) == type) return attr;
        }
        return DTM.NULL;
    }
    public String getAttributeValue(final int type, final int element)
    {
        final int attr = getAttributeNode(type, element);
        return (attr != DTM.NULL) ? getStringValueX(attr) : EMPTYSTRING;
    }
    public String getAttributeValue(final String name, final int element)
    {
        return getAttributeValue(getGeneralizedType(name), element);
    }
    public DTMAxisIterator getChildren(final int node)
    {
        return (new ChildrenIterator()).setStartNode(node);
    }
    public DTMAxisIterator getTypedChildren(final int type)
    {
        return(new TypedChildrenIterator(type));
    }
    public DTMAxisIterator getAxisIterator(final int axis)
    {
        switch (axis)
        {
            case Axis.SELF:
                return new SingletonIterator();
            case Axis.CHILD:
                return new ChildrenIterator();
            case Axis.PARENT:
                return new ParentIterator();
            case Axis.ANCESTOR:
                return new AncestorIterator();
            case Axis.ANCESTORORSELF:
                return (new AncestorIterator()).includeSelf();
            case Axis.ATTRIBUTE:
                return new AttributeIterator();
            case Axis.DESCENDANT:
                return new DescendantIterator();
            case Axis.DESCENDANTORSELF:
                return (new DescendantIterator()).includeSelf();
            case Axis.FOLLOWING:
                return new FollowingIterator();
            case Axis.PRECEDING:
                return new PrecedingIterator();
            case Axis.FOLLOWINGSIBLING:
                return new FollowingSiblingIterator();
            case Axis.PRECEDINGSIBLING:
                return new PrecedingSiblingIterator();
            case Axis.NAMESPACE:
                return new NamespaceIterator();
            case Axis.ROOT:
                return new RootIterator();
            default:
                BasisLibrary.runTimeError(BasisLibrary.AXIS_SUPPORT_ERR, 
                        Axis.getNames(axis));
        }
        return null;
    }
    public DTMAxisIterator getTypedAxisIterator(int axis, int type)
    {
        if (axis == Axis.CHILD) {
            return new TypedChildrenIterator(type);
        }
        if (type == NO_TYPE) {
            return(EMPTYITERATOR);
        }
        switch (axis)
        {
            case Axis.SELF:
                return new TypedSingletonIterator(type);
            case Axis.CHILD:
                return new TypedChildrenIterator(type);
            case Axis.PARENT:
                return new ParentIterator().setNodeType(type);
            case Axis.ANCESTOR:
                return new TypedAncestorIterator(type);
            case Axis.ANCESTORORSELF:
                return (new TypedAncestorIterator(type)).includeSelf();
            case Axis.ATTRIBUTE:
                return new TypedAttributeIterator(type);
            case Axis.DESCENDANT:
                return new TypedDescendantIterator(type);
            case Axis.DESCENDANTORSELF:
                return (new TypedDescendantIterator(type)).includeSelf();
            case Axis.FOLLOWING:
                return new TypedFollowingIterator(type);
            case Axis.PRECEDING:
                return new TypedPrecedingIterator(type);
            case Axis.FOLLOWINGSIBLING:
                return new TypedFollowingSiblingIterator(type);
            case Axis.PRECEDINGSIBLING:
                return new TypedPrecedingSiblingIterator(type);
            case Axis.NAMESPACE:
                return  new TypedNamespaceIterator(type);
            case Axis.ROOT:
                return new TypedRootIterator(type);
            default:
                BasisLibrary.runTimeError(BasisLibrary.TYPED_AXIS_SUPPORT_ERR, 
                        Axis.getNames(axis));
        }
        return null;
    }
    public DTMAxisIterator getNamespaceAxisIterator(int axis, int ns)
    {
        DTMAxisIterator iterator = null;
        if (ns == NO_TYPE) {
            return EMPTYITERATOR;
        }
        else {
            switch (axis) {
                case Axis.CHILD:
                    return new NamespaceChildrenIterator(ns);
                case Axis.ATTRIBUTE:
                    return new NamespaceAttributeIterator(ns);
                default:
                    return new NamespaceWildcardIterator(axis, ns);
            }
        }
    }
    public final class NamespaceWildcardIterator
        extends InternalAxisIteratorBase
    {
        protected int m_nsType;
        protected DTMAxisIterator m_baseIterator;
        public NamespaceWildcardIterator(int axis, int nsType) {
            m_nsType = nsType;
            switch (axis) {
                case Axis.ATTRIBUTE: {
                    m_baseIterator = getAxisIterator(axis);
                }
                case Axis.NAMESPACE: {
                    m_baseIterator = getAxisIterator(axis);
                }
                default: {
                    m_baseIterator = getTypedAxisIterator(axis,
                                                          DTM.ELEMENT_NODE);
                }
            }
        }
        public DTMAxisIterator setStartNode(int node) {
            if (_isRestartable) {
                _startNode = node;
                m_baseIterator.setStartNode(node);
                resetPosition();
            }
            return this;
        }
        public int next() {
            int node;
            while ((node = m_baseIterator.next()) != END) {
                if (getNSType(node) == m_nsType) {
                    return returnNode(node);
                }
            }
            return END;
        }
        public DTMAxisIterator cloneIterator() {
            try {
                DTMAxisIterator nestedClone = m_baseIterator.cloneIterator();
                NamespaceWildcardIterator clone =
                    (NamespaceWildcardIterator) super.clone();
                clone.m_baseIterator = nestedClone;
                clone.m_nsType = m_nsType;
                clone._isRestartable = false;
                return clone;
            } catch (CloneNotSupportedException e) {
                BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR,
                                          e.toString());
                return null;
            }
        }
        public boolean isReverse() {
            return m_baseIterator.isReverse();
        }
        public void setMark() {
            m_baseIterator.setMark();
        }
        public void gotoMark() {
            m_baseIterator.gotoMark();
        }
    }
    public final class NamespaceChildrenIterator
        extends InternalAxisIteratorBase
    {
        private final int _nsType;
        public NamespaceChildrenIterator(final int type) {
            _nsType = type;
        }
        public DTMAxisIterator setStartNode(int node) {
            if (node == DTMDefaultBase.ROOTNODE) {
                node = getDocument();
            }
            if (_isRestartable) {
                _startNode = node;
                _currentNode = (node == DTM.NULL) ? DTM.NULL : NOTPROCESSED;
                return resetPosition();
            }
            return this;
        }
        public int next() {
            if (_currentNode != DTM.NULL) {
                for (int node = (NOTPROCESSED == _currentNode)
                                     ? _firstch(makeNodeIdentity(_startNode))
                                     : _nextsib(_currentNode);
                     node != END;
                     node = _nextsib(node)) {
                    int nodeHandle = makeNodeHandle(node);
                    if (getNSType(nodeHandle) == _nsType) {
                        _currentNode = node;
                        return returnNode(nodeHandle);
                    }
                }
            }
            return END;
        }
    }  
    public final class NamespaceAttributeIterator
            extends InternalAxisIteratorBase
    {
        private final int _nsType;
        public NamespaceAttributeIterator(int nsType) {
            super();
            _nsType = nsType;
        }
        public DTMAxisIterator setStartNode(int node) {
            if (node == DTMDefaultBase.ROOTNODE) {
                node = getDocument();
            }
            if (_isRestartable) {
                int nsType = _nsType;
                _startNode = node;
                for (node = getFirstAttribute(node);
                     node != END;
                     node = getNextAttribute(node)) {
                    if (getNSType(node) == nsType) {
                        break;
                    }
                }
                _currentNode = node;
                return resetPosition();
            }
            return this;
        }
        public int next() {
            int node = _currentNode;
            int nsType = _nsType;
            int nextNode;
            if (node == END) {
                return END;
            }
            for (nextNode = getNextAttribute(node);
                 nextNode != END;
                 nextNode = getNextAttribute(nextNode)) {
                if (getNSType(nextNode) == nsType) {
                    break;
                }
            }
            _currentNode = nextNode;
            return returnNode(node);
        }
    }  
    public DTMAxisIterator getTypedDescendantIterator(int type)
    {
        return new TypedDescendantIterator(type);
    }
    public DTMAxisIterator getNthDescendant(int type, int n, boolean includeself)
    {
        DTMAxisIterator source = (DTMAxisIterator) new TypedDescendantIterator(type);
        return new NthDescendantIterator(n);
    }
    public void characters(final int node, SerializationHandler handler)
        throws TransletException
    {
        if (node != DTM.NULL) {
            try {
                dispatchCharactersEvents(node, handler, false);
            } catch (SAXException e) {
                throw new TransletException(e);
            }
        }
    }
    public void copy(DTMAxisIterator nodes, SerializationHandler handler)
        throws TransletException
    {
        int node;
        while ((node = nodes.next()) != DTM.NULL) {
            copy(node, handler);
        }
    }
    public void copy(SerializationHandler handler) throws TransletException
    {
        copy(getDocument(), handler);
    }
    public void copy(final int node, SerializationHandler handler)
        throws TransletException
    {
        copy(node, handler, false );
    }
 private final void copy(final int node, SerializationHandler handler, boolean isChild)
        throws TransletException
    {
     int nodeID = makeNodeIdentity(node);
        int eType = _exptype2(nodeID);
        int type = _exptype2Type(eType);
        try {
            switch(type)
            {
                case DTM.ROOT_NODE:
                case DTM.DOCUMENT_NODE:
                    for(int c = _firstch2(nodeID); c != DTM.NULL; c = _nextsib2(c)) {
                        copy(makeNodeHandle(c), handler, true);
                    }
                    break;
                case DTM.PROCESSING_INSTRUCTION_NODE:
                    copyPI(node, handler);
                    break;
                case DTM.COMMENT_NODE:
                    handler.comment(getStringValueX(node));
                    break;
                case DTM.TEXT_NODE:
                    boolean oldEscapeSetting = false;
                    boolean escapeBit = false;
                    if (_dontEscape != null) {
                        escapeBit = _dontEscape.getBit(getNodeIdent(node));
                        if (escapeBit) {
                            oldEscapeSetting = handler.setEscaping(false);
                        }
                    }
                    copyTextNode(nodeID, handler);
                    if (escapeBit) {
                        handler.setEscaping(oldEscapeSetting);
                    }
                    break;
                case DTM.ATTRIBUTE_NODE:
                    copyAttribute(nodeID, eType, handler);
                    break;
                case DTM.NAMESPACE_NODE:
                    handler.namespaceAfterStartElement(getNodeNameX(node), getNodeValue(node));
                    break;
                default:
                    if (type == DTM.ELEMENT_NODE) 
                    {
                        final String name = copyElement(nodeID, eType, handler);
                        copyNS(nodeID, handler,!isChild);
                        copyAttributes(nodeID, handler);
                        for (int c = _firstch2(nodeID); c != DTM.NULL; c = _nextsib2(c)) {
                            copy(makeNodeHandle(c), handler, true);
                        }
                        handler.endElement(name);
                    }
                    else {
                        final String uri = getNamespaceName(node);
                        if (uri.length() != 0) {
                            final String prefix = getPrefix(node);
                            handler.namespaceAfterStartElement(prefix, uri);
                        }
                        handler.addAttribute(getNodeName(node), getNodeValue(node));
                    }
                    break;
            }
        } 
        catch (Exception e) {
            throw new TransletException(e);
        }
    }
    private void copyPI(final int node, SerializationHandler handler)
	throws TransletException
    {
        final String target = getNodeName(node);
        final String value = getStringValueX(node);
        try {
            handler.processingInstruction(target, value);
        } catch (Exception e) {
            throw new TransletException(e);
        }
    }
    public String shallowCopy(final int node, SerializationHandler handler)
        throws TransletException
    {
        int nodeID = makeNodeIdentity(node);
        int exptype = _exptype2(nodeID);
        int type = _exptype2Type(exptype);
        try {
            switch(type)
            {
                case DTM.ELEMENT_NODE:
                    final String name = copyElement(nodeID, exptype, handler);
                    copyNS(nodeID, handler, true);
                    return name;
                case DTM.ROOT_NODE:
                case DTM.DOCUMENT_NODE:
                    return EMPTYSTRING;
                case DTM.TEXT_NODE:
                    copyTextNode(nodeID, handler);
                    return null;
                case DTM.PROCESSING_INSTRUCTION_NODE:
                    copyPI(node, handler);
                    return null;
                case DTM.COMMENT_NODE:
                    handler.comment(getStringValueX(node));
                    return null;
                case DTM.NAMESPACE_NODE:
                    handler.namespaceAfterStartElement(getNodeNameX(node), getNodeValue(node));
                    return null;
                case DTM.ATTRIBUTE_NODE:
                    copyAttribute(nodeID, exptype, handler);
                    return null;  
                default:
                    final String uri1 = getNamespaceName(node);
                    if (uri1.length() != 0) {
                        final String prefix = getPrefix(node);
                        handler.namespaceAfterStartElement(prefix, uri1);
                    }
                    handler.addAttribute(getNodeName(node), getNodeValue(node));
                    return null;
            }
        } catch (Exception e) {
            throw new TransletException(e);
        }   
    }
    public String getLanguage(int node)
    {
        int parent = node;
    	while (DTM.NULL != parent) {
            if (DTM.ELEMENT_NODE == getNodeType(parent)) {
                int langAttr = getAttributeNode(parent, "http://www.w3.org/XML/1998/namespace", "lang");
                if (DTM.NULL != langAttr) {
                    return getNodeValue(langAttr);     
                }
            }
            parent = getParent(parent);
        }      
        return(null);
    }
    public DOMBuilder getBuilder()
    {
	return this;
    }
    public SerializationHandler getOutputDomBuilder()
    {
        return new ToXMLSAXHandler(this, "UTF-8");
    }
    public DOM getResultTreeFrag(int initSize, int rtfType)
    {
        return getResultTreeFrag(initSize, rtfType, true);
    }
    public DOM getResultTreeFrag(int initSize, int rtfType, boolean addToManager)
    {
    	if (rtfType == DOM.SIMPLE_RTF) {
            if (addToManager) {
                int dtmPos = _dtmManager.getFirstFreeDTMID();
    	        SimpleResultTreeImpl rtf = new SimpleResultTreeImpl(_dtmManager,
    	                                   dtmPos << DTMManager.IDENT_DTM_NODE_BITS);
    	        _dtmManager.addDTM(rtf, dtmPos, 0);
    	        return rtf;
    	    }
            else {
            	return new SimpleResultTreeImpl(_dtmManager, 0);
            }
    	}
    	else if (rtfType == DOM.ADAPTIVE_RTF) {
            if (addToManager) {
                int dtmPos = _dtmManager.getFirstFreeDTMID();
    	        AdaptiveResultTreeImpl rtf = new AdaptiveResultTreeImpl(_dtmManager,
    	                               dtmPos << DTMManager.IDENT_DTM_NODE_BITS,
    	                               m_wsfilter, initSize, m_buildIdIndex);
    	        _dtmManager.addDTM(rtf, dtmPos, 0);
    	        return rtf;
    	    }
    	    else {
            	return new AdaptiveResultTreeImpl(_dtmManager, 0,
    	                               m_wsfilter, initSize, m_buildIdIndex);
            }    	
    	}
    	else {
    	    return (DOM) _dtmManager.getDTM(null, true, m_wsfilter,
                                            true, false, false,
                                            initSize, m_buildIdIndex);
        }
    }
    public Hashtable getElementsWithIDs() {
        if (m_idAttributes == null) {
            return null;
        }
        Enumeration idValues = m_idAttributes.keys();
        if (!idValues.hasMoreElements()) {
            return null;
        }
        Hashtable idAttrsTable = new Hashtable();
        while (idValues.hasMoreElements()) {
            Object idValue = idValues.nextElement();
            idAttrsTable.put(idValue, m_idAttributes.get(idValue));
        }
        return idAttrsTable;
    }
    public String getUnparsedEntityURI(String name)
    {
        if (_document != null) {
            String uri = "";
            DocumentType doctype = _document.getDoctype();
            if (doctype != null) {
                NamedNodeMap entities = doctype.getEntities();
                if (entities == null) {
                    return uri;
                }
                Entity entity = (Entity) entities.getNamedItem(name);
                if (entity == null) {
                    return uri;
                }
                String notationName = entity.getNotationName();
                if (notationName != null) {
                    uri = entity.getSystemId();
                    if (uri == null) {
                        uri = entity.getPublicId();
                    }
                }
            }
            return uri;
        }
        else {
            return super.getUnparsedEntityURI(name);
        }	
    }
}
