package org.apache.batik.util.gui.resource;
import javax.swing.Action;
public interface ActionMap {
    Action getAction(String key) throws MissingListenerException;
}
