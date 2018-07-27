package org.apache.batik.bridge;
import org.apache.batik.util.SVGConstants;
public abstract class AbstractSVGBridge implements Bridge, SVGConstants {
    protected AbstractSVGBridge() {}
    public String getNamespaceURI() {
        return SVG_NAMESPACE_URI;
    }
    public Bridge getInstance() {
        return this;
    }
}
