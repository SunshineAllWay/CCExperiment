package org.apache.batik.bridge;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import org.apache.batik.gvt.event.EventDispatcher;
import org.apache.batik.gvt.text.Mark;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAElement;
import org.w3c.dom.svg.SVGDocument;
public interface UserAgent {
    EventDispatcher getEventDispatcher();
    Dimension2D getViewportSize();
    void displayError(Exception ex);
    void displayMessage(String message);
    void showAlert(String message);
    String showPrompt(String message);
    String showPrompt(String message, String defaultValue);
    boolean showConfirm(String message);
    float getPixelUnitToMillimeter();
    float getPixelToMM();
    float getMediumFontSize();
    float getLighterFontWeight(float f);
    float getBolderFontWeight(float f);
    String getDefaultFontFamily();
    String getLanguages();
    String getUserStyleSheetURI();
    void openLink(SVGAElement elt);
    void setSVGCursor(Cursor cursor);
    void setTextSelection(Mark start, Mark end);
    void deselectAll();
    String getXMLParserClassName();
    boolean isXMLParserValidating();
    AffineTransform getTransform();
    void setTransform(AffineTransform at);
    String getMedia();
    String getAlternateStyleSheet();
    Point getClientAreaLocationOnScreen();
    boolean hasFeature(String s);
    boolean supportExtension(String s);
    void registerExtension(BridgeExtension ext);
    void handleElement(Element elt, Object data);
    ScriptSecurity getScriptSecurity(String scriptType,
                                     ParsedURL scriptURL,
                                     ParsedURL docURL);
    void checkLoadScript(String scriptType,
                         ParsedURL scriptURL,
                         ParsedURL docURL) throws SecurityException;
    ExternalResourceSecurity 
        getExternalResourceSecurity(ParsedURL resourceURL,
                                    ParsedURL docURL);
    void checkLoadExternalResource(ParsedURL resourceURL,
                                   ParsedURL docURL) throws SecurityException;
    SVGDocument getBrokenLinkDocument(Element e, String url, String message);
    void loadDocument(String url);
}
