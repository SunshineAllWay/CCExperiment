package org.apache.xerces.xs;
public interface XSAnnotation extends XSObject {
    public static final short W3C_DOM_ELEMENT           = 1;
    public static final short SAX_CONTENTHANDLER        = 2;
    public static final short W3C_DOM_DOCUMENT          = 3;
    public boolean writeAnnotation(Object target, 
                                   short targetType);
    public String getAnnotationString();
}
