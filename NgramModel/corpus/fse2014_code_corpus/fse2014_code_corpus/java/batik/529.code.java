package org.apache.batik.dom.svg;
public interface SVGItem {
    void setParent(AbstractSVGList list);
    AbstractSVGList getParent();
    String getValueAsString();
}
