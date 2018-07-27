package org.apache.batik.bridge.svg12;
import org.apache.batik.bridge.AbstractGraphicsNodeBridge;
import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg12.XBLOMContentElement;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XBLConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class XBLContentElementBridge extends AbstractGraphicsNodeBridge {
    protected ContentChangedListener contentChangedListener;
    protected ContentManager contentManager;
    public XBLContentElementBridge() {
    }
    public String getLocalName() {
        return XBLConstants.XBL_CONTENT_TAG;
    }
    public String getNamespaceURI() {
        return XBLConstants.XBL_NAMESPACE_URI;
    }
    public Bridge getInstance() {
        return new XBLContentElementBridge();
    }
    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
        CompositeGraphicsNode gn = buildCompositeGraphicsNode(ctx, e, null);
        return gn;
    }
    public CompositeGraphicsNode buildCompositeGraphicsNode
        (BridgeContext ctx, Element e, CompositeGraphicsNode cgn) {
        XBLOMContentElement content = (XBLOMContentElement) e;
        AbstractDocument doc = (AbstractDocument) e.getOwnerDocument();
        DefaultXBLManager xm = (DefaultXBLManager) doc.getXBLManager();
        contentManager = xm.getContentManager(e);
        if (cgn == null) {
            cgn = new CompositeGraphicsNode();
            associateSVGContext(ctx, e, cgn);
        } else {
            int s = cgn.size();
            for (int i = 0; i < s; i++) {
                cgn.remove(0);
            }
        }
        GVTBuilder builder = ctx.getGVTBuilder();
        NodeList nl = contentManager.getSelectedContent(content);
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    GraphicsNode gn = builder.build(ctx, (Element) n);
                    if (gn != null) {
                        cgn.add(gn);
                    }
                }
            }
        }
        if (ctx.isDynamic()) {
            if (contentChangedListener == null) {
                contentChangedListener = new ContentChangedListener();
                contentManager.addContentSelectionChangedListener
                    (content, contentChangedListener);
            }
        }
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
        return false;
    }
    public void dispose() {
        super.dispose();
        if (contentChangedListener != null) {
            contentManager.removeContentSelectionChangedListener
                ((XBLOMContentElement) e, contentChangedListener);
        }
    }
    protected class ContentChangedListener
            implements ContentSelectionChangedListener {
        public void contentSelectionChanged(ContentSelectionChangedEvent csce) {
            buildCompositeGraphicsNode(ctx, e, (CompositeGraphicsNode) node);
        }
    }
}
