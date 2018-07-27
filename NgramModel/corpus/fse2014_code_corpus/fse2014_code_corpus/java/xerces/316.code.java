package org.apache.xerces.impl.dtd.models;
public class CMAny
    extends CMNode {
    private final int fType;
    private final String fURI;
    private int fPosition = -1;
    public CMAny(int type, String uri, int position)  {
        super(type);
        fType = type;
        fURI = uri;
        fPosition = position;
    }
    final int getType() {
        return fType;
    }
    final String getURI() {
        return fURI;
    }
    final int getPosition()
    {
        return fPosition;
    }
    final void setPosition(int newPosition)
    {
        fPosition = newPosition;
    }
    public boolean isNullable() 
    {
        return (fPosition == -1);
    }
    public String toString()
    {
        StringBuffer strRet = new StringBuffer();
        strRet.append('(');
        strRet.append("##any:uri=");
        strRet.append(fURI);
        strRet.append(')');
        if (fPosition >= 0) {
            strRet.append(" (Pos:")
            	  .append(Integer.toString(fPosition))
            	  .append(')');
        }
        return strRet.toString();
    }
    protected void calcFirstPos(CMStateSet toSet) 
    {
        if (fPosition == -1)
            toSet.zeroBits();
        else
            toSet.setBit(fPosition);
    }
    protected void calcLastPos(CMStateSet toSet) 
    {
        if (fPosition == -1)
            toSet.zeroBits();
        else
            toSet.setBit(fPosition);
    }
} 
