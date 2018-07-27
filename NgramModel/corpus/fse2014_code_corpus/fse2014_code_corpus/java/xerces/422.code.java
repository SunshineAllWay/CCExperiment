package org.apache.xerces.impl.xs;
import org.apache.xerces.impl.xs.opti.ElementImpl;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
public class SchemaNamespaceSupport 
    extends NamespaceSupport {
    private SchemaRootContext fSchemaRootContext = null;
    public SchemaNamespaceSupport (Element schemaRoot, SymbolTable symbolTable) {
        super();
        if (schemaRoot != null && !(schemaRoot instanceof ElementImpl)) {
            Document ownerDocument = schemaRoot.getOwnerDocument();
            if (ownerDocument != null && schemaRoot != ownerDocument.getDocumentElement()) {
                fSchemaRootContext = new SchemaRootContext(schemaRoot, symbolTable);
            }
        }
    } 
    public SchemaNamespaceSupport(SchemaNamespaceSupport nSupport) {
        fSchemaRootContext = nSupport.fSchemaRootContext;
        fNamespaceSize = nSupport.fNamespaceSize;
        if (fNamespace.length < fNamespaceSize)
            fNamespace = new String[fNamespaceSize];
        System.arraycopy(nSupport.fNamespace, 0, fNamespace, 0, fNamespaceSize);
        fCurrentContext = nSupport.fCurrentContext;
        if (fContext.length <= fCurrentContext)
            fContext = new int[fCurrentContext+1];
        System.arraycopy(nSupport.fContext, 0, fContext, 0, fCurrentContext+1);
    } 
    public void setEffectiveContext (String [] namespaceDecls) {
        if(namespaceDecls == null || namespaceDecls.length == 0) return;
        pushContext();
        int newSize = fNamespaceSize + namespaceDecls.length;
        if (fNamespace.length < newSize) {
            String[] tempNSArray = new String[newSize];
            System.arraycopy(fNamespace, 0, tempNSArray, 0, fNamespace.length);
            fNamespace = tempNSArray;
        }
        System.arraycopy(namespaceDecls, 0, fNamespace, fNamespaceSize,
                         namespaceDecls.length);
        fNamespaceSize = newSize;
    } 
    public String [] getEffectiveLocalContext() {
        String[] returnVal = null;
        if (fCurrentContext >= 3) {
            int bottomLocalContext = fContext[3];
            int copyCount = fNamespaceSize - bottomLocalContext;
            if (copyCount > 0) {
                returnVal = new String[copyCount];
                System.arraycopy(fNamespace, bottomLocalContext, returnVal, 0,
                                 copyCount);
            }
        }
        return returnVal;
    } 
    public void makeGlobal() {
        if (fCurrentContext >= 3) {
            fCurrentContext = 3;
            fNamespaceSize = fContext[3];
        }
    } 
    public String getURI(String prefix) {
        String uri = super.getURI(prefix);
        if (uri == null && fSchemaRootContext != null) {
            if (!fSchemaRootContext.fDOMContextBuilt) {
                fSchemaRootContext.fillNamespaceContext();
                fSchemaRootContext.fDOMContextBuilt = true;
            }
            if (fSchemaRootContext.fNamespaceSize > 0 && 
                !containsPrefix(prefix)) {
                uri = fSchemaRootContext.getURI(prefix);
            }
        }
        return uri;
    }
    static final class SchemaRootContext {
        String[] fNamespace = new String[16 * 2];
        int fNamespaceSize = 0;
        boolean fDOMContextBuilt = false;
        private final Element fSchemaRoot;
        private final SymbolTable fSymbolTable;
        private final QName fAttributeQName = new QName();
        SchemaRootContext(Element schemaRoot, SymbolTable symbolTable) {
            fSchemaRoot = schemaRoot;
            fSymbolTable = symbolTable;
        }
        void fillNamespaceContext() {
            if (fSchemaRoot != null) {
                Node currentNode = fSchemaRoot.getParentNode();
                while (currentNode != null) {
                    if (Node.ELEMENT_NODE == currentNode.getNodeType()) {
                        NamedNodeMap attributes = currentNode.getAttributes();
                        final int attrCount = attributes.getLength();
                        for (int i = 0; i < attrCount; ++i) {
                            Attr attr = (Attr) attributes.item(i);
                            String value = attr.getValue();
                            if (value == null) {
                                value = XMLSymbols.EMPTY_STRING;
                            }
                            fillQName(fAttributeQName, attr);
                            if (fAttributeQName.uri == NamespaceContext.XMLNS_URI) {
                                if (fAttributeQName.prefix == XMLSymbols.PREFIX_XMLNS) {
                                    declarePrefix(fAttributeQName.localpart, value.length() != 0 ? fSymbolTable.addSymbol(value) : null);
                                }
                                else {
                                    declarePrefix(XMLSymbols.EMPTY_STRING, value.length() != 0 ? fSymbolTable.addSymbol(value) : null);
                                }
                            }
                        }
                    }
                    currentNode = currentNode.getParentNode();
                }
            }
        }
        String getURI(String prefix) {
            for (int i = 0; i < fNamespaceSize; i += 2) {
                if (fNamespace[i] == prefix) {
                    return fNamespace[i + 1];
                }
            }
            return null;
        }
        private void declarePrefix(String prefix, String uri) {           
            if (fNamespaceSize == fNamespace.length) {
                String[] namespacearray = new String[fNamespaceSize * 2];
                System.arraycopy(fNamespace, 0, namespacearray, 0, fNamespaceSize);
                fNamespace = namespacearray;
            }
            fNamespace[fNamespaceSize++] = prefix;
            fNamespace[fNamespaceSize++] = uri;
        }
        private void fillQName(QName toFill, Node node) {
            final String prefix = node.getPrefix();
            final String localName = node.getLocalName();
            final String rawName = node.getNodeName();
            final String namespace = node.getNamespaceURI();
            toFill.prefix = (prefix != null) ? fSymbolTable.addSymbol(prefix) : XMLSymbols.EMPTY_STRING;
            toFill.localpart = (localName != null) ? fSymbolTable.addSymbol(localName) : XMLSymbols.EMPTY_STRING;
            toFill.rawname = (rawName != null) ? fSymbolTable.addSymbol(rawName) : XMLSymbols.EMPTY_STRING; 
            toFill.uri = (namespace != null && namespace.length() > 0) ? fSymbolTable.addSymbol(namespace) : null;
        }
    }
} 
