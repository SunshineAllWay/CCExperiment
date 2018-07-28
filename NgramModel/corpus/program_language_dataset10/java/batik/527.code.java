package org.apache.batik.dom.svg;
import java.net.URL;
import org.apache.batik.css.dom.CSSOMSVGViewCSS;
import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.css.parser.ExtendedParser;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.AbstractStylableDocument;
import org.apache.batik.dom.ExtensibleDOMImplementation;
import org.apache.batik.dom.events.DOMTimeEvent;
import org.apache.batik.dom.events.DocumentEventSupport;
import org.apache.batik.dom.util.CSSStyleDeclarationFactory;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.HashTable;
import org.apache.batik.i18n.LocalizableSupport;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGConstants;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.ViewCSS;
import org.w3c.dom.events.Event;
import org.w3c.dom.stylesheets.StyleSheet;
public class SVGDOMImplementation
    extends    ExtensibleDOMImplementation
    implements CSSStyleDeclarationFactory {
    public static final String SVG_NAMESPACE_URI =
        SVGConstants.SVG_NAMESPACE_URI;
    protected static final String RESOURCES =
        "org.apache.batik.dom.svg.resources.Messages";
    protected HashTable factories;
    public static DOMImplementation getDOMImplementation() {
        return DOM_IMPLEMENTATION;
    }
    public SVGDOMImplementation() {
        factories = svg11Factories;
        registerFeature("CSS",            "2.0");
        registerFeature("StyleSheets",    "2.0");
        registerFeature("SVG",            new String[] {"1.0", "1.1"});
        registerFeature("SVGEvents",      new String[] {"1.0", "1.1"});
    }
    protected void initLocalizable() {
        localizableSupport = new LocalizableSupport
            (RESOURCES, getClass().getClassLoader());
    }
    public CSSEngine createCSSEngine(AbstractStylableDocument doc,
                                     CSSContext               ctx,
                                     ExtendedParser           ep,
                                     ValueManager     []      vms,
                                     ShorthandManager []      sms) {
        ParsedURL durl = ((SVGOMDocument)doc).getParsedURL();
        CSSEngine result = new SVGCSSEngine(doc, durl, ep, vms, sms, ctx);
        URL url = getClass().getResource("resources/UserAgentStyleSheet.css");
        if (url != null) {
            ParsedURL purl = new ParsedURL(url);
            InputSource is = new InputSource(purl.toString());
            result.setUserAgentStyleSheet
                (result.parseStyleSheet(is, purl, "all"));
        }
        return result;
    }
    public ViewCSS createViewCSS(AbstractStylableDocument doc) {
        return new CSSOMSVGViewCSS(doc.getCSSEngine());
    }
    public Document createDocument(String namespaceURI,
                                   String qualifiedName,
                                   DocumentType doctype)
        throws DOMException {
        Document result = new SVGOMDocument(doctype, this);
        if (qualifiedName != null)
            result.appendChild(result.createElementNS(namespaceURI,
                                                      qualifiedName));
        return result;
    }
    public CSSStyleSheet createCSSStyleSheet(String title, String media) {
        throw new UnsupportedOperationException
            ("DOMImplementationCSS.createCSSStyleSheet is not implemented"); 
    }
    public CSSStyleDeclaration createCSSStyleDeclaration() {
        throw new UnsupportedOperationException
            ("CSSStyleDeclarationFactory.createCSSStyleDeclaration is not implemented"); 
    }
    public StyleSheet createStyleSheet(Node n, HashTable attrs) {
        throw new UnsupportedOperationException
            ("StyleSheetFactory.createStyleSheet is not implemented"); 
    }
    public CSSStyleSheet getUserAgentStyleSheet() {
        throw new UnsupportedOperationException
            ("StyleSheetFactory.getUserAgentStyleSheet is not implemented"); 
    }
    public Element createElementNS(AbstractDocument document,
                                   String           namespaceURI,
                                   String           qualifiedName) {
        if (SVGConstants.SVG_NAMESPACE_URI.equals(namespaceURI)) {
            String name = DOMUtilities.getLocalName(qualifiedName);
            ElementFactory ef = (ElementFactory)factories.get(name);
            if (ef != null)
                return ef.create(DOMUtilities.getPrefix(qualifiedName),
                                 document);
            throw document.createDOMException
                (DOMException.NOT_FOUND_ERR, "invalid.element",
                 new Object[] { namespaceURI, qualifiedName });
        }
        return super.createElementNS(document, namespaceURI, qualifiedName);
    }
    public DocumentEventSupport createDocumentEventSupport() {
        DocumentEventSupport result =  new DocumentEventSupport();
        result.registerEventFactory("SVGEvents",
                                    new DocumentEventSupport.EventFactory() {
                                            public Event createEvent() {
                                                return new SVGOMEvent();
                                            }
                                        });
        result.registerEventFactory("TimeEvent",
                                    new DocumentEventSupport.EventFactory() {
                                            public Event createEvent() {
                                                return new DOMTimeEvent();
                                            }
                                        });
        return result;
    }
    protected static HashTable svg11Factories = new HashTable();
    static {
        svg11Factories.put(SVGConstants.SVG_A_TAG,
                           new AElementFactory());
        svg11Factories.put(SVGConstants.SVG_ALT_GLYPH_TAG,
                           new AltGlyphElementFactory());
        svg11Factories.put(SVGConstants.SVG_ALT_GLYPH_DEF_TAG,
                           new AltGlyphDefElementFactory());
        svg11Factories.put(SVGConstants.SVG_ALT_GLYPH_ITEM_TAG,
                           new AltGlyphItemElementFactory());
        svg11Factories.put(SVGConstants.SVG_ANIMATE_TAG,
                           new AnimateElementFactory());
        svg11Factories.put(SVGConstants.SVG_ANIMATE_COLOR_TAG,
                           new AnimateColorElementFactory());
        svg11Factories.put(SVGConstants.SVG_ANIMATE_MOTION_TAG,
                           new AnimateMotionElementFactory());
        svg11Factories.put(SVGConstants.SVG_ANIMATE_TRANSFORM_TAG,
                           new AnimateTransformElementFactory());
        svg11Factories.put(SVGConstants.SVG_CIRCLE_TAG,
                           new CircleElementFactory());
        svg11Factories.put(SVGConstants.SVG_CLIP_PATH_TAG,
                           new ClipPathElementFactory());
        svg11Factories.put(SVGConstants.SVG_COLOR_PROFILE_TAG,
                           new ColorProfileElementFactory());
        svg11Factories.put(SVGConstants.SVG_CURSOR_TAG,
                           new CursorElementFactory());
        svg11Factories.put(SVGConstants.SVG_DEFINITION_SRC_TAG,
                           new DefinitionSrcElementFactory());
        svg11Factories.put(SVGConstants.SVG_DEFS_TAG,
                           new DefsElementFactory());
        svg11Factories.put(SVGConstants.SVG_DESC_TAG,
                           new DescElementFactory());
        svg11Factories.put(SVGConstants.SVG_ELLIPSE_TAG,
                           new EllipseElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_BLEND_TAG,
                           new FeBlendElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_COLOR_MATRIX_TAG,
                           new FeColorMatrixElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_COMPONENT_TRANSFER_TAG,
                           new FeComponentTransferElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_COMPOSITE_TAG,
                           new FeCompositeElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_CONVOLVE_MATRIX_TAG,
                           new FeConvolveMatrixElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_DIFFUSE_LIGHTING_TAG,
                           new FeDiffuseLightingElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_DISPLACEMENT_MAP_TAG,
                           new FeDisplacementMapElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_DISTANT_LIGHT_TAG,
                           new FeDistantLightElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_FLOOD_TAG,
                           new FeFloodElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_FUNC_A_TAG,
                           new FeFuncAElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_FUNC_R_TAG,
                           new FeFuncRElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_FUNC_G_TAG,
                           new FeFuncGElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_FUNC_B_TAG,
                           new FeFuncBElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_GAUSSIAN_BLUR_TAG,
                           new FeGaussianBlurElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_IMAGE_TAG,
                           new FeImageElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_MERGE_TAG,
                           new FeMergeElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_MERGE_NODE_TAG,
                           new FeMergeNodeElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_MORPHOLOGY_TAG,
                           new FeMorphologyElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_OFFSET_TAG,
                           new FeOffsetElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_POINT_LIGHT_TAG,
                           new FePointLightElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_SPECULAR_LIGHTING_TAG,
                           new FeSpecularLightingElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_SPOT_LIGHT_TAG,
                           new FeSpotLightElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_TILE_TAG,
                           new FeTileElementFactory());
        svg11Factories.put(SVGConstants.SVG_FE_TURBULENCE_TAG,
                           new FeTurbulenceElementFactory());
        svg11Factories.put(SVGConstants.SVG_FILTER_TAG,
                           new FilterElementFactory());
        svg11Factories.put(SVGConstants.SVG_FONT_TAG,
                           new FontElementFactory());
        svg11Factories.put(SVGConstants.SVG_FONT_FACE_TAG,
                           new FontFaceElementFactory());
        svg11Factories.put(SVGConstants.SVG_FONT_FACE_FORMAT_TAG,
                           new FontFaceFormatElementFactory());
        svg11Factories.put(SVGConstants.SVG_FONT_FACE_NAME_TAG,
                           new FontFaceNameElementFactory());
        svg11Factories.put(SVGConstants.SVG_FONT_FACE_SRC_TAG,
                           new FontFaceSrcElementFactory());
        svg11Factories.put(SVGConstants.SVG_FONT_FACE_URI_TAG,
                           new FontFaceUriElementFactory());
        svg11Factories.put(SVGConstants.SVG_FOREIGN_OBJECT_TAG,
                           new ForeignObjectElementFactory());
        svg11Factories.put(SVGConstants.SVG_G_TAG,
                           new GElementFactory());
        svg11Factories.put(SVGConstants.SVG_GLYPH_TAG,
                           new GlyphElementFactory());
        svg11Factories.put(SVGConstants.SVG_GLYPH_REF_TAG,
                           new GlyphRefElementFactory());
        svg11Factories.put(SVGConstants.SVG_HKERN_TAG,
                           new HkernElementFactory());
        svg11Factories.put(SVGConstants.SVG_IMAGE_TAG,
                           new ImageElementFactory());
        svg11Factories.put(SVGConstants.SVG_LINE_TAG,
                           new LineElementFactory());
        svg11Factories.put(SVGConstants.SVG_LINEAR_GRADIENT_TAG,
                           new LinearGradientElementFactory());
        svg11Factories.put(SVGConstants.SVG_MARKER_TAG,
                           new MarkerElementFactory());
        svg11Factories.put(SVGConstants.SVG_MASK_TAG,
                           new MaskElementFactory());
        svg11Factories.put(SVGConstants.SVG_METADATA_TAG,
                           new MetadataElementFactory());
        svg11Factories.put(SVGConstants.SVG_MISSING_GLYPH_TAG,
                           new MissingGlyphElementFactory());
        svg11Factories.put(SVGConstants.SVG_MPATH_TAG,
                           new MpathElementFactory());
        svg11Factories.put(SVGConstants.SVG_PATH_TAG,
                           new PathElementFactory());
        svg11Factories.put(SVGConstants.SVG_PATTERN_TAG,
                           new PatternElementFactory());
        svg11Factories.put(SVGConstants.SVG_POLYGON_TAG,
                           new PolygonElementFactory());
        svg11Factories.put(SVGConstants.SVG_POLYLINE_TAG,
                           new PolylineElementFactory());
        svg11Factories.put(SVGConstants.SVG_RADIAL_GRADIENT_TAG,
                           new RadialGradientElementFactory());
        svg11Factories.put(SVGConstants.SVG_RECT_TAG,
                           new RectElementFactory());
        svg11Factories.put(SVGConstants.SVG_SET_TAG,
                           new SetElementFactory());
        svg11Factories.put(SVGConstants.SVG_SCRIPT_TAG,
                           new ScriptElementFactory());
        svg11Factories.put(SVGConstants.SVG_STOP_TAG,
                           new StopElementFactory());
        svg11Factories.put(SVGConstants.SVG_STYLE_TAG,
                           new StyleElementFactory());
        svg11Factories.put(SVGConstants.SVG_SVG_TAG,
                           new SvgElementFactory());
        svg11Factories.put(SVGConstants.SVG_SWITCH_TAG,
                           new SwitchElementFactory());
        svg11Factories.put(SVGConstants.SVG_SYMBOL_TAG,
                           new SymbolElementFactory());
        svg11Factories.put(SVGConstants.SVG_TEXT_TAG,
                           new TextElementFactory());
        svg11Factories.put(SVGConstants.SVG_TEXT_PATH_TAG,
                           new TextPathElementFactory());
        svg11Factories.put(SVGConstants.SVG_TITLE_TAG,
                           new TitleElementFactory());
        svg11Factories.put(SVGConstants.SVG_TREF_TAG,
                           new TrefElementFactory());
        svg11Factories.put(SVGConstants.SVG_TSPAN_TAG,
                           new TspanElementFactory());
        svg11Factories.put(SVGConstants.SVG_USE_TAG,
                           new UseElementFactory());
        svg11Factories.put(SVGConstants.SVG_VIEW_TAG,
                           new ViewElementFactory());
        svg11Factories.put(SVGConstants.SVG_VKERN_TAG,
                           new VkernElementFactory());
    }
    protected static class AElementFactory implements ElementFactory {
        public AElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMAElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class AltGlyphElementFactory implements ElementFactory {
        public AltGlyphElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMAltGlyphElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class AltGlyphDefElementFactory
        implements ElementFactory {
        public AltGlyphDefElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMAltGlyphDefElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class AltGlyphItemElementFactory
        implements ElementFactory {
        public AltGlyphItemElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMAltGlyphItemElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class AnimateElementFactory implements ElementFactory {
        public AnimateElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMAnimateElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class AnimateColorElementFactory
        implements ElementFactory {
        public AnimateColorElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMAnimateColorElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class AnimateMotionElementFactory
        implements ElementFactory {
        public AnimateMotionElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMAnimateMotionElement(prefix,
                                                 (AbstractDocument)doc);
        }
    }
    protected static class AnimateTransformElementFactory
        implements ElementFactory {
        public AnimateTransformElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMAnimateTransformElement(prefix,
                                                    (AbstractDocument)doc);
        }
    }
    protected static class CircleElementFactory implements ElementFactory {
        public CircleElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMCircleElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class ClipPathElementFactory implements ElementFactory {
        public ClipPathElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMClipPathElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class ColorProfileElementFactory
        implements ElementFactory {
        public ColorProfileElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMColorProfileElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class CursorElementFactory implements ElementFactory {
        public CursorElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMCursorElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class DefinitionSrcElementFactory
        implements ElementFactory {
        public DefinitionSrcElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMDefinitionSrcElement(prefix,
                                                 (AbstractDocument)doc);
        }
    }
    protected static class DefsElementFactory implements ElementFactory {
        public DefsElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMDefsElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class DescElementFactory implements ElementFactory {
        public DescElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMDescElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class EllipseElementFactory implements ElementFactory {
        public EllipseElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMEllipseElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FeBlendElementFactory implements ElementFactory {
        public FeBlendElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFEBlendElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FeColorMatrixElementFactory
        implements ElementFactory {
        public FeColorMatrixElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFEColorMatrixElement(prefix,
                                                 (AbstractDocument)doc);
        }
    }
    protected static class FeComponentTransferElementFactory
        implements ElementFactory {
        public FeComponentTransferElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFEComponentTransferElement(prefix,
                                                       (AbstractDocument)doc);
        }
    }
    protected static class FeCompositeElementFactory
        implements ElementFactory {
        public FeCompositeElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFECompositeElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FeConvolveMatrixElementFactory
        implements ElementFactory {
        public FeConvolveMatrixElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFEConvolveMatrixElement(prefix,
                                                    (AbstractDocument)doc);
        }
    }
    protected static class FeDiffuseLightingElementFactory
        implements ElementFactory {
        public FeDiffuseLightingElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFEDiffuseLightingElement(prefix,
                                                     (AbstractDocument)doc);
        }
    }
    protected static class FeDisplacementMapElementFactory
        implements ElementFactory {
        public FeDisplacementMapElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFEDisplacementMapElement(prefix,
                                                     (AbstractDocument)doc);
        }
    }
    protected static class FeDistantLightElementFactory
        implements ElementFactory {
        public FeDistantLightElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFEDistantLightElement(prefix,
                                                  (AbstractDocument)doc);
        }
    }
    protected static class FeFloodElementFactory implements ElementFactory {
        public FeFloodElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFEFloodElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FeFuncAElementFactory implements ElementFactory {
        public FeFuncAElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFEFuncAElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FeFuncRElementFactory implements ElementFactory {
        public FeFuncRElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFEFuncRElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FeFuncGElementFactory implements ElementFactory {
        public FeFuncGElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFEFuncGElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FeFuncBElementFactory
        implements ElementFactory {
        public FeFuncBElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFEFuncBElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FeGaussianBlurElementFactory
        implements ElementFactory {
        public FeGaussianBlurElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFEGaussianBlurElement(prefix,
                                                  (AbstractDocument)doc);
        }
    }
    protected static class FeImageElementFactory implements ElementFactory {
        public FeImageElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFEImageElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FeMergeElementFactory
        implements ElementFactory {
        public FeMergeElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFEMergeElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FeMergeNodeElementFactory
        implements ElementFactory {
        public FeMergeNodeElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFEMergeNodeElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FeMorphologyElementFactory
        implements ElementFactory {
        public FeMorphologyElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFEMorphologyElement(prefix,
                                                (AbstractDocument)doc);
        }
    }
    protected static class FeOffsetElementFactory implements ElementFactory {
        public FeOffsetElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFEOffsetElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FePointLightElementFactory
        implements ElementFactory {
        public FePointLightElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFEPointLightElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FeSpecularLightingElementFactory
        implements ElementFactory {
        public FeSpecularLightingElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFESpecularLightingElement(prefix,
                                                      (AbstractDocument)doc);
        }
    }
    protected static class FeSpotLightElementFactory
        implements ElementFactory {
        public FeSpotLightElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFESpotLightElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FeTileElementFactory implements ElementFactory {
        public FeTileElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFETileElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FeTurbulenceElementFactory
        implements ElementFactory{
        public FeTurbulenceElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFETurbulenceElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FilterElementFactory implements ElementFactory {
        public FilterElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFilterElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FontElementFactory implements ElementFactory {
        public FontElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFontElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FontFaceElementFactory implements ElementFactory {
        public FontFaceElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFontFaceElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FontFaceFormatElementFactory
        implements ElementFactory {
        public FontFaceFormatElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFontFaceFormatElement(prefix,
                                                  (AbstractDocument)doc);
        }
    }
    protected static class FontFaceNameElementFactory
        implements ElementFactory {
        public FontFaceNameElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFontFaceNameElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FontFaceSrcElementFactory
        implements ElementFactory {
        public FontFaceSrcElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFontFaceSrcElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FontFaceUriElementFactory
        implements ElementFactory {
        public FontFaceUriElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMFontFaceUriElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class ForeignObjectElementFactory
        implements ElementFactory {
        public ForeignObjectElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMForeignObjectElement(prefix,
                                                 (AbstractDocument)doc);
        }
    }
    protected static class GElementFactory implements ElementFactory {
        public GElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMGElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class GlyphElementFactory implements ElementFactory {
        public GlyphElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMGlyphElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class GlyphRefElementFactory implements ElementFactory {
        public GlyphRefElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMGlyphRefElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class HkernElementFactory implements ElementFactory {
        public HkernElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMHKernElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class ImageElementFactory implements ElementFactory {
        public ImageElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMImageElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class LineElementFactory implements ElementFactory {
        public LineElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMLineElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class LinearGradientElementFactory
        implements ElementFactory {
        public LinearGradientElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMLinearGradientElement(prefix,
                                                  (AbstractDocument)doc);
        }
    }
    protected static class MarkerElementFactory implements ElementFactory {
        public MarkerElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMMarkerElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class MaskElementFactory implements ElementFactory {
        public MaskElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMMaskElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class MetadataElementFactory implements ElementFactory {
        public MetadataElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMMetadataElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class MissingGlyphElementFactory
        implements ElementFactory {
        public MissingGlyphElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMMissingGlyphElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class MpathElementFactory implements ElementFactory {
        public MpathElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMMPathElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class PathElementFactory implements ElementFactory {
        public PathElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMPathElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class PatternElementFactory implements ElementFactory {
        public PatternElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMPatternElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class PolygonElementFactory implements ElementFactory {
        public PolygonElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMPolygonElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class PolylineElementFactory implements ElementFactory {
        public PolylineElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMPolylineElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class RadialGradientElementFactory
        implements ElementFactory {
        public RadialGradientElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMRadialGradientElement(prefix,
                                                  (AbstractDocument)doc);
        }
    }
    protected static class RectElementFactory implements ElementFactory {
        public RectElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMRectElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class ScriptElementFactory implements ElementFactory {
        public ScriptElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMScriptElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class SetElementFactory implements ElementFactory {
        public SetElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMSetElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class StopElementFactory implements ElementFactory {
        public StopElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMStopElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class StyleElementFactory implements ElementFactory {
        public StyleElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMStyleElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class SvgElementFactory implements ElementFactory {
        public SvgElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMSVGElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class SwitchElementFactory implements ElementFactory {
        public SwitchElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMSwitchElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class SymbolElementFactory implements ElementFactory {
        public SymbolElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMSymbolElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class TextElementFactory implements ElementFactory {
        public TextElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMTextElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class TextPathElementFactory implements ElementFactory {
        public TextPathElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMTextPathElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class TitleElementFactory implements ElementFactory {
        public TitleElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMTitleElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class TrefElementFactory implements ElementFactory {
        public TrefElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMTRefElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class TspanElementFactory implements ElementFactory {
        public TspanElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMTSpanElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class UseElementFactory implements ElementFactory {
        public UseElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMUseElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class ViewElementFactory implements ElementFactory {
        public ViewElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMViewElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class VkernElementFactory implements ElementFactory {
        public VkernElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMVKernElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static final DOMImplementation DOM_IMPLEMENTATION =
        new SVGDOMImplementation();
}
