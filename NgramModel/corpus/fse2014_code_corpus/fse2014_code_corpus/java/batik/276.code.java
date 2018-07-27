package org.apache.batik.css.engine;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Element;
public interface CSSContext {
    Value getSystemColor(String ident);
    Value getDefaultFontFamily();
    float getLighterFontWeight(float f);
    float getBolderFontWeight(float f);
    float getPixelUnitToMillimeter();
    float getPixelToMillimeter();
    float getMediumFontSize();
    float getBlockWidth(Element elt);
    float getBlockHeight(Element elt);
    void
        checkLoadExternalResource(ParsedURL resourceURL,
                                  ParsedURL docURL) throws SecurityException;
    boolean isDynamic();
    boolean isInteractive();
    CSSEngine getCSSEngineForElement(Element e);
}
