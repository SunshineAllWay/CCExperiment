package org.apache.xerces.dom3.as;
public interface ASContentModel extends ASObject {
    public static final int AS_UNBOUNDED              = Integer.MAX_VALUE;
    public static final short AS_SEQUENCE               = 0;
    public static final short AS_CHOICE                 = 1;
    public static final short AS_ALL                    = 2;
    public static final short AS_NONE                   = 3;
    public short getListOperator();
    public void setListOperator(short listOperator);
    public int getMinOccurs();
    public void setMinOccurs(int minOccurs);
    public int getMaxOccurs();
    public void setMaxOccurs(int maxOccurs);
    public ASObjectList getSubModels();
    public void setSubModels(ASObjectList subModels);
    public void removesubModel(ASObject oldNode);
    public void insertsubModel(ASObject newNode)
                               throws DOMASException;
    public int appendsubModel(ASObject newNode)
                              throws DOMASException;
}
