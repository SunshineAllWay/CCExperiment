package org.apache.batik.dom.events;
import org.apache.batik.dom.util.HashTable;
import org.w3c.dom.DOMException;
import org.w3c.dom.events.Event;
public class DocumentEventSupport {
    public static final String EVENT_TYPE = "Event";
    public static final String MUTATION_EVENT_TYPE = "MutationEvent";
    public static final String MUTATION_NAME_EVENT_TYPE = "MutationNameEvent";
    public static final String MOUSE_EVENT_TYPE = "MouseEvent";
    public static final String UI_EVENT_TYPE = "UIEvent";
    public static final String KEYBOARD_EVENT_TYPE = "KeyboardEvent";
    public static final String TEXT_EVENT_TYPE = "TextEvent";
    public static final String CUSTOM_EVENT_TYPE = "CustomEvent";
    public static final String EVENT_DOM2_TYPE = "Events";
    public static final String MUTATION_EVENT_DOM2_TYPE = "MutationEvents";
    public static final String MOUSE_EVENT_DOM2_TYPE = "MouseEvents";
    public static final String UI_EVENT_DOM2_TYPE = "UIEvents";
    public static final String KEY_EVENT_DOM2_TYPE = "KeyEvents";
    protected HashTable eventFactories = new HashTable();
    {
        eventFactories.put(EVENT_TYPE.toLowerCase(),
                           new SimpleEventFactory());
        eventFactories.put(MUTATION_EVENT_TYPE.toLowerCase(),
                           new MutationEventFactory());
        eventFactories.put(MUTATION_NAME_EVENT_TYPE.toLowerCase(),
                           new MutationNameEventFactory());
        eventFactories.put(MOUSE_EVENT_TYPE.toLowerCase(),
                           new MouseEventFactory());
        eventFactories.put(KEYBOARD_EVENT_TYPE.toLowerCase(),
                           new KeyboardEventFactory());
        eventFactories.put(UI_EVENT_TYPE.toLowerCase(),
                           new UIEventFactory());
        eventFactories.put(TEXT_EVENT_TYPE.toLowerCase(),
                           new TextEventFactory());
        eventFactories.put(CUSTOM_EVENT_TYPE.toLowerCase(),
                           new CustomEventFactory());
        eventFactories.put(EVENT_DOM2_TYPE.toLowerCase(),
                           new SimpleEventFactory());
        eventFactories.put(MUTATION_EVENT_DOM2_TYPE.toLowerCase(),
                           new MutationEventFactory());
        eventFactories.put(MOUSE_EVENT_DOM2_TYPE.toLowerCase(),
                           new MouseEventFactory());
        eventFactories.put(KEY_EVENT_DOM2_TYPE.toLowerCase(),
                           new KeyEventFactory());
        eventFactories.put(UI_EVENT_DOM2_TYPE.toLowerCase(),
                           new UIEventFactory());
    }
    public Event createEvent(String eventType)
            throws DOMException {
        EventFactory ef = (EventFactory)eventFactories.get(eventType.toLowerCase());
        if (ef == null) {
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                                   "Bad event type: " + eventType);
        }
        return ef.createEvent();
    }
    public void registerEventFactory(String eventType,
                                            EventFactory factory) {
        eventFactories.put(eventType.toLowerCase(), factory);
    }
    public interface EventFactory {
        Event createEvent();
    }
    protected static class SimpleEventFactory implements EventFactory {
        public Event createEvent() {
            return new DOMEvent();
        }
    }
    protected static class MutationEventFactory implements EventFactory {
        public Event createEvent() {
            return new DOMMutationEvent();
        }
    }
    protected static class MutationNameEventFactory implements EventFactory {
        public Event createEvent() {
            return new DOMMutationNameEvent();
        }
    }
    protected static class MouseEventFactory implements EventFactory {
        public Event createEvent() {
            return new DOMMouseEvent();
        }
    }
    protected static class KeyEventFactory implements EventFactory {
        public Event createEvent() {
            return new DOMKeyEvent();
        }
    }
    protected static class KeyboardEventFactory implements EventFactory {
        public Event createEvent() {
            return new DOMKeyboardEvent();
        }
    }
    protected static class UIEventFactory implements EventFactory {
        public Event createEvent() {
            return new DOMUIEvent();
        }
    }
    protected static class TextEventFactory implements EventFactory {
        public Event createEvent() {
            return new DOMTextEvent();
        }
    }
    protected static class CustomEventFactory implements EventFactory {
        public Event createEvent() {
            return new DOMCustomEvent();
        }
    }
}
