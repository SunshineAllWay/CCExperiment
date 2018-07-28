package org.apache.xerces.impl.dtd;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDTDContentModelFilter;
import org.apache.xerces.xni.parser.XMLDTDContentModelSource;
import org.apache.xerces.xni.parser.XMLDTDFilter;
import org.apache.xerces.xni.parser.XMLDTDSource;
public class XMLDTDProcessor
        implements XMLComponent, XMLDTDFilter, XMLDTDContentModelFilter {
    private static final int TOP_LEVEL_SCOPE = -1;
    protected static final String VALIDATION =
        Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
    protected static final String NOTIFY_CHAR_REFS =
        Constants.XERCES_FEATURE_PREFIX + Constants.NOTIFY_CHAR_REFS_FEATURE;
    protected static final String WARN_ON_DUPLICATE_ATTDEF = 
        Constants.XERCES_FEATURE_PREFIX +Constants.WARN_ON_DUPLICATE_ATTDEF_FEATURE;
    protected static final String WARN_ON_UNDECLARED_ELEMDEF =
        Constants.XERCES_FEATURE_PREFIX + Constants.WARN_ON_UNDECLARED_ELEMDEF_FEATURE;
	protected static final String PARSER_SETTINGS = 
        Constants.XERCES_FEATURE_PREFIX + Constants.PARSER_SETTINGS;
    protected static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
    protected static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    protected static final String GRAMMAR_POOL =
        Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;
    protected static final String DTD_VALIDATOR =
        Constants.XERCES_PROPERTY_PREFIX + Constants.DTD_VALIDATOR_PROPERTY;
    private static final String[] RECOGNIZED_FEATURES = {
        VALIDATION,
        WARN_ON_DUPLICATE_ATTDEF,
        WARN_ON_UNDECLARED_ELEMDEF,
        NOTIFY_CHAR_REFS,
    };
    private static final Boolean[] FEATURE_DEFAULTS = {
        null,
        Boolean.FALSE,
        Boolean.FALSE,
        null,
    };
    private static final String[] RECOGNIZED_PROPERTIES = {
        SYMBOL_TABLE,       
        ERROR_REPORTER,
        GRAMMAR_POOL,       
        DTD_VALIDATOR,
    };
    private static final Object[] PROPERTY_DEFAULTS = {
        null,
        null,
        null,
        null,
    };
    protected boolean fValidation;
    protected boolean fDTDValidation;
    protected boolean fWarnDuplicateAttdef;
    protected boolean fWarnOnUndeclaredElemdef;
    protected SymbolTable fSymbolTable;
    protected XMLErrorReporter fErrorReporter;
    protected DTDGrammarBucket fGrammarBucket;
    protected XMLDTDValidator fValidator;
    protected XMLGrammarPool fGrammarPool;
    protected Locale fLocale;
    protected XMLDTDHandler fDTDHandler;
    protected XMLDTDSource fDTDSource;
    protected XMLDTDContentModelHandler fDTDContentModelHandler;
    protected XMLDTDContentModelSource fDTDContentModelSource;
    protected DTDGrammar fDTDGrammar;
    private boolean fPerformValidation;
    protected boolean fInDTDIgnore;
    private boolean fMixed;
    private final XMLEntityDecl fEntityDecl = new XMLEntityDecl();
    private final HashMap fNDataDeclNotations = new HashMap();
    private String fDTDElementDeclName = null;
    private final ArrayList fMixedElementTypes = new ArrayList();
    private final ArrayList fDTDElementDecls = new ArrayList();
    private HashMap fTableOfIDAttributeNames;
    private HashMap fTableOfNOTATIONAttributeNames;
    private HashMap fNotationEnumVals;
    public XMLDTDProcessor() {
    } 
    public void reset(XMLComponentManager componentManager) throws XMLConfigurationException {
        boolean parser_settings;
        try {
            parser_settings = componentManager.getFeature(PARSER_SETTINGS);
        } catch (XMLConfigurationException e) {
            parser_settings = true;
        }
        if (!parser_settings) {
            reset();
            return;
        }
        try {
            fValidation = componentManager.getFeature(VALIDATION);
        } catch (XMLConfigurationException e) {
            fValidation = false;
        }
        try {
            fDTDValidation =
                !(componentManager
                    .getFeature(
                        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE));
        } catch (XMLConfigurationException e) {
            fDTDValidation = true;
        }
        try {
            fWarnDuplicateAttdef = componentManager.getFeature(WARN_ON_DUPLICATE_ATTDEF);
        } 
        catch (XMLConfigurationException e) {
            fWarnDuplicateAttdef = false;
        }
        try {
            fWarnOnUndeclaredElemdef = componentManager.getFeature(WARN_ON_UNDECLARED_ELEMDEF);
        } 
        catch (XMLConfigurationException e) {
            fWarnOnUndeclaredElemdef = false;
        }
        fErrorReporter =
            (XMLErrorReporter) componentManager.getProperty(
                Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY);
        fSymbolTable =
            (SymbolTable) componentManager.getProperty(
                Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY);
        try {
            fGrammarPool = (XMLGrammarPool) componentManager.getProperty(GRAMMAR_POOL);
        } catch (XMLConfigurationException e) {
            fGrammarPool = null;
        }
        try {
            fValidator = (XMLDTDValidator) componentManager.getProperty(DTD_VALIDATOR);
        } catch (XMLConfigurationException e) {
            fValidator = null;
        } catch (ClassCastException e) {
            fValidator = null;
        }
        if (fValidator != null) {
            fGrammarBucket = fValidator.getGrammarBucket();
        } else {
            fGrammarBucket = null;
        }
        reset();
    } 
    protected void reset() {
        fDTDGrammar = null;
        fInDTDIgnore = false;
        fNDataDeclNotations.clear();
        if (fValidation) {
            if (fNotationEnumVals == null) {
                fNotationEnumVals = new HashMap();
            }
            fNotationEnumVals.clear();
            fTableOfIDAttributeNames = new HashMap();
            fTableOfNOTATIONAttributeNames = new HashMap();
        }
    }
    public String[] getRecognizedFeatures() {
        return (String[])(RECOGNIZED_FEATURES.clone());
    } 
    public void setFeature(String featureId, boolean state)
            throws XMLConfigurationException {
    } 
    public String[] getRecognizedProperties() {
        return (String[])(RECOGNIZED_PROPERTIES.clone());
    } 
    public void setProperty(String propertyId, Object value)
            throws XMLConfigurationException {
    } 
    public Boolean getFeatureDefault(String featureId) {
        for (int i = 0; i < RECOGNIZED_FEATURES.length; i++) {
            if (RECOGNIZED_FEATURES[i].equals(featureId)) {
                return FEATURE_DEFAULTS[i];
            }
        }
        return null;
    } 
    public Object getPropertyDefault(String propertyId) {
        for (int i = 0; i < RECOGNIZED_PROPERTIES.length; i++) {
            if (RECOGNIZED_PROPERTIES[i].equals(propertyId)) {
                return PROPERTY_DEFAULTS[i];
            }
        }
        return null;
    } 
    public void setDTDHandler(XMLDTDHandler dtdHandler) {
        fDTDHandler = dtdHandler;
    } 
    public XMLDTDHandler getDTDHandler() {
        return fDTDHandler;
    } 
    public void setDTDContentModelHandler(XMLDTDContentModelHandler dtdContentModelHandler) {
        fDTDContentModelHandler = dtdContentModelHandler;
    } 
    public XMLDTDContentModelHandler getDTDContentModelHandler() {
        return fDTDContentModelHandler;
    } 
    public void startExternalSubset(XMLResourceIdentifier identifier, 
                                    Augmentations augs) throws XNIException {
        if(fDTDGrammar != null) 
            fDTDGrammar.startExternalSubset(identifier, augs);
        if(fDTDHandler != null){
            fDTDHandler.startExternalSubset(identifier, augs);
        }
    }
    public void endExternalSubset(Augmentations augs) throws XNIException {
        if(fDTDGrammar != null) 
            fDTDGrammar.endExternalSubset(augs);
        if(fDTDHandler != null){
            fDTDHandler.endExternalSubset(augs);
        }
    }
    protected static void checkStandaloneEntityRef(String name, DTDGrammar grammar,
                    XMLEntityDecl tempEntityDecl, XMLErrorReporter errorReporter) throws XNIException {
        int entIndex = grammar.getEntityDeclIndex(name);
        if (entIndex > -1) {
            grammar.getEntityDecl(entIndex, tempEntityDecl);
            if (tempEntityDecl.inExternal) {
                errorReporter.reportError( XMLMessageFormatter.XML_DOMAIN,
                                            "MSG_REFERENCE_TO_EXTERNALLY_DECLARED_ENTITY_WHEN_STANDALONE",
                                            new Object[]{name}, XMLErrorReporter.SEVERITY_ERROR);
            }
        }
    }
    public void comment(XMLString text, Augmentations augs) throws XNIException {
        if(fDTDGrammar != null) 
            fDTDGrammar.comment(text, augs);
        if (fDTDHandler != null) {
            fDTDHandler.comment(text, augs);
        }
    } 
    public void processingInstruction(String target, XMLString data, Augmentations augs)
    throws XNIException {
        if(fDTDGrammar != null) 
            fDTDGrammar.processingInstruction(target, data, augs);
        if (fDTDHandler != null) {
            fDTDHandler.processingInstruction(target, data, augs);
        }
    } 
    public void startDTD(XMLLocator locator, Augmentations augs) throws XNIException {
        fNDataDeclNotations.clear();
        fDTDElementDecls.clear();
       if( !fGrammarBucket.getActiveGrammar().isImmutable()) {
            fDTDGrammar = fGrammarBucket.getActiveGrammar();
        }
        if(fDTDGrammar != null )
            fDTDGrammar.startDTD(locator, augs);
        if (fDTDHandler != null) {
            fDTDHandler.startDTD(locator, augs);
        }
    } 
    public void ignoredCharacters(XMLString text, Augmentations augs) throws XNIException {
        if(fDTDGrammar != null )
            fDTDGrammar.ignoredCharacters(text, augs);
        if (fDTDHandler != null) {
            fDTDHandler.ignoredCharacters(text, augs);
        }
    }
    public void textDecl(String version, String encoding, Augmentations augs) throws XNIException {
        if(fDTDGrammar != null )
            fDTDGrammar.textDecl(version, encoding, augs);
        if (fDTDHandler != null) {
            fDTDHandler.textDecl(version, encoding, augs);
        }
    }
    public void startParameterEntity(String name, 
                                     XMLResourceIdentifier identifier,
                                     String encoding,
                                     Augmentations augs) throws XNIException {
        if (fPerformValidation && fDTDGrammar != null &&
                fGrammarBucket.getStandalone()) {
            checkStandaloneEntityRef(name, fDTDGrammar, fEntityDecl, fErrorReporter);
        }
        if(fDTDGrammar != null )
            fDTDGrammar.startParameterEntity(name, identifier, encoding, augs);
        if (fDTDHandler != null) {
            fDTDHandler.startParameterEntity(name, identifier, encoding, augs);
        }
    }
    public void endParameterEntity(String name, Augmentations augs) throws XNIException {
        if(fDTDGrammar != null )
            fDTDGrammar.endParameterEntity(name, augs);
        if (fDTDHandler != null) {
            fDTDHandler.endParameterEntity(name, augs);
        }
    } 
    public void elementDecl(String name, String contentModel, Augmentations augs)
    throws XNIException {
        if (fValidation) {
            if (fDTDElementDecls.contains(name)) {
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                           "MSG_ELEMENT_ALREADY_DECLARED",
                                           new Object[]{ name},
                                           XMLErrorReporter.SEVERITY_ERROR);
            }
            else {
                fDTDElementDecls.add(name);
            }
        }
        if(fDTDGrammar != null )
            fDTDGrammar.elementDecl(name, contentModel, augs);
        if (fDTDHandler != null) {
            fDTDHandler.elementDecl(name, contentModel, augs);
        }
    } 
    public void startAttlist(String elementName, Augmentations augs) 
        throws XNIException {
        if(fDTDGrammar != null )
            fDTDGrammar.startAttlist(elementName, augs);
        if (fDTDHandler != null) {
            fDTDHandler.startAttlist(elementName, augs);
        }
    } 
    public void attributeDecl(String elementName, String attributeName, 
                              String type, String[] enumeration, 
                              String defaultType, XMLString defaultValue,
                              XMLString nonNormalizedDefaultValue, Augmentations augs) throws XNIException {
        if (type != XMLSymbols.fCDATASymbol && defaultValue != null) {
            normalizeDefaultAttrValue(defaultValue);
        }
        if (fValidation) {
                boolean	duplicateAttributeDef = false ;
                DTDGrammar grammar = (fDTDGrammar != null? fDTDGrammar:fGrammarBucket.getActiveGrammar());
                int elementIndex       = grammar.getElementDeclIndex( elementName);
                if (grammar.getAttributeDeclIndex(elementIndex, attributeName) != -1) {
                    duplicateAttributeDef = true ;
                    if(fWarnDuplicateAttdef){
                        fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                                 "MSG_DUPLICATE_ATTRIBUTE_DEFINITION",
                                                 new Object[]{ elementName, attributeName },
                                                 XMLErrorReporter.SEVERITY_WARNING );
                    }
                }
            if (type == XMLSymbols.fIDSymbol) {
                if (defaultValue != null && defaultValue.length != 0) {
                    if (defaultType == null || 
                        !(defaultType == XMLSymbols.fIMPLIEDSymbol ||
                          defaultType == XMLSymbols.fREQUIREDSymbol)) {
                        fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                                   "IDDefaultTypeInvalid",
                                                   new Object[]{ attributeName},
                                                   XMLErrorReporter.SEVERITY_ERROR);
                    }
                }
                if (!fTableOfIDAttributeNames.containsKey(elementName)) {
                    fTableOfIDAttributeNames.put(elementName, attributeName);
                }
                else {
                        if(!duplicateAttributeDef){
                                String previousIDAttributeName = (String)fTableOfIDAttributeNames.get( elementName );
                                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                               "MSG_MORE_THAN_ONE_ID_ATTRIBUTE",
                                               new Object[]{ elementName, previousIDAttributeName, attributeName},
                                               XMLErrorReporter.SEVERITY_ERROR);
                        }
                }
            }
            if (type == XMLSymbols.fNOTATIONSymbol) {
                for (int i=0; i<enumeration.length; i++) {
                    fNotationEnumVals.put(enumeration[i], attributeName);
                }
                if (fTableOfNOTATIONAttributeNames.containsKey( elementName ) == false) {
                    fTableOfNOTATIONAttributeNames.put( elementName, attributeName);
                }
                else {
                        if(!duplicateAttributeDef){
                                String previousNOTATIONAttributeName = (String) fTableOfNOTATIONAttributeNames.get( elementName );
                                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                               "MSG_MORE_THAN_ONE_NOTATION_ATTRIBUTE",
                                               new Object[]{ elementName, previousNOTATIONAttributeName, attributeName},
                                               XMLErrorReporter.SEVERITY_ERROR);
                         }
                }
            }
            if (type == XMLSymbols.fENUMERATIONSymbol || type == XMLSymbols.fNOTATIONSymbol) {
                outer: 
                    for (int i = 0; i < enumeration.length; ++i) {
                        for (int j = i + 1; j < enumeration.length; ++j) {
                            if (enumeration[i].equals(enumeration[j])) {
                                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                               type == XMLSymbols.fENUMERATIONSymbol 
                                                   ? "MSG_DISTINCT_TOKENS_IN_ENUMERATION" 
                                                   : "MSG_DISTINCT_NOTATION_IN_ENUMERATION",
                                               new Object[]{ elementName, enumeration[i], attributeName },
                                               XMLErrorReporter.SEVERITY_ERROR);
                                break outer;
                            }
                        }
                    }
            }
            boolean ok = true;
            if (defaultValue != null && 
                (defaultType == null ||
                 (defaultType != null && defaultType == XMLSymbols.fFIXEDSymbol))) {
                String value = defaultValue.toString();
                if (type == XMLSymbols.fNMTOKENSSymbol ||
                    type == XMLSymbols.fENTITIESSymbol ||
                    type == XMLSymbols.fIDREFSSymbol) {
                    StringTokenizer tokenizer = new StringTokenizer(value," ");
                    if (tokenizer.hasMoreTokens()) {
                        while (true) {
                            String nmtoken = tokenizer.nextToken();
                            if (type == XMLSymbols.fNMTOKENSSymbol) {
                                if (!isValidNmtoken(nmtoken)) {
                                    ok = false;
                                    break;
                                }
                            }
                            else if (type == XMLSymbols.fENTITIESSymbol ||
                                     type == XMLSymbols.fIDREFSSymbol) {
                                if (!isValidName(nmtoken)) {
                                    ok = false;
                                    break;
                                }
                            }
                            if (!tokenizer.hasMoreTokens()) {
                                break;
                            }
                        }
                    }
                }
                else {
                    if (type == XMLSymbols.fENTITYSymbol ||
                        type == XMLSymbols.fIDSymbol ||
                        type == XMLSymbols.fIDREFSymbol ||
                        type == XMLSymbols.fNOTATIONSymbol) {
                        if (!isValidName(value)) {
                            ok = false;
                        }
                    }
                    else if (type == XMLSymbols.fNMTOKENSymbol ||
                             type == XMLSymbols.fENUMERATIONSymbol) {
                        if (!isValidNmtoken(value)) {
                            ok = false;
                        }
                    }
                    if (type == XMLSymbols.fNOTATIONSymbol ||
                        type == XMLSymbols.fENUMERATIONSymbol) {
                        ok = false;
                        for (int i=0; i<enumeration.length; i++) {
                            if (defaultValue.equals(enumeration[i])) {
                                ok = true;
                            }
                        }
                    }
                }
                if (!ok) {
                    fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                               "MSG_ATT_DEFAULT_INVALID",
                                               new Object[]{attributeName, value},
                                               XMLErrorReporter.SEVERITY_ERROR);
                }
            }
        }
        if(fDTDGrammar != null) 
            fDTDGrammar.attributeDecl(elementName, attributeName, 
                                  type, enumeration,
                                  defaultType, defaultValue, nonNormalizedDefaultValue, augs);
        if (fDTDHandler != null) {
            fDTDHandler.attributeDecl(elementName, attributeName, 
                                      type, enumeration, 
                                      defaultType, defaultValue, nonNormalizedDefaultValue, augs);
        }
    } 
    public void endAttlist(Augmentations augs) throws XNIException {
        if(fDTDGrammar != null) 
            fDTDGrammar.endAttlist(augs);
        if (fDTDHandler != null) {
            fDTDHandler.endAttlist(augs);
        }
    } 
    public void internalEntityDecl(String name, XMLString text,
                                   XMLString nonNormalizedText,
                                   Augmentations augs) throws XNIException {
        DTDGrammar grammar = (fDTDGrammar != null? fDTDGrammar: fGrammarBucket.getActiveGrammar());
        int index = grammar.getEntityDeclIndex(name) ;
        if(index == -1){
            if(fDTDGrammar != null) 
                fDTDGrammar.internalEntityDecl(name, text, nonNormalizedText, augs);
            if (fDTDHandler != null) {
                fDTDHandler.internalEntityDecl(name, text, nonNormalizedText, augs);
            }
        }
    } 
    public void externalEntityDecl(String name, XMLResourceIdentifier identifier,
                                   Augmentations augs) throws XNIException {
        DTDGrammar grammar = (fDTDGrammar != null? fDTDGrammar:  fGrammarBucket.getActiveGrammar());
        int index = grammar.getEntityDeclIndex(name) ;
        if(index == -1){
            if(fDTDGrammar != null) 
                fDTDGrammar.externalEntityDecl(name, identifier, augs);
            if (fDTDHandler != null) {
                fDTDHandler.externalEntityDecl(name, identifier, augs);
            }
        }
    } 
    public void unparsedEntityDecl(String name, XMLResourceIdentifier identifier,
                                   String notation, 
                                   Augmentations augs) throws XNIException {
        if (fValidation) {
            fNDataDeclNotations.put(name, notation);
        }
        if(fDTDGrammar != null) 
            fDTDGrammar.unparsedEntityDecl(name, identifier, notation, augs);
        if (fDTDHandler != null) {
            fDTDHandler.unparsedEntityDecl(name, identifier, notation, augs);
        }
    } 
    public void notationDecl(String name, XMLResourceIdentifier identifier,
                             Augmentations augs) throws XNIException {
        if (fValidation) {
            DTDGrammar grammar = (fDTDGrammar != null ? fDTDGrammar : fGrammarBucket.getActiveGrammar());
            if (grammar.getNotationDeclIndex(name) != -1) {
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                           "UniqueNotationName",
                                           new Object[]{name},
                                           XMLErrorReporter.SEVERITY_ERROR);
            }
        }
        if(fDTDGrammar != null) 
            fDTDGrammar.notationDecl(name, identifier, augs);
        if (fDTDHandler != null) {
            fDTDHandler.notationDecl(name, identifier, augs);
        }
    } 
    public void startConditional(short type, Augmentations augs) throws XNIException {
        fInDTDIgnore = type == XMLDTDHandler.CONDITIONAL_IGNORE;
        if(fDTDGrammar != null) 
            fDTDGrammar.startConditional(type, augs);
        if (fDTDHandler != null) {
            fDTDHandler.startConditional(type, augs);
        }
    } 
    public void endConditional(Augmentations augs) throws XNIException {
        fInDTDIgnore = false;
        if(fDTDGrammar != null) 
            fDTDGrammar.endConditional(augs);
        if (fDTDHandler != null) {
            fDTDHandler.endConditional(augs);
        }
    } 
    public void endDTD(Augmentations augs) throws XNIException {
        if(fDTDGrammar != null) {
            fDTDGrammar.endDTD(augs);
            if(fGrammarPool != null)
                fGrammarPool.cacheGrammars(XMLGrammarDescription.XML_DTD, new Grammar[] {fDTDGrammar});
        }
        if (fValidation) {
            DTDGrammar grammar = (fDTDGrammar != null? fDTDGrammar: fGrammarBucket.getActiveGrammar());
            Iterator entities = fNDataDeclNotations.entrySet().iterator();
            while (entities.hasNext()) {
                Map.Entry entry = (Map.Entry) entities.next();
                String notation = (String) entry.getValue();
                if (grammar.getNotationDeclIndex(notation) == -1) {
                    String entity = (String) entry.getKey();
                    fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                               "MSG_NOTATION_NOT_DECLARED_FOR_UNPARSED_ENTITYDECL",
                                               new Object[]{entity, notation},
                                               XMLErrorReporter.SEVERITY_ERROR);
                }
            }
            Iterator notationVals = fNotationEnumVals.entrySet().iterator();
            while (notationVals.hasNext()) {
                Map.Entry entry = (Map.Entry) notationVals.next();
                String notation = (String) entry.getKey();
                if (grammar.getNotationDeclIndex(notation) == -1) {
                    String attributeName = (String) entry.getValue();
                    fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                               "MSG_NOTATION_NOT_DECLARED_FOR_NOTATIONTYPE_ATTRIBUTE",
                                               new Object[]{attributeName, notation},
                                               XMLErrorReporter.SEVERITY_ERROR);
                }
            }
            Iterator elementsWithNotations = fTableOfNOTATIONAttributeNames.entrySet().iterator();
            while (elementsWithNotations.hasNext()) {
                Map.Entry entry = (Map.Entry) elementsWithNotations.next();
                String elementName = (String) entry.getKey();
                int elementIndex = grammar.getElementDeclIndex(elementName);
                if (grammar.getContentSpecType(elementIndex) == XMLElementDecl.TYPE_EMPTY) {
                    String attributeName = (String) entry.getValue();
                    fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                               "NoNotationOnEmptyElement",
                                               new Object[]{elementName, attributeName},
                                               XMLErrorReporter.SEVERITY_ERROR);
                }
            }
            fTableOfIDAttributeNames = null;
            fTableOfNOTATIONAttributeNames = null;
            if (fWarnOnUndeclaredElemdef) {
                checkDeclaredElements(grammar);
            }
        }
        if (fDTDHandler != null) {
            fDTDHandler.endDTD(augs);
        }
    } 
    public void setDTDSource(XMLDTDSource source ) {
        fDTDSource = source;
    } 
    public XMLDTDSource getDTDSource() {
        return fDTDSource;
    } 
    public void setDTDContentModelSource(XMLDTDContentModelSource source ) {
        fDTDContentModelSource = source;
    } 
    public XMLDTDContentModelSource getDTDContentModelSource() {
        return fDTDContentModelSource;
    } 
    public void startContentModel(String elementName, Augmentations augs) 
        throws XNIException {
        if (fValidation) {
            fDTDElementDeclName = elementName;
            fMixedElementTypes.clear();
        }
        if(fDTDGrammar != null) 
            fDTDGrammar.startContentModel(elementName, augs);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.startContentModel(elementName, augs);
        }
    } 
    public void any(Augmentations augs) throws XNIException {
        if(fDTDGrammar != null) 
            fDTDGrammar.any(augs);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.any(augs);
        }
    } 
    public void empty(Augmentations augs) throws XNIException {
        if(fDTDGrammar != null) 
            fDTDGrammar.empty(augs);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.empty(augs);
        }
    } 
    public void startGroup(Augmentations augs) throws XNIException {
        fMixed = false;
        if(fDTDGrammar != null) 
            fDTDGrammar.startGroup(augs);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.startGroup(augs);
        }
    } 
    public void pcdata(Augmentations augs) {
        fMixed = true;
        if(fDTDGrammar != null) 
            fDTDGrammar.pcdata(augs);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.pcdata(augs);
        }
    } 
    public void element(String elementName, Augmentations augs) throws XNIException {
        if (fMixed && fValidation) {
            if (fMixedElementTypes.contains(elementName)) {
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                           "DuplicateTypeInMixedContent",
                                           new Object[]{fDTDElementDeclName, elementName},
                                           XMLErrorReporter.SEVERITY_ERROR);
            }
            else {
                fMixedElementTypes.add(elementName);
            }
        }
        if(fDTDGrammar != null) 
            fDTDGrammar.element(elementName, augs);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.element(elementName, augs);
        }
    } 
    public void separator(short separator, Augmentations augs) 
        throws XNIException {
        if(fDTDGrammar != null) 
            fDTDGrammar.separator(separator, augs);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.separator(separator, augs);
        }
    } 
    public void occurrence(short occurrence, Augmentations augs) 
        throws XNIException {
        if(fDTDGrammar != null) 
            fDTDGrammar.occurrence(occurrence, augs);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.occurrence(occurrence, augs);
        }
    } 
    public void endGroup(Augmentations augs) throws XNIException {
        if(fDTDGrammar != null) 
            fDTDGrammar.endGroup(augs);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.endGroup(augs);
        }
    } 
    public void endContentModel(Augmentations augs) throws XNIException {
        if(fDTDGrammar != null) 
            fDTDGrammar.endContentModel(augs);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.endContentModel(augs);
        }
    } 
    private boolean normalizeDefaultAttrValue(XMLString value) {
        boolean skipSpace = true; 
        int current = value.offset;
        int end = value.offset + value.length;
        for (int i = value.offset; i < end; i++) {
            if (value.ch[i] == ' ') {
                if (!skipSpace) {
                    value.ch[current++] = ' ';
                    skipSpace = true;
                }
                else {
                }
            }
            else {
                if (current != i) {
                    value.ch[current] = value.ch[i];
                }
                current++;
                skipSpace = false;
            }
        }
        if (current != end) {
            if (skipSpace) {
                current--;
            }
            value.length = current - value.offset;
            return true;
        }
        return false;
    }
    protected boolean isValidNmtoken(String nmtoken) {
        return XMLChar.isValidNmtoken(nmtoken);
    } 
    protected boolean isValidName(String name) {
        return XMLChar.isValidName(name);
    } 
    private void checkDeclaredElements(DTDGrammar grammar) {
        int elementIndex = grammar.getFirstElementDeclIndex();
        XMLContentSpec contentSpec = new XMLContentSpec();
        while (elementIndex >= 0) {
            int type = grammar.getContentSpecType(elementIndex);
            if (type == XMLElementDecl.TYPE_CHILDREN || type == XMLElementDecl.TYPE_MIXED) {
                checkDeclaredElements(grammar, 
                        elementIndex, 
                        grammar.getContentSpecIndex(elementIndex),
                        contentSpec);
            }
            elementIndex = grammar.getNextElementDeclIndex(elementIndex);
        }
    }
    private void checkDeclaredElements(DTDGrammar grammar, int elementIndex, 
            int contentSpecIndex, XMLContentSpec contentSpec) {
        grammar.getContentSpec(contentSpecIndex, contentSpec);
        if (contentSpec.type == XMLContentSpec.CONTENTSPECNODE_LEAF) {
            String value = (String) contentSpec.value;
            if (value != null && grammar.getElementDeclIndex(value) == -1) {
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                        "UndeclaredElementInContentSpec",
                        new Object[]{grammar.getElementDeclName(elementIndex).rawname, value},
                        XMLErrorReporter.SEVERITY_WARNING);
            }
        }
        else if ((contentSpec.type == XMLContentSpec.CONTENTSPECNODE_CHOICE)
                || (contentSpec.type == XMLContentSpec.CONTENTSPECNODE_SEQ)) {
            final int leftNode = ((int[])contentSpec.value)[0];
            final int rightNode = ((int[])contentSpec.otherValue)[0];
            checkDeclaredElements(grammar, elementIndex, leftNode, contentSpec);
            checkDeclaredElements(grammar, elementIndex, rightNode, contentSpec);
        }
        else if (contentSpec.type == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE
                || contentSpec.type == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE
                || contentSpec.type == XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE) {
            final int leftNode = ((int[])contentSpec.value)[0];
            checkDeclaredElements(grammar, elementIndex, leftNode, contentSpec);
        }
    }
} 
