package org.apache.batik.dom.events;
import org.w3c.dom.events.CustomEvent;
public class DOMCustomEvent extends DOMEvent implements CustomEvent {
    protected Object detail;
    public Object getDetail() {
        return detail;
    }
    public void initCustomEventNS(String namespaceURIArg,
                                  String typeArg,
                                  boolean canBubbleArg,
                                  boolean cancelableArg,
                                  Object detailArg) {
        initEventNS(namespaceURIArg, typeArg, canBubbleArg, cancelableArg);
        detail = detailArg;
    }
}
