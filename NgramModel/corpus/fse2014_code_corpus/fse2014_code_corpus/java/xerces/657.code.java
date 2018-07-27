package org.apache.xerces.xni;
import org.apache.xerces.xni.parser.XMLDTDContentModelSource;
public interface XMLDTDContentModelHandler {
    public static final short SEPARATOR_CHOICE = 0;
    public static final short SEPARATOR_SEQUENCE = 1;
    public static final short OCCURS_ZERO_OR_ONE = 2;
    public static final short OCCURS_ZERO_OR_MORE = 3;
    public static final short OCCURS_ONE_OR_MORE = 4;
    public void startContentModel(String elementName, Augmentations augmentations)
        throws XNIException;
    public void any(Augmentations augmentations) throws XNIException;
    public void empty(Augmentations augmentations) throws XNIException;
    public void startGroup(Augmentations augmentations) throws XNIException;
    public void pcdata(Augmentations augmentations) throws XNIException;
    public void element(String elementName, Augmentations augmentations) 
        throws XNIException;
    public void separator(short separator, Augmentations augmentations) 
        throws XNIException;
    public void occurrence(short occurrence, Augmentations augmentations) 
        throws XNIException;
    public void endGroup(Augmentations augmentations) throws XNIException;
    public void endContentModel(Augmentations augmentations) throws XNIException;
    public void setDTDContentModelSource(XMLDTDContentModelSource source);
    public XMLDTDContentModelSource getDTDContentModelSource();
} 
