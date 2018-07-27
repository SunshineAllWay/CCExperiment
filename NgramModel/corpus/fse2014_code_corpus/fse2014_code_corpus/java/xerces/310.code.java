package org.apache.xerces.impl.dtd;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
public interface XMLDTDValidatorFilter 
    extends XMLDocumentFilter {
    public boolean hasGrammar();
    public boolean validate();
} 
