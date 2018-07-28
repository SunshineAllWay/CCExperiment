package org.apache.batik.dom.svg;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.batik.anim.values.AnimatableValue;
public abstract class AbstractSVGAnimatedValue
    implements AnimatedLiveAttributeValue {
    protected AbstractElement element;
    protected String namespaceURI;
    protected String localName;
    protected boolean hasAnimVal;
    protected LinkedList listeners = new LinkedList();
    public AbstractSVGAnimatedValue(AbstractElement elt, String ns, String ln) {
        element = elt;
        namespaceURI = ns;
        localName = ln;
    }
    public String getNamespaceURI() {
        return namespaceURI;
    }
    public String getLocalName() {
        return localName;
    }
    public boolean isSpecified() {
        return hasAnimVal || element.hasAttributeNS(namespaceURI, localName);
    }
    protected abstract void updateAnimatedValue(AnimatableValue val);
    public void addAnimatedAttributeListener(AnimatedAttributeListener aal) {
        if (!listeners.contains(aal)) {
            listeners.add(aal);
        }
    }
    public void removeAnimatedAttributeListener(AnimatedAttributeListener aal) {
        listeners.remove(aal);
    }
    protected void fireBaseAttributeListeners() {
        if (element instanceof SVGOMElement) {
            ((SVGOMElement) element).fireBaseAttributeListeners(namespaceURI,
                                                                localName);
        }
    }
    protected void fireAnimatedAttributeListeners() {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            AnimatedAttributeListener listener =
                (AnimatedAttributeListener) i.next();
            listener.animatedAttributeChanged(element, this);
        }
    }
}
