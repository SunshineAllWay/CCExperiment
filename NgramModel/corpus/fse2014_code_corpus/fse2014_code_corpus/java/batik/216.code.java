package org.apache.batik.bridge;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.ext.awt.image.ConcreteComponentTransferFunction;
import org.apache.batik.ext.awt.image.renderable.ComponentTransferRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.AbstractGraphicsNode;
import org.apache.batik.gvt.RootGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.PatternPaint;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
public class SVGPatternElementBridge extends AnimatableGenericSVGBridge
        implements PaintBridge, ErrorConstants {
    public SVGPatternElementBridge() {}
    public String getLocalName() {
        return SVG_PATTERN_TAG;
    }
    public Paint createPaint(BridgeContext ctx,
                             Element patternElement,
                             Element paintedElement,
                             GraphicsNode paintedNode,
                             float opacity) {
        RootGraphicsNode patternContentNode;
        patternContentNode = (RootGraphicsNode)
            ctx.getElementData(patternElement);
        if (patternContentNode == null) {
            patternContentNode = extractPatternContent(patternElement, ctx);
            ctx.setElementData(patternElement, patternContentNode);
        }
        if (patternContentNode == null) {
            return null; 
        }
        Rectangle2D patternRegion = SVGUtilities.convertPatternRegion
            (patternElement, paintedElement, paintedNode, ctx);
        String s;
        AffineTransform patternTransform;
        s = SVGUtilities.getChainableAttributeNS
            (patternElement, null, SVG_PATTERN_TRANSFORM_ATTRIBUTE, ctx);
        if (s.length() != 0) {
            patternTransform = SVGUtilities.convertTransform
                (patternElement, SVG_PATTERN_TRANSFORM_ATTRIBUTE, s, ctx);
        } else {
            patternTransform = new AffineTransform();
        }
        boolean overflowIsHidden = CSSUtilities.convertOverflow(patternElement);
        short contentCoordSystem;
        s = SVGUtilities.getChainableAttributeNS
            (patternElement, null, SVG_PATTERN_CONTENT_UNITS_ATTRIBUTE, ctx);
        if (s.length() == 0) {
            contentCoordSystem = SVGUtilities.USER_SPACE_ON_USE;
        } else {
            contentCoordSystem = SVGUtilities.parseCoordinateSystem
                (patternElement, SVG_PATTERN_CONTENT_UNITS_ATTRIBUTE, s, ctx);
        }
        AffineTransform patternContentTransform = new AffineTransform();
        patternContentTransform.translate(patternRegion.getX(),
                                          patternRegion.getY());
        String viewBoxStr = SVGUtilities.getChainableAttributeNS
            (patternElement, null, SVG_VIEW_BOX_ATTRIBUTE, ctx);
        if (viewBoxStr.length() > 0) {
            String aspectRatioStr = SVGUtilities.getChainableAttributeNS
               (patternElement, null, SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE, ctx);
            float w = (float)patternRegion.getWidth();
            float h = (float)patternRegion.getHeight();
            AffineTransform preserveAspectRatioTransform
                = ViewBox.getPreserveAspectRatioTransform
                (patternElement, viewBoxStr, aspectRatioStr, w, h, ctx);
            patternContentTransform.concatenate(preserveAspectRatioTransform);
        } else {
            if (contentCoordSystem == SVGUtilities.OBJECT_BOUNDING_BOX){
                AffineTransform patternContentUnitsTransform
                    = new AffineTransform();
                Rectangle2D objectBoundingBox = 
                    paintedNode.getGeometryBounds();
                patternContentUnitsTransform.translate
                    (objectBoundingBox.getX(),
                     objectBoundingBox.getY());
                patternContentUnitsTransform.scale
                    (objectBoundingBox.getWidth(),
                     objectBoundingBox.getHeight());
                patternContentTransform.concatenate
                    (patternContentUnitsTransform);
            }
        }
        GraphicsNode gn = new PatternGraphicsNode(patternContentNode);
        gn.setTransform(patternContentTransform);
        if (opacity != 1) {
            Filter filter = gn.getGraphicsNodeRable(true);
            filter = new ComponentTransferRable8Bit
                (filter,
                 ConcreteComponentTransferFunction.getLinearTransfer
                 (opacity, 0), 
                 ConcreteComponentTransferFunction.getIdentityTransfer(), 
                 ConcreteComponentTransferFunction.getIdentityTransfer(), 
                 ConcreteComponentTransferFunction.getIdentityTransfer());
            gn.setFilter(filter);
        }
        return new PatternPaint(gn,
                                patternRegion,
                                !overflowIsHidden,
                                patternTransform);
    }
    protected static
        RootGraphicsNode extractPatternContent(Element patternElement,
                                               BridgeContext ctx) {
        List refs = new LinkedList();
        for (;;) {
            RootGraphicsNode content
                = extractLocalPatternContent(patternElement, ctx);
            if (content != null) {
                return content; 
            }
            String uri = XLinkSupport.getXLinkHref(patternElement);
            if (uri.length() == 0) {
                return null; 
            }
            SVGOMDocument doc =
                (SVGOMDocument)patternElement.getOwnerDocument();
            ParsedURL purl = new ParsedURL(doc.getURL(), uri);
            if (!purl.complete())
                throw new BridgeException(ctx, patternElement,
                                          ERR_URI_MALFORMED,
                                          new Object[] {uri});
            if (contains(refs, purl)) {
                throw new BridgeException(ctx, patternElement,
                                          ERR_XLINK_HREF_CIRCULAR_DEPENDENCIES,
                                          new Object[] {uri});
            }
            refs.add(purl);
            patternElement = ctx.getReferencedElement(patternElement, uri);
        }
    }
    protected static
        RootGraphicsNode extractLocalPatternContent(Element e,
                                                         BridgeContext ctx) {
        GVTBuilder builder = ctx.getGVTBuilder();
        RootGraphicsNode content = null;
        for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            GraphicsNode gn = builder.build(ctx, (Element)n);
            if (gn != null) {
                if (content == null) {
                    content = new RootGraphicsNode();
                }
                content.getChildren().add(gn);
            }
        }
        return content;
    }
    private static boolean contains(List urls, ParsedURL key) {
        Iterator iter = urls.iterator();
        while (iter.hasNext()) {
            if (key.equals(iter.next()))
                return true;
        }
        return false;
    }
    public static class PatternGraphicsNode extends AbstractGraphicsNode {
        GraphicsNode pcn;
        Rectangle2D pBounds;
        Rectangle2D gBounds;
        Rectangle2D sBounds;
        Shape       oShape;
        public PatternGraphicsNode(GraphicsNode gn) {
            this.pcn = gn;
        }
        public void primitivePaint(Graphics2D g2d) {
            pcn.paint(g2d);
        }
        public Rectangle2D getPrimitiveBounds() {
            if (pBounds != null) return pBounds;
            pBounds = pcn.getTransformedBounds(IDENTITY);
            return pBounds;
        }
        public Rectangle2D getGeometryBounds() {
            if (gBounds != null) return gBounds;
            gBounds = pcn.getTransformedGeometryBounds(IDENTITY);
            return gBounds;
        }
        public Rectangle2D getSensitiveBounds() {
            if (sBounds != null) return sBounds;
            sBounds = pcn.getTransformedSensitiveBounds(IDENTITY);
            return sBounds;
        }
        public Shape getOutline() {
            if (oShape != null) return oShape;
            oShape = pcn.getOutline();
            AffineTransform tr = pcn.getTransform();
            if (tr != null)
                oShape = tr.createTransformedShape(oShape);
            return oShape;
        }
        protected void invalidateGeometryCache() {
            pBounds = null;
            gBounds = null;
            sBounds = null;
            oShape  = null;
            super.invalidateGeometryCache();
        }
    }
}
