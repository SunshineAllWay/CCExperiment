package org.apache.batik.bridge;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import org.apache.batik.dom.svg.SVGOMUseElement;
import org.apache.batik.ext.awt.image.renderable.ClipRable;
import org.apache.batik.ext.awt.image.renderable.ClipRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
public class SVGClipPathElementBridge extends AnimatableGenericSVGBridge
        implements ClipBridge {
    public SVGClipPathElementBridge() {}
    public String getLocalName() {
        return SVG_CLIP_PATH_TAG;
    }
    public ClipRable createClip(BridgeContext ctx,
                                Element clipElement,
                                Element clipedElement,
                                GraphicsNode clipedNode) {
        String s;
        AffineTransform Tx;
        s = clipElement.getAttributeNS(null, SVG_TRANSFORM_ATTRIBUTE);
        if (s.length() != 0) {
            Tx = SVGUtilities.convertTransform
                (clipElement, SVG_TRANSFORM_ATTRIBUTE, s, ctx);
        } else {
            Tx = new AffineTransform();
        }
        short coordSystemType;
        s = clipElement.getAttributeNS(null, SVG_CLIP_PATH_UNITS_ATTRIBUTE);
        if (s.length() == 0) {
            coordSystemType = SVGUtilities.USER_SPACE_ON_USE;
        } else {
            coordSystemType = SVGUtilities.parseCoordinateSystem
                (clipElement, SVG_CLIP_PATH_UNITS_ATTRIBUTE, s, ctx);
        }
        if (coordSystemType == SVGUtilities.OBJECT_BOUNDING_BOX) {
            Tx = SVGUtilities.toObjectBBox(Tx, clipedNode);
        }
        Area clipPath = new Area();
        GVTBuilder builder = ctx.getGVTBuilder();
        boolean hasChildren = false;
        for(Node node = clipElement.getFirstChild();
            node != null;
            node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element child = (Element)node;
            GraphicsNode clipNode = builder.build(ctx, child) ;
            if (clipNode == null) {
                continue;
            }
            hasChildren = true;
            if (child instanceof SVGOMUseElement) {
                Node shadowChild
                    = ((SVGOMUseElement) child).getCSSFirstChild();
                if (shadowChild != null
                        && shadowChild.getNodeType() == Node.ELEMENT_NODE) {
                    child = (Element) shadowChild;
                }
            }
            int wr = CSSUtilities.convertClipRule(child);
            GeneralPath path = new GeneralPath(clipNode.getOutline());
            path.setWindingRule(wr);
            AffineTransform at = clipNode.getTransform();
            if (at == null)  at = Tx;
            else             at.preConcatenate(Tx);
            Shape outline = at.createTransformedShape(path);
            ShapeNode outlineNode = new ShapeNode();
            outlineNode.setShape(outline);
            ClipRable clip = CSSUtilities.convertClipPath(child,
                                                          outlineNode,
                                                          ctx);
            if (clip != null) {
                Area area = new Area(outline);
                area.subtract(new Area(clip.getClipPath()));
                outline = area;
            }
            clipPath.add(new Area(outline));
        }
        if (!hasChildren) {
            return null; 
        }
        ShapeNode clipPathNode = new ShapeNode();
        clipPathNode.setShape(clipPath);
        ClipRable clipElementClipPath =
            CSSUtilities.convertClipPath(clipElement, clipPathNode, ctx);
        if (clipElementClipPath != null) {
            clipPath.subtract(new Area(clipElementClipPath.getClipPath()));
        }
        Filter filter = clipedNode.getFilter();
        if (filter == null) {
            filter = clipedNode.getGraphicsNodeRable(true);
        }
        boolean useAA = false;
        RenderingHints hints;
        hints = CSSUtilities.convertShapeRendering(clipElement, null);
        if (hints != null) {
            Object o = hints.get(RenderingHints.KEY_ANTIALIASING);
            useAA = (o == RenderingHints.VALUE_ANTIALIAS_ON);
        }
        return new ClipRable8Bit(filter, clipPath, useAA);
    }
}
