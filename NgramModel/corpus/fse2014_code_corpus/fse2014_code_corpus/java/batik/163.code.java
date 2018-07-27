package org.apache.batik.bridge;
import java.awt.Cursor;
import java.util.Iterator;
import java.util.List;
import org.apache.batik.dom.events.AbstractEvent;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.svg.SVGOMAElement;
import org.apache.batik.dom.svg.SVGOMAnimationElement;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.svg.SVGAElement;
public class SVGAElementBridge extends SVGGElementBridge {
    protected AnchorListener          al;
    protected CursorMouseOverListener bl;
    protected CursorMouseOutListener  cl;
    public SVGAElementBridge() {}
    public String getLocalName() {
        return SVG_A_TAG;
    }
    public Bridge getInstance() {
        return new SVGAElementBridge();
    }
    public void buildGraphicsNode(BridgeContext ctx,
                                  Element e,
                                  GraphicsNode node) {
        super.buildGraphicsNode(ctx, e, node);
        if (ctx.isInteractive()) {
            NodeEventTarget target = (NodeEventTarget)e;
            CursorHolder ch = new CursorHolder(CursorManager.DEFAULT_CURSOR);
            al = new AnchorListener(ctx.getUserAgent(), ch);
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_CLICK,
                 al, false, null);
            ctx.storeEventListenerNS
                (target, 
                 XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_CLICK,
                 al, false);
            bl = new CursorMouseOverListener(ctx.getUserAgent(), ch);
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOVER,
                 bl, false, null);
            ctx.storeEventListenerNS
                (target, 
                 XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOVER,
                 bl, false);
            cl = new CursorMouseOutListener(ctx.getUserAgent(), ch);
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOUT,
                 cl, false, null);
            ctx.storeEventListenerNS
                (target, 
                 XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOUT,
                 cl, false);
        }
    }
    public void dispose() {
        NodeEventTarget target = (NodeEventTarget)e;
        if (al != null) {
            target.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_CLICK, 
                 al, false);
            al = null;
        }
        if (bl != null) {
            target.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOVER, 
                 bl, false);
            bl = null;
        }
        if (cl != null) {
            target.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOUT, 
                 cl, false);
            cl = null;
        }
        super.dispose();
    }
    public boolean isComposite() {
        return true;
    }
    public static class CursorHolder {
        Cursor cursor = null;
        public CursorHolder(Cursor c) {
            cursor = c;
        }
        public void holdCursor(Cursor c) {
            cursor = c;
        }
        public Cursor getCursor() {
            return cursor;
        }
    }
    public static class AnchorListener implements EventListener {
        protected UserAgent userAgent;
        protected CursorHolder holder;
        public AnchorListener(UserAgent ua, CursorHolder ch) {
            userAgent = ua;
            holder    = ch;
        }
        public void handleEvent(Event evt) {
            if (!(evt instanceof AbstractEvent)) return;
            final AbstractEvent ae = (AbstractEvent)evt;
            List l = ae.getDefaultActions();
            if (l != null) {
                Iterator i = l.iterator();
                while (i.hasNext()) {
                    Object o = i.next();
                    if (o instanceof AnchorDefaultActionable)
                        return; 
                }
            }
            SVGAElement elt = (SVGAElement)evt.getCurrentTarget();
            ae.addDefaultAction(new AnchorDefaultActionable
                                (elt, userAgent, holder));
        }
    }
    public static class AnchorDefaultActionable implements Runnable {
        protected SVGOMAElement elt;
        protected UserAgent     userAgent;
        protected CursorHolder  holder;
        public AnchorDefaultActionable(SVGAElement   e, 
                                       UserAgent     ua, 
                                       CursorHolder  ch) {
            elt       = (SVGOMAElement) e;
            userAgent = ua;
            holder    = ch;
        }
        public void run() {
            userAgent.setSVGCursor(holder.getCursor());
            String href = elt.getHref().getAnimVal();
            ParsedURL purl = new ParsedURL(elt.getBaseURI(), href);
            SVGOMDocument doc = (SVGOMDocument) elt.getOwnerDocument();
            ParsedURL durl = doc.getParsedURL();
            if (purl.sameFile(durl)) {
                String frag = purl.getRef();
                if (frag != null && frag.length() != 0) {
                    Element refElt = doc.getElementById(frag);
                    if (refElt instanceof SVGOMAnimationElement) {
                        SVGOMAnimationElement aelt =
                            (SVGOMAnimationElement) refElt;
                        float t = aelt.getHyperlinkBeginTime();
                        if (Float.isNaN(t)) {
                            aelt.beginElement();
                        } else {
                            doc.getRootElement().setCurrentTime(t);
                        }
                        return;
                    }
                }
            }
            userAgent.openLink(elt);
        }
    }
    public static class CursorMouseOverListener implements EventListener {
        protected UserAgent userAgent;
        protected CursorHolder holder;
        public CursorMouseOverListener(UserAgent ua, CursorHolder ch) {
            userAgent = ua;
            holder    = ch;
        }
        public void handleEvent(Event evt) {
            if (!(evt instanceof AbstractEvent)) return;
            final AbstractEvent ae = (AbstractEvent)evt;
            List l = ae.getDefaultActions();
            if (l != null) {
                Iterator i = l.iterator();
                while (i.hasNext()) {
                    Object o = i.next();
                    if (o instanceof MouseOverDefaultActionable)
                        return; 
                }
            }
            Element     target     = (Element)ae.getTarget();
            SVGAElement elt        = (SVGAElement)ae.getCurrentTarget();
            ae.addDefaultAction(new MouseOverDefaultActionable
                                (target, elt, userAgent, holder));
        }
    }
    public static class MouseOverDefaultActionable implements Runnable {
        protected Element       target;
        protected SVGAElement   elt;
        protected UserAgent     userAgent;
        protected CursorHolder  holder;
        public MouseOverDefaultActionable(Element       t,
                                          SVGAElement   e,
                                          UserAgent     ua, 
                                          CursorHolder  ch) {
            target    = t;
            elt       = e;
            userAgent = ua;
            holder    = ch;
        }
        public void run() {
            if (CSSUtilities.isAutoCursor(target)) {
                holder.holdCursor(CursorManager.DEFAULT_CURSOR);
                userAgent.setSVGCursor(CursorManager.ANCHOR_CURSOR);
            }
            if (elt != null) {
                String href = elt.getHref().getAnimVal();
                userAgent.displayMessage(href);
            }
        }
    }
    public static class CursorMouseOutListener implements EventListener {
        protected UserAgent userAgent;
        protected CursorHolder holder;
        public CursorMouseOutListener(UserAgent ua, CursorHolder ch) {
            userAgent = ua;
            holder    = ch;
        }
        public void handleEvent(Event evt) {
            if (!(evt instanceof AbstractEvent)) return;
            final AbstractEvent ae = (AbstractEvent)evt;
            List l = ae.getDefaultActions();
            if (l != null) {
                Iterator i = l.iterator();
                while (i.hasNext()) {
                    Object o = i.next();
                    if (o instanceof MouseOutDefaultActionable)
                        return; 
                }
            }
            SVGAElement elt = (SVGAElement)evt.getCurrentTarget();
            ae.addDefaultAction(new MouseOutDefaultActionable
                                (elt, userAgent, holder));
        }
    }
    public static class MouseOutDefaultActionable implements Runnable {
        protected SVGAElement   elt;
        protected UserAgent     userAgent;
        protected CursorHolder  holder;
        public MouseOutDefaultActionable(SVGAElement   e,
                                         UserAgent     ua, 
                                         CursorHolder  ch) {
            elt       = e;
            userAgent = ua;
            holder    = ch;
        }
        public void run() {
            if (elt != null) {
                userAgent.displayMessage("");
            }
        }
    }
}
