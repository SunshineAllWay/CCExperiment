package org.apache.xerces.parsers;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.dv.DTDDVFactory;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
public abstract class XMLGrammarParser
    extends XMLParser {
    protected DTDDVFactory fDatatypeValidatorFactory;
    protected XMLGrammarParser(SymbolTable symbolTable) {
        super((XMLParserConfiguration)ObjectFactory.createObject(
            "org.apache.xerces.xni.parser.XMLParserConfiguration",
            "org.apache.xerces.parsers.XIncludeAwareParserConfiguration"
            ));
        fConfiguration.setProperty(Constants.XERCES_PROPERTY_PREFIX+Constants.SYMBOL_TABLE_PROPERTY, symbolTable);
    }
} 
