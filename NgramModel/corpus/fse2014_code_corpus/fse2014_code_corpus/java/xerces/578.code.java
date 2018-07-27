package org.apache.xerces.parsers;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XMLGrammarLoader;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
public class XMLGrammarPreparser {
    private final static String CONTINUE_AFTER_FATAL_ERROR =
        Constants.XERCES_FEATURE_PREFIX + Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE;
    protected static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
    protected static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    protected static final String ERROR_HANDLER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY;
    protected static final String ENTITY_RESOLVER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;
    protected static final String GRAMMAR_POOL =
        Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;
    private static final Hashtable KNOWN_LOADERS = new Hashtable();
    static {
        KNOWN_LOADERS.put(XMLGrammarDescription.XML_SCHEMA,
            "org.apache.xerces.impl.xs.XMLSchemaLoader");
        KNOWN_LOADERS.put(XMLGrammarDescription.XML_DTD,
            "org.apache.xerces.impl.dtd.XMLDTDLoader");
    }
    private static final String[] RECOGNIZED_PROPERTIES = {
        SYMBOL_TABLE,
        ERROR_REPORTER,
        ERROR_HANDLER,
        ENTITY_RESOLVER,
        GRAMMAR_POOL,
    };
    protected final SymbolTable fSymbolTable;
    protected final XMLErrorReporter fErrorReporter;
    protected XMLEntityResolver fEntityResolver;
    protected XMLGrammarPool fGrammarPool;
    protected Locale fLocale;
    private final Hashtable fLoaders;
    private int fModCount = 1;
    public XMLGrammarPreparser() {
        this(new SymbolTable());
    } 
    public XMLGrammarPreparser (SymbolTable symbolTable) {
        fSymbolTable = symbolTable;
        fLoaders = new Hashtable();
        fErrorReporter = new XMLErrorReporter();
        setLocale(Locale.getDefault());
        fEntityResolver = new XMLEntityManager();
    } 
    public boolean registerPreparser(String grammarType, XMLGrammarLoader loader) {
        if(loader == null) { 
            if(KNOWN_LOADERS.containsKey(grammarType)) {
                String loaderName = (String)KNOWN_LOADERS.get(grammarType);
                try {
                    ClassLoader cl = ObjectFactory.findClassLoader();
                    XMLGrammarLoader gl = (XMLGrammarLoader)(ObjectFactory.newInstance(loaderName, cl, true));
                    fLoaders.put(grammarType, new XMLGrammarLoaderContainer(gl));
                } catch (Exception e) {
                    return false;
                }
                return true;
            }
            return false;
        }
        fLoaders.put(grammarType, new XMLGrammarLoaderContainer(loader));
        return true;
    } 
    public Grammar preparseGrammar(String type, XMLInputSource
                is) throws XNIException, IOException {
        if (fLoaders.containsKey(type)) {
            XMLGrammarLoaderContainer xglc = (XMLGrammarLoaderContainer) fLoaders.get(type);
            XMLGrammarLoader gl = xglc.loader;
            if (xglc.modCount != fModCount) {
                gl.setProperty(SYMBOL_TABLE, fSymbolTable);
                gl.setProperty(ENTITY_RESOLVER, fEntityResolver);
                gl.setProperty(ERROR_REPORTER, fErrorReporter);
                if (fGrammarPool != null) {
                    try {
                        gl.setProperty(GRAMMAR_POOL, fGrammarPool);
                    } catch(Exception e) {
                    }
                }
                xglc.modCount = fModCount;
            }
            return gl.loadGrammar(is);
        }
        return null;
    } 
    public void setLocale(Locale locale) {
        fLocale = locale;
        fErrorReporter.setLocale(locale);
    } 
    public Locale getLocale() {
        return fLocale;
    } 
    public void setErrorHandler(XMLErrorHandler errorHandler) {
        fErrorReporter.setProperty(ERROR_HANDLER, errorHandler);
    } 
    public XMLErrorHandler getErrorHandler() {
        return fErrorReporter.getErrorHandler();
    } 
    public void setEntityResolver(XMLEntityResolver entityResolver) {
        if (fEntityResolver != entityResolver) {
            if (++fModCount < 0) {
                clearModCounts();
            }
            fEntityResolver = entityResolver;
        }
    } 
    public XMLEntityResolver getEntityResolver() {
        return fEntityResolver;
    } 
    public void setGrammarPool(XMLGrammarPool grammarPool) {
        if (fGrammarPool != grammarPool) {
            if (++fModCount < 0) {
                clearModCounts();
            }
            fGrammarPool = grammarPool;
        }
    } 
    public XMLGrammarPool getGrammarPool() {
        return fGrammarPool;
    } 
    public XMLGrammarLoader getLoader(String type) {
        XMLGrammarLoaderContainer xglc = (XMLGrammarLoaderContainer) fLoaders.get(type);
        return (xglc != null) ? xglc.loader : null;
    } 
    public void setFeature(String featureId, boolean value) {
        Enumeration loaders = fLoaders.elements();
        while (loaders.hasMoreElements()) {
            XMLGrammarLoader gl = ((XMLGrammarLoaderContainer)loaders.nextElement()).loader;
            try {
                gl.setFeature(featureId, value);
            } catch(Exception e) {
            }
        }
        if(featureId.equals(CONTINUE_AFTER_FATAL_ERROR)) {
            fErrorReporter.setFeature(CONTINUE_AFTER_FATAL_ERROR, value);
        }
    } 
    public void setProperty(String propId, Object value) {
        Enumeration loaders = fLoaders.elements();
        while (loaders.hasMoreElements()) {
            XMLGrammarLoader gl = ((XMLGrammarLoaderContainer)loaders.nextElement()).loader;
            try {
                gl.setProperty(propId, value);
            } catch(Exception e) {
            }
        }
    } 
    public boolean getFeature(String type, String featureId) {
        XMLGrammarLoader gl = ((XMLGrammarLoaderContainer)fLoaders.get(type)).loader;
        return gl.getFeature(featureId);
    } 
    public Object getProperty(String type, String propertyId) {
        XMLGrammarLoader gl = ((XMLGrammarLoaderContainer)fLoaders.get(type)).loader;
        return gl.getProperty(propertyId);
    } 
    static class XMLGrammarLoaderContainer {
        public final XMLGrammarLoader loader;
        public int modCount = 0;
        public XMLGrammarLoaderContainer(XMLGrammarLoader loader) {
            this.loader = loader;
        }
    }
    private void clearModCounts() {
        Enumeration loaders = fLoaders.elements();
        while (loaders.hasMoreElements()) {
            XMLGrammarLoaderContainer xglc = (XMLGrammarLoaderContainer) loaders.nextElement();
            xglc.modCount = 0;
        }
        fModCount = 1;
    }
} 
