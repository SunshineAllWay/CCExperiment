package org.apache.batik.bridge;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.ext.awt.image.renderable.ClipRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.Marker;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
public class SVGMarkerElementBridge extends AnimatableGenericSVGBridge
        implements MarkerBridge, ErrorConstants {
    protected SVGMarkerElementBridge() {}
    public String getLocalName() {
        return SVG_MARKER_TAG;
    }
    public Marker createMarker(BridgeContext ctx,
                               Element markerElement,
                               Element paintedElement) {
        GVTBuilder builder = ctx.getGVTBuilder();
        CompositeGraphicsNode markerContentNode
            = new CompositeGraphicsNode();
        boolean hasChildren = false;
        for(Node n = markerElement.getFirstChild();
            n != null;
            n = n.getNextSibling()) {
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element child = (Element)n;
            GraphicsNode markerNode = builder.build(ctx, child) ;
            if (markerNode == null) {
                continue; 
            }
            hasChildren = true;
            markerContentNode.getChildren().add(markerNode);
        }
        if (!hasChildren) {
            return null; 
        }
        String s;
        UnitProcessor.Context uctx
            = UnitProcessor.createContext(ctx, paintedElement);
        float markerWidth = 3;
        s = markerElement.getAttributeNS(null, SVG_MARKER_WIDTH_ATTRIBUTE);
        if (s.length() != 0) {
            markerWidth = UnitProcessor.svgHorizontalLengthToUserSpace
                (s, SVG_MARKER_WIDTH_ATTRIBUTE, uctx);
        }
        if (markerWidth == 0) {
            return null;
        }
        float markerHeight = 3;
        s = markerElement.getAttributeNS(null, SVG_MARKER_HEIGHT_ATTRIBUTE);
        if (s.length() != 0) {
            markerHeight = UnitProcessor.svgVerticalLengthToUserSpace
                (s, SVG_MARKER_HEIGHT_ATTRIBUTE, uctx);
        }
        if (markerHeight == 0) {
            return null;
        }
        double orient;
        s = markerElement.getAttributeNS(null, SVG_ORIENT_ATTRIBUTE);
        if (s.length() == 0) {
            orient = 0;
        } else if (SVG_AUTO_VALUE.equals(s)) {
            orient = Double.NaN;
        } else {
            try {
                orient = SVGUtilities.convertSVGNumber(s);
            } catch (NumberFormatException nfEx ) {
                throw new BridgeException
                    (ctx, markerElement, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                     new Object [] {SVG_ORIENT_ATTRIBUTE, s});
            }
        }
        Value val = CSSUtilities.getComputedStyle
            (paintedElement, SVGCSSEngine.STROKE_WIDTH_INDEX);
        float strokeWidth = val.getFloatValue();
        short unitsType;
        s = markerElement.getAttributeNS(null, SVG_MARKER_UNITS_ATTRIBUTE);
        if (s.length() == 0) {
            unitsType = SVGUtilities.STROKE_WIDTH;
        } else {
            unitsType = SVGUtilities.parseMarkerCoordinateSystem
                (markerElement, SVG_MARKER_UNITS_ATTRIBUTE, s, ctx);
        }
        AffineTransform markerTxf;
        if (unitsType == SVGUtilities.STROKE_WIDTH) {
            markerTxf = new AffineTransform();
            markerTxf.scale(strokeWidth, strokeWidth);
        } else {
            markerTxf = new AffineTransform();
        }
        AffineTransform preserveAspectRatioTransform
            = ViewBox.getPreserveAspectRatioTransform(markerElement,
                                                      markerWidth,
                                                      markerHeight, ctx);
        if (preserveAspectRatioTransform == null) {
            return null;
        } else {
            markerTxf.concatenate(preserveAspectRatioTransform);
        }
        markerContentNode.setTransform(markerTxf);
        if (CSSUtilities.convertOverflow(markerElement)) { 
            Rectangle2D markerClip;
            float [] offsets = CSSUtilities.convertClip(markerElement);
            if (offsets == null) { 
                markerClip
                    = new Rectangle2D.Float(0,
                                            0,
                                            strokeWidth * markerWidth,
                                            strokeWidth * markerHeight);
            } else { 
                markerClip = new Rectangle2D.Float
                    (offsets[3],
                     offsets[0],
                     strokeWidth * markerWidth - offsets[1] - offsets[3],
                     strokeWidth * markerHeight - offsets[2] - offsets[0]);
            }
            CompositeGraphicsNode comp = new CompositeGraphicsNode();
            comp.getChildren().add(markerContentNode);
            Filter clipSrc = comp.getGraphicsNodeRable(true);
            comp.setClip(new ClipRable8Bit(clipSrc, markerClip));
            markerContentNode = comp;
        }
        float refX = 0;
        s = markerElement.getAttributeNS(null, SVG_REF_X_ATTRIBUTE);
        if (s.length() != 0) {
            refX = UnitProcessor.svgHorizontalCoordinateToUserSpace
                (s, SVG_REF_X_ATTRIBUTE, uctx);
        }
        float refY = 0;
        s = markerElement.getAttributeNS(null, SVG_REF_Y_ATTRIBUTE);
        if (s.length() != 0) {
            refY = UnitProcessor.svgVerticalCoordinateToUserSpace
                (s, SVG_REF_Y_ATTRIBUTE, uctx);
        }
        float[] ref = {refX, refY};
        markerTxf.transform(ref, 0, ref, 0, 1);
        Marker marker = new Marker(markerContentNode,
                                   new Point2D.Float(ref[0], ref[1]),
                                   orient);
        return marker;
    }
}
