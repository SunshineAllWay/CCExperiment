package org.apache.batik.css.engine;
import org.apache.batik.util.ParsedURL;
public class ImportRule extends MediaRule {
    public static final short TYPE = (short)2;
    protected ParsedURL uri;
    public short getType() {
        return TYPE;
    }
    public void setURI(ParsedURL u) {
        uri = u;
    }
    public ParsedURL getURI() {
        return uri;
    }
    public String toString(CSSEngine eng) {
        StringBuffer sb = new StringBuffer();
        sb.append("@import \"");
        sb.append(uri);
        sb.append("\"");
        if (mediaList != null) {
            for (int i = 0; i < mediaList.getLength(); i++) {
                sb.append(' ');
                sb.append(mediaList.item(i));
            }
        }
        sb.append(";\n");
        return sb.toString();
    }
}
