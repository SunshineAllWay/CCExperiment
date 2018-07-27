package org.apache.batik.dom.svg;
public interface ListHandler {
    void startList();
    void item(SVGItem item);
    void endList();
}
