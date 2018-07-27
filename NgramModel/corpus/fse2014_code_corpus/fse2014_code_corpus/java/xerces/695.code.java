package org.apache.xerces.xs;
public interface ElementPSVI extends ItemPSVI {
    public XSElementDeclaration getElementDeclaration();
    public XSNotationDeclaration getNotation();
    public boolean getNil();
    public XSModel getSchemaInformation();
}
