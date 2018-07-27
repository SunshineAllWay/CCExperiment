package org.apache.xml.dtm;
import javax.xml.transform.SourceLocator;
import org.apache.xml.utils.XMLString;
public interface DTM
{
  public static final int NULL = -1;
  public static final short ROOT_NODE = 0;
  public static final short ELEMENT_NODE = 1;
  public static final short ATTRIBUTE_NODE = 2;
  public static final short TEXT_NODE = 3;
  public static final short CDATA_SECTION_NODE = 4;
  public static final short ENTITY_REFERENCE_NODE = 5;
  public static final short ENTITY_NODE = 6;
  public static final short PROCESSING_INSTRUCTION_NODE = 7;
  public static final short COMMENT_NODE = 8;
  public static final short DOCUMENT_NODE = 9;
  public static final short DOCUMENT_TYPE_NODE = 10;
  public static final short DOCUMENT_FRAGMENT_NODE = 11;
  public static final short NOTATION_NODE = 12;
  public static final short NAMESPACE_NODE = 13;
  public static final short  NTYPES = 14;
  public void setFeature(String featureId, boolean state);
  public void setProperty(String property, Object value);
  public DTMAxisTraverser getAxisTraverser(final int axis);
  public DTMAxisIterator getAxisIterator(final int axis);
  public DTMAxisIterator getTypedAxisIterator(final int axis, final int type);
  public boolean hasChildNodes(int nodeHandle);
  public int getFirstChild(int nodeHandle);
  public int getLastChild(int nodeHandle);
  public int getAttributeNode(int elementHandle, String namespaceURI,
                              String name);
  public int getFirstAttribute(int nodeHandle);
  public int getFirstNamespaceNode(int nodeHandle, boolean inScope);
  public int getNextSibling(int nodeHandle);
  public int getPreviousSibling(int nodeHandle);
  public int getNextAttribute(int nodeHandle);
  public int getNextNamespaceNode(int baseHandle, int namespaceHandle,
                                  boolean inScope);
  public int getParent(int nodeHandle);
  public int getDocument();
  public int getOwnerDocument(int nodeHandle);
  public int getDocumentRoot(int nodeHandle);
  public XMLString getStringValue(int nodeHandle);
  public int getStringValueChunkCount(int nodeHandle);
  public char[] getStringValueChunk(int nodeHandle, int chunkIndex,
                                    int[] startAndLen);
  public int getExpandedTypeID(int nodeHandle);
  public int getExpandedTypeID(String namespace, String localName, int type);
  public String getLocalNameFromExpandedNameID(int ExpandedNameID);
  public String getNamespaceFromExpandedNameID(int ExpandedNameID);
  public String getNodeName(int nodeHandle);
  public String getNodeNameX(int nodeHandle);
  public String getLocalName(int nodeHandle);
  public String getPrefix(int nodeHandle);
  public String getNamespaceURI(int nodeHandle);
  public String getNodeValue(int nodeHandle);
  public short getNodeType(int nodeHandle);
  public short getLevel(int nodeHandle);
  public boolean isSupported(String feature, String version);
  public String getDocumentBaseURI();
  public void setDocumentBaseURI(String baseURI);
  public String getDocumentSystemIdentifier(int nodeHandle);
  public String getDocumentEncoding(int nodeHandle);
  public String getDocumentStandalone(int nodeHandle);
  public String getDocumentVersion(int documentHandle);
  public boolean getDocumentAllDeclarationsProcessed();
  public String getDocumentTypeDeclarationSystemIdentifier();
  public String getDocumentTypeDeclarationPublicIdentifier();
  public int getElementById(String elementId);
  public String getUnparsedEntityURI(String name);
  public boolean supportsPreStripping();
  public boolean isNodeAfter(int firstNodeHandle, int secondNodeHandle);
  public boolean isCharacterElementContentWhitespace(int nodeHandle);
  public boolean isDocumentAllDeclarationsProcessed(int documentHandle);
  public boolean isAttributeSpecified(int attributeHandle);
  public void dispatchCharactersEvents(
    int nodeHandle, org.xml.sax.ContentHandler ch, boolean normalize)
      throws org.xml.sax.SAXException;
  public void dispatchToEvents(int nodeHandle, org.xml.sax.ContentHandler ch)
    throws org.xml.sax.SAXException;
  public org.w3c.dom.Node getNode(int nodeHandle);
  public boolean needsTwoThreads();
  public org.xml.sax.ContentHandler getContentHandler();
  public org.xml.sax.ext.LexicalHandler getLexicalHandler();
  public org.xml.sax.EntityResolver getEntityResolver();
  public org.xml.sax.DTDHandler getDTDHandler();
  public org.xml.sax.ErrorHandler getErrorHandler();
  public org.xml.sax.ext.DeclHandler getDeclHandler();
  public void appendChild(int newChild, boolean clone, boolean cloneDepth);
  public void appendTextChild(String str);
  public SourceLocator getSourceLocatorFor(int node);
  public void documentRegistration();
   public void documentRelease();
   public void migrateTo(DTMManager manager);
}
