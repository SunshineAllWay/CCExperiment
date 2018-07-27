package com.test.script;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.EventListenerInitializer;
import org.w3c.dom.svg.SVGDocument;
public class EventListenerInitializerImpl implements EventListenerInitializer {
    public void initializeEventListeners(SVGDocument doc) {
        System.err.println(">>>>>>>>>>>>>>>>>>> SVGDocument : " + doc);
        ((EventTarget)doc.getElementById("testContent")).
            addEventListener("mousedown", new EventListener() {
                public void handleEvent(Event evt) {
                    ((Element)evt.getTarget()).setAttributeNS(null, "fill", "orange");
                }
            }, false);
    }
}
