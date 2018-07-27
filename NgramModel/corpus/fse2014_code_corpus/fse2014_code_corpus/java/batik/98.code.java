package org.apache.batik.apps.svgbrowser;
import java.io.File;
import org.apache.batik.util.ParsedURL;
public interface SquiggleInputHandler {
    String[] getHandledMimeTypes();
    String[] getHandledExtensions();
    String getDescription();
    boolean accept(File f);
    boolean accept(ParsedURL purl);
    void handle(ParsedURL purl, JSVGViewerFrame svgFrame) throws Exception ;
}
