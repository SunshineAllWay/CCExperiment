package org.apache.batik.bridge.svg12;
import org.apache.batik.bridge.AbstractGraphicsNodeBridge;
import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.SVGUtilities;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XBLConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.MutationEvent;
public class XBLShadowTreeElementBridge extends AbstractGraphicsNodeBridge {
    public XBLShadowTreeElementBridge() {}
    public String getLocalName() {
        return XBLConstants.XBL_SHADOW_TREE_TAG;
    }
    public String getNamespaceURI() {
        return XBLConstants.XBL_NAMESPACE_URI;
    }
    public Bridge getInstance() {
        return new XBLShadowTreeElementBridge();
    }
    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
        if (!SVGUtilities.matchUserAgent(e, ctx.getUserAgent())) {
            return null;
        }
        CompositeGraphicsNode cgn = new CompositeGraphicsNode();
        associateSVGContext(ctx, e, cgn);
        return cgn;
    }
    protected GraphicsNode instantiateGraphicsNode() {
        return null;
    }
    public void buildGraphicsNode(BridgeContext ctx,
                                  Element e,
                                  GraphicsNode node) {
        initializeDynamicSupport(ctx, e, node);
    }
    public boolean getDisplay(Element e) {
        return true;
    }
    public boolean isComposite() {
        return true;
    }
    public void handleDOMNodeInsertedEvent(MutationEvent evt) {
        if (evt.getTarget() instanceof Element) {
            handleElementAdded((CompositeGraphicsNode)node, 
                               e, 
                               (Element)evt.getTarget());
        }
    }
    public void handleElementAdded(CompositeGraphicsNode gn, 
                                   Node parent, 
                                   Element childElt) {
        GVTBuilder builder = ctx.getGVTBuilder();
        GraphicsNode childNode = builder.build(ctx, childElt);
        if (childNode == null) {
            return; 
        }
        int idx = -1;
        for(Node ps = childElt.getPreviousSibling(); ps != null;
            ps = ps.getPreviousSibling()) {
            if (ps.getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element pse = (Element)ps;
            GraphicsNode psgn = ctx.getGraphicsNode(pse);
            while ((psgn != null) && (psgn.getParent() != gn)) {
                psgn = psgn.getParent();
            }
            if (psgn == null)
                continue;
            idx = gn.indexOf(psgn);
            if (idx == -1)
                continue;
            break;
        }
        idx++; 
        gn.add(idx, childNode);
    }
}
