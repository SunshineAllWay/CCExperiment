package org.apache.batik.dom.svg;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStyleSheetNode;
import org.apache.batik.css.engine.StyleSheet;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.StyleSheetFactory;
import org.apache.batik.dom.StyleSheetProcessingInstruction;
import org.apache.batik.dom.util.HashTable;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
public class SVGStyleSheetProcessingInstruction
    extends StyleSheetProcessingInstruction
    implements CSSStyleSheetNode {
    protected StyleSheet styleSheet;
    protected SVGStyleSheetProcessingInstruction() {
    }
    public SVGStyleSheetProcessingInstruction(String            data,
                                              AbstractDocument  owner,
                                              StyleSheetFactory f) {
        super(data, owner, f);
    }
    public String getStyleSheetURI() {
        SVGOMDocument svgDoc = (SVGOMDocument) getOwnerDocument();
        ParsedURL url = svgDoc.getParsedURL();
        String href = (String)getPseudoAttributes().get("href");
        if (url != null) {
            return new ParsedURL(url, href).toString();
        }
        return href;
    }
    public StyleSheet getCSSStyleSheet() {
        if (styleSheet == null) {
            HashTable attrs = getPseudoAttributes();
            String type = (String)attrs.get("type");
            if ("text/css".equals(type)) {
                String title     = (String)attrs.get("title");
                String media     = (String)attrs.get("media");
                String href      = (String)attrs.get("href");
                String alternate = (String)attrs.get("alternate");
                SVGOMDocument doc = (SVGOMDocument)getOwnerDocument();
                ParsedURL durl = doc.getParsedURL();
                ParsedURL burl = new ParsedURL(durl, href);
                CSSEngine e = doc.getCSSEngine();
                styleSheet = e.parseStyleSheet(burl, media);
                styleSheet.setAlternate("yes".equals(alternate));
                styleSheet.setTitle(title);
            }
        }
        return styleSheet;
    }
    public void setData(String data) throws DOMException {
        super.setData(data);
        styleSheet = null;
    }
    protected Node newNode() {
        return new SVGStyleSheetProcessingInstruction();
    }
}
