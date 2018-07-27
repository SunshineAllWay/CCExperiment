package org.apache.xerces.impl.xs.opti;
import java.io.IOException;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.w3c.dom.Document;
public class SchemaDOMParser extends DefaultXMLDocumentHandler {
    public static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    public static final String GENERATE_SYNTHETIC_ANNOTATION =
        Constants.XERCES_FEATURE_PREFIX + Constants.GENERATE_SYNTHETIC_ANNOTATIONS_FEATURE;
    protected XMLLocator   fLocator;
    protected NamespaceContext fNamespaceContext = null;
    SchemaDOM schemaDOM;
    XMLParserConfiguration config;
    public SchemaDOMParser(XMLParserConfiguration config) {
        this.config = config;
        config.setDocumentHandler(this);
        config.setDTDHandler(this);
        config.setDTDContentModelHandler(this);
    }
    private ElementImpl fCurrentAnnotationElement;
    private int fAnnotationDepth = -1;
    private int fInnerAnnotationDepth = -1;
    private int fDepth = -1;
    XMLErrorReporter fErrorReporter;
    private boolean fGenerateSyntheticAnnotation = false;
    private BooleanStack fHasNonSchemaAttributes = new BooleanStack();
    private BooleanStack fSawAnnotation = new BooleanStack();
    private XMLAttributes fEmptyAttr = new XMLAttributesImpl();
    public void startDocument(XMLLocator locator, String encoding, 
            NamespaceContext namespaceContext, Augmentations augs)
    throws XNIException {
        fErrorReporter = (XMLErrorReporter)config.getProperty(ERROR_REPORTER);
        fGenerateSyntheticAnnotation = config.getFeature(GENERATE_SYNTHETIC_ANNOTATION);
        fHasNonSchemaAttributes.clear();
        fSawAnnotation.clear();
        schemaDOM = new SchemaDOM();
        fCurrentAnnotationElement = null;
        fAnnotationDepth = -1;
        fInnerAnnotationDepth = -1;
        fDepth = -1;
        fLocator = locator;
        fNamespaceContext = namespaceContext;
        schemaDOM.setDocumentURI(locator.getExpandedSystemId());
    } 
    public void endDocument(Augmentations augs) throws XNIException {
    } 
    public void comment(XMLString text, Augmentations augs) throws XNIException {
        if(fAnnotationDepth > -1) {
            schemaDOM.comment(text);
        }
    }
    public void processingInstruction(String target, XMLString data, Augmentations augs)
    throws XNIException {
        if (fAnnotationDepth > -1) {
            schemaDOM.processingInstruction(target, data);
        }
    }
    public void characters(XMLString text, Augmentations augs) throws XNIException {
        if (fInnerAnnotationDepth == -1 ) {
            for (int i=text.offset; i<text.offset+text.length; i++) {
                if (!XMLChar.isSpace(text.ch[i])) {
                    String txt = new String(text.ch, i, text.length+text.offset-i);
                    fErrorReporter.reportError(fLocator, 
                            XSMessageFormatter.SCHEMA_DOMAIN,
                            "s4s-elt-character",
                            new Object[]{txt},
                            XMLErrorReporter.SEVERITY_ERROR);
                    break;
                }
            }
        }
        else {
            schemaDOM.characters(text);
        }
    }
    public void startElement(QName element, XMLAttributes attributes, Augmentations augs)
    throws XNIException {
        fDepth++;
        if (fAnnotationDepth == -1) {
            if (element.uri == SchemaSymbols.URI_SCHEMAFORSCHEMA &&
                    element.localpart == SchemaSymbols.ELT_ANNOTATION) {
                if (fGenerateSyntheticAnnotation) {
                    if (fSawAnnotation.size() > 0) {
                        fSawAnnotation.pop();
                    }
                    fSawAnnotation.push(true);
                }
                fAnnotationDepth = fDepth;
                schemaDOM.startAnnotation(element, attributes, fNamespaceContext);
                fCurrentAnnotationElement = schemaDOM.startElement(element, attributes, 
                        fLocator.getLineNumber(),
                        fLocator.getColumnNumber(),
                        fLocator.getCharacterOffset());
                return;
            } 
            else if (element.uri == SchemaSymbols.URI_SCHEMAFORSCHEMA && fGenerateSyntheticAnnotation) {
                fSawAnnotation.push(false);
                fHasNonSchemaAttributes.push(hasNonSchemaAttributes(element, attributes));
            }
        } 
        else if (fDepth == fAnnotationDepth + 1) {
            fInnerAnnotationDepth = fDepth;
            schemaDOM.startAnnotationElement(element, attributes);
        } 
        else {
            schemaDOM.startAnnotationElement(element, attributes);
            return;
        }
        schemaDOM.startElement(element, attributes, 
                fLocator.getLineNumber(),
                fLocator.getColumnNumber(),
                fLocator.getCharacterOffset());
    }
    public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs)
    throws XNIException {
        if (fGenerateSyntheticAnnotation && fAnnotationDepth == -1 && 
                element.uri == SchemaSymbols.URI_SCHEMAFORSCHEMA && element.localpart != SchemaSymbols.ELT_ANNOTATION && hasNonSchemaAttributes(element, attributes)) { 
            schemaDOM.startElement(element, attributes,
                    fLocator.getLineNumber(),
                    fLocator.getColumnNumber(),
                    fLocator.getCharacterOffset());
            attributes.removeAllAttributes();
            String schemaPrefix = fNamespaceContext.getPrefix(SchemaSymbols.URI_SCHEMAFORSCHEMA);
            final String annRawName = (schemaPrefix.length() == 0) ? SchemaSymbols.ELT_ANNOTATION : (schemaPrefix + ':' + SchemaSymbols.ELT_ANNOTATION);
            schemaDOM.startAnnotation(annRawName, attributes, fNamespaceContext);
            final String elemRawName = (schemaPrefix.length() == 0) ? SchemaSymbols.ELT_DOCUMENTATION : (schemaPrefix + ':' + SchemaSymbols.ELT_DOCUMENTATION);
            schemaDOM.startAnnotationElement(elemRawName, attributes);
            schemaDOM.charactersRaw("SYNTHETIC_ANNOTATION");     
            schemaDOM.endSyntheticAnnotationElement(elemRawName, false);
            schemaDOM.endSyntheticAnnotationElement(annRawName, true);
            schemaDOM.endElement();
            return;
        }
        if (fAnnotationDepth == -1) {
            if (element.uri == SchemaSymbols.URI_SCHEMAFORSCHEMA &&
                    element.localpart == SchemaSymbols.ELT_ANNOTATION) {
                schemaDOM.startAnnotation(element, attributes, fNamespaceContext);
            }
        } 
        else {
            schemaDOM.startAnnotationElement(element, attributes);
        }
        ElementImpl newElem = schemaDOM.emptyElement(element, attributes, 
                fLocator.getLineNumber(),
                fLocator.getColumnNumber(),
                fLocator.getCharacterOffset());
        if (fAnnotationDepth == -1) {
            if (element.uri == SchemaSymbols.URI_SCHEMAFORSCHEMA &&
                    element.localpart == SchemaSymbols.ELT_ANNOTATION) {
                schemaDOM.endAnnotation(element, newElem);
            }
        } 
        else {
            schemaDOM.endAnnotationElement(element);
        } 
    }
    public void endElement(QName element, Augmentations augs) throws XNIException {
        if(fAnnotationDepth > -1) {
            if (fInnerAnnotationDepth == fDepth) {
                fInnerAnnotationDepth = -1;
                schemaDOM.endAnnotationElement(element);
                schemaDOM.endElement();
            } else if (fAnnotationDepth == fDepth) {
                fAnnotationDepth = -1;
                schemaDOM.endAnnotation(element, fCurrentAnnotationElement);
                schemaDOM.endElement();
            } else { 
                schemaDOM.endAnnotationElement(element);
            }
        } else { 
            if(element.uri == SchemaSymbols.URI_SCHEMAFORSCHEMA && fGenerateSyntheticAnnotation) {
                boolean value = fHasNonSchemaAttributes.pop();
                boolean sawann = fSawAnnotation.pop();
                if (value && !sawann) {
                    String schemaPrefix = fNamespaceContext.getPrefix(SchemaSymbols.URI_SCHEMAFORSCHEMA);
                    final String annRawName = (schemaPrefix.length() == 0) ? SchemaSymbols.ELT_ANNOTATION : (schemaPrefix + ':' + SchemaSymbols.ELT_ANNOTATION);
                    schemaDOM.startAnnotation(annRawName, fEmptyAttr, fNamespaceContext);
                    final String elemRawName = (schemaPrefix.length() == 0) ? SchemaSymbols.ELT_DOCUMENTATION : (schemaPrefix + ':' + SchemaSymbols.ELT_DOCUMENTATION);
                    schemaDOM.startAnnotationElement(elemRawName, fEmptyAttr);
                    schemaDOM.charactersRaw("SYNTHETIC_ANNOTATION");     
                    schemaDOM.endSyntheticAnnotationElement(elemRawName, false);
                    schemaDOM.endSyntheticAnnotationElement(annRawName, true);
                }
            }
            schemaDOM.endElement();
        }
        fDepth--;
    }
    private boolean hasNonSchemaAttributes(QName element, XMLAttributes attributes) {
        final int length = attributes.getLength();
        for (int i = 0; i < length; ++i) {
            String uri = attributes.getURI(i);
            if (uri != null && uri != SchemaSymbols.URI_SCHEMAFORSCHEMA && 
                    uri != NamespaceContext.XMLNS_URI &&
                    !(uri == NamespaceContext.XML_URI && 
                            attributes.getQName(i) == SchemaSymbols.ATT_XML_LANG && element.localpart == SchemaSymbols.ELT_SCHEMA)) {
                return true;
            }
        }
        return false;
    }
    public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException {
        if (fAnnotationDepth != -1 ) {
            schemaDOM.characters(text);
        }
    }
    public void startCDATA(Augmentations augs) throws XNIException {
        if (fAnnotationDepth != -1) {
            schemaDOM.startAnnotationCDATA();
        }
    }
    public void endCDATA(Augmentations augs) throws XNIException {
        if (fAnnotationDepth != -1) {
            schemaDOM.endAnnotationCDATA();
        }
    }
    public Document getDocument() {
        return schemaDOM;
    }
    public void setFeature(String featureId, boolean state){
    	config.setFeature(featureId, state);
    }
    public boolean getFeature(String featureId){
        return config.getFeature(featureId);
    }
    public void setProperty(String propertyId, Object value){
        config.setProperty(propertyId, value);
    }
    public Object getProperty(String propertyId){
        return config.getProperty(propertyId);
    }
    public void setEntityResolver(XMLEntityResolver er) {
    	config.setEntityResolver(er);
    }
    public void parse(XMLInputSource inputSource) throws IOException {
        config.parse(inputSource);
    }
    public void reset() {
    	((SchemaParsingConfig)config).reset();
    }
    public void resetNodePool() {
    	((SchemaParsingConfig)config).resetNodePool();
    }
    private static final class BooleanStack {
        private int fDepth;
        private boolean[] fData;
        public BooleanStack () {}
        public int size() {
            return fDepth;
        }
        public void push(boolean value) {
            ensureCapacity(fDepth + 1);
            fData[fDepth++] = value;
        }
        public boolean pop() {
            return fData[--fDepth];
        }
        public void clear() {
            fDepth = 0;
        }
        private void ensureCapacity(int size) {
            if (fData == null) {
                fData = new boolean[32];
            }
            else if (fData.length <= size) {
                boolean[] newdata = new boolean[fData.length * 2];
                System.arraycopy(fData, 0, newdata, 0, fData.length);
                fData = newdata;
            }
        }
    }
}
