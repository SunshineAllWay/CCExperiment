package org.apache.xerces.impl.dtd.models;
import org.apache.xerces.xni.QName;
public interface ContentModelValidator {
    public int validate(QName[] children, int offset, int length);
} 
