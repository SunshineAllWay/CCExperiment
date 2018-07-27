package org.apache.batik.bridge.svg12;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.ext.awt.image.renderable.ClipRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.CSSUtilities;
import org.apache.batik.bridge.SVGImageElementBridge;
import org.apache.batik.bridge.SVGUtilities;
import org.apache.batik.bridge.UnitProcessor;
import org.apache.batik.bridge.Viewport;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ImageNode;
import org.apache.batik.gvt.svg12.MultiResGraphicsNode;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVG12Constants;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
public class SVGMultiImageElementBridge extends SVGImageElementBridge {
    public SVGMultiImageElementBridge() { }
    public String getNamespaceURI() {
        return SVG12Constants.SVG_NAMESPACE_URI;
    }
    public String getLocalName() {
        return SVG12Constants.SVG_MULTI_IMAGE_TAG;
    }
    public Bridge getInstance() {
        return new SVGMultiImageElementBridge();
    }
    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
        if (!SVGUtilities.matchUserAgent(e, ctx.getUserAgent())) {
            return null;
        }
        ImageNode imgNode = (ImageNode)instantiateGraphicsNode();
        if (imgNode == null) {
            return null;
        }
        associateSVGContext(ctx, e, imgNode);
        Rectangle2D b = getImageBounds(ctx, e);
        AffineTransform at = null;
        String s = e.getAttribute(SVG_TRANSFORM_ATTRIBUTE);
        if (s.length() != 0) {
            at = SVGUtilities.convertTransform(e, SVG_TRANSFORM_ATTRIBUTE, s,
                                               ctx);
        } else {
            at = new AffineTransform();
        }
        at.translate(b.getX(), b.getY());
        imgNode.setTransform(at);
        imgNode.setVisible(CSSUtilities.convertVisibility(e));
        Rectangle2D clip;
        clip = new Rectangle2D.Double(0,0,b.getWidth(), b.getHeight());
        Filter filter = imgNode.getGraphicsNodeRable(true);
        imgNode.setClip(new ClipRable8Bit(filter, clip));
        Rectangle2D r = CSSUtilities.convertEnableBackground(e);
        if (r != null) {
            imgNode.setBackgroundEnable(r);
        }
        ctx.openViewport(e, new MultiImageElementViewport
                         ((float)b.getWidth(), (float)b.getHeight()));
        List elems  = new LinkedList();
        List minDim = new LinkedList();
        List maxDim = new LinkedList();
        for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element se = (Element)n;
            if (!getNamespaceURI().equals(se.getNamespaceURI()))
                continue;
            if (se.getLocalName().equals(SVG12Constants.SVG_SUB_IMAGE_TAG)) {
                addInfo(se, elems, minDim, maxDim, b);
            }
            if (se.getLocalName().equals(SVG12Constants.SVG_SUB_IMAGE_REF_TAG)) {
                addRefInfo(se, elems, minDim, maxDim, b);
            }
        }
        Dimension [] mindary = new Dimension[elems.size()];
        Dimension [] maxdary = new Dimension[elems.size()];
        Element   [] elemary = new Element  [elems.size()];
        Iterator mindi = minDim.iterator();
        Iterator maxdi = maxDim.iterator();
        Iterator ei = elems.iterator();
        int n=0;
        while (mindi.hasNext()) {
            Dimension minD = (Dimension)mindi.next();
            Dimension maxD = (Dimension)maxdi.next();
            int i =0;
            if (minD != null) {
                for (; i<n; i++) {
                    if ((mindary[i] != null) &&
                        (minD.width < mindary[i].width)) {
                        break;
                    }
                }
            }
            for (int j=n; j>i; j--) {
                elemary[j] = elemary[j-1];
                mindary[j] = mindary[j-1];
                maxdary[j] = maxdary[j-1];
            }
            elemary[i] = (Element)ei.next();
            mindary[i] = minD;
            maxdary[i] = maxD;
            n++;
        }
        GraphicsNode node = new MultiResGraphicsNode(e, clip, elemary, 
                                                     mindary, maxdary,
                                                     ctx);
        imgNode.setImage(node);
        return imgNode;
    }
    public boolean isComposite() {
        return false;
    }
    public void buildGraphicsNode(BridgeContext ctx,
                                  Element e,
                                  GraphicsNode node) {
        initializeDynamicSupport(ctx, e, node);
        ctx.closeViewport(e);
    }
    protected void initializeDynamicSupport(BridgeContext ctx,
                                            Element e,
                                            GraphicsNode node) {
        if (ctx.isInteractive()) {
            ImageNode imgNode = (ImageNode)node;
            ctx.bind(e, imgNode.getImage());
        }
    }
    public void dispose() {
        ctx.removeViewport(e);
        super.dispose();
    }
    protected static
        Rectangle2D getImageBounds(BridgeContext ctx, Element element) {
        UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, element);
        String s = element.getAttributeNS(null, SVG_X_ATTRIBUTE);
        float x = 0;
        if (s.length() != 0) {
            x = UnitProcessor.svgHorizontalCoordinateToUserSpace
                (s, SVG_X_ATTRIBUTE, uctx);
        }
        s = element.getAttributeNS(null, SVG_Y_ATTRIBUTE);
        float y = 0;
        if (s.length() != 0) {
            y = UnitProcessor.svgVerticalCoordinateToUserSpace
                (s, SVG_Y_ATTRIBUTE, uctx);
        }
        s = element.getAttributeNS(null, SVG_WIDTH_ATTRIBUTE);
        float w;
        if (s.length() == 0) {
            throw new BridgeException(ctx, element, ERR_ATTRIBUTE_MISSING,
                                      new Object[] {SVG_WIDTH_ATTRIBUTE});
        } else {
            w = UnitProcessor.svgHorizontalLengthToUserSpace
                (s, SVG_WIDTH_ATTRIBUTE, uctx);
        }
        s = element.getAttributeNS(null, SVG_HEIGHT_ATTRIBUTE);
        float h;
        if (s.length() == 0) {
            throw new BridgeException(ctx, element, ERR_ATTRIBUTE_MISSING,
                                      new Object[] {SVG_HEIGHT_ATTRIBUTE});
        } else {
            h = UnitProcessor.svgVerticalLengthToUserSpace
                (s, SVG_HEIGHT_ATTRIBUTE, uctx);
        }
        return new Rectangle2D.Float(x, y, w, h);
    }
    protected void addInfo(Element e, Collection elems, 
                           Collection minDim, Collection maxDim,
                           Rectangle2D bounds) {
        Document doc   = e.getOwnerDocument();
        Element  gElem = doc.createElementNS(SVG_NAMESPACE_URI, 
                                              SVG_G_TAG);
        NamedNodeMap attrs = e.getAttributes();
        int len = attrs.getLength();
        for (int i = 0; i < len; i++) {
            Attr attr = (Attr)attrs.item(i);
            gElem.setAttributeNS(attr.getNamespaceURI(),
                                 attr.getName(),
                                 attr.getValue());
        }
        for (Node n = e.getFirstChild();
             n != null;
             n = e.getFirstChild()) {
            gElem.appendChild(n);
        }
        e.appendChild(gElem);
        elems.add(gElem);
        minDim.add(getElementMinPixel(e, bounds));
        maxDim.add(getElementMaxPixel(e, bounds));
    }
    protected void addRefInfo(Element e, Collection elems, 
                              Collection minDim, Collection maxDim,
                              Rectangle2D bounds) {
        String uriStr = XLinkSupport.getXLinkHref(e);
        if (uriStr.length() == 0) {
            throw new BridgeException(ctx, e, ERR_ATTRIBUTE_MISSING,
                                      new Object[] {"xlink:href"});
        }
        String baseURI = AbstractNode.getBaseURI(e);
        ParsedURL purl;
        if (baseURI == null) purl = new ParsedURL(uriStr);
        else                 purl = new ParsedURL(baseURI, uriStr);
        Document doc = e.getOwnerDocument();
        Element imgElem = doc.createElementNS(SVG_NAMESPACE_URI, 
                                              SVG_IMAGE_TAG);
        imgElem.setAttributeNS(XLINK_NAMESPACE_URI, 
                               XLINK_HREF_ATTRIBUTE, purl.toString());
        NamedNodeMap attrs = e.getAttributes();
        int len = attrs.getLength();
        for (int i = 0; i < len; i++) {
            Attr attr = (Attr)attrs.item(i);
            imgElem.setAttributeNS(attr.getNamespaceURI(),
                                   attr.getName(),
                                   attr.getValue());
        }
        String s;
        s = e.getAttribute("x");
        if (s.length() == 0) imgElem.setAttribute("x", "0");
        s = e.getAttribute("y");
        if (s.length() == 0) imgElem.setAttribute("y", "0");
        s = e.getAttribute("width");
        if (s.length() == 0) imgElem.setAttribute("width", "100%");
        s = e.getAttribute("height");
        if (s.length() == 0) imgElem.setAttribute("height", "100%");
        e.appendChild(imgElem);
        elems.add(imgElem);
        minDim.add(getElementMinPixel(e, bounds));
        maxDim.add(getElementMaxPixel(e, bounds));
    }
    protected Dimension getElementMinPixel(Element e, Rectangle2D bounds) {
        return getElementPixelSize
            (e, SVG12Constants.SVG_MAX_PIXEL_SIZE_ATTRIBUTE, bounds);
    }
    protected Dimension getElementMaxPixel(Element e, Rectangle2D bounds) {
        return getElementPixelSize
            (e, SVG12Constants.SVG_MIN_PIXEL_SIZE_ATTRIBUTE, bounds);
    }
    protected Dimension getElementPixelSize(Element e, 
                                            String attr,
                                            Rectangle2D bounds) {
        String s;
        s = e.getAttribute(attr);
        if (s.length() == 0) return null;
        Float[] vals = SVGUtilities.convertSVGNumberOptionalNumber
            (e, attr, s, ctx);
        if (vals[0] == null) return null;
        float xPixSz = vals[0].floatValue();
        float yPixSz = xPixSz;
        if (vals[1] != null)
            yPixSz = vals[1].floatValue();
        return new Dimension((int)(bounds.getWidth()/xPixSz+0.5), 
                             (int)(bounds.getHeight()/yPixSz+0.5)); 
    }
    public static class MultiImageElementViewport implements Viewport {
        private float width;
        private float height;
        public MultiImageElementViewport(float w, float h) {
            this.width = w;
            this.height = h;
        }
        public float getWidth(){
            return width;
        }
        public float getHeight(){
            return height;
        }
    }
}
