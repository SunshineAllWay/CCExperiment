package org.apache.xerces.util;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
public class XMLGrammarPoolImpl implements XMLGrammarPool {
    protected static final int TABLE_SIZE = 11;
    protected Entry[] fGrammars = null;
    protected boolean fPoolIsLocked;
    protected int fGrammarCount = 0;
    private static final boolean DEBUG = false ;
    public XMLGrammarPoolImpl() {
        fGrammars = new Entry[TABLE_SIZE];
        fPoolIsLocked = false;
    } 
    public XMLGrammarPoolImpl(int initialCapacity) {
        fGrammars = new Entry[initialCapacity];
        fPoolIsLocked = false;
    }
    public Grammar [] retrieveInitialGrammarSet (String grammarType) {
        synchronized (fGrammars) {
            int grammarSize = fGrammars.length ;
            Grammar [] tempGrammars = new Grammar[fGrammarCount];
            int pos = 0;
            for (int i = 0; i < grammarSize; i++) {
                for (Entry e = fGrammars[i]; e != null; e = e.next) {
                    if (e.desc.getGrammarType().equals(grammarType)) {
                        tempGrammars[pos++] = e.grammar;
                    }
                }
            }
            Grammar[] toReturn = new Grammar[pos];
            System.arraycopy(tempGrammars, 0, toReturn, 0, pos);
            return toReturn;
        }
    } 
    public void cacheGrammars(String grammarType, Grammar[] grammars) {
        if(!fPoolIsLocked) {
            for (int i = 0; i < grammars.length; i++) {
                if(DEBUG) {
                    System.out.println("CACHED GRAMMAR " + (i+1) ) ;
                    Grammar temp = grammars[i] ;
                }
                putGrammar(grammars[i]);
            }
        }
    } 
    public Grammar retrieveGrammar(XMLGrammarDescription desc) {
        if(DEBUG){
            System.out.println("RETRIEVING GRAMMAR FROM THE APPLICATION WITH FOLLOWING DESCRIPTION :");
        }
        return getGrammar(desc);
    } 
    public void putGrammar(Grammar grammar) {
        if(!fPoolIsLocked) {
            synchronized (fGrammars) {
                XMLGrammarDescription desc = grammar.getGrammarDescription();
                int hash = hashCode(desc);
                int index = (hash & 0x7FFFFFFF) % fGrammars.length;
                for (Entry entry = fGrammars[index]; entry != null; entry = entry.next) {
                    if (entry.hash == hash && equals(entry.desc, desc)) {
                        entry.grammar = grammar;
                        return;
                    }
                }
                Entry entry = new Entry(hash, desc, grammar, fGrammars[index]);
                fGrammars[index] = entry;
                fGrammarCount++;
            }
        }
    } 
    public Grammar getGrammar(XMLGrammarDescription desc) {
        synchronized (fGrammars) {
            int hash = hashCode(desc);
        int index = (hash & 0x7FFFFFFF) % fGrammars.length;
        for (Entry entry = fGrammars[index] ; entry != null ; entry = entry.next) {
            if ((entry.hash == hash) && equals(entry.desc, desc)) {
                return entry.grammar;
            }
        }
        return null;
    }
    } 
    public Grammar removeGrammar(XMLGrammarDescription desc) {
        synchronized (fGrammars) {
            int hash = hashCode(desc);
        int index = (hash & 0x7FFFFFFF) % fGrammars.length;
        for (Entry entry = fGrammars[index], prev = null ; entry != null ; prev = entry, entry = entry.next) {
            if ((entry.hash == hash) && equals(entry.desc, desc)) {
                if (prev != null) {
                        prev.next = entry.next;
            }
            else {
                fGrammars[index] = entry.next;
            }
                Grammar tempGrammar = entry.grammar;
                entry.grammar = null;
                fGrammarCount--;
                return tempGrammar;
            }
        }
        return null;
        }
    } 
    public boolean containsGrammar(XMLGrammarDescription desc) {
        synchronized (fGrammars) {
            int hash = hashCode(desc);
        int index = (hash & 0x7FFFFFFF) % fGrammars.length;
        for (Entry entry = fGrammars[index] ; entry != null ; entry = entry.next) {
            if ((entry.hash == hash) && equals(entry.desc, desc)) {
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
        return desc1.equals(desc2);
    }
    public int hashCode(XMLGrammarDescription desc) {
        return desc.hashCode();
    }
    protected static final class Entry {
        public int hash;
        public XMLGrammarDescription desc;
        public Grammar grammar;
        public Entry next;
        protected Entry(int hash, XMLGrammarDescription desc, Grammar grammar, Entry next) {
            this.hash = hash;
            this.desc = desc;
            this.grammar = grammar;
            this.next = next;
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
} 
