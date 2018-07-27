package org.apache.batik.bridge;
import java.net.URL;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.AbstractDocument;
public class Location implements org.w3c.dom.Location {
    private BridgeContext bridgeContext;
    public Location(BridgeContext ctx) {
        bridgeContext = ctx;
    }
    public void assign(String url) {
        ((UserAgent)bridgeContext.getUserAgent()).loadDocument(url);
    }
    public void reload() {
        String url = ((AbstractDocument) bridgeContext.getDocument())
                    .getDocumentURI();
        ((UserAgent)bridgeContext.getUserAgent()).loadDocument(url);
    }
    public String toString() {
        return ((AbstractDocument) bridgeContext.getDocument())
                    .getDocumentURI();
    }
}
