package org.apache.xerces.parsers;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
public class XMLDocumentParser
    extends AbstractXMLDocumentParser {
    public XMLDocumentParser() {
        super((XMLParserConfiguration)ObjectFactory.createObject(
            "org.apache.xerces.xni.parser.XMLParserConfiguration",
            "org.apache.xerces.parsers.XIncludeAwareParserConfiguration"
            ));
    } 
    public XMLDocumentParser(XMLParserConfiguration config) {
        super(config);
    } 
    public XMLDocumentParser(SymbolTable symbolTable) {
        super((XMLParserConfiguration)ObjectFactory.createObject(
            "org.apache.xerces.xni.parser.XMLParserConfiguration",
            "org.apache.xerces.parsers.XIncludeAwareParserConfiguration"
            ));
        fConfiguration.setProperty(Constants.XERCES_PROPERTY_PREFIX+Constants.SYMBOL_TABLE_PROPERTY, symbolTable);
    } 
    public XMLDocumentParser(SymbolTable symbolTable,
                             XMLGrammarPool grammarPool) {
        super((XMLParserConfiguration)ObjectFactory.createObject(
            "org.apache.xerces.xni.parser.XMLParserConfiguration",
            "org.apache.xerces.parsers.XIncludeAwareParserConfiguration"
            ));
        fConfiguration.setProperty(Constants.XERCES_PROPERTY_PREFIX+Constants.SYMBOL_TABLE_PROPERTY, symbolTable);
        fConfiguration.setProperty(Constants.XERCES_PROPERTY_PREFIX+Constants.XMLGRAMMAR_POOL_PROPERTY, grammarPool);
    }
} 
