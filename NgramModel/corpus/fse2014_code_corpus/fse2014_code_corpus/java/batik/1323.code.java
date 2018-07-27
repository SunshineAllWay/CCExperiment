package org.apache.batik.swing.svg;
import org.apache.batik.bridge.ExternalResourceSecurity;
import org.apache.batik.bridge.ScriptSecurity;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Element;
public interface SVGUserAgent {
    void displayError(String message);
    void displayError(Exception ex);
    void displayMessage(String message);
    void showAlert(String message);
    String showPrompt(String message);
    String showPrompt(String message, String defaultValue);
    boolean showConfirm(String message);
    float getPixelUnitToMillimeter();
    float getPixelToMM();
    String getDefaultFontFamily();
    float getMediumFontSize();
    float getLighterFontWeight(float f);
    float getBolderFontWeight(float f);
    String getLanguages();
    String getUserStyleSheetURI();
    String getXMLParserClassName();
    boolean isXMLParserValidating();
    String getMedia();
    String getAlternateStyleSheet();
    void openLink(String uri, boolean newc);
    boolean supportExtension(String s);
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
}
