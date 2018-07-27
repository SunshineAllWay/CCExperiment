package org.apache.batik.bridge;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.parser.AWTTransformProducer;
import org.apache.batik.parser.ClockHandler;
import org.apache.batik.parser.ClockParser;
import org.apache.batik.parser.ParseException;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVG12Constants;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLangSpace;
import org.w3c.dom.svg.SVGNumberList;
public abstract class SVGUtilities implements SVGConstants, ErrorConstants {
    protected SVGUtilities() {}
    public static Element getParentElement(Element elt) {
        Node n = CSSEngine.getCSSParentNode(elt);
        while (n != null && n.getNodeType() != Node.ELEMENT_NODE) {
            n = CSSEngine.getCSSParentNode(n);
        }
        return (Element) n;
    }
    public static float[] convertSVGNumberList(SVGNumberList l) {
        int n = l.getNumberOfItems();
        if (n == 0) {
            return null;
        }
        float[] fl = new float[n];
        for (int i=0; i < n; i++) {
            fl[i] = l.getItem(i).getValue();
        }
        return fl;
    }
    public static float convertSVGNumber(String s) {
        return Float.parseFloat(s);
    }
    public static int convertSVGInteger(String s) {
        return Integer.parseInt(s);
    }
    public static float convertRatio(String v) {
        float d = 1;
        if (v.endsWith("%")) {
            v = v.substring(0, v.length() - 1);
            d = 100;
        }
        float r = Float.parseFloat(v)/d;
        if (r < 0) {
            r = 0;
        } else if (r > 1) {
            r = 1;
        }
        return r;
    }
    public static String getDescription(SVGElement elt) {
        String result = "";
        boolean preserve = false;
        Node n = elt.getFirstChild();
        if (n != null && n.getNodeType() == Node.ELEMENT_NODE) {
            String name =
                (n.getPrefix() == null) ? n.getNodeName() : n.getLocalName();
            if (name.equals(SVG_DESC_TAG)) {
                preserve = ((SVGLangSpace)n).getXMLspace().equals
                    (SVG_PRESERVE_VALUE);
                for (n = n.getFirstChild();
                     n != null;
                     n = n.getNextSibling()) {
                    if (n.getNodeType() == Node.TEXT_NODE) {
                        result += n.getNodeValue();
                    }
                }
            }
        }
        return (preserve)
            ? XMLSupport.preserveXMLSpace(result)
            : XMLSupport.defaultXMLSpace(result);
    }
    public static boolean matchUserAgent(Element elt, UserAgent ua) {
        test: if (elt.hasAttributeNS(null, SVG_SYSTEM_LANGUAGE_ATTRIBUTE)) {
            String sl = elt.getAttributeNS(null,
                                           SVG_SYSTEM_LANGUAGE_ATTRIBUTE);
            if (sl.length() == 0) 
                return false;
            StringTokenizer st = new StringTokenizer(sl, ", ");
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                if (matchUserLanguage(s, ua.getLanguages())) {
                    break test;
                }
            }
            return false;
        }
        if (elt.hasAttributeNS(null, SVG_REQUIRED_FEATURES_ATTRIBUTE)) {
            String rf = elt.getAttributeNS(null,
                                           SVG_REQUIRED_FEATURES_ATTRIBUTE);
            if (rf.length() == 0)  
                return false;
            StringTokenizer st = new StringTokenizer(rf, " ");
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                if (!ua.hasFeature(s)) {
                    return false;
                }
            }
        }
        if (elt.hasAttributeNS(null, SVG_REQUIRED_EXTENSIONS_ATTRIBUTE)) {
            String re = elt.getAttributeNS(null,
                                           SVG_REQUIRED_EXTENSIONS_ATTRIBUTE);
            if (re.length() == 0)  
                return false;
            StringTokenizer st = new StringTokenizer(re, " ");
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                if (!ua.supportExtension(s)) {
                    return false;
                }
            }
        }
        return true;
    }
    protected static boolean matchUserLanguage(String s,
                                               String userLanguages) {
        StringTokenizer st = new StringTokenizer(userLanguages, ", ");
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if (s.startsWith(t)) {
                if (s.length() > t.length()) {
                    return (s.charAt(t.length()) == '-');
                }
                return true;
            }
        }
        return false;
    }
    public static String getChainableAttributeNS(Element element,
                                                 String namespaceURI,
                                                 String attrName,
                                                 BridgeContext ctx) {
        DocumentLoader loader = ctx.getDocumentLoader();
        Element e = element;
        List refs = new LinkedList();
        for (;;) {
            String v = e.getAttributeNS(namespaceURI, attrName);
            if (v.length() > 0) { 
                return v;
            }
            String uriStr = XLinkSupport.getXLinkHref(e);
            if (uriStr.length() == 0) { 
                return "";
            }
            String baseURI = ((AbstractNode) e).getBaseURI();
            ParsedURL purl = new ParsedURL(baseURI, uriStr);
            Iterator iter = refs.iterator();
            while (iter.hasNext()) {
                if (purl.equals(iter.next()))
                    throw new BridgeException
                        (ctx, e, ERR_XLINK_HREF_CIRCULAR_DEPENDENCIES,
                         new Object[] {uriStr});
            }
            try {
                SVGDocument svgDoc = (SVGDocument)e.getOwnerDocument();
                URIResolver resolver = ctx.createURIResolver(svgDoc, loader);
                e = resolver.getElement(purl.toString(), e);
                refs.add(purl);
            } catch(IOException ioEx ) {
                throw new BridgeException(ctx, e, ioEx, ERR_URI_IO,
                                          new Object[] {uriStr});
            } catch(SecurityException secEx ) {
                throw new BridgeException(ctx, e, secEx, ERR_URI_UNSECURE,
                                          new Object[] {uriStr});
            }
        }
    }
    public static Point2D convertPoint(String xStr,
                                       String xAttr,
                                       String yStr,
                                       String yAttr,
                                       short unitsType,
                                       UnitProcessor.Context uctx) {
        float x, y;
        switch (unitsType) {
        case OBJECT_BOUNDING_BOX:
            x = UnitProcessor.svgHorizontalCoordinateToObjectBoundingBox
                (xStr, xAttr, uctx);
            y = UnitProcessor.svgVerticalCoordinateToObjectBoundingBox
                (yStr, yAttr, uctx);
            break;
        case USER_SPACE_ON_USE:
            x = UnitProcessor.svgHorizontalCoordinateToUserSpace
                (xStr, xAttr, uctx);
            y = UnitProcessor.svgVerticalCoordinateToUserSpace
                (yStr, yAttr, uctx);
            break;
        default:
            throw new IllegalArgumentException("Invalid unit type");
        }
        return new Point2D.Float(x, y);
    }
    public static float convertLength(String length,
                                      String attr,
                                      short unitsType,
                                      UnitProcessor.Context uctx) {
        switch (unitsType) {
        case OBJECT_BOUNDING_BOX:
            return UnitProcessor.svgOtherLengthToObjectBoundingBox
                (length, attr, uctx);
        case USER_SPACE_ON_USE:
            return UnitProcessor.svgOtherLengthToUserSpace(length, attr, uctx);
        default:
            throw new IllegalArgumentException("Invalid unit type");
        }
    }
    public static Rectangle2D convertMaskRegion(Element maskElement,
                                                Element maskedElement,
                                                GraphicsNode maskedNode,
                                                BridgeContext ctx) {
        String xStr = maskElement.getAttributeNS(null, SVG_X_ATTRIBUTE);
        if (xStr.length() == 0) {
            xStr = SVG_MASK_X_DEFAULT_VALUE;
        }
        String yStr = maskElement.getAttributeNS(null, SVG_Y_ATTRIBUTE);
        if (yStr.length() == 0) {
            yStr = SVG_MASK_Y_DEFAULT_VALUE;
        }
        String wStr = maskElement.getAttributeNS(null, SVG_WIDTH_ATTRIBUTE);
        if (wStr.length() == 0) {
            wStr = SVG_MASK_WIDTH_DEFAULT_VALUE;
        }
        String hStr = maskElement.getAttributeNS(null, SVG_HEIGHT_ATTRIBUTE);
        if (hStr.length() == 0) {
            hStr = SVG_MASK_HEIGHT_DEFAULT_VALUE;
        }
        short unitsType;
        String units =
            maskElement.getAttributeNS(null, SVG_MASK_UNITS_ATTRIBUTE);
        if (units.length() == 0) {
            unitsType = OBJECT_BOUNDING_BOX;
        } else {
            unitsType = parseCoordinateSystem
                (maskElement, SVG_MASK_UNITS_ATTRIBUTE, units, ctx);
        }
        UnitProcessor.Context uctx
            = UnitProcessor.createContext(ctx, maskedElement);
        return convertRegion(xStr,
                             yStr,
                             wStr,
                             hStr,
                             unitsType,
                             maskedNode,
                             uctx);
    }
    public static Rectangle2D convertPatternRegion(Element patternElement,
                                                   Element paintedElement,
                                                   GraphicsNode paintedNode,
                                                   BridgeContext ctx) {
        String xStr = getChainableAttributeNS
            (patternElement, null, SVG_X_ATTRIBUTE, ctx);
        if (xStr.length() == 0) {
            xStr = SVG_PATTERN_X_DEFAULT_VALUE;
        }
        String yStr = getChainableAttributeNS
            (patternElement, null, SVG_Y_ATTRIBUTE, ctx);
        if (yStr.length() == 0) {
            yStr = SVG_PATTERN_Y_DEFAULT_VALUE;
        }
        String wStr = getChainableAttributeNS
            (patternElement, null, SVG_WIDTH_ATTRIBUTE, ctx);
        if (wStr.length() == 0) {
            throw new BridgeException
                (ctx, patternElement, ERR_ATTRIBUTE_MISSING,
                 new Object[] {SVG_WIDTH_ATTRIBUTE});
        }
        String hStr = getChainableAttributeNS
            (patternElement, null, SVG_HEIGHT_ATTRIBUTE, ctx);
        if (hStr.length() == 0) {
            throw new BridgeException
                (ctx, patternElement, ERR_ATTRIBUTE_MISSING,
                 new Object[] {SVG_HEIGHT_ATTRIBUTE});
        }
        short unitsType;
        String units = getChainableAttributeNS
            (patternElement, null, SVG_PATTERN_UNITS_ATTRIBUTE, ctx);
        if (units.length() == 0) {
            unitsType = OBJECT_BOUNDING_BOX;
        } else {
            unitsType = parseCoordinateSystem
                (patternElement, SVG_PATTERN_UNITS_ATTRIBUTE, units, ctx);
        }
        UnitProcessor.Context uctx
            = UnitProcessor.createContext(ctx, paintedElement);
        return convertRegion(xStr,
                             yStr,
                             wStr,
                             hStr,
                             unitsType,
                             paintedNode,
                             uctx);
    }
    public static
        float [] convertFilterRes(Element filterElement, BridgeContext ctx) {
        float [] filterRes = new float[2];
        String s = getChainableAttributeNS
            (filterElement, null, SVG_FILTER_RES_ATTRIBUTE, ctx);
        Float [] vals = convertSVGNumberOptionalNumber
            (filterElement, SVG_FILTER_RES_ATTRIBUTE, s, ctx);
        if (filterRes[0] < 0 || filterRes[1] < 0) {
            throw new BridgeException
                (ctx, filterElement, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] {SVG_FILTER_RES_ATTRIBUTE, s});
        }
        if (vals[0] == null)
            filterRes[0] = -1;
        else {
            filterRes[0] = vals[0].floatValue();
            if (filterRes[0] < 0)
                throw new BridgeException
                    (ctx, filterElement, ERR_ATTRIBUTE_VALUE_MALFORMED,
                     new Object[] {SVG_FILTER_RES_ATTRIBUTE, s});
        }
        if (vals[1] == null)
            filterRes[1] = filterRes[0];
        else {
            filterRes[1] = vals[1].floatValue();
            if (filterRes[1] < 0)
                throw new BridgeException
                    (ctx, filterElement, ERR_ATTRIBUTE_VALUE_MALFORMED,
                     new Object[] {SVG_FILTER_RES_ATTRIBUTE, s});
        }
        return filterRes;
    }
    public static Float[] convertSVGNumberOptionalNumber(Element elem,
                                                         String attrName,
                                                         String attrValue,
                                                         BridgeContext ctx) {
        Float[] ret = new Float[2];
        if (attrValue.length() == 0)
            return ret;
        try {
            StringTokenizer tokens = new StringTokenizer(attrValue, " ");
            ret[0] = new Float(Float.parseFloat(tokens.nextToken()));
            if (tokens.hasMoreTokens()) {
                ret[1] = new Float(Float.parseFloat(tokens.nextToken()));
            }
            if (tokens.hasMoreTokens()) {
                throw new BridgeException
                    (ctx, elem, ERR_ATTRIBUTE_VALUE_MALFORMED,
                     new Object[] {attrName, attrValue});
            }
        } catch (NumberFormatException nfEx ) {
            throw new BridgeException
                (ctx, elem, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] {attrName, attrValue, nfEx });
        }
        return ret;
    }
   public static
       Rectangle2D convertFilterChainRegion(Element filterElement,
                                            Element filteredElement,
                                            GraphicsNode filteredNode,
                                            BridgeContext ctx) {
       String xStr = getChainableAttributeNS
           (filterElement, null, SVG_X_ATTRIBUTE, ctx);
       if (xStr.length() == 0) {
           xStr = SVG_FILTER_X_DEFAULT_VALUE;
       }
       String yStr = getChainableAttributeNS
           (filterElement, null, SVG_Y_ATTRIBUTE, ctx);
       if (yStr.length() == 0) {
           yStr = SVG_FILTER_Y_DEFAULT_VALUE;
       }
       String wStr = getChainableAttributeNS
           (filterElement, null, SVG_WIDTH_ATTRIBUTE, ctx);
       if (wStr.length() == 0) {
           wStr = SVG_FILTER_WIDTH_DEFAULT_VALUE;
       }
       String hStr = getChainableAttributeNS
           (filterElement, null, SVG_HEIGHT_ATTRIBUTE, ctx);
       if (hStr.length() == 0) {
           hStr = SVG_FILTER_HEIGHT_DEFAULT_VALUE;
       }
       short unitsType;
       String units = getChainableAttributeNS
           (filterElement, null, SVG_FILTER_UNITS_ATTRIBUTE, ctx);
       if (units.length() == 0) {
           unitsType = OBJECT_BOUNDING_BOX;
       } else {
           unitsType = parseCoordinateSystem
               (filterElement, SVG_FILTER_UNITS_ATTRIBUTE, units, ctx);
       }
       UnitProcessor.Context uctx
           = UnitProcessor.createContext(ctx, filteredElement);
       Rectangle2D region = convertRegion(xStr,
                                          yStr,
                                          wStr,
                                          hStr,
                                          unitsType,
                                          filteredNode,
                                          uctx);
       units = getChainableAttributeNS
           (filterElement, null,
            SVG12Constants.SVG_FILTER_MARGINS_UNITS_ATTRIBUTE, ctx);
       if (units.length() == 0) {
           unitsType = USER_SPACE_ON_USE;
       } else {
           unitsType = parseCoordinateSystem
               (filterElement,
                SVG12Constants.SVG_FILTER_MARGINS_UNITS_ATTRIBUTE, units, ctx);
       }
       String dxStr = filterElement.getAttributeNS(null,
                                                   SVG12Constants.SVG_MX_ATRIBUTE);
       if (dxStr.length() == 0) {
           dxStr = SVG12Constants.SVG_FILTER_MX_DEFAULT_VALUE;
       }
       String dyStr = filterElement.getAttributeNS(null, SVG12Constants.SVG_MY_ATRIBUTE);
       if (dyStr.length() == 0) {
           dyStr = SVG12Constants.SVG_FILTER_MY_DEFAULT_VALUE;
       }
       String dwStr = filterElement.getAttributeNS(null, SVG12Constants.SVG_MW_ATRIBUTE);
       if (dwStr.length() == 0) {
           dwStr = SVG12Constants.SVG_FILTER_MW_DEFAULT_VALUE;
       }
       String dhStr = filterElement.getAttributeNS(null, SVG12Constants.SVG_MH_ATRIBUTE);
       if (dhStr.length() == 0) {
           dhStr = SVG12Constants.SVG_FILTER_MH_DEFAULT_VALUE;
       }
       return extendRegion(dxStr,
                           dyStr,
                           dwStr,
                           dhStr,
                           unitsType,
                           filteredNode,
                           region,
                           uctx);
   }
    protected static Rectangle2D extendRegion(String dxStr,
                                              String dyStr,
                                              String dwStr,
                                              String dhStr,
                                              short unitsType,
                                              GraphicsNode filteredNode,
                                              Rectangle2D region,
                                              UnitProcessor.Context uctx) {
        float dx,dy,dw,dh;
        switch (unitsType) {
        case USER_SPACE_ON_USE:
            dx = UnitProcessor.svgHorizontalCoordinateToUserSpace
                (dxStr, SVG12Constants.SVG_MX_ATRIBUTE, uctx);
            dy = UnitProcessor.svgVerticalCoordinateToUserSpace
                (dyStr, SVG12Constants.SVG_MY_ATRIBUTE, uctx);
            dw = UnitProcessor.svgHorizontalCoordinateToUserSpace
                (dwStr, SVG12Constants.SVG_MW_ATRIBUTE, uctx);
            dh = UnitProcessor.svgVerticalCoordinateToUserSpace
                (dhStr, SVG12Constants.SVG_MH_ATRIBUTE, uctx);
            break;
        case OBJECT_BOUNDING_BOX:
            Rectangle2D bounds = filteredNode.getGeometryBounds();
            if (bounds == null) {
                dx = dy = dw = dh = 0;
            } else {
                dx = UnitProcessor.svgHorizontalCoordinateToObjectBoundingBox
                    (dxStr, SVG12Constants.SVG_MX_ATRIBUTE, uctx);
                dx *= bounds.getWidth();
                dy = UnitProcessor.svgVerticalCoordinateToObjectBoundingBox
                    (dyStr, SVG12Constants.SVG_MY_ATRIBUTE, uctx);
                dy *= bounds.getHeight();
                dw = UnitProcessor.svgHorizontalCoordinateToObjectBoundingBox
                    (dwStr, SVG12Constants.SVG_MW_ATRIBUTE, uctx);
                dw *= bounds.getWidth();
                dh = UnitProcessor.svgVerticalCoordinateToObjectBoundingBox
                    (dhStr, SVG12Constants.SVG_MH_ATRIBUTE, uctx);
                dh *= bounds.getHeight();
            }
            break;
        default:
            throw new IllegalArgumentException("Invalid unit type");
        }
        region.setRect(region.getX() + dx,
                       region.getY() + dy,
                       region.getWidth() + dw,
                       region.getHeight() + dh);
        return region;
    }
    public static Rectangle2D
        getBaseFilterPrimitiveRegion(Element filterPrimitiveElement,
                                     Element filteredElement,
                                     GraphicsNode filteredNode,
                                     Rectangle2D defaultRegion,
                                     BridgeContext ctx) {
        String s;
        UnitProcessor.Context uctx;
        uctx = UnitProcessor.createContext(ctx, filteredElement);
        double x = defaultRegion.getX();
        s = filterPrimitiveElement.getAttributeNS(null, SVG_X_ATTRIBUTE);
        if (s.length() != 0) {
            x = UnitProcessor.svgHorizontalCoordinateToUserSpace
                (s, SVG_X_ATTRIBUTE, uctx);
        }
        double y = defaultRegion.getY();
        s = filterPrimitiveElement.getAttributeNS(null, SVG_Y_ATTRIBUTE);
        if (s.length() != 0) {
            y = UnitProcessor.svgVerticalCoordinateToUserSpace
                (s, SVG_Y_ATTRIBUTE, uctx);
        }
        double w = defaultRegion.getWidth();
        s = filterPrimitiveElement.getAttributeNS(null, SVG_WIDTH_ATTRIBUTE);
        if (s.length() != 0) {
            w = UnitProcessor.svgHorizontalLengthToUserSpace
                (s, SVG_WIDTH_ATTRIBUTE, uctx);
        }
        double h = defaultRegion.getHeight();
        s = filterPrimitiveElement.getAttributeNS(null, SVG_HEIGHT_ATTRIBUTE);
        if (s.length() != 0) {
            h = UnitProcessor.svgVerticalLengthToUserSpace
                (s, SVG_HEIGHT_ATTRIBUTE, uctx);
        }
        return new Rectangle2D.Double(x, y, w, h);
    }
    public static Rectangle2D
        convertFilterPrimitiveRegion(Element filterPrimitiveElement,
                                     Element filterElement,
                                     Element filteredElement,
                                     GraphicsNode filteredNode,
                                     Rectangle2D defaultRegion,
                                     Rectangle2D filterRegion,
                                     BridgeContext ctx) {
        String units = "";
        if (filterElement != null) {
            units = getChainableAttributeNS(filterElement,
                                            null,
                                            SVG_PRIMITIVE_UNITS_ATTRIBUTE,
                                            ctx);
        }
        short unitsType;
        if (units.length() == 0) {
            unitsType = USER_SPACE_ON_USE;
        } else {
            unitsType = parseCoordinateSystem
                (filterElement, SVG_FILTER_UNITS_ATTRIBUTE, units, ctx);
        }
        String xStr = "", yStr = "", wStr = "", hStr = "";
        if (filterPrimitiveElement != null) {
            xStr = filterPrimitiveElement.getAttributeNS(null,
                                                         SVG_X_ATTRIBUTE);
            yStr = filterPrimitiveElement.getAttributeNS(null,
                                                         SVG_Y_ATTRIBUTE);
            wStr = filterPrimitiveElement.getAttributeNS(null,
                                                         SVG_WIDTH_ATTRIBUTE);
            hStr = filterPrimitiveElement.getAttributeNS(null,
                                                         SVG_HEIGHT_ATTRIBUTE);
        }
        double x = defaultRegion.getX();
        double y = defaultRegion.getY();
        double w = defaultRegion.getWidth();
        double h = defaultRegion.getHeight();
        UnitProcessor.Context uctx
            = UnitProcessor.createContext(ctx, filteredElement);
        switch (unitsType) {
        case OBJECT_BOUNDING_BOX:
            Rectangle2D bounds = filteredNode.getGeometryBounds();
            if (bounds != null) {
                if (xStr.length() != 0) {
                    x = UnitProcessor.svgHorizontalCoordinateToObjectBoundingBox
                        (xStr, SVG_X_ATTRIBUTE, uctx);
                    x = bounds.getX() + x*bounds.getWidth();
                }
                if (yStr.length() != 0) {
                    y = UnitProcessor.svgVerticalCoordinateToObjectBoundingBox
                        (yStr, SVG_Y_ATTRIBUTE, uctx);
                    y = bounds.getY() + y*bounds.getHeight();
                }
                if (wStr.length() != 0) {
                    w = UnitProcessor.svgHorizontalLengthToObjectBoundingBox
                        (wStr, SVG_WIDTH_ATTRIBUTE, uctx);
                    w *= bounds.getWidth();
                }
                if (hStr.length() != 0) {
                    h = UnitProcessor.svgVerticalLengthToObjectBoundingBox
                        (hStr, SVG_HEIGHT_ATTRIBUTE, uctx);
                    h *= bounds.getHeight();
                }
            }
            break;
        case USER_SPACE_ON_USE:
            if (xStr.length() != 0) {
                x = UnitProcessor.svgHorizontalCoordinateToUserSpace
                    (xStr, SVG_X_ATTRIBUTE, uctx);
            }
            if (yStr.length() != 0) {
                y = UnitProcessor.svgVerticalCoordinateToUserSpace
                    (yStr, SVG_Y_ATTRIBUTE, uctx);
            }
            if (wStr.length() != 0) {
                w = UnitProcessor.svgHorizontalLengthToUserSpace
                    (wStr, SVG_WIDTH_ATTRIBUTE, uctx);
            }
            if (hStr.length() != 0) {
                h = UnitProcessor.svgVerticalLengthToUserSpace
                    (hStr, SVG_HEIGHT_ATTRIBUTE, uctx);
            }
            break;
        default:
            throw new Error("invalid unitsType:" + unitsType); 
        }
        Rectangle2D region = new Rectangle2D.Double(x, y, w, h);
        units = "";
        if (filterElement != null) {
            units = getChainableAttributeNS
                (filterElement, null,
                 SVG12Constants.SVG_FILTER_PRIMITIVE_MARGINS_UNITS_ATTRIBUTE,
                 ctx);
        }
        if (units.length() == 0) {
            unitsType = USER_SPACE_ON_USE;
        } else {
            unitsType = parseCoordinateSystem
                (filterElement,
                 SVG12Constants.SVG_FILTER_PRIMITIVE_MARGINS_UNITS_ATTRIBUTE,
                 units, ctx);
        }
        String dxStr = "", dyStr = "", dwStr = "", dhStr = "";
        if (filterPrimitiveElement != null) {
            dxStr = filterPrimitiveElement.getAttributeNS
                (null, SVG12Constants.SVG_MX_ATRIBUTE);
            dyStr = filterPrimitiveElement.getAttributeNS
                (null, SVG12Constants.SVG_MY_ATRIBUTE);
            dwStr = filterPrimitiveElement.getAttributeNS
                (null, SVG12Constants.SVG_MW_ATRIBUTE);
            dhStr = filterPrimitiveElement.getAttributeNS
                (null, SVG12Constants.SVG_MH_ATRIBUTE);
        }
        if (dxStr.length() == 0) {
            dxStr = SVG12Constants.SVG_FILTER_MX_DEFAULT_VALUE;
        }
        if (dyStr.length() == 0) {
            dyStr = SVG12Constants.SVG_FILTER_MY_DEFAULT_VALUE;
        }
        if (dwStr.length() == 0) {
            dwStr = SVG12Constants.SVG_FILTER_MW_DEFAULT_VALUE;
        }
        if (dhStr.length() == 0) {
            dhStr = SVG12Constants.SVG_FILTER_MH_DEFAULT_VALUE;
        }
        region = extendRegion(dxStr,
                              dyStr,
                              dwStr,
                              dhStr,
                              unitsType,
                              filteredNode,
                              region,
                              uctx);
        Rectangle2D.intersect(region, filterRegion, region);
        return region;
    }
    public static Rectangle2D
        convertFilterPrimitiveRegion(Element filterPrimitiveElement,
                                     Element filteredElement,
                                     GraphicsNode filteredNode,
                                     Rectangle2D defaultRegion,
                                     Rectangle2D filterRegion,
                                     BridgeContext ctx) {
        Node parentNode = filterPrimitiveElement.getParentNode();
        Element filterElement = null;
        if (parentNode != null &&
                parentNode.getNodeType() == Node.ELEMENT_NODE) {
            filterElement = (Element) parentNode;
        }
        return convertFilterPrimitiveRegion(filterPrimitiveElement,
                                            filterElement,
                                            filteredElement,
                                            filteredNode,
                                            defaultRegion,
                                            filterRegion,
                                            ctx);
    }
    public static final short USER_SPACE_ON_USE = 1;
    public static final short OBJECT_BOUNDING_BOX = 2;
    public static final short STROKE_WIDTH = 3;
    public static short parseCoordinateSystem(Element e,
                                              String attr,
                                              String coordinateSystem,
                                              BridgeContext ctx) {
        if (SVG_USER_SPACE_ON_USE_VALUE.equals(coordinateSystem)) {
            return USER_SPACE_ON_USE;
        } else if (SVG_OBJECT_BOUNDING_BOX_VALUE.equals(coordinateSystem)) {
            return OBJECT_BOUNDING_BOX;
        } else {
            throw new BridgeException(ctx, e, ERR_ATTRIBUTE_VALUE_MALFORMED,
                                      new Object[] {attr, coordinateSystem});
        }
    }
    public static short parseMarkerCoordinateSystem(Element e,
                                                    String attr,
                                                    String coordinateSystem,
                                                    BridgeContext ctx) {
        if (SVG_USER_SPACE_ON_USE_VALUE.equals(coordinateSystem)) {
            return USER_SPACE_ON_USE;
        } else if (SVG_STROKE_WIDTH_VALUE.equals(coordinateSystem)) {
            return STROKE_WIDTH;
        } else {
            throw new BridgeException(ctx, e, ERR_ATTRIBUTE_VALUE_MALFORMED,
                                      new Object[] {attr, coordinateSystem});
        }
    }
    protected static Rectangle2D convertRegion(String xStr,
                                               String yStr,
                                               String wStr,
                                               String hStr,
                                               short unitsType,
                                               GraphicsNode targetNode,
                                               UnitProcessor.Context uctx) {
        double x, y, w, h;
        switch (unitsType) {
        case OBJECT_BOUNDING_BOX:
            x = UnitProcessor.svgHorizontalCoordinateToObjectBoundingBox
                (xStr, SVG_X_ATTRIBUTE, uctx);
            y = UnitProcessor.svgVerticalCoordinateToObjectBoundingBox
                (yStr, SVG_Y_ATTRIBUTE, uctx);
            w = UnitProcessor.svgHorizontalLengthToObjectBoundingBox
                (wStr, SVG_WIDTH_ATTRIBUTE, uctx);
            h = UnitProcessor.svgVerticalLengthToObjectBoundingBox
                (hStr, SVG_HEIGHT_ATTRIBUTE, uctx);
            Rectangle2D bounds = targetNode.getGeometryBounds();
            if (bounds != null ) {
                x = bounds.getX() + x*bounds.getWidth();
                y = bounds.getY() + y*bounds.getHeight();
                w *= bounds.getWidth();
                h *= bounds.getHeight();
            } else {
                x = y = w = h = 0;
            }
            break;
        case USER_SPACE_ON_USE:
            x = UnitProcessor.svgHorizontalCoordinateToUserSpace
                (xStr, SVG_X_ATTRIBUTE, uctx);
            y = UnitProcessor.svgVerticalCoordinateToUserSpace
                (yStr, SVG_Y_ATTRIBUTE, uctx);
            w = UnitProcessor.svgHorizontalLengthToUserSpace
                (wStr, SVG_WIDTH_ATTRIBUTE, uctx);
            h = UnitProcessor.svgVerticalLengthToUserSpace
                (hStr, SVG_HEIGHT_ATTRIBUTE, uctx);
            break;
        default:
            throw new Error("invalid unitsType:" + unitsType ); 
        }
        return new Rectangle2D.Double(x, y, w, h);
    }
    public static AffineTransform convertTransform(Element e,
                                                   String attr,
                                                   String transform,
                                                   BridgeContext ctx) {
        try {
            return AWTTransformProducer.createAffineTransform(transform);
        } catch (ParseException pEx) {
            throw new BridgeException(ctx, e, pEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                                      new Object[] {attr, transform, pEx });
        }
    }
    public static AffineTransform toObjectBBox(AffineTransform Tx,
                                               GraphicsNode node) {
        AffineTransform Mx = new AffineTransform();
        Rectangle2D bounds = node.getGeometryBounds();
        if (bounds != null) {
            Mx.translate(bounds.getX(), bounds.getY());
            Mx.scale(bounds.getWidth(), bounds.getHeight());
        }
        Mx.concatenate(Tx);
        return Mx;
    }
    public static Rectangle2D toObjectBBox(Rectangle2D r,
                                           GraphicsNode node) {
        Rectangle2D bounds = node.getGeometryBounds();
        if (bounds != null) {
            return new Rectangle2D.Double
                (bounds.getX() + r.getX()*bounds.getWidth(),
                 bounds.getY() + r.getY()*bounds.getHeight(),
                 r.getWidth() * bounds.getWidth(),
                 r.getHeight() * bounds.getHeight());
        } else {
            return new Rectangle2D.Double();
        }
    }
    public static float convertSnapshotTime(Element e, BridgeContext ctx) {
        if (!e.hasAttributeNS(null, SVG_SNAPSHOT_TIME_ATTRIBUTE)) {
            return 0f;
        }
        String t = e.getAttributeNS(null, SVG_SNAPSHOT_TIME_ATTRIBUTE);
        if (t.equals(SVG_NONE_VALUE)) {
            return 0f;
        }
        class Handler implements ClockHandler {
            float time;
            public void clockValue(float t) {
                time = t;
            }
        }
        ClockParser p = new ClockParser(false);
        Handler h = new Handler();
        p.setClockHandler(h);
        try {
            p.parse(t);
        } catch (ParseException pEx ) {
            throw new BridgeException
                (null, e, pEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] { SVG_SNAPSHOT_TIME_ATTRIBUTE, t, pEx });
        }
        return h.time;
    }
}
