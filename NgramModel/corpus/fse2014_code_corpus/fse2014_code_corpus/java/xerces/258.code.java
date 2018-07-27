package org.apache.xerces.dom3.as;
public interface ASAttributeDeclaration extends ASObject {
    public static final short VALUE_NONE                = 0;
    public static final short VALUE_DEFAULT             = 1;
    public static final short VALUE_FIXED               = 2;
    public ASDataType getDataType();
    public void setDataType(ASDataType dataType);
    public String getDataValue();
    public void setDataValue(String dataValue);
    public String getEnumAttr();
    public void setEnumAttr(String enumAttr);
    public ASObjectList getOwnerElements();
    public void setOwnerElements(ASObjectList ownerElements);
    public short getDefaultType();
    public void setDefaultType(short defaultType);
}
