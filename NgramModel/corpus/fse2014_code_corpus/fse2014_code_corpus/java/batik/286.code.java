package org.apache.batik.css.engine;
import org.apache.batik.util.ParsedURL;
public class FontFaceRule implements Rule {
    public static final short TYPE = (short)3;
    StyleMap sm;
    ParsedURL purl;
    public FontFaceRule(StyleMap sm, ParsedURL purl) {
        this.sm = sm;
        this.purl = purl;
    }
    public short getType() { return TYPE; }
    public ParsedURL getURL() {
        return purl;
    }
    public StyleMap getStyleMap() {
        return sm;
    }
    public String toString(CSSEngine eng) {
        StringBuffer sb = new StringBuffer();
        sb.append("@font-face { ");
        sb.append(sm.toString(eng));
        sb.append(" }\n");
        return sb.toString();
    }
}
