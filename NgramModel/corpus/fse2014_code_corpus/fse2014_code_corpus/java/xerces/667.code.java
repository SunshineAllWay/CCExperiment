package org.apache.xerces.xni.grammars;
public interface XMLGrammarPool {
    public Grammar[] retrieveInitialGrammarSet(String grammarType);
    public void cacheGrammars(String grammarType, Grammar[] grammars);
    public Grammar retrieveGrammar(XMLGrammarDescription desc);
    public void lockPool();
    public void unlockPool();
    public void clear();
} 
