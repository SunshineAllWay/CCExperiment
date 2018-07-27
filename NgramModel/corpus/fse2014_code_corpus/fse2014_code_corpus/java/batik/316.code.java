package org.apache.batik.css.engine.sac;
import java.util.Set;
import org.apache.batik.util.XMLConstants;
import org.w3c.css.sac.LangCondition;
import org.w3c.dom.Element;
public class CSSLangCondition
    implements LangCondition,
               ExtendedCondition {
    protected String lang;
    protected String langHyphen;
    public CSSLangCondition(String lang) {
        this.lang = lang.toLowerCase();
        this.langHyphen = lang + '-';
    }
    public boolean equals(Object obj) {
        if (obj == null || (obj.getClass() != getClass())) {
            return false;
        }
        CSSLangCondition c = (CSSLangCondition)obj;
        return c.lang.equals(lang);
    }
    public short getConditionType() {
        return SAC_LANG_CONDITION;
    }
    public String getLang() {
        return lang;
    }
    public int getSpecificity() {
        return 1 << 8;
    }
    public boolean match(Element e, String pseudoE) {
        String s = e.getAttribute("lang").toLowerCase();
        if (s.equals(lang) || s.startsWith(langHyphen)) {
            return true;
        }
        s = e.getAttributeNS(XMLConstants.XML_NAMESPACE_URI,
                             XMLConstants.XML_LANG_ATTRIBUTE).toLowerCase();
        return s.equals(lang) || s.startsWith(langHyphen);
    }
    public void fillAttributeSet(Set attrSet) {
        attrSet.add("lang");
    }
    public String toString() {
        return ":lang(" + lang + ')';
    }
}
