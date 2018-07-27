package org.apache.batik.dom.svg;
import org.w3c.dom.Element;
public class LiveAttributeException extends RuntimeException {
    public static final short ERR_ATTRIBUTE_MISSING   = 0;
    public static final short ERR_ATTRIBUTE_MALFORMED = 1;
    public static final short ERR_ATTRIBUTE_NEGATIVE  = 2;
    protected Element e;
    protected String attributeName;
    protected short code;
    protected String value;
    public LiveAttributeException(Element e, String an, short code,
                                  String val) {
        this.e = e;
        this.attributeName = an;
        this.code = code;
        this.value = val;
    }
    public Element getElement() {
        return e;
    }
    public String getAttributeName() {
        return attributeName;
    }
    public short getCode() {
        return code;
    }
    public String getValue() {
        return value;
    }
}
