package org.apache.xerces.xni.grammars;
import org.apache.xerces.xs.XSModel;
public interface XSGrammar extends Grammar {
    public XSModel toXSModel();
    public XSModel toXSModel(XSGrammar[] grammars);
} 
