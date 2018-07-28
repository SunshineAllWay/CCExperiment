package org.apache.batik.transcoder;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.batik.bridge.BaseScriptingEnvironment;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.DefaultScriptSecurity;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.NoLoadScriptSecurity;
import org.apache.batik.bridge.RelaxedScriptSecurity;
import org.apache.batik.bridge.SVGUtilities;
import org.apache.batik.bridge.ScriptSecurity;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.DocumentFactory;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.keys.BooleanKey;
import org.apache.batik.transcoder.keys.FloatKey;
import org.apache.batik.transcoder.keys.LengthKey;
import org.apache.batik.transcoder.keys.Rectangle2DKey;
import org.apache.batik.transcoder.keys.StringKey;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGSVGElement;
public abstract class SVGAbstractTranscoder extends XMLAbstractTranscoder {
    public static final String DEFAULT_DEFAULT_FONT_FAMILY
        = "Arial, Helvetica, sans-serif";
    protected Rectangle2D curAOI;
    protected AffineTransform curTxf;
    protected GraphicsNode root;
    protected BridgeContext ctx;
    protected GVTBuilder builder;
    protected float width=400, height=400;
    protected UserAgent userAgent;
    protected SVGAbstractTranscoder() {
        userAgent = createUserAgent();
        hints.put(KEY_DOCUMENT_ELEMENT_NAMESPACE_URI,
                  SVGConstants.SVG_NAMESPACE_URI);
        hints.put(KEY_DOCUMENT_ELEMENT,
                  SVGConstants.SVG_SVG_TAG);
        hints.put(KEY_DOM_IMPLEMENTATION,
                  SVGDOMImplementation.getDOMImplementation());
        hints.put(KEY_MEDIA,
                  "screen");
        hints.put(KEY_DEFAULT_FONT_FAMILY,
                  DEFAULT_DEFAULT_FONT_FAMILY);
        hints.put(KEY_EXECUTE_ONLOAD,
                  Boolean.FALSE);
        hints.put(KEY_ALLOWED_SCRIPT_TYPES,
                  DEFAULT_ALLOWED_SCRIPT_TYPES);
    }
    protected UserAgent createUserAgent() {
        return new SVGAbstractTranscoderUserAgent();
    }
    protected DocumentFactory createDocumentFactory(DOMImplementation domImpl,
                                                    String parserClassname) {
        return new SAXSVGDocumentFactory(parserClassname);
    }
    public void transcode(TranscoderInput input, TranscoderOutput output)
            throws TranscoderException {
        super.transcode(input, output);
        if (ctx != null)
            ctx.dispose();
    }
    protected void transcode(Document document,
                             String uri,
                             TranscoderOutput output)
            throws TranscoderException {
        if ((document != null) &&
            !(document.getImplementation() instanceof SVGDOMImplementation)) {
            DOMImplementation impl;
            impl = (DOMImplementation)hints.get(KEY_DOM_IMPLEMENTATION);
            document = DOMUtilities.deepCloneDocument(document, impl);
            if (uri != null) {
                ParsedURL url = new ParsedURL(uri);
                ((SVGOMDocument)document).setParsedURL(url);
            }
        }
        if (hints.containsKey(KEY_WIDTH))
            width = ((Float)hints.get(KEY_WIDTH)).floatValue();
        if (hints.containsKey(KEY_HEIGHT))
            height = ((Float)hints.get(KEY_HEIGHT)).floatValue();
        SVGOMDocument svgDoc = (SVGOMDocument)document;
        SVGSVGElement root = svgDoc.getRootElement();
        ctx = createBridgeContext(svgDoc);
        builder = new GVTBuilder();
        boolean isDynamic =
            hints.containsKey(KEY_EXECUTE_ONLOAD) &&
             ((Boolean)hints.get(KEY_EXECUTE_ONLOAD)).booleanValue();
        GraphicsNode gvtRoot;
        try {
            if (isDynamic)
                ctx.setDynamicState(BridgeContext.DYNAMIC);
            gvtRoot = builder.build(ctx, svgDoc);
            if (ctx.isDynamic()) {
                BaseScriptingEnvironment se;
                se = new BaseScriptingEnvironment(ctx);
                se.loadScripts();
                se.dispatchSVGLoadEvent();
                if (hints.containsKey(KEY_SNAPSHOT_TIME)) {
                    float t =
                        ((Float) hints.get(KEY_SNAPSHOT_TIME)).floatValue();
                    ctx.getAnimationEngine().setCurrentTime(t);
                } else if (ctx.isSVG12()) {
                    float t = SVGUtilities.convertSnapshotTime(root, null);
                    ctx.getAnimationEngine().setCurrentTime(t);
                }
            }
        } catch (BridgeException ex) {
            ex.printStackTrace();
            throw new TranscoderException(ex);
        }
        float docWidth = (float)ctx.getDocumentSize().getWidth();
        float docHeight = (float)ctx.getDocumentSize().getHeight();
        setImageSize(docWidth, docHeight);
        AffineTransform Px;
        if (hints.containsKey(KEY_AOI)) {
            Rectangle2D aoi = (Rectangle2D)hints.get(KEY_AOI);
            Px = new AffineTransform();
            double sx = width / aoi.getWidth();
            double sy = height / aoi.getHeight();
            double scale = Math.min(sx,sy);
            Px.scale(scale, scale);
            double tx = -aoi.getX() + (width/scale - aoi.getWidth())/2;
            double ty = -aoi.getY() + (height/scale -aoi.getHeight())/2;
            Px.translate(tx, ty);
            curAOI = aoi;
        } else {
            String ref = new ParsedURL(uri).getRef();
            String viewBox = root.getAttributeNS
                (null, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE);
            if ((ref != null) && (ref.length() != 0)) {
                Px = ViewBox.getViewTransform(ref, root, width, height, ctx);
            } else if ((viewBox != null) && (viewBox.length() != 0)) {
                String aspectRatio = root.getAttributeNS
                    (null, SVGConstants.SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE);
                Px = ViewBox.getPreserveAspectRatioTransform
                    (root, viewBox, aspectRatio, width, height, ctx);
            } else {
                float xscale, yscale;
                xscale = width/docWidth;
                yscale = height/docHeight;
                float scale = Math.min(xscale,yscale);
                Px = AffineTransform.getScaleInstance(scale, scale);
            }
            curAOI = new Rectangle2D.Float(0, 0, width, height);
        }
        CanvasGraphicsNode cgn = getCanvasGraphicsNode(gvtRoot);
        if (cgn != null) {
            cgn.setViewingTransform(Px);
            curTxf = new AffineTransform();
        } else {
            curTxf = Px;
        }
        this.root = gvtRoot;
    }
    protected CanvasGraphicsNode getCanvasGraphicsNode(GraphicsNode gn) {
        if (!(gn instanceof CompositeGraphicsNode))
            return null;
        CompositeGraphicsNode cgn = (CompositeGraphicsNode)gn;
        List children = cgn.getChildren();
        if (children.size() == 0)
            return null;
        gn = (GraphicsNode)children.get(0);
        if (!(gn instanceof CanvasGraphicsNode))
            return null;
        return (CanvasGraphicsNode)gn;
    }
    protected BridgeContext createBridgeContext(SVGOMDocument doc) {
        return createBridgeContext(doc.isSVG12() ? "1.2" : "1.x");
    }
    protected BridgeContext createBridgeContext() {
        return createBridgeContext("1.x");
    }
    protected BridgeContext createBridgeContext(String svgVersion) {
        if ("1.2".equals(svgVersion)) {
            return new SVG12BridgeContext(userAgent);
        } else {
            return new BridgeContext(userAgent);
        }
    }
    protected void setImageSize(float docWidth, float docHeight) {
        float imgWidth = -1;
        if (hints.containsKey(KEY_WIDTH)) {
            imgWidth = ((Float)hints.get(KEY_WIDTH)).floatValue();
        }
        float imgHeight = -1;
        if (hints.containsKey(KEY_HEIGHT)) {
            imgHeight = ((Float)hints.get(KEY_HEIGHT)).floatValue();
        }
        if (imgWidth > 0 && imgHeight > 0) {
            width = imgWidth;
            height = imgHeight;
        } else if (imgHeight > 0) {
            width = (docWidth * imgHeight) / docHeight;
            height = imgHeight;
        } else if (imgWidth > 0) {
            width = imgWidth;
            height = (docHeight * imgWidth) / docWidth;
        } else {
            width = docWidth;
            height = docHeight;
        }
        float imgMaxWidth = -1;
        if (hints.containsKey(KEY_MAX_WIDTH)) {
            imgMaxWidth = ((Float)hints.get(KEY_MAX_WIDTH)).floatValue();
        }
        float imgMaxHeight = -1;
        if (hints.containsKey(KEY_MAX_HEIGHT)) {
            imgMaxHeight = ((Float)hints.get(KEY_MAX_HEIGHT)).floatValue();
        }
        if ((imgMaxHeight > 0) && (height > imgMaxHeight)) {
            width = (docWidth * imgMaxHeight) / docHeight;
            height = imgMaxHeight;
        }
        if ((imgMaxWidth > 0) && (width > imgMaxWidth)) {
            width = imgMaxWidth;
            height = (docHeight * imgMaxWidth) / docWidth;
        }
    }
    public static final TranscodingHints.Key KEY_WIDTH
        = new LengthKey();
    public static final TranscodingHints.Key KEY_HEIGHT
        = new LengthKey();
    public static final TranscodingHints.Key KEY_MAX_WIDTH
        = new LengthKey();
    public static final TranscodingHints.Key KEY_MAX_HEIGHT
        = new LengthKey();
    public static final TranscodingHints.Key KEY_AOI
        = new Rectangle2DKey();
    public static final TranscodingHints.Key KEY_LANGUAGE
        = new StringKey();
    public static final TranscodingHints.Key KEY_MEDIA
        = new StringKey();
    public static final TranscodingHints.Key KEY_DEFAULT_FONT_FAMILY
        = new StringKey();
    public static final TranscodingHints.Key KEY_ALTERNATE_STYLESHEET
        = new StringKey();
    public static final TranscodingHints.Key KEY_USER_STYLESHEET_URI
        = new StringKey();
    public static final TranscodingHints.Key KEY_PIXEL_UNIT_TO_MILLIMETER
        = new FloatKey();
    public static final TranscodingHints.Key KEY_PIXEL_TO_MM
        = KEY_PIXEL_UNIT_TO_MILLIMETER;
    public static final TranscodingHints.Key KEY_EXECUTE_ONLOAD
        = new BooleanKey();
    public static final TranscodingHints.Key KEY_SNAPSHOT_TIME
        = new FloatKey();
    public static final TranscodingHints.Key KEY_ALLOWED_SCRIPT_TYPES
        = new StringKey();
    public static final String DEFAULT_ALLOWED_SCRIPT_TYPES
        = SVGConstants.SVG_SCRIPT_TYPE_ECMASCRIPT + ", "
        + SVGConstants.SVG_SCRIPT_TYPE_APPLICATION_ECMASCRIPT + ", "
        + SVGConstants.SVG_SCRIPT_TYPE_JAVASCRIPT + ", "
        + SVGConstants.SVG_SCRIPT_TYPE_APPLICATION_JAVASCRIPT + ", "
        + SVGConstants.SVG_SCRIPT_TYPE_JAVA;
    public static final TranscodingHints.Key KEY_CONSTRAIN_SCRIPT_ORIGIN
        = new BooleanKey();
    protected class SVGAbstractTranscoderUserAgent extends UserAgentAdapter {
        protected List scripts;
        public SVGAbstractTranscoderUserAgent() {
            addStdFeatures();
        }
        public AffineTransform getTransform() {
            return SVGAbstractTranscoder.this.curTxf;
        }
        public void setTransform(AffineTransform at) {
            SVGAbstractTranscoder.this.curTxf = at;
        }
        public Dimension2D getViewportSize() {
            return new Dimension((int)SVGAbstractTranscoder.this.width,
                                 (int)SVGAbstractTranscoder.this.height);
        }
        public void displayError(String message) {
            try {
                SVGAbstractTranscoder.this.handler.error
                    (new TranscoderException(message));
            } catch (TranscoderException ex) {
                throw new RuntimeException( ex.getMessage() );
            }
        }
        public void displayError(Exception e) {
            try {
                e.printStackTrace();
                SVGAbstractTranscoder.this.handler.error
                    (new TranscoderException(e));
            } catch (TranscoderException ex) {
                throw new RuntimeException( ex.getMessage() );
            }
        }
        public void displayMessage(String message) {
            try {
                SVGAbstractTranscoder.this.handler.warning
                    (new TranscoderException(message));
            } catch (TranscoderException ex) {
                throw new RuntimeException( ex.getMessage() );
            }
        }
        public float getPixelUnitToMillimeter() {
            Object obj = SVGAbstractTranscoder.this.hints.get
                (KEY_PIXEL_UNIT_TO_MILLIMETER);
            if (obj != null) {
                return ((Float)obj).floatValue();
            }
            return super.getPixelUnitToMillimeter();
        }
        public String getLanguages() {
            if (SVGAbstractTranscoder.this.hints.containsKey(KEY_LANGUAGE)) {
                return (String)SVGAbstractTranscoder.this.hints.get
                    (KEY_LANGUAGE);
            }
            return super.getLanguages();
        }
        public String getMedia() {
            String s = (String)hints.get(KEY_MEDIA);
            if (s != null) return s;
            return super.getMedia();
        }
        public String getDefaultFontFamily() {
            String s = (String)hints.get(KEY_DEFAULT_FONT_FAMILY);
            if (s != null) return s;
            return super.getDefaultFontFamily();
        }
        public String getAlternateStyleSheet() {
            String s = (String)hints.get(KEY_ALTERNATE_STYLESHEET);
            if (s != null)
                return s;
            return super.getAlternateStyleSheet();
        }
        public String getUserStyleSheetURI() {
            String s = (String)SVGAbstractTranscoder.this.hints.get
                (KEY_USER_STYLESHEET_URI);
            if (s != null)
                return s;
            return super.getUserStyleSheetURI();
        }
        public String getXMLParserClassName() {
            String s = (String)SVGAbstractTranscoder.this.hints.get
                (KEY_XML_PARSER_CLASSNAME);
            if (s != null)
                return s;
            return super.getXMLParserClassName();
        }
        public boolean isXMLParserValidating() {
            Boolean b = (Boolean)SVGAbstractTranscoder.this.hints.get
                (KEY_XML_PARSER_VALIDATING);
            if (b != null)
                return b.booleanValue();
            return super.isXMLParserValidating();
        }
        public ScriptSecurity getScriptSecurity(String scriptType,
                                                ParsedURL scriptPURL,
                                                ParsedURL docPURL){
            if (scripts == null){
                computeAllowedScripts();
            }
            if (!scripts.contains(scriptType)) {
                return new NoLoadScriptSecurity(scriptType);
            }
            boolean constrainOrigin = true;
            if (SVGAbstractTranscoder.this.hints.containsKey
                (KEY_CONSTRAIN_SCRIPT_ORIGIN)) {
                constrainOrigin =
                    ((Boolean)SVGAbstractTranscoder.this.hints.get
                     (KEY_CONSTRAIN_SCRIPT_ORIGIN)).booleanValue();
            }
            if (constrainOrigin) {
                return new DefaultScriptSecurity
                    (scriptType,scriptPURL,docPURL);
            } else {
                return new RelaxedScriptSecurity
                    (scriptType,scriptPURL,docPURL);
            }
        }
        protected void computeAllowedScripts(){
            scripts = new LinkedList();
            if (!SVGAbstractTranscoder.this.hints.containsKey
                (KEY_ALLOWED_SCRIPT_TYPES)) {
                return;
            }
            String allowedScripts
                = (String)SVGAbstractTranscoder.this.hints.get
                (KEY_ALLOWED_SCRIPT_TYPES);
            StringTokenizer st = new StringTokenizer(allowedScripts, ",");
            while (st.hasMoreTokens()) {
                scripts.add(st.nextToken());
            }
        }
    }
}
