package org.apache.batik.bridge;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.batik.gvt.event.EventDispatcher;
import org.apache.batik.gvt.text.Mark;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGFeatureStrings;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAElement;
import org.w3c.dom.svg.SVGDocument;
public class UserAgentAdapter implements UserAgent {
    protected Set FEATURES   = new HashSet();
    protected Set extensions = new HashSet();
    protected BridgeContext ctx;
    public void setBridgeContext(BridgeContext ctx) {
        this.ctx = ctx;
    }
    public void addStdFeatures() {
        SVGFeatureStrings.addSupportedFeatureStrings(FEATURES);
    }
    public Dimension2D getViewportSize() {
        return new Dimension(1, 1);
    }
    public void displayMessage(String message) {
    }
    public void displayError(String message) {
        displayMessage(message);
    }
    public void displayError(Exception e) {
        displayError(e.getMessage());
    }
    public void showAlert(String message) {
    }
    public String showPrompt(String message) {
        return null;
    }
    public String showPrompt(String message, String defaultValue) {
        return null;
    }
    public boolean showConfirm(String message) {
        return false;
    }
    public float getPixelUnitToMillimeter() {
        return 0.26458333333333333333333333333333f; 
    }
    public float getPixelToMM() {
        return getPixelUnitToMillimeter();
    }
    public String getDefaultFontFamily() {
        return "Arial, Helvetica, sans-serif";
    }
    public float getMediumFontSize() {
        return 9f * 25.4f / (72f * getPixelUnitToMillimeter());
    }
    public float getLighterFontWeight(float f) { 
        return getStandardLighterFontWeight(f);
    }
    public float getBolderFontWeight(float f) {
        return getStandardBolderFontWeight(f);
    }
    public String getLanguages() {
        return "en";
    }
    public String getMedia() {
        return "all";
    }
    public String getAlternateStyleSheet() {
        return null;
    }
    public String getUserStyleSheetURI() {
        return null;
    }
    public String getXMLParserClassName() {
        return XMLResourceDescriptor.getXMLParserClassName();
    }
    public boolean isXMLParserValidating() {
        return false;
    }
    public EventDispatcher getEventDispatcher() {
        return null;
    }
    public void openLink(SVGAElement elt) { }
    public void setSVGCursor(Cursor cursor) { }
    public void setTextSelection(Mark start, Mark end) { }
    public void deselectAll() { }
    public void runThread(Thread t) { }
    public AffineTransform getTransform() {
        return null;
    }
    public void setTransform(AffineTransform at) {
    }
    public Point getClientAreaLocationOnScreen() {
        return new Point();
    }
    public boolean hasFeature(String s) {
        return FEATURES.contains(s);
    }
    public boolean supportExtension(String s) {
        return extensions.contains(s);
    }
    public void registerExtension(BridgeExtension ext) {
        Iterator i = ext.getImplementedExtensions();
        while (i.hasNext())
            extensions.add(i.next());
    }
    public void handleElement(Element elt, Object data){
    }
    public ScriptSecurity getScriptSecurity(String    scriptType,
                                            ParsedURL scriptURL,
                                            ParsedURL docURL){
        return new DefaultScriptSecurity(scriptType, scriptURL, docURL);
    }
    public void checkLoadScript(String scriptType,
                                ParsedURL scriptURL,
                                ParsedURL docURL) throws SecurityException {
        ScriptSecurity s = getScriptSecurity(scriptType,
                                             scriptURL,
                                             docURL);
        if (s != null) {
            s.checkLoadScript();
        }
    }
    public ExternalResourceSecurity 
        getExternalResourceSecurity(ParsedURL resourceURL,
                                    ParsedURL docURL) {
        return new RelaxedExternalResourceSecurity(resourceURL, docURL);
    }
    public void 
        checkLoadExternalResource(ParsedURL resourceURL,
                                  ParsedURL docURL) throws SecurityException {
        ExternalResourceSecurity s 
            =  getExternalResourceSecurity(resourceURL, docURL);
        if (s != null) {
            s.checkLoadExternalResource();
        }
    }
    public static float getStandardLighterFontWeight(float f) {
        int weight = ((int)((f+50)/100))*100;
        switch (weight) {
        case 100: return 100;
        case 200: return 100;
        case 300: return 200;
        case 400: return 300;
        case 500: return 400;
        case 600: return 400;
        case 700: return 400;
        case 800: return 400;
        case 900: return 400;
        default:
            throw new IllegalArgumentException("Bad Font Weight: " + f);
        }
    }
    public static float getStandardBolderFontWeight(float f) {
        int weight = ((int)((f+50)/100))*100;
        switch (weight) {
        case 100: return 600;
        case 200: return 600;
        case 300: return 600;
        case 400: return 600;
        case 500: return 600;
        case 600: return 700;
        case 700: return 800;
        case 800: return 900;
        case 900: return 900;
        default:
            throw new IllegalArgumentException("Bad Font Weight: " + f);
        }
    }
    public SVGDocument getBrokenLinkDocument(Element e, 
                                             String url, 
                                             String message) {
        throw new BridgeException(ctx, e, ErrorConstants.ERR_URI_IMAGE_BROKEN,
                                  new Object[] {url, message });
    }
    public void loadDocument(String url) {
    }
}
