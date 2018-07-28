package org.apache.batik.bridge.svg12;
import java.util.Collections;
import java.util.Iterator;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.SVGBridgeExtension;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.SVG12Constants;
import org.apache.batik.util.XBLConstants;
import org.w3c.dom.Element;
public class SVG12BridgeExtension extends SVGBridgeExtension {
    public float getPriority() { return 0f; }
    public Iterator getImplementedExtensions() {
        return Collections.EMPTY_LIST.iterator();
    }
    public String getAuthor() {
        return "The Apache Batik Team.";
    }
    public String getContactAddress() {
        return "batik-dev@xmlgraphics.apache.org";
    }
    public String getURL() {
        return "http://xml.apache.org/batik";
    }
    public String getDescription() {
        return "The required SVG 1.2 tags";
    }
    public void registerTags(BridgeContext ctx) {
        super.registerTags(ctx);
        ctx.putBridge(new SVGFlowRootElementBridge());
        ctx.putBridge(new SVGMultiImageElementBridge());
        ctx.putBridge(new SVGSolidColorElementBridge());
        ctx.putBridge(new SVG12TextElementBridge());
        ctx.putBridge(new XBLShadowTreeElementBridge());
        ctx.putBridge(new XBLContentElementBridge());
        ctx.setDefaultBridge(new BindableElementBridge());
        ctx.putReservedNamespaceURI(null);
        ctx.putReservedNamespaceURI(SVGConstants.SVG_NAMESPACE_URI);
        ctx.putReservedNamespaceURI(XBLConstants.XBL_NAMESPACE_URI);
    }
    public boolean isDynamicElement(Element e) {
        String ns = e.getNamespaceURI();
        if (XBLConstants.XBL_NAMESPACE_URI.equals(ns)) {
            return true;
        }
        if (!SVGConstants.SVG_NAMESPACE_URI.equals(ns)) {
            return false;
        }
        String ln = e.getLocalName();
        if (ln.equals(SVGConstants.SVG_SCRIPT_TAG)
                || ln.equals(SVG12Constants.SVG_HANDLER_TAG)
                || ln.startsWith("animate")
                || ln.equals("set")) {
            return true;
        }
        return false;
    }
}
