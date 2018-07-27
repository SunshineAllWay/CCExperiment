package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGZoomAndPan;
public class SVGZoomAndPanSupport implements SVGConstants {
    protected SVGZoomAndPanSupport() {
    }
    public static void setZoomAndPan(Element elt, short val)
        throws DOMException {
        switch (val) {
        case SVGZoomAndPan.SVG_ZOOMANDPAN_DISABLE:
            elt.setAttributeNS(null, SVG_ZOOM_AND_PAN_ATTRIBUTE,
                               SVG_DISABLE_VALUE);
            break;
        case SVGZoomAndPan.SVG_ZOOMANDPAN_MAGNIFY:
            elt.setAttributeNS(null, SVG_ZOOM_AND_PAN_ATTRIBUTE,
                               SVG_MAGNIFY_VALUE);
            break;
        default:
            throw ((AbstractNode)elt).createDOMException
                (DOMException.INVALID_MODIFICATION_ERR,
                 "zoom.and.pan",
                 new Object[] { new Integer(val) });
        }
    }
    public static short getZoomAndPan(Element elt) {
        String s = elt.getAttributeNS(null, SVG_ZOOM_AND_PAN_ATTRIBUTE);
        if (s.equals(SVG_MAGNIFY_VALUE)) {
            return SVGZoomAndPan.SVG_ZOOMANDPAN_MAGNIFY;
        }
        return SVGZoomAndPan.SVG_ZOOMANDPAN_DISABLE;
    }
}
