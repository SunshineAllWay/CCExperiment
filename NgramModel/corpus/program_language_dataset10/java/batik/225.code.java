package org.apache.batik.bridge;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGTests;
public class SVGSwitchElementBridge extends SVGGElementBridge {
    protected Element selectedChild;
    public SVGSwitchElementBridge() {}
    public String getLocalName() {
        return SVG_SWITCH_TAG;
    }
    public Bridge getInstance() {
        return new SVGSwitchElementBridge();
    }
    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
        GraphicsNode refNode = null;
        GVTBuilder builder = ctx.getGVTBuilder();
        selectedChild = null;
        for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element ref = (Element)n;
                if (n instanceof SVGTests &&
                        SVGUtilities.matchUserAgent(ref, ctx.getUserAgent())) {
                    selectedChild = ref;
                    refNode = builder.build(ctx, ref);
                    break;
                }
            }
        }
        if (refNode == null) {
            return null;
        }
        CompositeGraphicsNode group =
            (CompositeGraphicsNode) super.createGraphicsNode(ctx, e);
        if (group == null) {
            return null;
        }
        group.add(refNode);
        return group;
    }
    public boolean isComposite() {
        return false;
    }
    public void dispose() {
        selectedChild = null;
        super.dispose();
    }
    protected void handleElementAdded(CompositeGraphicsNode gn, 
                                      Node parent, 
                                      Element childElt) {
        for (Node n = childElt.getPreviousSibling(); n
                != null;
                n = n.getPreviousSibling()) {
            if (n == childElt) {
                return;
            }
        }
        if (childElt instanceof SVGTests
                && SVGUtilities.matchUserAgent(childElt, ctx.getUserAgent())) {
            if (selectedChild != null) {
                gn.remove(0);
                disposeTree(selectedChild);
            }
            selectedChild = childElt;
            GVTBuilder builder = ctx.getGVTBuilder();
            GraphicsNode refNode = builder.build(ctx, childElt);
            if (refNode != null) {
                gn.add(refNode);
            }
        }
    }
    protected void handleChildElementRemoved(Element e) {
        CompositeGraphicsNode gn = (CompositeGraphicsNode) node;
        if (selectedChild == e) {
            gn.remove(0);
            disposeTree(selectedChild);
            selectedChild = null;
            GraphicsNode refNode = null;
            GVTBuilder builder = ctx.getGVTBuilder();
            for (Node n = e.getNextSibling();
                    n != null;
                    n = n.getNextSibling()) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element ref = (Element) n;
                    if (n instanceof SVGTests &&
                            SVGUtilities.matchUserAgent
                                (ref, ctx.getUserAgent())) {
                        refNode = builder.build(ctx, ref);
                        selectedChild = ref;
                        break;
                    }
                }
            }
            if (refNode != null) {
                gn.add(refNode);
            }
        }
    }
}
