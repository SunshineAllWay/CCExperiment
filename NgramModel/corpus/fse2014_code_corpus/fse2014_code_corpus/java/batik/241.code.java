package org.apache.batik.bridge;
import java.awt.geom.AffineTransform;
import java.util.StringTokenizer;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.svg.SVGOMAnimatedRect;
import org.apache.batik.parser.AWTTransformProducer;
import org.apache.batik.parser.FragmentIdentifierHandler;
import org.apache.batik.parser.FragmentIdentifierParser;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PreserveAspectRatioParser;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedPreserveAspectRatio;
import org.w3c.dom.svg.SVGAnimatedRect;
import org.w3c.dom.svg.SVGPreserveAspectRatio;
import org.w3c.dom.svg.SVGRect;
public abstract class ViewBox implements SVGConstants, ErrorConstants {
    protected ViewBox() { }
    public static AffineTransform getViewTransform(String ref,
                                                   Element e,
                                                   float w,
                                                   float h,
                                                   BridgeContext ctx) {
        if (ref == null || ref.length() == 0) {
            return getPreserveAspectRatioTransform(e, w, h, ctx);
        }
        ViewHandler vh = new ViewHandler();
        FragmentIdentifierParser p = new FragmentIdentifierParser();
        p.setFragmentIdentifierHandler(vh);
        p.parse(ref);
        Element viewElement = e;
        if (vh.hasId) {
            Document document = e.getOwnerDocument();
            viewElement = document.getElementById(vh.id);
        }
        if (viewElement == null) {
            throw new BridgeException(ctx, e, ERR_URI_MALFORMED,
                                      new Object[] {ref});
        }
        if (!(viewElement.getNamespaceURI().equals(SVG_NAMESPACE_URI)
              && viewElement.getLocalName().equals(SVG_VIEW_TAG))) {
            viewElement = null;
        }
        Element ancestorSVG = getClosestAncestorSVGElement(e);
        float[] vb;
        if (vh.hasViewBox) {
            vb = vh.viewBox;
        } else {
            Element elt;
            if (DOMUtilities.isAttributeSpecifiedNS
                    (viewElement, null, SVG_VIEW_BOX_ATTRIBUTE)) {
                elt = viewElement;
            } else {
                elt = ancestorSVG;
            }
            String viewBoxStr = elt.getAttributeNS(null, SVG_VIEW_BOX_ATTRIBUTE);
            vb = parseViewBoxAttribute(elt, viewBoxStr, ctx);
        }
        short align;
        boolean meet;
        if (vh.hasPreserveAspectRatio) {
            align = vh.align;
            meet = vh.meet;
        } else {
            Element elt;
            if (DOMUtilities.isAttributeSpecifiedNS
                    (viewElement, null, SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE)) {
                elt = viewElement;
            } else {
                elt = ancestorSVG;
            }
            String aspectRatio =
                elt.getAttributeNS(null, SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE);
            PreserveAspectRatioParser pp = new PreserveAspectRatioParser();
            ViewHandler ph = new ViewHandler();
            pp.setPreserveAspectRatioHandler(ph);
            try {
                pp.parse(aspectRatio);
            } catch (ParseException pEx) {
                throw new BridgeException
                    (ctx, elt, pEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                     new Object[] {SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE,
                                       aspectRatio, pEx });
            }
            align = ph.align;
            meet = ph.meet;
        }
        AffineTransform transform
            = getPreserveAspectRatioTransform(vb, align, meet, w, h);
        if (vh.hasTransform) {
            transform.concatenate(vh.getAffineTransform());
        }
        return transform;
    }
    private static Element getClosestAncestorSVGElement(Element e) {
        for  (Node n = e;
              n != null && n.getNodeType() == Node.ELEMENT_NODE;
              n = n.getParentNode()) {
            Element tmp = (Element)n;
            if (tmp.getNamespaceURI().equals(SVG_NAMESPACE_URI)
                && tmp.getLocalName().equals(SVG_SVG_TAG)) {
                return tmp;
            }
        }
        return null;
    }
    public static AffineTransform getPreserveAspectRatioTransform(Element e,
                                                                  float w,
                                                                  float h) {
        return getPreserveAspectRatioTransform(e, w, h, null);
    }
    public static AffineTransform getPreserveAspectRatioTransform
            (Element e, float w, float h, BridgeContext ctx) {
        String viewBox
            = e.getAttributeNS(null, SVG_VIEW_BOX_ATTRIBUTE);
        String aspectRatio
            = e.getAttributeNS(null, SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE);
        return getPreserveAspectRatioTransform(e, viewBox, aspectRatio, w, h,
                                               ctx);
    }
    public static
        AffineTransform getPreserveAspectRatioTransform(Element e,
                                                        String viewBox,
                                                        String aspectRatio,
                                                        float w,
                                                        float h,
                                                        BridgeContext ctx) {
        if (viewBox.length() == 0) {
            return new AffineTransform();
        }
        float[] vb = parseViewBoxAttribute(e, viewBox, ctx);
        PreserveAspectRatioParser p = new PreserveAspectRatioParser();
        ViewHandler ph = new ViewHandler();
        p.setPreserveAspectRatioHandler(ph);
        try {
            p.parse(aspectRatio);
        } catch (ParseException pEx ) {
            throw new BridgeException
                (ctx, e, pEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] {SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE,
                                   aspectRatio, pEx });
        }
        return getPreserveAspectRatioTransform(vb, ph.align, ph.meet, w, h);
    }
    public static
        AffineTransform getPreserveAspectRatioTransform(Element e,
                                                        float[] vb,
                                                        float w,
                                                        float h,
                                                        BridgeContext ctx) {
        String aspectRatio
            = e.getAttributeNS(null, SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE);
        PreserveAspectRatioParser p = new PreserveAspectRatioParser();
        ViewHandler ph = new ViewHandler();
        p.setPreserveAspectRatioHandler(ph);
        try {
            p.parse(aspectRatio);
        } catch (ParseException pEx ) {
            throw new BridgeException
                (ctx, e, pEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] {SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE,
                                   aspectRatio, pEx });
        }
        return getPreserveAspectRatioTransform(vb, ph.align, ph.meet, w, h);
    }
    public static AffineTransform getPreserveAspectRatioTransform
            (Element e, float[] vb, float w, float h,
             SVGAnimatedPreserveAspectRatio aPAR, BridgeContext ctx) {
        try {
            SVGPreserveAspectRatio pAR = aPAR.getAnimVal();
            short align = pAR.getAlign();
            boolean meet = pAR.getMeetOrSlice() ==
                SVGPreserveAspectRatio.SVG_MEETORSLICE_MEET;
            return getPreserveAspectRatioTransform(vb, align, meet, w, h);
        } catch (LiveAttributeException ex) {
            throw new BridgeException(ctx, ex);
        }
    }
    public static AffineTransform getPreserveAspectRatioTransform
            (Element e, SVGAnimatedRect aViewBox,
             SVGAnimatedPreserveAspectRatio aPAR,
             float w, float h, BridgeContext ctx) {
        if (!((SVGOMAnimatedRect) aViewBox).isSpecified()) {
            return new AffineTransform();
        }
        SVGRect viewBox = aViewBox.getAnimVal();
        float[] vb = new float[] { viewBox.getX(), viewBox.getY(),
                                   viewBox.getWidth(), viewBox.getHeight() };
        return getPreserveAspectRatioTransform(e, vb, w, h, aPAR, ctx);
    }
    public static float[] parseViewBoxAttribute(Element e, String value,
                                                BridgeContext ctx) {
        if (value.length() == 0) {
            return null;
        }
        int i = 0;
        float[] vb = new float[4];
        StringTokenizer st = new StringTokenizer(value, " ,");
        try {
            while (i < 4 && st.hasMoreTokens()) {
                vb[i] = Float.parseFloat(st.nextToken());
                i++;
            }
        } catch (NumberFormatException nfEx ) {
            throw new BridgeException
                (ctx, e, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] {SVG_VIEW_BOX_ATTRIBUTE, value, nfEx });
        }
        if (i != 4) {
            throw new BridgeException
                (ctx, e, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] {SVG_VIEW_BOX_ATTRIBUTE, value});
        }
        if (vb[2] < 0 || vb[3] < 0) {
            throw new BridgeException
                (ctx, e, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] {SVG_VIEW_BOX_ATTRIBUTE, value});
        }
        if (vb[2] == 0 || vb[3] == 0) {
            return null; 
        }
        return vb;
    }
    public static
        AffineTransform getPreserveAspectRatioTransform(float [] vb,
                                                        short align,
                                                        boolean meet,
                                                        float w,
                                                        float h) {
        if (vb == null) {
            return new AffineTransform();
        }
        AffineTransform result = new AffineTransform();
        float vpar  = vb[2] / vb[3];
        float svgar = w / h;
        if (align == SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_NONE) {
            result.scale(w / vb[2], h / vb[3]);
            result.translate(-vb[0], -vb[1]);
        } else if (vpar < svgar && meet || vpar >= svgar && !meet) {
            float sf = h / vb[3];
            result.scale(sf, sf);
            switch (align) {
            case SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMINYMIN:
            case SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMINYMID:
            case SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMINYMAX:
                result.translate(-vb[0], -vb[1]);
                break;
            case SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMIN:
            case SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMID:
            case SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMAX:
                result.translate(-vb[0] - (vb[2] - w * vb[3] / h) / 2 , -vb[1]);
                break;
            default:
                result.translate(-vb[0] - (vb[2] - w * vb[3] / h) , -vb[1]);
            }
        } else {
            float sf = w / vb[2];
            result.scale(sf, sf);
            switch (align) {
            case SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMINYMIN:
            case SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMIN:
            case SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMAXYMIN:
                result.translate(-vb[0], -vb[1]);
                break;
            case SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMINYMID:
            case SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMID:
            case SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMAXYMID:
                result.translate(-vb[0], -vb[1] - (vb[3] - h * vb[2] / w) / 2);
                break;
            default:
                result.translate(-vb[0], -vb[1] - (vb[3] - h * vb[2] / w));
            }
        }
        return result;
    }
    protected static class ViewHandler extends AWTTransformProducer
        implements FragmentIdentifierHandler {
        protected ViewHandler() { }
        public boolean hasTransform;
        public void endTransformList() throws ParseException {
            super.endTransformList();
            hasTransform = true;
        }
        public boolean hasId;
        public boolean hasViewBox;
        public boolean hasViewTargetParams;
        public boolean hasZoomAndPanParams;
        public String id;
        public float [] viewBox;
        public String viewTargetParams;
        public boolean isMagnify;
        public void startFragmentIdentifier() throws ParseException { }
        public void idReference(String s) throws ParseException {
            id = s;
            hasId = true;
        }
        public void viewBox(float x, float y, float width, float height)
            throws ParseException {
            hasViewBox = true;
            viewBox = new float[4];
            viewBox[0] = x;
            viewBox[1] = y;
            viewBox[2] = width;
            viewBox[3] = height;
        }
        public void startViewTarget() throws ParseException { }
        public void viewTarget(String name) throws ParseException {
            viewTargetParams = name;
            hasViewTargetParams = true;
        }
        public void endViewTarget() throws ParseException { }
        public void zoomAndPan(boolean magnify) {
            isMagnify = magnify;
            hasZoomAndPanParams = true;
        }
        public void endFragmentIdentifier() throws ParseException { }
        public boolean hasPreserveAspectRatio;
        public short align;
        public boolean meet = true;
        public void startPreserveAspectRatio() throws ParseException { }
        public void none() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_NONE;
        }
        public void xMaxYMax() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMAXYMAX;
        }
        public void xMaxYMid() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMAXYMID;
        }
        public void xMaxYMin() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMAXYMIN;
        }
        public void xMidYMax() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMAX;
        }
        public void xMidYMid() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMID;
        }
        public void xMidYMin() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMIDYMIN;
        }
        public void xMinYMax() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMINYMAX;
        }
        public void xMinYMid() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMINYMID;
        }
        public void xMinYMin() throws ParseException {
            align = SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_XMINYMIN;
        }
        public void meet() throws ParseException {
            meet = true;
        }
        public void slice() throws ParseException {
            meet = false;
        }
        public void endPreserveAspectRatio() throws ParseException {
            hasPreserveAspectRatio = true;
        }
    }
}
