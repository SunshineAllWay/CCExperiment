package org.apache.batik.css.engine;
import org.w3c.css.sac.SACMediaList;
public class MediaRule extends StyleSheet implements Rule {
    public static final short TYPE = (short)1;
    protected SACMediaList mediaList;
    public short getType() {
        return TYPE;
    }
    public void setMediaList(SACMediaList ml) {
        mediaList = ml;
    }
    public SACMediaList getMediaList() {
        return mediaList;
    }
    public String toString(CSSEngine eng) {
        StringBuffer sb = new StringBuffer();
        sb.append("@media");
        if (mediaList != null) {
            for (int i = 0; i < mediaList.getLength(); i++) {
                sb.append(' ');
                sb.append(mediaList.item(i));
            }
        }
        sb.append(" {\n");
        for (int i = 0; i < size; i++) {
            sb.append(rules[i].toString(eng));
        }
        sb.append("}\n");
        return sb.toString();
    }
}
