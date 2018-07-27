package org.apache.batik.svggen;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import org.apache.batik.bridge.BaseScriptingEnvironment;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.test.AbstractTest;
import org.apache.batik.test.TestReport;
public class ShowGraphics2DOutput extends AbstractTest {
    public TestReport runImpl() throws Exception {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        SVGDocument doc = (SVGDocument)impl.createDocument(svgNS, "svg", null);
        SVGGraphics2D g = new SVGGraphics2D(doc);
        Shape circle = new Ellipse2D.Double(0,0,50,50);
        g.setPaint(Color.red);
        g.fill(circle);
        g.translate(60,0);
        g.setPaint(Color.green);
        g.fill(circle);
        g.translate(60,0);
        g.setPaint(Color.blue);
        g.fill(circle);
        g.setSVGCanvasSize(new Dimension(180,50));
        Element root = doc.getDocumentElement();
        g.getRoot(root);
        root.setAttribute("onload", "System.out.println('hello')");
        TestUserAgent userAgent = new TestUserAgent();
        GVTBuilder builder = new GVTBuilder();
        BridgeContext ctx = new BridgeContext(userAgent);
        ctx.setDynamic(true);
        builder.build(ctx, doc);
        BaseScriptingEnvironment scriptEnvironment 
            = new BaseScriptingEnvironment(ctx);
        scriptEnvironment.loadScripts();
        scriptEnvironment.dispatchSVGLoadEvent();
        if (!userAgent.failed) {
            return reportSuccess();
        } else {
            return reportError("Got exception while processing document");
        }
    }
    class TestUserAgent extends UserAgentAdapter {
        boolean failed;
        public void displayError(Exception e) {
            failed = true;
        } 
    }
}
