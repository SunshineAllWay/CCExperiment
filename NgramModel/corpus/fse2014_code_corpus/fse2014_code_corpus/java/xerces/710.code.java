package org.apache.xerces.xs;
public interface XSIDCDefinition extends XSObject {
    public static final short IC_KEY                    = 1;
    public static final short IC_KEYREF                 = 2;
    public static final short IC_UNIQUE                 = 3;
    public short getCategory();
    public String getSelectorStr();
    public StringList getFieldStrs();
    public XSIDCDefinition getRefKey();
    public XSObjectList getAnnotations();
}
