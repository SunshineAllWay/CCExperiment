package org.apache.xerces.jaxp.validation;
import java.lang.ref.WeakReference;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
final class WeakReferenceXMLSchema extends AbstractXMLSchema {
    private WeakReference fGrammarPool = new WeakReference(null);
    public WeakReferenceXMLSchema() {}
    public synchronized XMLGrammarPool getGrammarPool() {
        XMLGrammarPool grammarPool = (XMLGrammarPool) fGrammarPool.get();
        if (grammarPool == null) {
            grammarPool = new SoftReferenceGrammarPool();
            fGrammarPool = new WeakReference(grammarPool);
        }
        return grammarPool;
    }
    public boolean isFullyComposed() {
        return false;
    }
} 
