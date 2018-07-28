package org.apache.batik.svggen;
import java.awt.geom.Rectangle2D;
import org.apache.batik.test.AbstractTest;
import org.apache.batik.test.TestReport;
import org.apache.batik.dom.GenericDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
public class DoubleString extends AbstractTest {
    public TestReport runImpl() throws Exception {
        DOMImplementation domImpl =
            GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument(null, "svg", null);
        SVGGraphics2D g = new SVGGraphics2D(document);
        Rectangle2D r = new Rectangle2D.Float(0.5f, 0.5f, 2.33f, 2.33f);
        g.fill(r);
        return reportSuccess();
    }
}
