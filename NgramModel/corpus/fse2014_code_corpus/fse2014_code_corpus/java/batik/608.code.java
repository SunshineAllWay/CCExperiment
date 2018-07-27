package org.apache.batik.dom.svg;
public class SVGOMLength extends AbstractSVGLength {
    protected AbstractElement element;
    public SVGOMLength(AbstractElement elt){
        super(OTHER_LENGTH);
        element = elt;
    }
    protected SVGOMElement getAssociatedElement(){
        return (SVGOMElement)element;
    }
}
