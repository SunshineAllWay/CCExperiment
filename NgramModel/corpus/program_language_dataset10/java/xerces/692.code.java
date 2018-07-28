package org.apache.xerces.xpointer;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XNIException;
public interface XPointerPart {
    public static final int EVENT_ELEMENT_START = 0;
    public static final int EVENT_ELEMENT_END = 1;
    public static final int EVENT_ELEMENT_EMPTY = 2;
    public void parseXPointer(String part) throws XNIException;
    public boolean resolveXPointer(QName element, XMLAttributes attributes,
            Augmentations augs, int event) throws XNIException;
    public boolean isFragmentResolved() throws XNIException;
    public boolean isChildFragmentResolved() throws XNIException;
    public String getSchemeName();
    public String getSchemeData();
    public void setSchemeName(String schemeName);
    public void setSchemeData(String schemeData);
}