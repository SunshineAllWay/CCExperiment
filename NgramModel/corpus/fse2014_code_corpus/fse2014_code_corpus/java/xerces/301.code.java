package org.apache.xerces.impl.dtd;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XML11DTDScannerImpl;
import org.apache.xerces.impl.XMLDTDScannerImpl;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XML11Char;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLEntityResolver;
public class XML11DTDProcessor extends XMLDTDLoader{
    public XML11DTDProcessor() {
        super();
    } 
    public XML11DTDProcessor(SymbolTable symbolTable) {
        super(symbolTable);
    } 
    public XML11DTDProcessor(SymbolTable symbolTable,
                XMLGrammarPool grammarPool) {
        super(symbolTable, grammarPool);
    } 
    XML11DTDProcessor(SymbolTable symbolTable,
                XMLGrammarPool grammarPool, XMLErrorReporter errorReporter, 
                XMLEntityResolver entityResolver) {
        super(symbolTable, grammarPool, errorReporter, entityResolver);
    } 
    protected boolean isValidNmtoken(String nmtoken) {
        return XML11Char.isXML11ValidNmtoken(nmtoken);
    } 
    protected boolean isValidName(String name) {
        return XML11Char.isXML11ValidName(name);
    } 
    protected XMLDTDScannerImpl createDTDScanner(SymbolTable symbolTable,
            XMLErrorReporter errorReporter, XMLEntityManager entityManager) {
        return new XML11DTDScannerImpl(symbolTable, errorReporter, entityManager);
    } 
    protected short getScannerVersion() {
        return Constants.XML_VERSION_1_1;
    } 
} 
