package org.apache.batik.apps.svgbrowser;
import java.io.File;
import org.apache.batik.util.ParsedURL;
public class SVGInputHandler implements SquiggleInputHandler {
    public static final String[] SVG_MIME_TYPES = 
    { "image/svg+xml" };
    public static final String[] SVG_FILE_EXTENSIONS =
    { ".svg", ".svgz" };
    public String[] getHandledMimeTypes() {
        return SVG_MIME_TYPES;
    }
    public String[] getHandledExtensions() {
        return SVG_FILE_EXTENSIONS;
    }
    public String getDescription() {
        return "";
    }
    public void handle(ParsedURL purl, JSVGViewerFrame svgViewerFrame) {
        svgViewerFrame.getJSVGCanvas().loadSVGDocument(purl.toString());
    }
    public boolean accept(File f) {
        return f != null && f.isFile() && accept(f.getPath());
    }
    public boolean accept(ParsedURL purl) {
        if (purl == null) {
            return false;
        }
        String path = purl.getPath();
        if (path == null) return false;
        return accept(path);
    }
    public boolean accept(String path) {
        if (path == null) return false;
        for (int i=0; i<SVG_FILE_EXTENSIONS.length; i++) {
            if (path.endsWith(SVG_FILE_EXTENSIONS[i])) {
                return true;
            }
        }
        return false;
    }
}
