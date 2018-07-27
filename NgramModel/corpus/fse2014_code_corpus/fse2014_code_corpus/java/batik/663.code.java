package org.apache.batik.dom.svg12;
import java.net.URL;
import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.SVG12CSSEngine;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.css.parser.ExtendedParser;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.AbstractStylableDocument;
import org.apache.batik.dom.GenericElement;
import org.apache.batik.dom.events.DocumentEventSupport;
import org.apache.batik.dom.events.EventSupport;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.util.HashTable;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVG12Constants;
import org.apache.batik.util.XBLConstants;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
public class SVG12DOMImplementation
    extends    SVGDOMImplementation {
    public SVG12DOMImplementation() {
        factories = svg12Factories;
        registerFeature("CSS",            "2.0");
        registerFeature("StyleSheets",    "2.0");
        registerFeature("SVG",            new String[] {"1.0", "1.1", "1.2"});
        registerFeature("SVGEvents",      new String[] {"1.0", "1.1", "1.2"});
    }
    public CSSEngine createCSSEngine(AbstractStylableDocument doc,
                                     CSSContext               ctx,
                                     ExtendedParser      ep,
                                     ValueManager     [] vms,
                                     ShorthandManager [] sms) {
        ParsedURL durl = ((SVGOMDocument)doc).getParsedURL();
        CSSEngine result = new SVG12CSSEngine(doc, durl, ep, vms, sms, ctx);
        URL url = getClass().getResource("resources/UserAgentStyleSheet.css");
        if (url != null) {
            ParsedURL purl = new ParsedURL(url);
            InputSource is = new InputSource(purl.toString());
            result.setUserAgentStyleSheet
                (result.parseStyleSheet(is, purl, "all"));
        }
        return result;
    }
    public Document createDocument(String namespaceURI,
                                   String qualifiedName,
                                   DocumentType doctype)
        throws DOMException {
        SVGOMDocument result = new SVG12OMDocument(doctype, this);
        result.setIsSVG12(true);
        if (qualifiedName != null)
            result.appendChild(result.createElementNS(namespaceURI,
                                                      qualifiedName));
        return result;
    }
    public Element createElementNS(AbstractDocument document,
                                   String           namespaceURI,
                                   String           qualifiedName) {
        if (namespaceURI == null)
            return new GenericElement(qualifiedName.intern(), document);
        String name = DOMUtilities.getLocalName(qualifiedName);
        String prefix = DOMUtilities.getPrefix(qualifiedName);
        if (SVG12Constants.SVG_NAMESPACE_URI.equals(namespaceURI)) {
            ElementFactory ef = (ElementFactory)factories.get(name);
            if (ef != null) {
                return ef.create(prefix, document);
            }
        } else if (XBLConstants.XBL_NAMESPACE_URI.equals(namespaceURI)) {
            ElementFactory ef = (ElementFactory)xblFactories.get(name);
            if (ef != null) {
                return ef.create(prefix, document);
            }
        }
        if (customFactories != null) {
            ElementFactory cef;
            cef = (ElementFactory)customFactories.get(namespaceURI, name);
            if (cef != null) {
                return cef.create(prefix, document);
            }
        }
        return new BindableElement(prefix, document, namespaceURI, name);
    }
    public DocumentEventSupport createDocumentEventSupport() {
        DocumentEventSupport result =  super.createDocumentEventSupport();
        result.registerEventFactory("WheelEvent",
                                    new DocumentEventSupport.EventFactory() {
                                        public Event createEvent() {
                                            return new SVGOMWheelEvent();
                                        }
                                    });
        result.registerEventFactory("ShadowTreeEvent",
                                    new DocumentEventSupport.EventFactory() {
                                        public Event createEvent() {
                                            return new XBLOMShadowTreeEvent();
                                        }
                                    });
        return result;
    }
    public EventSupport createEventSupport(AbstractNode n) {
        return new XBLEventSupport(n);
    }
    protected static HashTable svg12Factories = new HashTable(svg11Factories);
    static {
        svg12Factories.put(SVG12Constants.SVG_FLOW_DIV_TAG,
                           new FlowDivElementFactory());
        svg12Factories.put(SVG12Constants.SVG_FLOW_LINE_TAG,
                           new FlowLineElementFactory());
        svg12Factories.put(SVG12Constants.SVG_FLOW_PARA_TAG,
                           new FlowParaElementFactory());
        svg12Factories.put(SVG12Constants.SVG_FLOW_REGION_BREAK_TAG,
                           new FlowRegionBreakElementFactory());
        svg12Factories.put(SVG12Constants.SVG_FLOW_REGION_TAG,
                           new FlowRegionElementFactory());
        svg12Factories.put(SVG12Constants.SVG_FLOW_REGION_EXCLUDE_TAG,
                           new FlowRegionExcludeElementFactory());
        svg12Factories.put(SVG12Constants.SVG_FLOW_ROOT_TAG,
                           new FlowRootElementFactory());
        svg12Factories.put(SVG12Constants.SVG_FLOW_SPAN_TAG,
                           new FlowSpanElementFactory());
        svg12Factories.put(SVG12Constants.SVG_HANDLER_TAG,
                           new HandlerElementFactory());
        svg12Factories.put(SVG12Constants.SVG_MULTI_IMAGE_TAG,
                           new MultiImageElementFactory());
        svg12Factories.put(SVG12Constants.SVG_SOLID_COLOR_TAG,
                           new SolidColorElementFactory());
        svg12Factories.put(SVG12Constants.SVG_SUB_IMAGE_TAG,
                           new SubImageElementFactory());
        svg12Factories.put(SVG12Constants.SVG_SUB_IMAGE_REF_TAG,
                           new SubImageRefElementFactory());
    }
    protected static class FlowDivElementFactory
        implements ElementFactory {
        public FlowDivElementFactory() {
        }
        public Element create(String prefix, Document doc) {
            return new SVGOMFlowDivElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FlowLineElementFactory
        implements ElementFactory {
        public FlowLineElementFactory() {
        }
        public Element create(String prefix, Document doc) {
            return new SVGOMFlowLineElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FlowParaElementFactory
        implements ElementFactory {
        public FlowParaElementFactory() {
        }
        public Element create(String prefix, Document doc) {
            return new SVGOMFlowParaElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FlowRegionBreakElementFactory
        implements ElementFactory {
        public FlowRegionBreakElementFactory() {
        }
        public Element create(String prefix, Document doc) {
            return new SVGOMFlowRegionBreakElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FlowRegionElementFactory
        implements ElementFactory {
        public FlowRegionElementFactory() {
        }
        public Element create(String prefix, Document doc) {
            return new SVGOMFlowRegionElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FlowRegionExcludeElementFactory
        implements ElementFactory {
        public FlowRegionExcludeElementFactory() {
        }
        public Element create(String prefix, Document doc) {
            return new SVGOMFlowRegionExcludeElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FlowRootElementFactory
        implements ElementFactory {
        public FlowRootElementFactory() {
        }
        public Element create(String prefix, Document doc) {
            return new SVGOMFlowRootElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FlowSpanElementFactory
        implements ElementFactory {
        public FlowSpanElementFactory() {
        }
        public Element create(String prefix, Document doc) {
            return new SVGOMFlowSpanElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class HandlerElementFactory
        implements ElementFactory {
        public HandlerElementFactory() {
        }
        public Element create(String prefix, Document doc) {
            return new SVGOMHandlerElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class MultiImageElementFactory
        implements ElementFactory {
        public MultiImageElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMMultiImageElement
                (prefix, (AbstractDocument)doc);
        }
    }
    protected static class SolidColorElementFactory
        implements ElementFactory {
        public SolidColorElementFactory() {
        }
        public Element create(String prefix, Document doc) {
            return new SVGOMSolidColorElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class SubImageElementFactory
        implements ElementFactory {
        public SubImageElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMSubImageElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class SubImageRefElementFactory
        implements ElementFactory {
        public SubImageRefElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new SVGOMSubImageRefElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static HashTable xblFactories = new HashTable();
    static {
        xblFactories.put(XBLConstants.XBL_XBL_TAG,
                         new XBLXBLElementFactory());
        xblFactories.put(XBLConstants.XBL_DEFINITION_TAG,
                         new XBLDefinitionElementFactory());
        xblFactories.put(XBLConstants.XBL_TEMPLATE_TAG,
                         new XBLTemplateElementFactory());
        xblFactories.put(XBLConstants.XBL_CONTENT_TAG,
                         new XBLContentElementFactory());
        xblFactories.put(XBLConstants.XBL_HANDLER_GROUP_TAG,
                         new XBLHandlerGroupElementFactory());
        xblFactories.put(XBLConstants.XBL_IMPORT_TAG,
                         new XBLImportElementFactory());
        xblFactories.put(XBLConstants.XBL_SHADOW_TREE_TAG,
                         new XBLShadowTreeElementFactory());
    }
    protected static class XBLXBLElementFactory implements ElementFactory {
        public XBLXBLElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new XBLOMXBLElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class XBLDefinitionElementFactory
            implements ElementFactory {
        public XBLDefinitionElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new XBLOMDefinitionElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class XBLTemplateElementFactory
            implements ElementFactory {
        public XBLTemplateElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new XBLOMTemplateElement(prefix, (AbstractDocument) doc);
        }
    }
    protected static class XBLContentElementFactory
            implements ElementFactory {
        public XBLContentElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new XBLOMContentElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class XBLHandlerGroupElementFactory
            implements ElementFactory {
        public XBLHandlerGroupElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new XBLOMHandlerGroupElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class XBLImportElementFactory
            implements ElementFactory {
        public XBLImportElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new XBLOMImportElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class XBLShadowTreeElementFactory
            implements ElementFactory {
        public XBLShadowTreeElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new XBLOMShadowTreeElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static final DOMImplementation DOM_IMPLEMENTATION =
        new SVG12DOMImplementation();
    public static DOMImplementation getDOMImplementation() {
        return DOM_IMPLEMENTATION;
    }
}
