package org.apache.xerces.parsers;
import org.apache.xerces.util.ShadowedSymbolTable;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.SynchronizedSymbolTable;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
public class CachingParserPool {
    public static final boolean DEFAULT_SHADOW_SYMBOL_TABLE = false;
    public static final boolean DEFAULT_SHADOW_GRAMMAR_POOL = false;
    protected SymbolTable fSynchronizedSymbolTable;
    protected XMLGrammarPool fSynchronizedGrammarPool;
    protected boolean fShadowSymbolTable = DEFAULT_SHADOW_SYMBOL_TABLE;
    protected boolean fShadowGrammarPool = DEFAULT_SHADOW_GRAMMAR_POOL;
    public CachingParserPool() {
        this(new SymbolTable(), new XMLGrammarPoolImpl());
    } 
    public CachingParserPool(SymbolTable symbolTable, XMLGrammarPool grammarPool) {
        fSynchronizedSymbolTable = new SynchronizedSymbolTable(symbolTable);
        fSynchronizedGrammarPool = new SynchronizedGrammarPool(grammarPool);
    } 
    public SymbolTable getSymbolTable() {
        return fSynchronizedSymbolTable;
    } 
    public XMLGrammarPool getXMLGrammarPool() {
        return fSynchronizedGrammarPool;
    } 
    public void setShadowSymbolTable(boolean shadow) {
        fShadowSymbolTable = shadow;
    } 
    public DOMParser createDOMParser() {
        SymbolTable symbolTable = fShadowSymbolTable
                                ? new ShadowedSymbolTable(fSynchronizedSymbolTable)
                                : fSynchronizedSymbolTable;
        XMLGrammarPool grammarPool = fShadowGrammarPool
                                ? new ShadowedGrammarPool(fSynchronizedGrammarPool)
                                : fSynchronizedGrammarPool;
        return new DOMParser(symbolTable, grammarPool);
    } 
    public SAXParser createSAXParser() {
        SymbolTable symbolTable = fShadowSymbolTable
                                ? new ShadowedSymbolTable(fSynchronizedSymbolTable)
                                : fSynchronizedSymbolTable;
        XMLGrammarPool grammarPool = fShadowGrammarPool
                                ? new ShadowedGrammarPool(fSynchronizedGrammarPool)
                                : fSynchronizedGrammarPool;
        return new SAXParser(symbolTable, grammarPool);
    } 
    public static final class SynchronizedGrammarPool
        implements XMLGrammarPool {
        private XMLGrammarPool fGrammarPool;
        public SynchronizedGrammarPool(XMLGrammarPool grammarPool) {
            fGrammarPool = grammarPool;
        } 
        public Grammar [] retrieveInitialGrammarSet(String grammarType ) {
            synchronized (fGrammarPool) {
                return fGrammarPool.retrieveInitialGrammarSet(grammarType);
            }
        } 
        public Grammar retrieveGrammar(XMLGrammarDescription gDesc) {
            synchronized (fGrammarPool) {
                return fGrammarPool.retrieveGrammar(gDesc);
            }
        } 
        public void cacheGrammars(String grammarType, Grammar[] grammars) {
            synchronized (fGrammarPool) {
                fGrammarPool.cacheGrammars(grammarType, grammars);
            }
        } 
        public void lockPool() {
            synchronized (fGrammarPool) {
                fGrammarPool.lockPool();
            }
        } 
        public void clear() {
            synchronized (fGrammarPool) {
                fGrammarPool.clear();
            }
        } 
        public void unlockPool() {
            synchronized (fGrammarPool) {
                fGrammarPool.unlockPool();
            }
        } 
    } 
    public static final class ShadowedGrammarPool
        extends XMLGrammarPoolImpl {
        private XMLGrammarPool fGrammarPool;
        public ShadowedGrammarPool(XMLGrammarPool grammarPool) {
            fGrammarPool = grammarPool;
        } 
        public Grammar [] retrieveInitialGrammarSet(String grammarType ) {
            Grammar [] grammars = super.retrieveInitialGrammarSet(grammarType);
            if (grammars != null) return grammars;
            return fGrammarPool.retrieveInitialGrammarSet(grammarType);
        } 
        public Grammar retrieveGrammar(XMLGrammarDescription gDesc) {
            Grammar g = super.retrieveGrammar(gDesc);
            if(g != null) return g;
            return fGrammarPool.retrieveGrammar(gDesc);
        } 
        public void cacheGrammars(String grammarType, Grammar[] grammars) { 
           super.cacheGrammars(grammarType, grammars);
           fGrammarPool.cacheGrammars(grammarType, grammars);
        } 
        public Grammar getGrammar(XMLGrammarDescription desc) {
            if (super.containsGrammar(desc)) {
                return super.getGrammar(desc);
            }
            return null;
        } 
        public boolean containsGrammar(XMLGrammarDescription desc) {
            return super.containsGrammar(desc);
        } 
    } 
} 
