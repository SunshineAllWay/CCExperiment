package org.apache.xerces.jaxp.validation;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.grammars.XMLSchemaDescription;
final class SoftReferenceGrammarPool implements XMLGrammarPool {
    protected static final int TABLE_SIZE = 11;
    protected static final Grammar [] ZERO_LENGTH_GRAMMAR_ARRAY = new Grammar [0];
    protected Entry [] fGrammars = null;
    protected boolean fPoolIsLocked;
    protected int fGrammarCount = 0;
    protected final ReferenceQueue fReferenceQueue = new ReferenceQueue();
    public SoftReferenceGrammarPool() {
        fGrammars = new Entry[TABLE_SIZE];
        fPoolIsLocked = false;
    } 
    public SoftReferenceGrammarPool(int initialCapacity) {
        fGrammars = new Entry[initialCapacity];
        fPoolIsLocked = false;
    }
    public Grammar [] retrieveInitialGrammarSet (String grammarType) {
        synchronized (fGrammars) {
            clean();
            return ZERO_LENGTH_GRAMMAR_ARRAY;
        }
    } 
    public void cacheGrammars(String grammarType, Grammar[] grammars) {
        if (!fPoolIsLocked) {
            for (int i = 0; i < grammars.length; ++i) {
                putGrammar(grammars[i]);
            }
        }
    } 
    public Grammar retrieveGrammar(XMLGrammarDescription desc) {
        return getGrammar(desc);
    } 
    public void putGrammar(Grammar grammar) {
        if (!fPoolIsLocked) {
            synchronized (fGrammars) {
                clean();
                XMLGrammarDescription desc = grammar.getGrammarDescription();
                int hash = hashCode(desc);
                int index = (hash & 0x7FFFFFFF) % fGrammars.length;
                for (Entry entry = fGrammars[index]; entry != null; entry = entry.next) {
                    if (entry.hash == hash && equals(entry.desc, desc)) {
                        if (entry.grammar.get() != grammar) {
                            entry.grammar = new SoftGrammarReference(entry, grammar, fReferenceQueue);
                        }
                        return;
                    }
                }
                Entry entry = new Entry(hash, index, desc, grammar, fGrammars[index], fReferenceQueue);
                fGrammars[index] = entry;
                fGrammarCount++;
            }
        }
    } 
    public Grammar getGrammar(XMLGrammarDescription desc) {
        synchronized (fGrammars) {
            clean();
            int hash = hashCode(desc);
            int index = (hash & 0x7FFFFFFF) % fGrammars.length;
            for (Entry entry = fGrammars[index]; entry != null; entry = entry.next) {
                Grammar tempGrammar = (Grammar) entry.grammar.get();
                if (tempGrammar == null) {
                    removeEntry(entry);
                }
                else if ((entry.hash == hash) && equals(entry.desc, desc)) {
                    return tempGrammar;
                }
            }
            return null;
        }
    } 
    public Grammar removeGrammar(XMLGrammarDescription desc) {
        synchronized (fGrammars) {
            clean();
            int hash = hashCode(desc);
            int index = (hash & 0x7FFFFFFF) % fGrammars.length;
            for (Entry entry = fGrammars[index]; entry != null; entry = entry.next) {
                if ((entry.hash == hash) && equals(entry.desc, desc)) {
                    return removeEntry(entry);
                }
            }
            return null;
        }
    } 
    public boolean containsGrammar(XMLGrammarDescription desc) {
        synchronized (fGrammars) {
            clean();
            int hash = hashCode(desc);
            int index = (hash & 0x7FFFFFFF) % fGrammars.length;
            for (Entry entry = fGrammars[index]; entry != null ; entry = entry.next) {
                Grammar tempGrammar = (Grammar) entry.grammar.get();
                if (tempGrammar == null) {
                    removeEntry(entry);
                }
                else if ((entry.hash == hash) && equals(entry.desc, desc)) {
                    return true;
                }
            }
            return false;
        }
    } 
    public void lockPool() {
        fPoolIsLocked = true;
    } 
    public void unlockPool() {
        fPoolIsLocked = false;
    } 
    public void clear() {
        for (int i=0; i<fGrammars.length; i++) {
            if(fGrammars[i] != null) {
                fGrammars[i].clear();
                fGrammars[i] = null;
            }
        }
        fGrammarCount = 0;
    } 
    public boolean equals(XMLGrammarDescription desc1, XMLGrammarDescription desc2) {
        if (desc1 instanceof XMLSchemaDescription) {
            if (!(desc2 instanceof XMLSchemaDescription)) {
                return false;
            }
            final XMLSchemaDescription sd1 = (XMLSchemaDescription) desc1;
            final XMLSchemaDescription sd2 = (XMLSchemaDescription) desc2;
            final String targetNamespace = sd1.getTargetNamespace();
            if (targetNamespace != null) {
                if (!targetNamespace.equals(sd2.getTargetNamespace())) {
                    return false;
                }
            }
            else if (sd2.getTargetNamespace() != null) {
                return false;
            }
            final String expandedSystemId = sd1.getExpandedSystemId();
            if (expandedSystemId != null) {
                if (!expandedSystemId.equals(sd2.getExpandedSystemId())) {
                    return false;
                }
            }
            else if (sd2.getExpandedSystemId() != null) {
                return false;
            }
            return true;
        }
        return desc1.equals(desc2);
    }
    public int hashCode(XMLGrammarDescription desc) {
        if (desc instanceof XMLSchemaDescription) {
            final XMLSchemaDescription sd = (XMLSchemaDescription) desc;
            final String targetNamespace = sd.getTargetNamespace();
            final String expandedSystemId = sd.getExpandedSystemId();
            int hash = (targetNamespace != null) ? targetNamespace.hashCode() : 0;
            hash ^= (expandedSystemId != null) ? expandedSystemId.hashCode() : 0;
            return hash;     
        }
        return desc.hashCode();
    }
    private Grammar removeEntry(Entry entry) {
        if (entry.prev != null) {
            entry.prev.next = entry.next;
        }
        else {
            fGrammars[entry.bucket] = entry.next;
        }
        if (entry.next != null) {
            entry.next.prev = entry.prev;
        }
        --fGrammarCount;
        entry.grammar.entry = null;
        return (Grammar) entry.grammar.get();
    }
    private void clean() {
        Reference ref = fReferenceQueue.poll();
        while (ref != null) {
            Entry entry = ((SoftGrammarReference) ref).entry;
            if (entry != null) {
                removeEntry(entry);
            }
            ref = fReferenceQueue.poll();
        }
    }
    static final class Entry {
        public int hash;
        public int bucket;
        public Entry prev;
        public Entry next;
        public XMLGrammarDescription desc;
        public SoftGrammarReference grammar;
        protected Entry(int hash, int bucket, XMLGrammarDescription desc, Grammar grammar, Entry next, ReferenceQueue queue) {
            this.hash = hash;
            this.bucket = bucket;
            this.prev = null;
            this.next = next;
            if (next != null) {
                next.prev = this;
            }
            this.desc = desc;
            this.grammar = new SoftGrammarReference(this, grammar, queue);
        }
        protected void clear () {
            desc = null;
            grammar = null;
            if(next != null) {
                next.clear();
                next = null;
            }
        } 
    } 
    static final class SoftGrammarReference extends SoftReference {
        public Entry entry;
        protected SoftGrammarReference(Entry entry, Grammar grammar, ReferenceQueue queue) {
            super(grammar, queue);
            this.entry = entry;
        }
    } 
} 
