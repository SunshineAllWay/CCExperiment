package org.apache.batik.bridge;
import java.text.AttributedCharacterIterator;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.gvt.font.Glyph;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class SVGAltGlyphElementBridge extends AbstractSVGBridge
                                      implements ErrorConstants {
    public static final AttributedCharacterIterator.Attribute PAINT_INFO
        = GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO;
    public SVGAltGlyphElementBridge() {
    }
    public String getLocalName() {
        return SVG_ALT_GLYPH_TAG;
    }
    public Glyph[] createAltGlyphArray(BridgeContext ctx,
                                       Element altGlyphElement,
                                       float fontSize,
                                       AttributedCharacterIterator aci) {
        String uri = XLinkSupport.getXLinkHref(altGlyphElement);
        Element refElement = null;
        try {
            refElement = ctx.getReferencedElement(altGlyphElement, uri);
        } catch (BridgeException e) {
            if (ERR_URI_UNSECURE.equals(e.getCode())) {
                ctx.getUserAgent().displayError(e);
            }
        }
        if (refElement == null) {
            return null;
        }
        if (!SVG_NAMESPACE_URI.equals(refElement.getNamespaceURI()))
            return null; 
        if (refElement.getLocalName().equals(SVG_GLYPH_TAG)) {
            Glyph glyph = getGlyph(ctx, uri, altGlyphElement, fontSize, aci);
            if (glyph == null) {
                return null;
            }
            Glyph[] glyphArray = new Glyph[1];
            glyphArray[0] = glyph;
            return glyphArray;
        }
        if (refElement.getLocalName().equals(SVG_ALT_GLYPH_DEF_TAG)) {
            SVGOMDocument document
                = (SVGOMDocument)altGlyphElement.getOwnerDocument();
            SVGOMDocument refDocument
                = (SVGOMDocument)refElement.getOwnerDocument();
            boolean isLocal = (refDocument == document);
            Element localRefElement = (isLocal) ? refElement
                                 : (Element)document.importNode(refElement, true);
            if (!isLocal) {
                String base = AbstractNode.getBaseURI(altGlyphElement);
                Element g = document.createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
                g.appendChild(localRefElement);
                g.setAttributeNS(XMLConstants.XML_NAMESPACE_URI,
                                 "xml:base",
                                 base);
                CSSUtilities.computeStyleAndURIs(refElement, 
                                                 localRefElement, 
                                                 uri);
            }
            NodeList altGlyphDefChildren = localRefElement.getChildNodes();
            boolean containsGlyphRefNodes = false;
            int numAltGlyphDefChildren = altGlyphDefChildren.getLength();
            for (int i = 0; i < numAltGlyphDefChildren; i++) {
                Node altGlyphChild = altGlyphDefChildren.item(i);
                if (altGlyphChild.getNodeType() == Node.ELEMENT_NODE) {
                    Element agc = (Element)altGlyphChild;
                    if (SVG_NAMESPACE_URI.equals(agc.getNamespaceURI()) &&
                        SVG_GLYPH_REF_TAG.equals(agc.getLocalName())) {
                        containsGlyphRefNodes = true;
                        break;
                    }
                }
            }
            if (containsGlyphRefNodes) { 
                NodeList glyphRefNodes
                    = localRefElement.getElementsByTagNameNS(SVG_NAMESPACE_URI,
                                                             SVG_GLYPH_REF_TAG);
                int numGlyphRefNodes = glyphRefNodes.getLength();
                Glyph[] glyphArray = new Glyph[numGlyphRefNodes];
                for (int i = 0; i < numGlyphRefNodes; i++) {
                    Element glyphRefElement = (Element)glyphRefNodes.item(i);
                    String glyphUri = XLinkSupport.getXLinkHref(glyphRefElement);
                    Glyph glyph
                        = getGlyph(ctx, glyphUri, glyphRefElement, fontSize, aci);
                    if (glyph == null) {
                        return null;
                    }
                    glyphArray[i] = glyph;
                }
                return glyphArray;
            } else { 
                NodeList altGlyphItemNodes
                    = localRefElement.getElementsByTagNameNS
                    (SVG_NAMESPACE_URI, SVG_ALT_GLYPH_ITEM_TAG);
                int numAltGlyphItemNodes = altGlyphItemNodes.getLength();
                if (numAltGlyphItemNodes > 0) {
                    boolean foundMatchingGlyph = false;
                    Glyph[] glyphArray = null;
                    for (int i = 0; i < numAltGlyphItemNodes && !foundMatchingGlyph ; i++) {
                        Element altGlyphItemElement = (Element)altGlyphItemNodes.item(i);
                        NodeList altGlyphRefNodes
                            = altGlyphItemElement.getElementsByTagNameNS
                            (SVG_NAMESPACE_URI, SVG_GLYPH_REF_TAG);
                        int numAltGlyphRefNodes = altGlyphRefNodes.getLength();
                        glyphArray = new Glyph[numAltGlyphRefNodes];
                        foundMatchingGlyph = true;
                        for (int j = 0; j < numAltGlyphRefNodes; j++) {
                            Element glyphRefElement = (Element)altGlyphRefNodes.item(j);
                            String glyphUri = XLinkSupport.getXLinkHref(glyphRefElement);
                            Glyph glyph = getGlyph(ctx, glyphUri, glyphRefElement, fontSize, aci);
                            if (glyph != null) {
                                glyphArray[j] = glyph;
                            }
                            else{
                                foundMatchingGlyph = false;
                                break;
                            }
                        }
                    }
                    if (!foundMatchingGlyph) {
                        return null;
                    }
                    return glyphArray;
                }
            }
        }
        return null;
    }
    private Glyph getGlyph(BridgeContext ctx,
                           String glyphUri,
                           Element altGlyphElement,
                           float fontSize,
                           AttributedCharacterIterator aci) {
        Element refGlyphElement = null;
        try {
            refGlyphElement = ctx.getReferencedElement(altGlyphElement, 
                                                       glyphUri);
        } catch (BridgeException e) {
            if (ERR_URI_UNSECURE.equals(e.getCode())) {
                ctx.getUserAgent().displayError(e);
            }
        }
        if ((refGlyphElement == null) ||
            (!SVG_NAMESPACE_URI.equals(refGlyphElement.getNamespaceURI())) ||
            (!SVG_GLYPH_TAG.equals(refGlyphElement.getLocalName())))
            return null;
        SVGOMDocument document
            = (SVGOMDocument)altGlyphElement.getOwnerDocument();
        SVGOMDocument refDocument
            = (SVGOMDocument)refGlyphElement.getOwnerDocument();
        boolean isLocal = (refDocument == document);
        Element localGlyphElement = null;
        Element localFontFaceElement = null;
        Element localFontElement = null;
        if (isLocal) {
            localGlyphElement = refGlyphElement;
            localFontElement = (Element)localGlyphElement.getParentNode();
            NodeList fontFaceElements
                = localFontElement.getElementsByTagNameNS
                (SVG_NAMESPACE_URI, SVG_FONT_FACE_TAG);
            if (fontFaceElements.getLength() > 0) {
                localFontFaceElement = (Element)fontFaceElements.item(0);
            }
        } else {
            localFontElement = (Element)document.importNode
                (refGlyphElement.getParentNode(), true);
            String base = AbstractNode.getBaseURI(altGlyphElement);
            Element g = document.createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
            g.appendChild(localFontElement);
            g.setAttributeNS(XMLConstants.XML_NAMESPACE_URI,
                             "xml:base",
                             base);
            CSSUtilities.computeStyleAndURIs(
                (Element)refGlyphElement.getParentNode(), 
                localFontElement, glyphUri);
            String glyphId = refGlyphElement.getAttributeNS
                (null, SVG_ID_ATTRIBUTE);
            NodeList glyphElements = localFontElement.getElementsByTagNameNS
                (SVG_NAMESPACE_URI, SVG_GLYPH_TAG);
            for (int i = 0; i < glyphElements.getLength(); i++) {
                Element glyphElem = (Element)glyphElements.item(i);
                if (glyphElem.getAttributeNS(null, SVG_ID_ATTRIBUTE).equals(glyphId)) {
                    localGlyphElement = glyphElem;
                    break;
                }
            }
            NodeList fontFaceElements
                = localFontElement.getElementsByTagNameNS
                (SVG_NAMESPACE_URI, SVG_FONT_FACE_TAG);
            if (fontFaceElements.getLength() > 0) {
                localFontFaceElement = (Element)fontFaceElements.item(0);
            }
        }
        if (localGlyphElement == null || localFontFaceElement == null) {
            return null;
        }
        SVGFontFaceElementBridge fontFaceBridge
            = (SVGFontFaceElementBridge)ctx.getBridge(localFontFaceElement);
        SVGFontFace fontFace = fontFaceBridge.createFontFace
            (ctx, localFontFaceElement);
        SVGGlyphElementBridge glyphBridge
            = (SVGGlyphElementBridge)ctx.getBridge(localGlyphElement);
        aci.first();
        TextPaintInfo tpi = (TextPaintInfo)aci.getAttribute(PAINT_INFO);
        return glyphBridge.createGlyph(ctx, localGlyphElement, altGlyphElement,
                                       -1, fontSize, fontFace, tpi);
    }
}
