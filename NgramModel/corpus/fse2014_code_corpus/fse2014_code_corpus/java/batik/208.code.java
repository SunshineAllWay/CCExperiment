package org.apache.batik.bridge;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.events.DOMMouseEvent;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.svg.AbstractSVGAnimatedLength;
import org.apache.batik.dom.svg.AnimatedLiveAttributeValue;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.svg.SVGOMAnimatedPreserveAspectRatio;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.ext.awt.color.ICCColorSpaceExt;
import org.apache.batik.ext.awt.image.renderable.ClipRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.spi.BrokenLinkProvider;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ImageNode;
import org.apache.batik.gvt.RasterImageNode;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.util.HaltingThread;
import org.apache.batik.util.MimeTypeConstants;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGImageElement;
import org.w3c.dom.svg.SVGSVGElement;
public class SVGImageElementBridge extends AbstractGraphicsNodeBridge {
    protected SVGDocument imgDocument;
    protected EventListener listener = null;
    protected BridgeContext subCtx = null;
    protected boolean hitCheckChildren = false;
    public SVGImageElementBridge() {}
    public String getLocalName() {
        return SVG_IMAGE_TAG;
    }
    public Bridge getInstance() {
        return new SVGImageElementBridge();
    }
    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
        ImageNode imageNode = (ImageNode)super.createGraphicsNode(ctx, e);
        if (imageNode == null) {
            return null;
        }
        associateSVGContext(ctx, e, imageNode);
        hitCheckChildren = false;
        GraphicsNode node = buildImageGraphicsNode(ctx,e);
        if (node == null) {
            SVGImageElement ie = (SVGImageElement) e;
            String uriStr = ie.getHref().getAnimVal();
            throw new BridgeException(ctx, e, ERR_URI_IMAGE_INVALID,
                                      new Object[] {uriStr});
        }
        imageNode.setImage(node);
        imageNode.setHitCheckChildren(hitCheckChildren);
        RenderingHints hints = null;
        hints = CSSUtilities.convertImageRendering(e, hints);
        hints = CSSUtilities.convertColorRendering(e, hints);
        if (hints != null)
            imageNode.setRenderingHints(hints);
        return imageNode;
    }
    protected GraphicsNode buildImageGraphicsNode
        (BridgeContext ctx, Element e){
        SVGImageElement ie = (SVGImageElement) e;
        String uriStr = ie.getHref().getAnimVal();
        if (uriStr.length() == 0) {
            throw new BridgeException(ctx, e, ERR_ATTRIBUTE_MISSING,
                                      new Object[] {"xlink:href"});
        }
        if (uriStr.indexOf('#') != -1) {
            throw new BridgeException(ctx, e, ERR_ATTRIBUTE_VALUE_MALFORMED,
                                      new Object[] {"xlink:href", uriStr});
        }
        String baseURI = AbstractNode.getBaseURI(e);
        ParsedURL purl;
        if (baseURI == null) {
            purl = new ParsedURL(uriStr);
        } else {
            purl = new ParsedURL(baseURI, uriStr);
        }
        return createImageGraphicsNode(ctx, e, purl);
    }
    protected GraphicsNode createImageGraphicsNode(BridgeContext ctx,
                                                   Element e,
                                                   ParsedURL purl) {
        Rectangle2D bounds = getImageBounds(ctx, e);
        if ((bounds.getWidth() == 0) || (bounds.getHeight() == 0)) {
            ShapeNode sn = new ShapeNode();
            sn.setShape(bounds);
            return sn;
        }
        SVGDocument svgDoc = (SVGDocument)e.getOwnerDocument();
        String docURL = svgDoc.getURL();
        ParsedURL pDocURL = null;
        if (docURL != null)
            pDocURL = new ParsedURL(docURL);
        UserAgent userAgent = ctx.getUserAgent();
        try {
            userAgent.checkLoadExternalResource(purl, pDocURL);
        } catch (SecurityException secEx ) {
            throw new BridgeException(ctx, e, secEx, ERR_URI_UNSECURE,
                                      new Object[] {purl});
        }
        DocumentLoader loader = ctx.getDocumentLoader();
        ImageTagRegistry reg = ImageTagRegistry.getRegistry();
        ICCColorSpaceExt colorspace = extractColorSpace(e, ctx);
        {
            try {
                Document doc = loader.checkCache(purl.toString());
                if (doc != null) {
                    imgDocument = (SVGDocument)doc;
                    return createSVGImageNode(ctx, e, imgDocument);
                }
            } catch (BridgeException ex) {
                throw ex;
            } catch (Exception ex) {
            }
            Filter img = reg.checkCache(purl, colorspace);
            if (img != null) {
                return createRasterImageNode(ctx, e, img, purl);
            }
        }
        ProtectedStream reference = null;
        try {
            reference = openStream(e, purl);
        } catch (SecurityException secEx ) {
            throw new BridgeException(ctx, e, secEx, ERR_URI_UNSECURE,
                                      new Object[] {purl});
        } catch (IOException ioe) {
            return createBrokenImageNode(ctx, e, purl.toString(),
                                         ioe.getLocalizedMessage());
        }
        {
            Filter img = reg.readURL(reference, purl, colorspace,
                                     false, false);
            if (img != null) {
                try {
                    reference.tie();
                } catch (IOException ioe) {
                }
                return createRasterImageNode(ctx, e, img, purl);
            }
        }
        try {
            reference.retry();
        } catch (IOException ioe) {
            reference.release();
            reference = null;
            try {
                reference = openStream(e, purl);
            } catch (IOException ioe2) {
                return createBrokenImageNode(ctx, e, purl.toString(),
                                             ioe2.getLocalizedMessage());
            }
        }
        try {
            Document doc = loader.loadDocument(purl.toString(), reference);
            reference.release();
            imgDocument = (SVGDocument)doc;
            return createSVGImageNode(ctx, e, imgDocument);
        } catch (BridgeException ex) {
            reference.release();
            throw ex;
        } catch (SecurityException secEx ) {
            reference.release();
            throw new BridgeException(ctx, e, secEx, ERR_URI_UNSECURE,
                                      new Object[] {purl});
        } catch (InterruptedIOException iioe) {
            reference.release();
            if (HaltingThread.hasBeenHalted())
                throw new InterruptedBridgeException();
        } catch (InterruptedBridgeException ibe) {
            reference.release();
            throw ibe;
        } catch (Exception ex) {
        }
        try {
            reference.retry();
        } catch (IOException ioe) {
            reference.release();
            reference = null;
            try {
                reference = openStream(e, purl);
            } catch (IOException ioe2) {
                return createBrokenImageNode(ctx, e, purl.toString(),
                                             ioe2.getLocalizedMessage());
            }
        }
        try {
            Filter img = reg.readURL(reference, purl, colorspace,
                                     true, true);
            if (img != null) {
                return createRasterImageNode(ctx, e, img, purl);
            }
        } finally {
            reference.release();
        }
        return null;
    }
    public static class ProtectedStream extends BufferedInputStream {
        static final int BUFFER_SIZE = 8192;
        ProtectedStream(InputStream is) {
            super(is, BUFFER_SIZE);
            super.mark(BUFFER_SIZE); 
        }
        ProtectedStream(InputStream is, int size) {
            super(is, size);
            super.mark(size); 
        }
        public boolean markSupported() {
            return false;
        }
        public void mark(int sz){
        }
        public void reset() throws IOException {
            throw new IOException("Reset unsupported");
        }
        public synchronized void retry() throws IOException {
            super.reset();
            wasClosed = false;
            isTied = false;
        }
        public synchronized void close() throws IOException {
            wasClosed = true;
            if (isTied) {
                super.close();
            }
        }
        public synchronized void tie() throws IOException {
            isTied = true;
            if (wasClosed) {
                super.close();
            }
        }
        public void release() {
            try {
                super.close();
            } catch (IOException ioe) {
            }
        }
        boolean wasClosed = false;
        boolean isTied = false;
    }
    protected ProtectedStream openStream(Element e, ParsedURL purl)
        throws IOException {
        List mimeTypes = new ArrayList
            (ImageTagRegistry.getRegistry().getRegisteredMimeTypes());
        mimeTypes.addAll(MimeTypeConstants.MIME_TYPES_SVG_LIST);
        InputStream reference = purl.openStream(mimeTypes.iterator());
        return new ProtectedStream(reference);
    }
    protected GraphicsNode instantiateGraphicsNode() {
        return new ImageNode();
    }
    public boolean isComposite() {
        return false;
    }
    protected void initializeDynamicSupport(BridgeContext ctx,
                                            Element e,
                                            GraphicsNode node) {
        if (!ctx.isInteractive())
            return;
        ctx.bind(e, node);
        if (ctx.isDynamic()) {
            this.e = e;
            this.node = node;
            this.ctx = ctx;
            ((SVGOMElement)e).setSVGContext(this);
        }
    }
    public void handleAnimatedAttributeChanged
            (AnimatedLiveAttributeValue alav) {
        try {
            String ns = alav.getNamespaceURI();
            String ln = alav.getLocalName();
            if (ns == null) {
                if (ln.equals(SVG_X_ATTRIBUTE)
                        || ln.equals(SVG_Y_ATTRIBUTE)) {
                    updateImageBounds();
                    return;
                } else if (ln.equals(SVG_WIDTH_ATTRIBUTE)
                        || ln.equals(SVG_HEIGHT_ATTRIBUTE)) {
                    SVGImageElement ie = (SVGImageElement) e;
                    ImageNode imageNode = (ImageNode) node;
                    AbstractSVGAnimatedLength _attr;
                    if (ln.charAt(0) == 'w') {
                        _attr = (AbstractSVGAnimatedLength) ie.getWidth();
                    } else {
                        _attr = (AbstractSVGAnimatedLength) ie.getHeight();
                    }
                    float val = _attr.getCheckedValue();
                    if (val == 0 || imageNode.getImage() instanceof ShapeNode) {
                        rebuildImageNode();
                    } else {
                        updateImageBounds();
                    }
                    return;
                } else if (ln.equals(SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE)) {
                    updateImageBounds();
                    return;
                }
            } else if (ns.equals(XLINK_NAMESPACE_URI)
                    && ln.equals(XLINK_HREF_ATTRIBUTE)) {
                rebuildImageNode();
                return;
            }
        } catch (LiveAttributeException ex) {
            throw new BridgeException(ctx, ex);
        }
        super.handleAnimatedAttributeChanged(alav);
    }
    protected void updateImageBounds() {
        Rectangle2D bounds = getImageBounds(ctx, e);
        GraphicsNode imageNode = ((ImageNode)node).getImage();
        float[] vb = null;
        if (imageNode instanceof RasterImageNode) {
            Rectangle2D imgBounds =
                ((RasterImageNode)imageNode).getImageBounds();
            vb = new float[4];
            vb[0] = 0; 
            vb[1] = 0; 
            vb[2] = (float)imgBounds.getWidth(); 
            vb[3] = (float)imgBounds.getHeight(); 
        } else {
            if (imgDocument != null) {
                Element svgElement = imgDocument.getRootElement();
                String viewBox = svgElement.getAttributeNS
                    (null, SVG_VIEW_BOX_ATTRIBUTE);
                vb = ViewBox.parseViewBoxAttribute(e, viewBox, ctx);
            }
        }
        if (imageNode != null) {
            initializeViewport(ctx, e, imageNode, vb, bounds);
        }
    }
    protected void rebuildImageNode() {
        if ((imgDocument != null) && (listener != null)) {
            NodeEventTarget tgt = (NodeEventTarget)imgDocument.getRootElement();
            tgt.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_CLICK,
                 listener, false);
            tgt.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_KEYDOWN,
                 listener, false);
            tgt.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_KEYPRESS,
                 listener, false);
            tgt.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_KEYUP,
                 listener, false);
            tgt.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEDOWN,
                 listener, false);
            tgt.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEMOVE,
                 listener, false);
            tgt.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOUT,
                 listener, false);
            tgt.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOVER,
                 listener, false);
            tgt.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEUP,
                 listener, false);
            listener = null;
        }
        if (imgDocument != null) {
            SVGSVGElement svgElement = imgDocument.getRootElement();
            disposeTree(svgElement);
        }
        imgDocument = null;
        subCtx = null;
        GraphicsNode inode = buildImageGraphicsNode(ctx,e);
        ImageNode imgNode = (ImageNode)node;
        imgNode.setImage(inode);
        if (inode == null) {
            SVGImageElement ie = (SVGImageElement) e;
            String uriStr = ie.getHref().getAnimVal();
            throw new BridgeException(ctx, e, ERR_URI_IMAGE_INVALID,
                                      new Object[] {uriStr});
        }
    }
    protected void handleCSSPropertyChanged(int property) {
        switch(property) {
        case SVGCSSEngine.IMAGE_RENDERING_INDEX:
        case SVGCSSEngine.COLOR_INTERPOLATION_INDEX:
            RenderingHints hints = CSSUtilities.convertImageRendering(e, null);
            hints = CSSUtilities.convertColorRendering(e, hints);
            if (hints != null) {
                node.setRenderingHints(hints);
            }
            break;
        default:
            super.handleCSSPropertyChanged(property);
        }
    }
    protected GraphicsNode createRasterImageNode(BridgeContext ctx,
                                                 Element       e,
                                                 Filter        img,
                                                 ParsedURL     purl) {
        Rectangle2D bounds = getImageBounds(ctx, e);
        if ((bounds.getWidth() == 0) || (bounds.getHeight() == 0)) {
            ShapeNode sn = new ShapeNode();
            sn.setShape(bounds);
            return sn;
        }
        if (BrokenLinkProvider.hasBrokenLinkProperty(img)) {
            Object o=img.getProperty(BrokenLinkProvider.BROKEN_LINK_PROPERTY);
            String msg = "unknown";
            if (o instanceof String)
                msg = (String)o;
            SVGDocument doc = ctx.getUserAgent().getBrokenLinkDocument
                (e, purl.toString(), msg);
            return createSVGImageNode(ctx, e, doc);
        }
        RasterImageNode node = new RasterImageNode();
        node.setImage(img);
        Rectangle2D imgBounds = img.getBounds2D();
        float [] vb = new float[4];
        vb[0] = 0; 
        vb[1] = 0; 
        vb[2] = (float)imgBounds.getWidth(); 
        vb[3] = (float)imgBounds.getHeight(); 
        initializeViewport(ctx, e, node, vb, bounds);
        return node;
    }
    protected GraphicsNode createSVGImageNode(BridgeContext ctx,
                                              Element e,
                                              SVGDocument imgDocument) {
        CSSEngine eng = ((SVGOMDocument)imgDocument).getCSSEngine();
        subCtx = ctx.createSubBridgeContext((SVGOMDocument)imgDocument);
        CompositeGraphicsNode result = new CompositeGraphicsNode();
        Rectangle2D bounds = getImageBounds(ctx, e);
        if ((bounds.getWidth() == 0) || (bounds.getHeight() == 0)) {
            ShapeNode sn = new ShapeNode();
            sn.setShape(bounds);
            result.getChildren().add(sn);
            return result;
        }
        Rectangle2D r = CSSUtilities.convertEnableBackground(e);
        if (r != null) {
            result.setBackgroundEnable(r);
        }
        SVGSVGElement svgElement = imgDocument.getRootElement();
        CanvasGraphicsNode node;
        node = (CanvasGraphicsNode)subCtx.getGVTBuilder().build
            (subCtx, svgElement);
        if ((eng == null) && ctx.isInteractive()) {
            subCtx.addUIEventListeners(imgDocument);
        }
        node.setClip(null);
        node.setViewingTransform(new AffineTransform());
        result.getChildren().add(node);
        String viewBox =
            svgElement.getAttributeNS(null, SVG_VIEW_BOX_ATTRIBUTE);
        float[] vb = ViewBox.parseViewBoxAttribute(e, viewBox, ctx);
        initializeViewport(ctx, e, result, vb, bounds);
        if (ctx.isInteractive()) {
            listener = new ForwardEventListener(svgElement, e);
            NodeEventTarget tgt = (NodeEventTarget)svgElement;
            tgt.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_CLICK,
                 listener, false, null);
            subCtx.storeEventListenerNS
                (tgt, XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_CLICK,
                 listener, false);
            tgt.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_KEYDOWN,
                 listener, false, null);
            subCtx.storeEventListenerNS
                (tgt, XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_KEYDOWN,
                 listener, false);
            tgt.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_KEYPRESS,
                 listener, false, null);
            subCtx.storeEventListenerNS
                (tgt, XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_KEYPRESS,
                 listener, false);
            tgt.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_KEYUP,
                 listener, false, null);
            subCtx.storeEventListenerNS
                (tgt, XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_KEYUP,
                 listener, false);
            tgt.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEDOWN,
                 listener, false, null);
            subCtx.storeEventListenerNS
                (tgt, XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEDOWN,
                 listener, false);
            tgt.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEMOVE,
                 listener, false, null);
            subCtx.storeEventListenerNS
                (tgt, XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEMOVE,
                 listener, false);
            tgt.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOUT,
                 listener, false, null);
            subCtx.storeEventListenerNS
                (tgt, XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOUT,
                 listener, false);
            tgt.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOVER,
                 listener, false, null);
            subCtx.storeEventListenerNS
                (tgt, XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOVER,
                 listener, false);
            tgt.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEUP,
                 listener, false, null);
            subCtx.storeEventListenerNS
                (tgt, XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEUP,
                 listener, false);
        }
        return result;
    }
    public void dispose() {
        if ((imgDocument != null) && (listener != null)) {
            NodeEventTarget tgt = (NodeEventTarget)imgDocument.getRootElement();
            tgt.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_CLICK,
                 listener, false);
            tgt.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_KEYDOWN,
                 listener, false);
            tgt.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_KEYPRESS,
                 listener, false);
            tgt.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_KEYUP,
                 listener, false);
            tgt.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEDOWN,
                 listener, false);
            tgt.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEMOVE,
                 listener, false);
            tgt.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOUT,
                 listener, false);
            tgt.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOVER,
                 listener, false);
            tgt.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEUP,
                 listener, false);
            listener = null;
        }
        if (imgDocument != null) {
            SVGSVGElement svgElement = imgDocument.getRootElement();
            disposeTree(svgElement);
            imgDocument = null;
            subCtx = null;
        }
        super.dispose();
    }
    protected static class ForwardEventListener implements EventListener {
        protected Element svgElement;
        protected Element imgElement;
        public ForwardEventListener(Element svgElement, Element imgElement) {
            this.svgElement = svgElement;
            this.imgElement = imgElement;
        }
        public void handleEvent(Event e) {
            DOMMouseEvent evt = (DOMMouseEvent) e;
            DOMMouseEvent newMouseEvent = (DOMMouseEvent)
                ((DocumentEvent)imgElement.getOwnerDocument()).createEvent("MouseEvents");
            newMouseEvent.initMouseEventNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI,
                 evt.getType(),
                 evt.getBubbles(),
                 evt.getCancelable(),
                 evt.getView(),
                 evt.getDetail(),
                 evt.getScreenX(),
                 evt.getScreenY(),
                 evt.getClientX(),
                 evt.getClientY(),
                 evt.getButton(),
                 (EventTarget)imgElement,
                 evt.getModifiersString());
            ((EventTarget)imgElement).dispatchEvent(newMouseEvent);
        }
    }
    protected static void initializeViewport(BridgeContext ctx,
                                             Element e,
                                             GraphicsNode node,
                                             float[] vb,
                                             Rectangle2D bounds) {
        float x = (float)bounds.getX();
        float y = (float)bounds.getY();
        float w = (float)bounds.getWidth();
        float h = (float)bounds.getHeight();
        try {
            SVGImageElement ie = (SVGImageElement) e;
            SVGOMAnimatedPreserveAspectRatio _par =
                (SVGOMAnimatedPreserveAspectRatio) ie.getPreserveAspectRatio();
            _par.check();
            AffineTransform at = ViewBox.getPreserveAspectRatioTransform
                (e, vb, w, h, _par, ctx);
            at.preConcatenate(AffineTransform.getTranslateInstance(x, y));
            node.setTransform(at);
            Shape clip = null;
            if (CSSUtilities.convertOverflow(e)) { 
                float [] offsets = CSSUtilities.convertClip(e);
                if (offsets == null) { 
                    clip = new Rectangle2D.Float(x, y, w, h);
                } else { 
                    clip = new Rectangle2D.Float(x+offsets[3],
                                                 y+offsets[0],
                                                 w-offsets[1]-offsets[3],
                                                 h-offsets[2]-offsets[0]);
                }
            }
            if (clip != null) {
                try {
                    at = at.createInverse(); 
                    Filter filter = node.getGraphicsNodeRable(true);
                    clip = at.createTransformedShape(clip);
                    node.setClip(new ClipRable8Bit(filter, clip));
                } catch (java.awt.geom.NoninvertibleTransformException ex) {}
            }
        } catch (LiveAttributeException ex) {
            throw new BridgeException(ctx, ex);
        }
    }
    protected static ICCColorSpaceExt extractColorSpace(Element element,
                                                        BridgeContext ctx) {
        String colorProfileProperty = CSSUtilities.getComputedStyle
            (element, SVGCSSEngine.COLOR_PROFILE_INDEX).getStringValue();
        ICCColorSpaceExt colorSpace = null;
        if (CSS_SRGB_VALUE.equalsIgnoreCase(colorProfileProperty)) {
            colorSpace = new ICCColorSpaceExt
                (ICC_Profile.getInstance(ColorSpace.CS_sRGB),
                 ICCColorSpaceExt.AUTO);
        } else if (!CSS_AUTO_VALUE.equalsIgnoreCase(colorProfileProperty)
                   && !"".equalsIgnoreCase(colorProfileProperty)){
            SVGColorProfileElementBridge profileBridge =
                (SVGColorProfileElementBridge) ctx.getBridge
                (SVG_NAMESPACE_URI, SVG_COLOR_PROFILE_TAG);
            if (profileBridge != null) {
                colorSpace = profileBridge.createICCColorSpaceExt
                    (ctx, element, colorProfileProperty);
            }
        }
        return colorSpace;
    }
    protected static Rectangle2D getImageBounds(BridgeContext ctx,
                                                Element element) {
        try {
            SVGImageElement ie = (SVGImageElement) element;
            AbstractSVGAnimatedLength _x =
                (AbstractSVGAnimatedLength) ie.getX();
            float x = _x.getCheckedValue();
            AbstractSVGAnimatedLength _y =
                (AbstractSVGAnimatedLength) ie.getY();
            float y = _y.getCheckedValue();
            AbstractSVGAnimatedLength _width =
                (AbstractSVGAnimatedLength) ie.getWidth();
            float w = _width.getCheckedValue();
            AbstractSVGAnimatedLength _height =
                (AbstractSVGAnimatedLength) ie.getHeight();
            float h = _height.getCheckedValue();
            return new Rectangle2D.Float(x, y, w, h);
        } catch (LiveAttributeException ex) {
            throw new BridgeException(ctx, ex);
        }
    }
    GraphicsNode createBrokenImageNode
        (BridgeContext ctx, Element e, String uri, String message) {
        SVGDocument doc = ctx.getUserAgent().getBrokenLinkDocument
            (e, uri, Messages.formatMessage(URI_IMAGE_ERROR,
                                           new Object[] { message } ));
        return createSVGImageNode(ctx, e, doc);
    }
    static SVGBrokenLinkProvider brokenLinkProvider
        = new SVGBrokenLinkProvider();
    static {
        ImageTagRegistry.setBrokenLinkProvider(brokenLinkProvider);
    }
}
