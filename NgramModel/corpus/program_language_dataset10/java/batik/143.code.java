package org.apache.batik.bridge;
import java.awt.Font;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.gvt.font.AWTFontFamily;
import org.apache.batik.gvt.font.FontFamilyResolver;
import org.apache.batik.gvt.font.GVTFontFace;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;
public abstract class FontFace extends GVTFontFace
    implements ErrorConstants  {
    List srcs;
    public FontFace
        (List srcs,
         String familyName, float unitsPerEm, String fontWeight,
         String fontStyle, String fontVariant, String fontStretch,
         float slope, String panose1, float ascent, float descent,
         float strikethroughPosition, float strikethroughThickness,
         float underlinePosition,     float underlineThickness,
         float overlinePosition,      float overlineThickness) {
        super(familyName, unitsPerEm, fontWeight,
              fontStyle, fontVariant, fontStretch,
              slope, panose1, ascent, descent,
              strikethroughPosition, strikethroughThickness,
              underlinePosition, underlineThickness,
              overlinePosition, overlineThickness);
        this.srcs = srcs;
    }
    protected FontFace(String familyName) {
        super(familyName);
    }
    public static CSSFontFace createFontFace(String familyName,
                                             FontFace src) {
        return new CSSFontFace
            (new LinkedList(src.srcs),
             familyName, src.unitsPerEm, src.fontWeight,
             src.fontStyle, src.fontVariant, src.fontStretch,
             src.slope, src.panose1, src.ascent, src.descent,
             src.strikethroughPosition, src.strikethroughThickness,
             src.underlinePosition, src.underlineThickness,
             src.overlinePosition, src.overlineThickness);
    }
    public GVTFontFamily getFontFamily(BridgeContext ctx) {
        String name = FontFamilyResolver.lookup(familyName);
        if (name != null) {
            GVTFontFace ff = createFontFace(name, this);
            return new AWTFontFamily(ff);
        }
        Iterator iter = srcs.iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o instanceof String) {
                String str = (String)o;
                name = FontFamilyResolver.lookup(str);
                if (name != null) {
                    GVTFontFace ff = createFontFace(str, this);
                    return new AWTFontFamily(ff);
                }
            } else if (o instanceof ParsedURL) {
                try {
                    GVTFontFamily ff = getFontFamily(ctx, (ParsedURL)o);
                    if (ff != null)
                        return ff;
                } catch (SecurityException ex) {
                    ctx.getUserAgent().displayError(ex);
                } catch (BridgeException ex) {
                    if (ERR_URI_UNSECURE.equals(ex.getCode()))
                        ctx.getUserAgent().displayError(ex);
                } catch (Exception ex) {
                }
            }
        }
        return new AWTFontFamily(this);
    }
    protected GVTFontFamily getFontFamily(BridgeContext ctx,
                                          ParsedURL purl) {
        String purlStr = purl.toString();
        Element e = getBaseElement(ctx);
        SVGDocument svgDoc = (SVGDocument)e.getOwnerDocument();
        String docURL = svgDoc.getURL();
        ParsedURL pDocURL = null;
        if (docURL != null)
            pDocURL = new ParsedURL(docURL);
        String baseURI = AbstractNode.getBaseURI(e);
        purl = new ParsedURL(baseURI, purlStr);
        UserAgent userAgent = ctx.getUserAgent();
        try {
            userAgent.checkLoadExternalResource(purl, pDocURL);
        } catch (SecurityException ex) {
            userAgent.displayError(ex);
            return null;
        }
        if (purl.getRef() != null) {
            Element ref = ctx.getReferencedElement(e, purlStr);
            if (!ref.getNamespaceURI().equals(SVG_NAMESPACE_URI) ||
                !ref.getLocalName().equals(SVG_FONT_TAG)) {
                return null;
            }
            SVGDocument doc  = (SVGDocument)e.getOwnerDocument();
            SVGDocument rdoc = (SVGDocument)ref.getOwnerDocument();
            Element fontElt = ref;
            if (doc != rdoc) {
                fontElt = (Element)doc.importNode(ref, true);
                String base = AbstractNode.getBaseURI(ref);
                Element g = doc.createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
                g.appendChild(fontElt);
                g.setAttributeNS(XMLConstants.XML_NAMESPACE_URI,
                                 "xml:base", base);
                CSSUtilities.computeStyleAndURIs(ref, fontElt, purlStr);
            }
            Element fontFaceElt = null;
            for (Node n = fontElt.getFirstChild();
                 n != null;
                 n = n.getNextSibling()) {
                if ((n.getNodeType() == Node.ELEMENT_NODE) &&
                    n.getNamespaceURI().equals(SVG_NAMESPACE_URI) &&
                    n.getLocalName().equals(SVG_FONT_FACE_TAG)) {
                    fontFaceElt = (Element)n;
                    break;
                }
            }
            SVGFontFaceElementBridge fontFaceBridge;
            fontFaceBridge = (SVGFontFaceElementBridge)ctx.getBridge
                (SVG_NAMESPACE_URI, SVG_FONT_FACE_TAG);
            GVTFontFace gff = fontFaceBridge.createFontFace(ctx, fontFaceElt);
            return new SVGFontFamily(gff, fontElt, ctx);
        }
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT,
                                        purl.openStream());
            return new AWTFontFamily(this, font);
        } catch (Exception ex) {
        }
        return null;
    }
    protected Element getBaseElement(BridgeContext ctx) {
        SVGDocument d = (SVGDocument)ctx.getDocument();
        return d.getRootElement();
    }
}
