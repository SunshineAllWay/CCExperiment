package org.apache.batik.extension.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.extension.PrefixableStylableExtensionElement;
import org.w3c.dom.Node;
public class ColorSwitchElement
    extends    PrefixableStylableExtensionElement 
    implements BatikExtConstants {
    protected ColorSwitchElement() {
    }
    public ColorSwitchElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return BATIK_EXT_COLOR_SWITCH_TAG;
    }
    public String getNamespaceURI() {
        return BATIK_EXT_NAMESPACE_URI;
    }
    protected Node newNode() {
        return new ColorSwitchElement();
    }
}
