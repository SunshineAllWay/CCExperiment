package org.apache.xerces.parsers;
import org.apache.xerces.impl.XMLDocumentScannerImpl;
import org.apache.xerces.impl.XMLNSDocumentScannerImpl;
import org.apache.xerces.impl.dtd.XMLDTDValidator;
import org.apache.xerces.impl.dtd.XMLNSDTDValidator;
import org.apache.xerces.impl.xs.XMLSchemaValidator;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLDocumentScanner;
public class IntegratedParserConfiguration
extends StandardParserConfiguration {
    protected XMLNSDocumentScannerImpl fNamespaceScanner;
    protected XMLDocumentScannerImpl fNonNSScanner;
    protected XMLDTDValidator fNonNSDTDValidator;
    public IntegratedParserConfiguration() {
        this(null, null, null);
    } 
    public IntegratedParserConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    } 
    public IntegratedParserConfiguration(SymbolTable symbolTable,
                                         XMLGrammarPool grammarPool) {
        this(symbolTable, grammarPool, null);
    } 
    public IntegratedParserConfiguration(SymbolTable symbolTable,
                                         XMLGrammarPool grammarPool,
                                         XMLComponentManager parentSettings) {
        super(symbolTable, grammarPool, parentSettings);
        fNonNSScanner = new XMLDocumentScannerImpl();
        fNonNSDTDValidator = new XMLDTDValidator();
        addComponent((XMLComponent)fNonNSScanner);
        addComponent((XMLComponent)fNonNSDTDValidator);
    } 
	protected void configurePipeline() {
		setProperty(DATATYPE_VALIDATOR_FACTORY, fDatatypeValidatorFactory);
		configureDTDPipeline();
		if (fFeatures.get(NAMESPACES) == Boolean.TRUE) {
            fProperties.put(NAMESPACE_BINDER, fNamespaceBinder);
			fScanner = fNamespaceScanner;
			fProperties.put(DOCUMENT_SCANNER, fNamespaceScanner);
			if (fDTDValidator != null) {
				fProperties.put(DTD_VALIDATOR, fDTDValidator);
				fNamespaceScanner.setDTDValidator(fDTDValidator);
				fNamespaceScanner.setDocumentHandler(fDTDValidator);
				fDTDValidator.setDocumentSource(fNamespaceScanner);
				fDTDValidator.setDocumentHandler(fDocumentHandler);
				if (fDocumentHandler != null) {
					fDocumentHandler.setDocumentSource(fDTDValidator);
				}
				fLastComponent = fDTDValidator;
			}
			else {
				fNamespaceScanner.setDocumentHandler(fDocumentHandler);
                fNamespaceScanner.setDTDValidator(null);
				if (fDocumentHandler != null) {
					fDocumentHandler.setDocumentSource(fNamespaceScanner);
				}
				fLastComponent = fNamespaceScanner;
			}
		}
		else {
			fScanner = fNonNSScanner;
			fProperties.put(DOCUMENT_SCANNER, fNonNSScanner);
			if (fNonNSDTDValidator != null) {
				fProperties.put(DTD_VALIDATOR, fNonNSDTDValidator);
				fNonNSScanner.setDocumentHandler(fNonNSDTDValidator);
				fNonNSDTDValidator.setDocumentSource(fNonNSScanner);
				fNonNSDTDValidator.setDocumentHandler(fDocumentHandler);
				if (fDocumentHandler != null) {
					fDocumentHandler.setDocumentSource(fNonNSDTDValidator);
				}
				fLastComponent = fNonNSDTDValidator;
			}
			else {
				fScanner.setDocumentHandler(fDocumentHandler);
				if (fDocumentHandler != null) {
					fDocumentHandler.setDocumentSource(fScanner);
				}
				fLastComponent = fScanner;
			}
		}
		if (fFeatures.get(XMLSCHEMA_VALIDATION) == Boolean.TRUE) {
			if (fSchemaValidator == null) {
				fSchemaValidator = new XMLSchemaValidator();
				fProperties.put(SCHEMA_VALIDATOR, fSchemaValidator);
				addComponent(fSchemaValidator);
				if (fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN) == null) {
					XSMessageFormatter xmft = new XSMessageFormatter();
					fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, xmft);
				}
			}
			fLastComponent.setDocumentHandler(fSchemaValidator);
			fSchemaValidator.setDocumentSource(fLastComponent);
			fSchemaValidator.setDocumentHandler(fDocumentHandler);
			if (fDocumentHandler != null) {
				fDocumentHandler.setDocumentSource(fSchemaValidator);
			}
			fLastComponent = fSchemaValidator;
		}
	} 
    protected XMLDocumentScanner createDocumentScanner() {
        fNamespaceScanner = new XMLNSDocumentScannerImpl();
        return fNamespaceScanner;
    } 
    protected XMLDTDValidator createDTDValidator() {
        return new XMLNSDTDValidator();
    } 
} 
