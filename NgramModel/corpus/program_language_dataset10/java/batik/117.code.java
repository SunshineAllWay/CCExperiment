package org.apache.batik.bridge;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.batik.dom.anim.AnimationTarget;
import org.apache.batik.dom.anim.AnimationTargetListener;
import org.apache.batik.dom.svg.SVGAnimationTargetContext;
import org.w3c.dom.Element;
public abstract class AnimatableSVGBridge
        extends AbstractSVGBridge
        implements SVGAnimationTargetContext {
    protected Element e;
    protected BridgeContext ctx;
    protected HashMap targetListeners;
    public void addTargetListener(String pn, AnimationTargetListener l) {
        if (targetListeners == null) {
            targetListeners = new HashMap();
        }
        LinkedList ll = (LinkedList) targetListeners.get(pn);
        if (ll == null) {
            ll = new LinkedList();
            targetListeners.put(pn, ll);
        }
        ll.add(l);
    }
    public void removeTargetListener(String pn, AnimationTargetListener l) {
        LinkedList ll = (LinkedList) targetListeners.get(pn);
        ll.remove(l);
    }
    protected void fireBaseAttributeListeners(String pn) {
        if (targetListeners != null) {
            LinkedList ll = (LinkedList) targetListeners.get(pn);
            if (ll != null) {
                Iterator it = ll.iterator();
                while (it.hasNext()) {
                    AnimationTargetListener l =
                        (AnimationTargetListener) it.next();
                    l.baseValueChanged((AnimationTarget) e, null, pn, true);
                }
            }
        }
    }
}
