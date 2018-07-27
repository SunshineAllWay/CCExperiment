package org.apache.batik.swing;
import org.apache.batik.test.svg.JSVGRenderingAccuracyTest;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.dom.GenericDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
public class SetSVGDocumentTest extends JSVGRenderingAccuracyTest {
    public SetSVGDocumentTest() {
    }
    protected String[] breakSVGFile(String svgFile){
        if(svgFile == null) {
            throw new IllegalArgumentException(svgFile);
        }
        String [] ret = new String[3];
        ret[0] = "test-resources/org/apache/batik/test/svg/";
        ret[1] = "SetSVGDocumentTest";
        ret[2] = ".svg";
        return ret;
    }
    public boolean canvasInit(JSVGCanvas canvas) {
        DOMImplementation impl = 
            GenericDOMImplementation.getDOMImplementation();
        Document doc = impl.createDocument(SVGConstants.SVG_NAMESPACE_URI, 
                                           SVGConstants.SVG_SVG_TAG, null);
        Element e = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, 
                                        SVGConstants.SVG_RECT_TAG);
        e.setAttribute("x", "10");
        e.setAttribute("y", "10");
        e.setAttribute("width", "100");
        e.setAttribute("height", "50");
        e.setAttribute("fill", "crimson");
        doc.getDocumentElement().appendChild(e);
        e = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, 
                                SVGConstants.SVG_CIRCLE_TAG);
        e.setAttribute("cx", "55");
        e.setAttribute("cy", "35");
        e.setAttribute("r", "30");
        e.setAttribute("fill", "gold");
        doc.getDocumentElement().appendChild(e);
        canvas.setDocument(doc);
        return false; 
    }
    public boolean canvasUpdated(JSVGCanvas canvas) {
        return true;
    }
}
