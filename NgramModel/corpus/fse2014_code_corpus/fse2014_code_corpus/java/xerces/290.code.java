package org.apache.xerces.impl;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
public interface XMLEntityHandler {
    public void startEntity(String name, 
                            XMLResourceIdentifier identifier,
                            String encoding, Augmentations augs) throws XNIException;
    public void endEntity(String name, Augmentations augs) throws XNIException;
} 
