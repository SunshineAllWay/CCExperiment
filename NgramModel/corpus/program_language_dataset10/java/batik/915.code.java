package org.apache.batik.extension;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.DOMUtilities;
import org.w3c.dom.DOMException;
public abstract class PrefixableStylableExtensionElement
    extends StylableExtensionElement {
    protected String prefix = null;
    protected PrefixableStylableExtensionElement() {
    }
    public PrefixableStylableExtensionElement(String prefix,
                                              AbstractDocument owner) {
        super(prefix, owner);
        setPrefix(prefix);
    }
    public String getNodeName() {
        return (prefix == null || prefix.equals(""))
            ? getLocalName() : prefix + ':' + getLocalName();
    }
    public void setPrefix(String prefix) throws DOMException {
        if (isReadonly()) {
            throw createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "readonly.node",
                 new Object[] { new Integer(getNodeType()), getNodeName() });
        }
        if (prefix != null &&
            !prefix.equals("") &&
            !DOMUtilities.isValidName(prefix)) {
            throw createDOMException
                (DOMException.INVALID_CHARACTER_ERR, "prefix",
                 new Object[] { new Integer(getNodeType()),
                                getNodeName(),
                                prefix });
        }
        this.prefix = prefix;
    }
}
