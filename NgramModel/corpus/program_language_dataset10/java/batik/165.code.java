package org.apache.batik.bridge;
import java.awt.font.FontRenderContext;
import java.text.AttributedCharacterIterator;
import org.apache.batik.gvt.font.AltGlyphHandler;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.font.Glyph;
import org.apache.batik.gvt.font.SVGGVTGlyphVector;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;
public class SVGAltGlyphHandler implements AltGlyphHandler, SVGConstants {
    private BridgeContext ctx;
    private Element textElement;
    public SVGAltGlyphHandler(BridgeContext ctx, Element textElement) {
        this.ctx = ctx;
        this.textElement = textElement;
    }
    public GVTGlyphVector createGlyphVector
        (FontRenderContext frc, float fontSize,
         AttributedCharacterIterator aci) {
        try {
            if (SVG_NAMESPACE_URI.equals(textElement.getNamespaceURI()) &&
                SVG_ALT_GLYPH_TAG.equals(textElement.getLocalName())) {
                SVGAltGlyphElementBridge altGlyphBridge
                    = (SVGAltGlyphElementBridge)ctx.getBridge(textElement);
                Glyph[] glyphArray = altGlyphBridge.createAltGlyphArray
                    (ctx, textElement, fontSize, aci);
                if (glyphArray != null) {
                    return new SVGGVTGlyphVector(null, glyphArray, frc);
                }
            }
        } catch (SecurityException e) {
            ctx.getUserAgent().displayError(e);
            throw e;
        }
        return null;
    }
}
