package org.apache.batik.svggen;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.StringWriter;
import java.io.Writer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.test.AbstractTest;
import org.apache.batik.test.TestReport;
public class Bug21259 extends AbstractTest{
    public TestReport runImpl() throws Exception {
        Document document = 
            SVGDOMImplementation.getDOMImplementation()
            .createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI , "svg", null);
        SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
        ctx.setComment("Test");
        SVGGraphics2D graphics = new SVGGraphics2D(ctx, false);
        graphics.setSVGCanvasSize(new Dimension(600, 400));
        graphics.setColor(Color.red);
        graphics.setBackground(Color.black);
        graphics.fill(new Rectangle(0,0,100,100));
        Element root = document.getDocumentElement();
        graphics.getRoot(root);
        Writer writer = new StringWriter();
        graphics.stream(root, writer);
        assertTrue(root.getParentNode() == document);
        return reportSuccess();
    }
}
