package org.apache.batik.css.engine;
import org.w3c.css.sac.SACMediaList;
public class StyleSheet {
    protected Rule[] rules = new Rule[16];
    protected int size;
    protected StyleSheet parent;
    protected boolean alternate;
    protected SACMediaList media;
    protected String title;
    public void setMedia(SACMediaList m) {
        media = m;
    }
    public SACMediaList getMedia() {
        return media;
    }
    public StyleSheet getParent() {
        return parent;
    }
    public void setParent(StyleSheet ss) {
        parent = ss;
    }
    public void setAlternate(boolean b) {
        alternate = b;
    }
    public boolean isAlternate() {
        return alternate;
    }
    public void setTitle(String t) {
        title = t;
    }
    public String getTitle() {
        return title;
    }
    public int getSize() {
        return size;
    }
    public Rule getRule(int i) {
        return rules[i];
    }
    public void clear() {
        size = 0;
        rules = new Rule[10];
    }
    public void append(Rule r) {
        if (size == rules.length) {
            Rule[] t = new Rule[size * 2];
            System.arraycopy( rules, 0, t, 0, size );
            rules = t;
        }
        rules[size++] = r;
    }
    public String toString(CSSEngine eng) {
        StringBuffer sb = new StringBuffer( size * 8 );
        for (int i = 0; i < size; i++) {
            sb.append(rules[i].toString(eng));
        }
        return sb.toString();
    }
}
