package org.apache.xerces.impl;
import java.io.IOException;
import org.apache.xerces.impl.dtd.XMLDTDValidatorFilter;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDocumentSource;
public class XMLNSDocumentScannerImpl
extends XMLDocumentScannerImpl {
    protected boolean fBindNamespaces;
    protected boolean fPerformValidation;
    private XMLDTDValidatorFilter fDTDValidator;
    private boolean fSawSpace;
    public void setDTDValidator(XMLDTDValidatorFilter dtdValidator) {
        fDTDValidator = dtdValidator;
    }
    protected boolean scanStartElement()
    throws IOException, XNIException {
        if (DEBUG_CONTENT_SCANNING) System.out.println(">>> scanStartElementNS()");
        fEntityScanner.scanQName(fElementQName);
        String rawname = fElementQName.rawname;
        if (fBindNamespaces) {
            fNamespaceContext.pushContext();
            if (fScannerState == SCANNER_STATE_ROOT_ELEMENT) {
                if (fPerformValidation) {
                    fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                               "MSG_GRAMMAR_NOT_FOUND",
                                               new Object[]{ rawname},
                                               XMLErrorReporter.SEVERITY_ERROR);
                    if (fDoctypeName == null || !fDoctypeName.equals(rawname)) {
                        fErrorReporter.reportError( XMLMessageFormatter.XML_DOMAIN,
                                                    "RootElementTypeMustMatchDoctypedecl",
                                                    new Object[]{fDoctypeName, rawname},
                                                    XMLErrorReporter.SEVERITY_ERROR);
                    }
                }
            }
        }
        fCurrentElement = fElementStack.pushElement(fElementQName);
        boolean empty = false;
        fAttributes.removeAllAttributes();
        do {
            boolean sawSpace = fEntityScanner.skipSpaces();
            int c = fEntityScanner.peekChar();
            if (c == '>') {
                fEntityScanner.scanChar();
                break;
            }
            else if (c == '/') {
                fEntityScanner.scanChar();
                if (!fEntityScanner.skipChar('>')) {
                    reportFatalError("ElementUnterminated",
                                     new Object[]{rawname});
                }
                empty = true;
                break;
            }
            else if (!isValidNameStartChar(c) || !sawSpace) {
                reportFatalError("ElementUnterminated", new Object[]{rawname});
            }
            scanAttribute(fAttributes);
        } while (true);
        if (fBindNamespaces) {
            if (fElementQName.prefix == XMLSymbols.PREFIX_XMLNS) {
                fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                           "ElementXMLNSPrefix",
                                           new Object[]{fElementQName.rawname},
                                           XMLErrorReporter.SEVERITY_FATAL_ERROR);
            }
            String prefix = fElementQName.prefix != null
                            ? fElementQName.prefix : XMLSymbols.EMPTY_STRING;
            fElementQName.uri = fNamespaceContext.getURI(prefix);
            fCurrentElement.uri = fElementQName.uri;
            if (fElementQName.prefix == null && fElementQName.uri != null) {
                fElementQName.prefix = XMLSymbols.EMPTY_STRING;
                fCurrentElement.prefix = XMLSymbols.EMPTY_STRING;
            }
            if (fElementQName.prefix != null && fElementQName.uri == null) {
                fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                           "ElementPrefixUnbound",
                                           new Object[]{fElementQName.prefix, fElementQName.rawname},
                                           XMLErrorReporter.SEVERITY_FATAL_ERROR);
            }
            int length = fAttributes.getLength();
            for (int i = 0; i < length; i++) {
                fAttributes.getName(i, fAttributeQName);
                String aprefix = fAttributeQName.prefix != null
                                 ? fAttributeQName.prefix : XMLSymbols.EMPTY_STRING;
                String uri = fNamespaceContext.getURI(aprefix);
                if (fAttributeQName.uri != null && fAttributeQName.uri == uri) {
                    continue;
                }
                if (aprefix != XMLSymbols.EMPTY_STRING) {
                    fAttributeQName.uri = uri;
                    if (uri == null) {
                        fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                                   "AttributePrefixUnbound",
                                                   new Object[]{fElementQName.rawname,fAttributeQName.rawname,aprefix},
                                                   XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                    fAttributes.setURI(i, uri);
                }
            }
            if (length > 1) {
                QName name = fAttributes.checkDuplicatesNS();
                if (name != null) {
                    if (name.uri != null) {
                        fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                                   "AttributeNSNotUnique",
                                                   new Object[]{fElementQName.rawname, name.localpart, name.uri},
                                                   XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                    else {
                        fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                                   "AttributeNotUnique",
                                                   new Object[]{fElementQName.rawname, name.rawname}, 
                                                   XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                }
            }
        }
        if (fDocumentHandler != null) {
            if (empty) {
                fMarkupDepth--;
                if (fMarkupDepth < fEntityStack[fEntityDepth - 1]) {
                    reportFatalError("ElementEntityMismatch",
                                     new Object[]{fCurrentElement.rawname});
                }
                fDocumentHandler.emptyElement(fElementQName, fAttributes, null);
                if (fBindNamespaces) {
                    fNamespaceContext.popContext();
                }
                fElementStack.popElement(fElementQName);
            } else {
                fDocumentHandler.startElement(fElementQName, fAttributes, null);
            }
        }
        if (DEBUG_CONTENT_SCANNING) System.out.println("<<< scanStartElement(): "+empty);
        return empty;
    } 
    protected void scanStartElementName ()
        throws IOException, XNIException {
        fEntityScanner.scanQName(fElementQName);
        fSawSpace = fEntityScanner.skipSpaces();
    } 
    protected boolean scanStartElementAfterName()
        throws IOException, XNIException {
        String rawname = fElementQName.rawname;
        if (fBindNamespaces) {
            fNamespaceContext.pushContext();
            if (fScannerState == SCANNER_STATE_ROOT_ELEMENT) {
                if (fPerformValidation) {
                    fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                               "MSG_GRAMMAR_NOT_FOUND",
                                               new Object[]{ rawname},
                                               XMLErrorReporter.SEVERITY_ERROR);
                    if (fDoctypeName == null || !fDoctypeName.equals(rawname)) {
                        fErrorReporter.reportError( XMLMessageFormatter.XML_DOMAIN,
                                                    "RootElementTypeMustMatchDoctypedecl",
                                                    new Object[]{fDoctypeName, rawname},
                                                    XMLErrorReporter.SEVERITY_ERROR);
                    }
                }
            }
        }
        fCurrentElement = fElementStack.pushElement(fElementQName);
        boolean empty = false;
        fAttributes.removeAllAttributes();
        do {
            int c = fEntityScanner.peekChar();
            if (c == '>') {
                fEntityScanner.scanChar();
                break;
            }
            else if (c == '/') {
                fEntityScanner.scanChar();
                if (!fEntityScanner.skipChar('>')) {
                    reportFatalError("ElementUnterminated",
                                     new Object[]{rawname});
                }
                empty = true;
                break;
            }
            else if (!isValidNameStartChar(c) || !fSawSpace) {
                reportFatalError("ElementUnterminated", new Object[]{rawname});
            }
            scanAttribute(fAttributes);
            fSawSpace = fEntityScanner.skipSpaces();
        } while (true);
        if (fBindNamespaces) {
            if (fElementQName.prefix == XMLSymbols.PREFIX_XMLNS) {
                fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                           "ElementXMLNSPrefix",
                                           new Object[]{fElementQName.rawname},
                                           XMLErrorReporter.SEVERITY_FATAL_ERROR);
            }
            String prefix = fElementQName.prefix != null
                            ? fElementQName.prefix : XMLSymbols.EMPTY_STRING;
            fElementQName.uri = fNamespaceContext.getURI(prefix);
            fCurrentElement.uri = fElementQName.uri;
            if (fElementQName.prefix == null && fElementQName.uri != null) {
                fElementQName.prefix = XMLSymbols.EMPTY_STRING;
                fCurrentElement.prefix = XMLSymbols.EMPTY_STRING;
            }
            if (fElementQName.prefix != null && fElementQName.uri == null) {
                fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                           "ElementPrefixUnbound",
                                           new Object[]{fElementQName.prefix, fElementQName.rawname},
                                           XMLErrorReporter.SEVERITY_FATAL_ERROR);
            }
            int length = fAttributes.getLength();
            for (int i = 0; i < length; i++) {
                fAttributes.getName(i, fAttributeQName);
                String aprefix = fAttributeQName.prefix != null
                                 ? fAttributeQName.prefix : XMLSymbols.EMPTY_STRING;
                String uri = fNamespaceContext.getURI(aprefix);
                if (fAttributeQName.uri != null && fAttributeQName.uri == uri) {
                    continue;
                }
                if (aprefix != XMLSymbols.EMPTY_STRING) {
                    fAttributeQName.uri = uri;
                    if (uri == null) {
                        fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                                   "AttributePrefixUnbound",
                                                   new Object[]{fElementQName.rawname,fAttributeQName.rawname,aprefix},
                                                   XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                    fAttributes.setURI(i, uri);
                }
            }
            if (length > 1) {
                QName name = fAttributes.checkDuplicatesNS();
                if (name != null) {
                    if (name.uri != null) {
                        fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                                   "AttributeNSNotUnique",
                                                   new Object[]{fElementQName.rawname, name.localpart, name.uri},
                                                   XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                    else {
                        fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                                   "AttributeNotUnique",
                                                   new Object[]{fElementQName.rawname, name.rawname}, 
                                                   XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                }
            }
        }
        if (fDocumentHandler != null) {
            if (empty) {
                fMarkupDepth--;
                if (fMarkupDepth < fEntityStack[fEntityDepth - 1]) {
                    reportFatalError("ElementEntityMismatch",
                                     new Object[]{fCurrentElement.rawname});
                }
                fDocumentHandler.emptyElement(fElementQName, fAttributes, null);
                if (fBindNamespaces) {
                    fNamespaceContext.popContext();
                }
                fElementStack.popElement(fElementQName);
            } else {
                fDocumentHandler.startElement(fElementQName, fAttributes, null);
            }
        }
        if (DEBUG_CONTENT_SCANNING) System.out.println("<<< scanStartElementAfterName(): "+empty);
        return empty;
    } 
    protected void scanAttribute(XMLAttributesImpl attributes)
    throws IOException, XNIException {
        if (DEBUG_CONTENT_SCANNING) System.out.println(">>> scanAttribute()");
        fEntityScanner.scanQName(fAttributeQName);
        fEntityScanner.skipSpaces();
        if (!fEntityScanner.skipChar('=')) {
            reportFatalError("EqRequiredInAttribute",
                             new Object[]{fCurrentElement.rawname,fAttributeQName.rawname});
        }
        fEntityScanner.skipSpaces();
        int attrIndex;
        if (fBindNamespaces) {
            attrIndex = attributes.getLength();
            attributes.addAttributeNS(fAttributeQName, XMLSymbols.fCDATASymbol, null);
        }
        else {
            int oldLen = attributes.getLength();
            attrIndex = attributes.addAttribute(fAttributeQName, XMLSymbols.fCDATASymbol, null);
            if (oldLen == attributes.getLength()) {
                reportFatalError("AttributeNotUnique",
                                 new Object[]{fCurrentElement.rawname,
                                 fAttributeQName.rawname});
            }
        }
        boolean isSameNormalizedAttr = scanAttributeValue(this.fTempString, fTempString2,
                fAttributeQName.rawname, fIsEntityDeclaredVC, fCurrentElement.rawname);
        String value = fTempString.toString();
        attributes.setValue(attrIndex, value);
        if (!isSameNormalizedAttr) {
            attributes.setNonNormalizedValue(attrIndex, fTempString2.toString());
        }
        attributes.setSpecified(attrIndex, true);
        if (fBindNamespaces) {
            String localpart = fAttributeQName.localpart;
            String prefix = fAttributeQName.prefix != null
                            ? fAttributeQName.prefix : XMLSymbols.EMPTY_STRING;
            if (prefix == XMLSymbols.PREFIX_XMLNS ||
                prefix == XMLSymbols.EMPTY_STRING && localpart == XMLSymbols.PREFIX_XMLNS) {
                String uri = fSymbolTable.addSymbol(value);
                if (prefix == XMLSymbols.PREFIX_XMLNS && localpart == XMLSymbols.PREFIX_XMLNS) {
                    fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                               "CantBindXMLNS",
                                               new Object[]{fAttributeQName},
                                               XMLErrorReporter.SEVERITY_FATAL_ERROR);
                }
                if (uri == NamespaceContext.XMLNS_URI) {
                    fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                               "CantBindXMLNS",
                                               new Object[]{fAttributeQName},
                                               XMLErrorReporter.SEVERITY_FATAL_ERROR);
                }
                if (localpart == XMLSymbols.PREFIX_XML) {
                    if (uri != NamespaceContext.XML_URI) {
                        fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                                   "CantBindXML",
                                                   new Object[]{fAttributeQName},
                                                   XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                }
                else {
                    if (uri ==NamespaceContext.XML_URI) {
                        fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                                   "CantBindXML",
                                                   new Object[]{fAttributeQName},
                                                   XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                }
                prefix = localpart != XMLSymbols.PREFIX_XMLNS ? localpart : XMLSymbols.EMPTY_STRING;
                if (uri == XMLSymbols.EMPTY_STRING && localpart != XMLSymbols.PREFIX_XMLNS) {
                    fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                               "EmptyPrefixedAttName",
                                               new Object[]{fAttributeQName},
                                               XMLErrorReporter.SEVERITY_FATAL_ERROR);
                }
                fNamespaceContext.declarePrefix(prefix, uri.length() != 0 ? uri : null);
                attributes.setURI(attrIndex, fNamespaceContext.getURI(XMLSymbols.PREFIX_XMLNS));
            }
            else {
                if (fAttributeQName.prefix != null) {
                    attributes.setURI(attrIndex, fNamespaceContext.getURI(fAttributeQName.prefix));
                }
            }
        }
        if (DEBUG_CONTENT_SCANNING) System.out.println("<<< scanAttribute()");
    } 
    protected int scanEndElement() throws IOException, XNIException {
        if (DEBUG_CONTENT_SCANNING) System.out.println(">>> scanEndElement()");
        fElementStack.popElement(fElementQName) ;
        if (!fEntityScanner.skipString(fElementQName.rawname)) {
            reportFatalError("ETagRequired", new Object[]{fElementQName.rawname});
        }
        fEntityScanner.skipSpaces();
        if (!fEntityScanner.skipChar('>')) {
            reportFatalError("ETagUnterminated",
                             new Object[]{fElementQName.rawname});
        }
        fMarkupDepth--;
        fMarkupDepth--;
        if (fMarkupDepth < fEntityStack[fEntityDepth - 1]) {
            reportFatalError("ElementEntityMismatch",
                             new Object[]{fCurrentElement.rawname});
        }
        if (fDocumentHandler != null ) {
            fDocumentHandler.endElement(fElementQName, null);
            if (fBindNamespaces) {
                fNamespaceContext.popContext();
            }
        }
        return fMarkupDepth;
    } 
    public void reset(XMLComponentManager componentManager)
    throws XMLConfigurationException {
        super.reset(componentManager);
        fPerformValidation = false;
        fBindNamespaces = false;
    }
    protected Dispatcher createContentDispatcher() {
        return new NSContentDispatcher();
    } 
    protected final class NSContentDispatcher
        extends ContentDispatcher {
        protected boolean scanRootElementHook()
            throws IOException, XNIException {
            if (fExternalSubsetResolver != null && !fSeenDoctypeDecl
                && !fDisallowDoctype && (fValidation || fLoadExternalDTD)) {
                scanStartElementName();
                resolveExternalSubsetAndRead();
                reconfigurePipeline();
                if (scanStartElementAfterName()) {
                    setScannerState(SCANNER_STATE_TRAILING_MISC);
                    setDispatcher(fTrailingMiscDispatcher);
                    return true;
                }
            }
            else {
                reconfigurePipeline();
                if (scanStartElement()) {
                    setScannerState(SCANNER_STATE_TRAILING_MISC);
                    setDispatcher(fTrailingMiscDispatcher);
                    return true;
                }
            }
            return false;
        } 
        private void reconfigurePipeline() {
            if (fDTDValidator == null) {
                fBindNamespaces = true;
            }
            else if (!fDTDValidator.hasGrammar()) {
                fBindNamespaces = true;
                fPerformValidation = fDTDValidator.validate();
                XMLDocumentSource source = fDTDValidator.getDocumentSource();
                XMLDocumentHandler handler = fDTDValidator.getDocumentHandler();
                source.setDocumentHandler(handler);
                if (handler != null)
                    handler.setDocumentSource(source);
                fDTDValidator.setDocumentSource(null);
                fDTDValidator.setDocumentHandler(null);
            }
        } 
    }
} 
