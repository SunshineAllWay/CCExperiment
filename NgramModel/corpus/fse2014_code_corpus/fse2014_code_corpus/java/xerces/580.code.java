package org.apache.xerces.parsers;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xinclude.XIncludeHandler;
import org.apache.xerces.xinclude.XIncludeNamespaceSupport;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xpointer.XPointerHandler;
public class XPointerParserConfiguration extends XML11Configuration {
    private XPointerHandler fXPointerHandler;
    private XIncludeHandler fXIncludeHandler;
    protected static final String ALLOW_UE_AND_NOTATION_EVENTS =
        Constants.SAX_FEATURE_PREFIX + Constants.ALLOW_DTD_EVENTS_AFTER_ENDDTD_FEATURE;
    protected static final String XINCLUDE_FIXUP_BASE_URIS =
        Constants.XERCES_FEATURE_PREFIX + Constants.XINCLUDE_FIXUP_BASE_URIS_FEATURE;
    protected static final String XINCLUDE_FIXUP_LANGUAGE =
        Constants.XERCES_FEATURE_PREFIX + Constants.XINCLUDE_FIXUP_LANGUAGE_FEATURE;
    protected static final String XPOINTER_HANDLER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.XPOINTER_HANDLER_PROPERTY;
    protected static final String XINCLUDE_HANDLER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.XINCLUDE_HANDLER_PROPERTY;
    protected static final String NAMESPACE_CONTEXT =
        Constants.XERCES_PROPERTY_PREFIX + Constants.NAMESPACE_CONTEXT_PROPERTY;
    public XPointerParserConfiguration() {
        this(null, null, null);
    } 
    public XPointerParserConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    } 
    public XPointerParserConfiguration(
        SymbolTable symbolTable,
        XMLGrammarPool grammarPool) {
        this(symbolTable, grammarPool, null);
    } 
    public XPointerParserConfiguration(
        SymbolTable symbolTable,
        XMLGrammarPool grammarPool,
        XMLComponentManager parentSettings) {
        super(symbolTable, grammarPool, parentSettings);
        fXIncludeHandler = new XIncludeHandler();
        addCommonComponent(fXIncludeHandler);
        fXPointerHandler = new XPointerHandler();
        addCommonComponent(fXPointerHandler);
        final String[] recognizedFeatures = {
            ALLOW_UE_AND_NOTATION_EVENTS,
            XINCLUDE_FIXUP_BASE_URIS,
            XINCLUDE_FIXUP_LANGUAGE
        };
        addRecognizedFeatures(recognizedFeatures);
        final String[] recognizedProperties =
            { XINCLUDE_HANDLER, XPOINTER_HANDLER, NAMESPACE_CONTEXT };
        addRecognizedProperties(recognizedProperties);
        setFeature(ALLOW_UE_AND_NOTATION_EVENTS, true);
        setFeature(XINCLUDE_FIXUP_BASE_URIS, true);
        setFeature(XINCLUDE_FIXUP_LANGUAGE, true);
        setProperty(XINCLUDE_HANDLER, fXIncludeHandler);
        setProperty(XPOINTER_HANDLER, fXPointerHandler);
        setProperty(NAMESPACE_CONTEXT, new XIncludeNamespaceSupport());
    } 
    protected void configurePipeline() {
        super.configurePipeline();
        fDTDScanner.setDTDHandler(fDTDProcessor);
        fDTDProcessor.setDTDSource(fDTDScanner);
        fDTDProcessor.setDTDHandler(fXIncludeHandler);
        fXIncludeHandler.setDTDSource(fDTDProcessor);
        fXIncludeHandler.setDTDHandler(fXPointerHandler);
        fXPointerHandler.setDTDSource(fXIncludeHandler);
        fXPointerHandler.setDTDHandler(fDTDHandler);
        if (fDTDHandler != null) {
            fDTDHandler.setDTDSource(fXPointerHandler);
        }
        XMLDocumentSource prev = null;
        if (fFeatures.get(XMLSCHEMA_VALIDATION) == Boolean.TRUE) {
            prev = fSchemaValidator.getDocumentSource();
        }
        else {
            prev = fLastComponent;
            fLastComponent = fXPointerHandler;
        }
        XMLDocumentHandler next = prev.getDocumentHandler();
		prev.setDocumentHandler(fXIncludeHandler);
		fXIncludeHandler.setDocumentSource(prev);
		if (next != null) {
			fXIncludeHandler.setDocumentHandler(next);
            next.setDocumentSource(fXIncludeHandler);
        }
		fXIncludeHandler.setDocumentHandler(fXPointerHandler);
		fXPointerHandler.setDocumentSource(fXIncludeHandler);
    } 
	protected void configureXML11Pipeline() {
		super.configureXML11Pipeline();
		fXML11DTDScanner.setDTDHandler(fXML11DTDProcessor);
		fXML11DTDProcessor.setDTDSource(fXML11DTDScanner);
        fDTDProcessor.setDTDHandler(fXIncludeHandler);
        fXIncludeHandler.setDTDSource(fXML11DTDProcessor);
        fXIncludeHandler.setDTDHandler(fXPointerHandler);
        fXPointerHandler.setDTDSource(fXIncludeHandler);
        fXPointerHandler.setDTDHandler(fDTDHandler);
        if (fDTDHandler != null) {
            fDTDHandler.setDTDSource(fXPointerHandler);
        }
		XMLDocumentSource prev = null;
		if (fFeatures.get(XMLSCHEMA_VALIDATION) == Boolean.TRUE) {
			prev = fSchemaValidator.getDocumentSource();
		}
		else {
			prev = fLastComponent;
			fLastComponent = fXPointerHandler;
		}
        XMLDocumentHandler next = prev.getDocumentHandler();
		prev.setDocumentHandler(fXIncludeHandler);
		fXIncludeHandler.setDocumentSource(prev);
		if (next != null) {
			fXIncludeHandler.setDocumentHandler(next);
            next.setDocumentSource(fXIncludeHandler);
        }
		fXIncludeHandler.setDocumentHandler(fXPointerHandler);
		fXPointerHandler.setDocumentSource(fXIncludeHandler);
	} 
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {
        super.setProperty(propertyId, value);
    } 
}