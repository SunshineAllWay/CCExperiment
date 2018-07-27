package org.apache.xerces.dom.events;
import org.w3c.dom.events.UIEvent;
import org.w3c.dom.views.AbstractView;
public class UIEventImpl 
    extends EventImpl 
    implements UIEvent {
    private AbstractView fView;
    private int fDetail;
    public AbstractView getView() {
        return fView;
    }
    public int getDetail() {
        return fDetail;
    }
    public void initUIEvent(String typeArg, boolean canBubbleArg, boolean cancelableArg, 
            AbstractView viewArg, int detailArg) {
        fView = viewArg;
        fDetail = detailArg;
        super.initEvent(typeArg, canBubbleArg, cancelableArg);
    }
}
