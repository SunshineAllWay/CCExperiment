package org.apache.xerces.impl.dtd;
import java.util.Hashtable;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
public class DTDGrammarBucket {
    protected final Hashtable fGrammars;
    protected DTDGrammar fActiveGrammar;
    protected boolean fIsStandalone;
    public DTDGrammarBucket() {
        fGrammars = new Hashtable();
    } 
    public void putGrammar(DTDGrammar grammar) {
        XMLDTDDescription desc = (XMLDTDDescription)grammar.getGrammarDescription();
        fGrammars.put(desc, grammar);
    } 
    public DTDGrammar getGrammar(XMLGrammarDescription desc) {
        return (DTDGrammar)(fGrammars.get((XMLDTDDescription)desc));
    } 
    public void clear() {
        fGrammars.clear();
        fActiveGrammar = null;
        fIsStandalone = false;
    } 
    void setStandalone(boolean standalone) {
        fIsStandalone = standalone;
    }
    boolean getStandalone() {
        return fIsStandalone;
    }
    void setActiveGrammar (DTDGrammar grammar) {
        fActiveGrammar = grammar;
    }
    DTDGrammar getActiveGrammar () {
        return fActiveGrammar;
    }
} 
