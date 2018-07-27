package org.apache.xerces.impl;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
public interface RevalidationHandler extends XMLDocumentFilter {
    public boolean characterData(String data, Augmentations augs);
} 
