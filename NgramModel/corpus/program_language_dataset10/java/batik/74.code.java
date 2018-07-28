package org.apache.batik.apps.svgbrowser;
import javax.swing.Action;
public interface Application {
    JSVGViewerFrame createAndShowJSVGViewerFrame();
    void closeJSVGViewerFrame(JSVGViewerFrame f);
    Action createExitAction(JSVGViewerFrame vf);
    void openLink(String url);
    String getXMLParserClassName();
    boolean isXMLParserValidating();
    void showPreferenceDialog(JSVGViewerFrame f);
    String getLanguages();
    String getUserStyleSheetURI();
    String getDefaultFontFamily();
    String getMedia();
    boolean isSelectionOverlayXORMode();
    boolean canLoadScriptType(String scriptType);
    int getAllowedScriptOrigin();
    int getAllowedExternalResourceOrigin();
    void addVisitedURI(String uri);
    String[] getVisitedURIs();
    String getUISpecialization();
}
