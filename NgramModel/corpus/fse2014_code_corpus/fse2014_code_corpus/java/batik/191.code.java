package org.apache.batik.bridge;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.AffineRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class SVGFeImageElementBridge
    extends AbstractSVGFilterPrimitiveElementBridge {
    public SVGFeImageElementBridge() {}
    public String getLocalName() {
        return SVG_FE_IMAGE_TAG;
    }
    public Filter createFilter(BridgeContext ctx,
                               Element filterElement,
                               Element filteredElement,
                               GraphicsNode filteredNode,
                               Filter inputFilter,
                               Rectangle2D filterRegion,
                               Map filterMap) {
        String uriStr = XLinkSupport.getXLinkHref(filterElement);
        if (uriStr.length() == 0) {
            throw new BridgeException(ctx, filterElement, ERR_ATTRIBUTE_MISSING,
                                      new Object[] {"xlink:href"});
        }
        Document document = filterElement.getOwnerDocument();
        boolean isUse = uriStr.indexOf('#') != -1;
        Element contentElement = null;
        if (isUse) {
            contentElement = document.createElementNS(SVG_NAMESPACE_URI,
                                                      SVG_USE_TAG);
        } else {
            contentElement = document.createElementNS(SVG_NAMESPACE_URI,
                                                      SVG_IMAGE_TAG);
        }
        contentElement.setAttributeNS(XLINK_NAMESPACE_URI,
                                      XLINK_HREF_QNAME,
                                      uriStr);
        Element proxyElement = document.createElementNS(SVG_NAMESPACE_URI,
                                                        SVG_G_TAG);
        proxyElement.appendChild(contentElement);
        Rectangle2D defaultRegion = filterRegion;
        Element filterDefElement = (Element)(filterElement.getParentNode());
        Rectangle2D primitiveRegion =
            SVGUtilities.getBaseFilterPrimitiveRegion(filterElement,
                                                      filteredElement,
                                                      filteredNode,
                                                      defaultRegion,
                                                      ctx);
        contentElement.setAttributeNS(null, SVG_X_ATTRIBUTE,      String.valueOf( primitiveRegion.getX() ) );
        contentElement.setAttributeNS(null, SVG_Y_ATTRIBUTE,      String.valueOf( primitiveRegion.getY() ) );
        contentElement.setAttributeNS(null, SVG_WIDTH_ATTRIBUTE,  String.valueOf( primitiveRegion.getWidth() ) );
        contentElement.setAttributeNS(null, SVG_HEIGHT_ATTRIBUTE, String.valueOf( primitiveRegion.getHeight() ) );
        GraphicsNode node = ctx.getGVTBuilder().build(ctx, proxyElement);
        Filter filter = node.getGraphicsNodeRable(true);
        short coordSystemType;
        String s = SVGUtilities.getChainableAttributeNS
            (filterDefElement, null, SVG_PRIMITIVE_UNITS_ATTRIBUTE, ctx);
        if (s.length() == 0) {
            coordSystemType = SVGUtilities.USER_SPACE_ON_USE;
        } else {
            coordSystemType = SVGUtilities.parseCoordinateSystem
                (filterDefElement, SVG_PRIMITIVE_UNITS_ATTRIBUTE, s, ctx);
        }
        AffineTransform at = new AffineTransform();
        if (coordSystemType == SVGUtilities.OBJECT_BOUNDING_BOX) {
            at = SVGUtilities.toObjectBBox(at, filteredNode);
        }
        filter = new AffineRable8Bit(filter, at);
        handleColorInterpolationFilters(filter, filterElement);
        Rectangle2D primitiveRegionUserSpace
            = SVGUtilities.convertFilterPrimitiveRegion(filterElement,
                                                        filteredElement,
                                                        filteredNode,
                                                        defaultRegion,
                                                        filterRegion,
                                                        ctx);
        filter = new PadRable8Bit(filter, primitiveRegionUserSpace,
                                  PadMode.ZERO_PAD);
        updateFilterMap(filterElement, filter, filterMap);
        return filter;
    }
    protected static Filter createSVGFeImage(BridgeContext ctx,
                                             Rectangle2D primitiveRegion,
                                             Element refElement,
                                             boolean toBBoxNeeded,
                                             Element filterElement,
                                             GraphicsNode filteredNode) {
        GraphicsNode node = ctx.getGVTBuilder().build(ctx, refElement);
        Filter filter = node.getGraphicsNodeRable(true);
        AffineTransform at = new AffineTransform();
        if (toBBoxNeeded){
            short coordSystemType;
            Element filterDefElement = (Element)(filterElement.getParentNode());
            String s = SVGUtilities.getChainableAttributeNS
                (filterDefElement, null, SVG_PRIMITIVE_UNITS_ATTRIBUTE, ctx);
            if (s.length() == 0) {
                coordSystemType = SVGUtilities.USER_SPACE_ON_USE;
            } else {
                coordSystemType = SVGUtilities.parseCoordinateSystem
                    (filterDefElement, SVG_PRIMITIVE_UNITS_ATTRIBUTE, s, ctx);
            }
            if (coordSystemType == SVGUtilities.OBJECT_BOUNDING_BOX) {
                at = SVGUtilities.toObjectBBox(at, filteredNode);
            }
            Rectangle2D bounds = filteredNode.getGeometryBounds();
            at.preConcatenate(AffineTransform.getTranslateInstance
                              (primitiveRegion.getX() - bounds.getX(),
                               primitiveRegion.getY() - bounds.getY()));
        } else {
            at.translate(primitiveRegion.getX(), primitiveRegion.getY());
        }
        return new AffineRable8Bit(filter, at);
    }
    protected static Filter createRasterFeImage(BridgeContext ctx,
                                                Rectangle2D   primitiveRegion,
                                                ParsedURL     purl) {
        Filter filter = ImageTagRegistry.getRegistry().readURL(purl);
        Rectangle2D bounds = filter.getBounds2D();
        AffineTransform scale = new AffineTransform();
        scale.translate(primitiveRegion.getX(), primitiveRegion.getY());
        scale.scale(primitiveRegion.getWidth()/(bounds.getWidth()-1),
                    primitiveRegion.getHeight()/(bounds.getHeight()-1));
        scale.translate(-bounds.getX(), -bounds.getY());
        return new AffineRable8Bit(filter, scale);
    }
}
