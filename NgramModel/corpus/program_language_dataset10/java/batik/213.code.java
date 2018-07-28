package org.apache.batik.bridge;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.filter.Mask;
import org.apache.batik.gvt.filter.MaskRable8Bit;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
public class SVGMaskElementBridge extends AnimatableGenericSVGBridge
        implements MaskBridge {
    public SVGMaskElementBridge() {}
    public String getLocalName() {
        return SVG_MASK_TAG;
    }
    public Mask createMask(BridgeContext ctx,
                           Element maskElement,
                           Element maskedElement,
                           GraphicsNode maskedNode) {
        String s;
        Rectangle2D maskRegion = SVGUtilities.convertMaskRegion
            (maskElement, maskedElement, maskedNode, ctx);
        GVTBuilder builder = ctx.getGVTBuilder();
        CompositeGraphicsNode maskNode = new CompositeGraphicsNode();
        CompositeGraphicsNode maskNodeContent = new CompositeGraphicsNode();
        maskNode.getChildren().add(maskNodeContent);
        boolean hasChildren = false;
        for(Node node = maskElement.getFirstChild();
            node != null;
            node = node.getNextSibling()){
            if(node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element child = (Element)node;
            GraphicsNode gn = builder.build(ctx, child) ;
            if(gn == null) {
                continue;
            }
            hasChildren = true;
            maskNodeContent.getChildren().add(gn);
        }
        if (!hasChildren) {
            return null; 
        }
        AffineTransform Tx;
        s = maskElement.getAttributeNS(null, SVG_TRANSFORM_ATTRIBUTE);
        if (s.length() != 0) {
            Tx = SVGUtilities.convertTransform
                (maskElement, SVG_TRANSFORM_ATTRIBUTE, s, ctx);
        } else {
            Tx = new AffineTransform();
        }
        short coordSystemType;
        s = maskElement.getAttributeNS(null, SVG_MASK_CONTENT_UNITS_ATTRIBUTE);
        if (s.length() == 0) {
            coordSystemType = SVGUtilities.USER_SPACE_ON_USE;
        } else {
            coordSystemType = SVGUtilities.parseCoordinateSystem
                (maskElement, SVG_MASK_CONTENT_UNITS_ATTRIBUTE, s, ctx);
        }
        if (coordSystemType == SVGUtilities.OBJECT_BOUNDING_BOX) {
            Tx = SVGUtilities.toObjectBBox(Tx, maskedNode);
        }
        maskNodeContent.setTransform(Tx);
        Filter filter = maskedNode.getFilter();
        if (filter == null) {
            filter = maskedNode.getGraphicsNodeRable(true);
        }
        return new MaskRable8Bit(filter, maskNode, maskRegion);
    }
}
