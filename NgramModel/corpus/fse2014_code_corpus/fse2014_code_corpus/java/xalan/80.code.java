package org.apache.xalan.lib.sql;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.xml.transform.SourceLocator;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMAxisIterator;
import org.apache.xml.dtm.DTMAxisTraverser;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.ref.DTMDefaultBaseIterators;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.StringBufferPool;
import org.apache.xml.utils.SuballocatedIntVector;
import org.apache.xml.utils.XMLString;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
public class DTMDocument extends DTMDefaultBaseIterators
{
  public interface CharacterNodeHandler
  {
    public void characters( Node node )throws org.xml.sax.SAXException ;
  }
  private boolean DEBUG = false;
  protected static final String S_NAMESPACE = "http://xml.apache.org/xalan/SQLExtension";
  protected static final String S_ATTRIB_NOT_SUPPORTED = "Not Supported";
  protected static final String S_ISTRUE = "true";
  protected static final String S_ISFALSE = "false";
  protected static final String S_DOCUMENT = "#root";
  protected static final String S_TEXT_NODE = "#text";
  protected static final String S_ELEMENT_NODE = "#element";
  protected int m_Document_TypeID = 0;
  protected int m_TextNode_TypeID = 0;
  protected ObjectArray m_ObjectArray = new ObjectArray();
  protected SuballocatedIntVector m_attribute;
  protected int m_DocumentIdx;
  public DTMDocument( DTMManager mgr, int ident )
  {
    super(mgr, null, ident,
      null, mgr.getXMLStringFactory(), true);
    m_attribute = new SuballocatedIntVector(DEFAULT_BLOCKSIZE);
  }
  private int allocateNodeObject( Object o )
  {
    m_size++;
    return m_ObjectArray.append(o);
  }
  protected int addElementWithData( Object o, int level, int extendedType, int parent, int prevsib )
  {
    int elementIdx = addElement(level,extendedType,parent,prevsib);
    int data = allocateNodeObject(o);
    m_firstch.setElementAt(data,elementIdx);
    m_exptype.setElementAt(m_TextNode_TypeID, data);
    m_parent.setElementAt(elementIdx, data);
    m_prevsib.setElementAt(DTM.NULL, data);
    m_nextsib.setElementAt(DTM.NULL, data);
    m_attribute.setElementAt(DTM.NULL, data);
    m_firstch.setElementAt(DTM.NULL, data);
    return elementIdx;
  }
  protected int addElement( int level, int extendedType, int parent, int prevsib )
  {
    int node = DTM.NULL;
    try
    {
      node = allocateNodeObject(S_ELEMENT_NODE);
      m_exptype.setElementAt(extendedType, node);
      m_nextsib.setElementAt(DTM.NULL, node);
      m_prevsib.setElementAt(prevsib, node);
      m_parent.setElementAt(parent, node);
      m_firstch.setElementAt(DTM.NULL, node);
      m_attribute.setElementAt(DTM.NULL, node);
      if (prevsib != DTM.NULL)
      {
        if (m_nextsib.elementAt(prevsib) != DTM.NULL)
          m_nextsib.setElementAt(m_nextsib.elementAt(prevsib), node);
        m_nextsib.setElementAt(node, prevsib);
      }
      if ((parent != DTM.NULL) && (m_prevsib.elementAt(node) == DTM.NULL))
      {
        m_firstch.setElementAt(node, parent);
      }
    }
    catch(Exception e)
    {
      error("Error in addElement: "+e.getMessage());
    }
    return node;
  }
  protected int addAttributeToNode( Object o, int extendedType, int pnode )
  {
    int attrib = DTM.NULL;
    int lastattrib = DTM.NULL;
    try
    {
      attrib = allocateNodeObject(o);
      m_attribute.setElementAt(DTM.NULL, attrib);
      m_exptype.setElementAt(extendedType, attrib);
      m_nextsib.setElementAt(DTM.NULL, attrib);
      m_prevsib.setElementAt(DTM.NULL,attrib);
      m_parent.setElementAt(pnode, attrib);
      m_firstch.setElementAt(DTM.NULL, attrib);
      if (m_attribute.elementAt(pnode) != DTM.NULL)
      {
        lastattrib = m_attribute.elementAt(pnode);
        m_nextsib.setElementAt(lastattrib, attrib);
        m_prevsib.setElementAt(attrib, lastattrib);
      }
      m_attribute.setElementAt(attrib, pnode);
    }
    catch(Exception e)
    {
      error("Error in addAttributeToNode: "+e.getMessage());
    }
    return attrib;
  }
  protected void cloneAttributeFromNode( int toNode, int fromNode )
  {
   try
    {
      if (m_attribute.elementAt(toNode) != DTM.NULL)
      {
        error("Cloneing Attributes, where from Node already had addtibures assigned");
      }
      m_attribute.setElementAt(m_attribute.elementAt(fromNode), toNode);
    }
    catch(Exception e)
    {
      error("Cloning attributes");
    }
  }
  public int getFirstAttribute( int parm1 )
  {
    if (DEBUG) System.out.println("getFirstAttribute("+ parm1+")");
    int nodeIdx = makeNodeIdentity(parm1);
    if (nodeIdx != DTM.NULL)
    {
      int attribIdx =  m_attribute.elementAt(nodeIdx);
      return makeNodeHandle(attribIdx);
    }
    else return DTM.NULL;
  }
  public String getNodeValue( int parm1 )
  {
    if (DEBUG) System.out.println("getNodeValue(" + parm1 + ")");
    try
    {
      Object o = m_ObjectArray.getAt(makeNodeIdentity(parm1));
      if (o != null && o != S_ELEMENT_NODE)
      {
        return o.toString();
      }
      else
      {
        return "";
      }
    }
    catch(Exception e)
    {
      error("Getting String Value");
      return null;
    }
  }
  public XMLString getStringValue(int nodeHandle)
  {
    int nodeIdx = makeNodeIdentity(nodeHandle);
    if (DEBUG) System.out.println("getStringValue(" + nodeIdx + ")");
      Object o = m_ObjectArray.getAt(nodeIdx);
    if ( o == S_ELEMENT_NODE )
      {
        FastStringBuffer buf = StringBufferPool.get();
        String s;
        try
        {
          getNodeData(nodeIdx, buf);
          s = (buf.length() > 0) ? buf.toString() : "";
        }
        finally
        {
          StringBufferPool.free(buf);
        }
        return m_xstrf.newstr( s );
      }
      else if( o != null )
      {
        return m_xstrf.newstr(o.toString());
    }
    else
      return(m_xstrf.emptystr());
  }
  protected void getNodeData(int nodeIdx, FastStringBuffer buf)
  {
    for ( int child = _firstch(nodeIdx) ; child != DTM.NULL ; child = _nextsib(child) )
    {
      Object o = m_ObjectArray.getAt(child);
      if ( o == S_ELEMENT_NODE )
        getNodeData(child, buf);
      else if ( o != null )
        buf.append(o.toString());
    }
  }
  public int getNextAttribute( int parm1 )
  {
    int nodeIdx = makeNodeIdentity(parm1);
    if (DEBUG) System.out.println("getNextAttribute(" + nodeIdx + ")");
    if (nodeIdx != DTM.NULL) return makeNodeHandle(m_nextsib.elementAt(nodeIdx));
    else return DTM.NULL;
  }
  protected int getNumberOfNodes( )
  {
    if (DEBUG) System.out.println("getNumberOfNodes()");
    return m_size;
  }
  protected boolean nextNode( )
  {
    if (DEBUG) System.out.println("nextNode()");
    return false;
  }
  protected void createExpandedNameTable( )
  {
    m_Document_TypeID =
      m_expandedNameTable.getExpandedTypeID(S_NAMESPACE, S_DOCUMENT, DTM.DOCUMENT_NODE);
    m_TextNode_TypeID =
      m_expandedNameTable.getExpandedTypeID(S_NAMESPACE, S_TEXT_NODE, DTM.TEXT_NODE);
  }
  public void dumpDTM( )
  {
    try
    {
      File f = new File("DTMDump.txt");
      System.err.println("Dumping... "+f.getAbsolutePath());
      PrintStream ps = new PrintStream(new FileOutputStream(f));
      while (nextNode()){}
      int nRecords = m_size;
      ps.println("Total nodes: " + nRecords);
      for (int i = 0; i < nRecords; i++)
      {
        ps.println("=========== " + i + " ===========");
        ps.println("NodeName: " + getNodeName(makeNodeHandle(i)));
        ps.println("NodeNameX: " + getNodeNameX(makeNodeHandle(i)));
        ps.println("LocalName: " + getLocalName(makeNodeHandle(i)));
        ps.println("NamespaceURI: " + getNamespaceURI(makeNodeHandle(i)));
        ps.println("Prefix: " + getPrefix(makeNodeHandle(i)));
        int exTypeID = getExpandedTypeID(makeNodeHandle(i));
        ps.println("Expanded Type ID: "
                           + Integer.toHexString(exTypeID));
        int type = getNodeType(makeNodeHandle(i));
        String typestring;
        switch (type)
        {
        case DTM.ATTRIBUTE_NODE :
          typestring = "ATTRIBUTE_NODE";
          break;
        case DTM.CDATA_SECTION_NODE :
          typestring = "CDATA_SECTION_NODE";
          break;
        case DTM.COMMENT_NODE :
          typestring = "COMMENT_NODE";
          break;
        case DTM.DOCUMENT_FRAGMENT_NODE :
          typestring = "DOCUMENT_FRAGMENT_NODE";
          break;
        case DTM.DOCUMENT_NODE :
          typestring = "DOCUMENT_NODE";
          break;
        case DTM.DOCUMENT_TYPE_NODE :
          typestring = "DOCUMENT_NODE";
          break;
        case DTM.ELEMENT_NODE :
          typestring = "ELEMENT_NODE";
          break;
        case DTM.ENTITY_NODE :
          typestring = "ENTITY_NODE";
          break;
        case DTM.ENTITY_REFERENCE_NODE :
          typestring = "ENTITY_REFERENCE_NODE";
          break;
        case DTM.NAMESPACE_NODE :
          typestring = "NAMESPACE_NODE";
          break;
        case DTM.NOTATION_NODE :
          typestring = "NOTATION_NODE";
          break;
        case DTM.NULL :
          typestring = "NULL";
          break;
        case DTM.PROCESSING_INSTRUCTION_NODE :
          typestring = "PROCESSING_INSTRUCTION_NODE";
          break;
        case DTM.TEXT_NODE :
          typestring = "TEXT_NODE";
          break;
        default :
          typestring = "Unknown!";
          break;
        }
        ps.println("Type: " + typestring);
        int firstChild = _firstch(i);
        if (DTM.NULL == firstChild)
          ps.println("First child: DTM.NULL");
        else if (NOTPROCESSED == firstChild)
          ps.println("First child: NOTPROCESSED");
        else
          ps.println("First child: " + firstChild);
        int prevSibling = _prevsib(i);
        if (DTM.NULL == prevSibling)
          ps.println("Prev sibling: DTM.NULL");
        else if (NOTPROCESSED == prevSibling)
          ps.println("Prev sibling: NOTPROCESSED");
        else
          ps.println("Prev sibling: " + prevSibling);
        int nextSibling = _nextsib(i);
        if (DTM.NULL == nextSibling)
          ps.println("Next sibling: DTM.NULL");
        else if (NOTPROCESSED == nextSibling)
          ps.println("Next sibling: NOTPROCESSED");
        else
          ps.println("Next sibling: " + nextSibling);
        int parent = _parent(i);
        if (DTM.NULL == parent)
          ps.println("Parent: DTM.NULL");
        else if (NOTPROCESSED == parent)
          ps.println("Parent: NOTPROCESSED");
        else
          ps.println("Parent: " + parent);
        int level = _level(i);
        ps.println("Level: " + level);
        ps.println("Node Value: " + getNodeValue(i));
        ps.println("String Value: " + getStringValue(i));
        ps.println("First Attribute Node: " + m_attribute.elementAt(i));
      }
    }
    catch(IOException ioe)
    {
      ioe.printStackTrace(System.err);
      throw new RuntimeException(ioe.getMessage());
    }
  }
  protected static void dispatchNodeData( Node node, ContentHandler ch, int depth )throws org.xml.sax.SAXException
  {
    switch (node.getNodeType())
    {
    case Node.DOCUMENT_FRAGMENT_NODE :
    case Node.DOCUMENT_NODE :
    case Node.ELEMENT_NODE :
    {
      for (Node child = node.getFirstChild(); null != child;
              child = child.getNextSibling())
      {
        dispatchNodeData(child, ch, depth+1);
      }
    }
    break;
    case Node.PROCESSING_INSTRUCTION_NODE : 
    case Node.COMMENT_NODE :
      if(0 != depth)
        break;
    case Node.TEXT_NODE :
    case Node.CDATA_SECTION_NODE :
    case Node.ATTRIBUTE_NODE :
      String str = node.getNodeValue();
      if(ch instanceof CharacterNodeHandler)
      {
        ((CharacterNodeHandler)ch).characters(node);
      }
      else
      {
        ch.characters(str.toCharArray(), 0, str.length());
      }
      break;
    default :
      break;
    }
  }
  public void setProperty( String property, Object value )
  {
  }
  public SourceLocator getSourceLocatorFor( int node )
  {
    return null;
  }
  protected int getNextNodeIdentity( int parm1 )
  {
    if (DEBUG) System.out.println("getNextNodeIdenty(" + parm1 + ")");
    return DTM.NULL;
  }
  public int getAttributeNode( int parm1, String parm2, String parm3 )
  {
    if (DEBUG)
    {
      System.out.println(
        "getAttributeNode(" +
        parm1 + "," +
        parm2 + "," +
        parm3 + ")");
    }
    return DTM.NULL;
  }
  public String getLocalName( int parm1 )
  {
      int exID = getExpandedTypeID(parm1);
    if (DEBUG)
    {
      DEBUG = false;
      System.out.print("getLocalName(" + parm1 + ") -> ");
      System.out.println("..." + getLocalNameFromExpandedNameID(exID) );
      DEBUG = true;
    }
    return getLocalNameFromExpandedNameID(exID);
  }
  public String getNodeName( int parm1 )
  {
    int exID = getExpandedTypeID( parm1 );
    if (DEBUG)
    {
      DEBUG = false;
      System.out.print("getLocalName(" + parm1 + ") -> ");
      System.out.println("..." + getLocalNameFromExpandedNameID(exID) );
      DEBUG = true;
    }
    return getLocalNameFromExpandedNameID(exID);
  }
  public boolean isAttributeSpecified( int parm1 )
  {
    if (DEBUG) System.out.println("isAttributeSpecified(" + parm1 + ")");
    return false;
  }
  public String getUnparsedEntityURI( String parm1 )
  {
    if (DEBUG) System.out.println("getUnparsedEntityURI(" + parm1 + ")");
    return "";
  }
  public DTDHandler getDTDHandler( )
  {
    if (DEBUG) System.out.println("getDTDHandler()");
    return null;
  }
  public String getPrefix( int parm1 )
  {
    if (DEBUG) System.out.println("getPrefix(" + parm1  + ")");
    return "";
  }
  public EntityResolver getEntityResolver( )
  {
    if (DEBUG) System.out.println("getEntityResolver()");
    return null;
  }
  public String getDocumentTypeDeclarationPublicIdentifier( )
  {
    if (DEBUG) System.out.println("get_DTD_PubId()");
    return "";
  }
  public LexicalHandler getLexicalHandler( )
  {
    if (DEBUG) System.out.println("getLexicalHandler()");
    return null;
  }
  public boolean needsTwoThreads( )
  {
    if (DEBUG) System.out.println("needsTwoThreads()");
    return false;
  }
  public ContentHandler getContentHandler( )
  {
    if (DEBUG) System.out.println("getContentHandler()");
    return null;
  }
  public void dispatchToEvents( int parm1, ContentHandler parm2 )throws org.xml.sax.SAXException
  {
    if (DEBUG)
    {
      System.out.println(
      "dispathcToEvents(" +
      parm1 + "," +
      parm2 + ")");
    }
    return;
  }
  public String getNamespaceURI( int parm1 )
  {
    if (DEBUG) System.out.println("getNamespaceURI(" +parm1+")");
    return "";
  }
  public void dispatchCharactersEvents( int nodeHandle, ContentHandler ch, boolean normalize )throws org.xml.sax.SAXException
  {
    if (DEBUG)
    {
      System.out.println("dispatchCharacterEvents(" +
      nodeHandle + "," +
      ch + "," +
      normalize + ")");
    }
    if(normalize)
    {
      XMLString str = getStringValue(nodeHandle);
      str = str.fixWhiteSpace(true, true, false);
      str.dispatchCharactersEvents(ch);
    }
    else
    {
      Node node = getNode(nodeHandle);
      dispatchNodeData(node, ch, 0);
    }
  }
  public boolean supportsPreStripping( )
  {
    if (DEBUG) System.out.println("supportsPreStripping()");
    return super.supportsPreStripping();
  }
  protected int _exptype( int parm1 )
  {
    if (DEBUG) System.out.println("_exptype(" + parm1 + ")");
    return super._exptype( parm1);
  }
  protected SuballocatedIntVector findNamespaceContext( int parm1 )
  {
    if (DEBUG) System.out.println("SuballocatedIntVector(" + parm1 + ")");
    return super.findNamespaceContext( parm1);
  }
  protected int _prevsib( int parm1 )
  {
    if (DEBUG) System.out.println("_prevsib(" + parm1+ ")");
    return super._prevsib( parm1);
  }
  protected short _type( int parm1 )
  {
    if (DEBUG) System.out.println("_type(" + parm1 + ")");
    return super._type( parm1);
  }
  public Node getNode( int parm1 )
  {
    if (DEBUG) System.out.println("getNode(" + parm1 + ")");
    return super.getNode( parm1);
  }
  public int getPreviousSibling( int parm1 )
  {
    if (DEBUG) System.out.println("getPrevSib(" + parm1 + ")");
    return super.getPreviousSibling( parm1);
  }
  public String getDocumentStandalone( int parm1 )
  {
    if (DEBUG) System.out.println("getDOcStandAlone(" + parm1 + ")");
    return super.getDocumentStandalone( parm1);
  }
  public String getNodeNameX( int parm1 )
  {
    if (DEBUG) System.out.println("getNodeNameX(" + parm1 + ")");
    return getNodeName(parm1);
  }
  public void setFeature( String parm1, boolean parm2 )
  {
    if (DEBUG)
    {
      System.out.println(
        "setFeature(" +
        parm1 + "," +
        parm2 + ")");
    }
    super.setFeature( parm1,  parm2);
  }
  protected int _parent( int parm1 )
  {
    if (DEBUG) System.out.println("_parent(" + parm1 + ")");
    return super._parent( parm1);
  }
  protected void indexNode( int parm1, int parm2 )
  {
    if (DEBUG) System.out.println("indexNode("+parm1+","+parm2+")");
    super.indexNode( parm1,  parm2);
  }
  protected boolean getShouldStripWhitespace( )
  {
    if (DEBUG) System.out.println("getShouldStripWS()");
    return super.getShouldStripWhitespace();
  }
  protected void popShouldStripWhitespace( )
  {
    if (DEBUG) System.out.println("popShouldStripWS()");
    super.popShouldStripWhitespace();
  }
  public boolean isNodeAfter( int parm1, int parm2 )
  {
    if (DEBUG) System.out.println("isNodeAfter(" + parm1 + "," + parm2 + ")");
    return super.isNodeAfter( parm1,  parm2);
  }
  public int getNamespaceType( int parm1 )
  {
    if (DEBUG) System.out.println("getNamespaceType(" + parm1 + ")");
    return super.getNamespaceType( parm1);
  }
  protected int _level( int parm1 )
  {
    if (DEBUG) System.out.println("_level(" + parm1 + ")");
    return super._level( parm1);
  }
  protected void pushShouldStripWhitespace( boolean parm1 )
  {
    if (DEBUG) System.out.println("push_ShouldStripWS(" + parm1 + ")");
    super.pushShouldStripWhitespace( parm1);
  }
  public String getDocumentVersion( int parm1 )
  {
    if (DEBUG) System.out.println("getDocVer("+parm1+")");
    return super.getDocumentVersion( parm1);
  }
  public boolean isSupported( String parm1, String parm2 )
  {
    if (DEBUG) System.out.println("isSupported("+parm1+","+parm2+")");
    return super.isSupported( parm1,  parm2);
  }
  protected void setShouldStripWhitespace( boolean parm1 )
  {
    if (DEBUG) System.out.println("set_ShouldStripWS("+parm1+")");
    super.setShouldStripWhitespace( parm1);
  }
  protected void ensureSizeOfIndex( int parm1, int parm2 )
  {
    if (DEBUG) System.out.println("ensureSizeOfIndex("+parm1+","+parm2+")");
    super.ensureSizeOfIndex( parm1,  parm2);
  }
  protected void ensureSize( int parm1 )
  {
    if (DEBUG) System.out.println("ensureSize("+parm1+")");
  }
  public String getDocumentEncoding( int parm1 )
  {
    if (DEBUG) System.out.println("getDocumentEncoding("+parm1+")");
    return super.getDocumentEncoding( parm1);
  }
  public void appendChild( int parm1, boolean parm2, boolean parm3 )
  {
    if (DEBUG)
    {
      System.out.println(
        "appendChild(" +
        parm1 + "," +
        parm2 + "," +
        parm3 + ")");
    }
    super.appendChild( parm1,  parm2,  parm3);
  }
  public short getLevel( int parm1 )
  {
    if (DEBUG) System.out.println("getLevel("+parm1+")");
    return super.getLevel( parm1);
  }
  public String getDocumentBaseURI( )
  {
    if (DEBUG) System.out.println("getDocBaseURI()");
    return super.getDocumentBaseURI();
  }
  public int getNextNamespaceNode( int parm1, int parm2, boolean parm3 )
  {
    if (DEBUG)
    {
      System.out.println(
      "getNextNamesapceNode(" +
      parm1 + "," +
      parm2 + "," +
      parm3 + ")");
    }
    return super.getNextNamespaceNode( parm1,  parm2,  parm3);
  }
  public void appendTextChild( String parm1 )
  {
    if (DEBUG) System.out.println("appendTextChild(" + parm1 + ")");
    super.appendTextChild( parm1);
  }
  protected int findGTE( int[] parm1, int parm2, int parm3, int parm4 )
  {
    if (DEBUG)
    {
      System.out.println(
      "findGTE("+
      parm1 + "," +
      parm2 + "," +
      parm3 + ")");
    }
    return super.findGTE( parm1,  parm2,  parm3,  parm4);
  }
  public int getFirstNamespaceNode( int parm1, boolean parm2 )
  {
    if (DEBUG) System.out.println("getFirstNamespaceNode()");
    return super.getFirstNamespaceNode( parm1,  parm2);
  }
  public int getStringValueChunkCount( int parm1 )
  {
    if (DEBUG) System.out.println("getStringChunkCount(" + parm1 + ")");
    return super.getStringValueChunkCount( parm1);
  }
  public int getLastChild( int parm1 )
  {
    if (DEBUG) System.out.println("getLastChild(" + parm1 + ")");
    return super.getLastChild( parm1);
  }
  public boolean hasChildNodes( int parm1 )
  {
    if (DEBUG) System.out.println("hasChildNodes(" + parm1 + ")");
    return super.hasChildNodes( parm1);
  }
  public short getNodeType( int parm1 )
  {
    if (DEBUG)
    {
      DEBUG=false;
      System.out.print("getNodeType(" + parm1 + ") ");
      int exID = getExpandedTypeID(parm1);
      String name = getLocalNameFromExpandedNameID(exID);
      System.out.println(
        ".. Node name [" + name + "]" +
        "[" + getNodeType( parm1) + "]");
      DEBUG=true;
    }
    return super.getNodeType( parm1);
  }
  public boolean isCharacterElementContentWhitespace( int parm1 )
  {
    if (DEBUG) System.out.println("isCharacterElementContentWhitespace(" + parm1 +")");
    return super.isCharacterElementContentWhitespace( parm1);
  }
  public int getFirstChild( int parm1 )
  {
    if (DEBUG) System.out.println("getFirstChild(" + parm1 + ")");
    return super.getFirstChild( parm1);
  }
  public String getDocumentSystemIdentifier( int parm1 )
  {
    if (DEBUG) System.out.println("getDocSysID(" + parm1 + ")");
    return super.getDocumentSystemIdentifier( parm1);
  }
  protected void declareNamespaceInContext( int parm1, int parm2 )
  {
    if (DEBUG) System.out.println("declareNamespaceContext("+parm1+","+parm2+")");
    super.declareNamespaceInContext( parm1,  parm2);
  }
  public String getNamespaceFromExpandedNameID( int parm1 )
  {
    if (DEBUG)
    {
      DEBUG = false;
      System.out.print("getNamespaceFromExpandedNameID("+parm1+")");
      System.out.println("..." + super.getNamespaceFromExpandedNameID( parm1) );
      DEBUG = true;
    }
    return super.getNamespaceFromExpandedNameID( parm1);
  }
  public String getLocalNameFromExpandedNameID( int parm1 )
  {
    if (DEBUG)
    {
      DEBUG = false;
      System.out.print("getLocalNameFromExpandedNameID("+parm1+")");
      System.out.println("..." + super.getLocalNameFromExpandedNameID( parm1));
      DEBUG = true;
    }
    return super.getLocalNameFromExpandedNameID( parm1);
  }
  public int getExpandedTypeID( int parm1 )
  {
    if (DEBUG) System.out.println("getExpandedTypeID("+parm1+")");
    return super.getExpandedTypeID( parm1);
  }
  public int getDocument( )
  {
    if (DEBUG) System.out.println("getDocument()");
    return super.getDocument();
  }
  protected int findInSortedSuballocatedIntVector( SuballocatedIntVector parm1, int parm2 )
  {
    if (DEBUG)
    {
      System.out.println(
      "findInSortedSubAlloctedVector(" +
      parm1 + "," +
      parm2 + ")");
    }
    return super.findInSortedSuballocatedIntVector( parm1,  parm2);
  }
  public boolean isDocumentAllDeclarationsProcessed( int parm1 )
  {
    if (DEBUG) System.out.println("isDocumentAllDeclProc("+parm1+")");
    return super.isDocumentAllDeclarationsProcessed( parm1);
  }
  protected void error( String parm1 )
  {
    if (DEBUG) System.out.println("error("+parm1+")");
    super.error( parm1);
  }
  protected int _firstch( int parm1 )
  {
    if (DEBUG) System.out.println("_firstch("+parm1+")");
    return super._firstch( parm1);
  }
  public int getOwnerDocument( int parm1 )
  {
    if (DEBUG) System.out.println("getOwnerDoc("+parm1+")");
    return super.getOwnerDocument( parm1);
  }
  protected int _nextsib( int parm1 )
  {
    if (DEBUG) System.out.println("_nextSib("+parm1+")");
    return super._nextsib( parm1);
  }
  public int getNextSibling( int parm1 )
  {
    if (DEBUG) System.out.println("getNextSibling("+parm1+")");
    return super.getNextSibling( parm1);
  }
  public boolean getDocumentAllDeclarationsProcessed( )
  {
    if (DEBUG) System.out.println("getDocAllDeclProc()");
    return super.getDocumentAllDeclarationsProcessed();
  }
  public int getParent( int parm1 )
  {
    if (DEBUG) System.out.println("getParent("+parm1+")");
    return super.getParent( parm1);
  }
  public int getExpandedTypeID( String parm1, String parm2, int parm3 )
  {
    if (DEBUG) System.out.println("getExpandedTypeID()");
    return super.getExpandedTypeID( parm1,  parm2,  parm3);
  }
  public void setDocumentBaseURI( String parm1 )
  {
    if (DEBUG) System.out.println("setDocBaseURI()");
    super.setDocumentBaseURI( parm1);
  }
  public char[] getStringValueChunk( int parm1, int parm2, int[] parm3 )
  {
    if (DEBUG)
    {
      System.out.println("getStringChunkValue(" +
      parm1 + "," +
      parm2 + ")");
    }
    return super.getStringValueChunk( parm1,  parm2,  parm3);
  }
  public DTMAxisTraverser getAxisTraverser( int parm1 )
  {
    if (DEBUG) System.out.println("getAxixTraverser("+parm1+")");
    return super.getAxisTraverser( parm1);
  }
  public DTMAxisIterator getTypedAxisIterator( int parm1, int parm2 )
  {
    if (DEBUG) System.out.println("getTypedAxisIterator("+parm1+","+parm2+")");
    return super.getTypedAxisIterator( parm1,  parm2);
  }
  public DTMAxisIterator getAxisIterator( int parm1 )
  {
    if (DEBUG) System.out.println("getAxisIterator("+parm1+")");
    return super.getAxisIterator( parm1);
  }
  public int getElementById( String parm1 )
  {
    if (DEBUG) System.out.println("getElementByID("+parm1+")");
    return DTM.NULL;
  }
  public DeclHandler getDeclHandler( )
  {
    if (DEBUG) System.out.println("getDeclHandler()");
    return null;
  }
  public ErrorHandler getErrorHandler( )
  {
    if (DEBUG) System.out.println("getErrorHandler()");
    return null;
  }
  public String getDocumentTypeDeclarationSystemIdentifier( )
  {
    if (DEBUG) System.out.println("get_DTD-SID()");
    return null;
  }
}
