package org.apache.batik.svggen;
import java.awt.Font;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext.GraphicContextDefaults;
import org.apache.batik.util.SVGConstants;
public class GeneratorContext extends SVGAccuracyTest implements SVGConstants {
    public static class TestIDGenerator extends SVGIDGenerator {
        public String generateID(String prefix) {
            return "test"+super.generateID(prefix);
        }
    }
    public static class TestStyleHandler extends DefaultStyleHandler {
        private CDATASection styleSheet;
        public TestStyleHandler(CDATASection styleSheet) {
            this.styleSheet = styleSheet;
        }
        public void setStyle(Element element, Map styleMap,
                             SVGGeneratorContext generatorContext) {
            Iterator iter = styleMap.keySet().iterator();
            String id = generatorContext.getIDGenerator().generateID("C");
            styleSheet.appendData("."+id+" {");
            while (iter.hasNext()) {
                String key = (String)iter.next();
                String value = (String)styleMap.get(key);
                styleSheet.appendData(key+":"+value+";");
            }
            styleSheet.appendData("}\n");
            element.setAttribute("class", id);
        }
    }
    private Element topLevelGroup = null;
    public GeneratorContext(Painter painter,
                            URL refURL) {
        super(painter, refURL);
    }
    protected SVGGraphics2D buildSVGGraphics2D(){
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String namespaceURI = SVGDOMImplementation.SVG_NAMESPACE_URI;
        Document domFactory = impl.createDocument(namespaceURI, SVG_SVG_TAG, null);
        SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(domFactory);
        ctx.setIDGenerator(new TestIDGenerator());
        GenericImageHandler ihandler = new CachedImageHandlerBase64Encoder();
        ctx.setGenericImageHandler(ihandler);
        CDATASection styleSheet = domFactory.createCDATASection("");
        ctx.setStyleHandler(new TestStyleHandler(styleSheet));
        ctx.setComment("Generated by the Batik Test Framework. Test:\u00e9j");
        ctx.setEmbeddedFontsOn(true);
        GraphicContextDefaults defaults 
            = new GraphicContextDefaults();
        defaults.font = new Font("Arial", Font.PLAIN, 12);
        ctx.setGraphicContextDefaults(defaults);
        SVGGraphics2D g2d = new SVGGraphics2D(ctx, false);
        topLevelGroup = g2d.getTopLevelGroup();
        Element style = domFactory.createElementNS(SVG_NAMESPACE_URI, SVG_STYLE_TAG);
        style.setAttributeNS(null, SVG_TYPE_ATTRIBUTE, "text/css");
        style.appendChild(styleSheet);
        topLevelGroup.appendChild(style);
        return g2d;
    }
    protected void configureSVGGraphics2D(SVGGraphics2D g2d) {
        topLevelGroup.appendChild(g2d.getTopLevelGroup());
        g2d.setTopLevelGroup(topLevelGroup);
    }
}
