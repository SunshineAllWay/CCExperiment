package org.apache.xerces.parsers;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponentManager;
public class SecurityConfiguration extends XIncludeAwareParserConfiguration
{
    protected static final String SECURITY_MANAGER_PROPERTY =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY;
    public SecurityConfiguration () {
        this(null, null, null);
    } 
    public SecurityConfiguration (SymbolTable symbolTable) {
        this(symbolTable, null, null);
    } 
    public SecurityConfiguration (SymbolTable symbolTable,
                                         XMLGrammarPool grammarPool) {
        this(symbolTable, grammarPool, null);
    } 
    public SecurityConfiguration (SymbolTable symbolTable,
                                         XMLGrammarPool grammarPool,
                                         XMLComponentManager parentSettings) {
        super(symbolTable, grammarPool, parentSettings);
        setProperty(SECURITY_MANAGER_PROPERTY, new SecurityManager());
    } 
} 
