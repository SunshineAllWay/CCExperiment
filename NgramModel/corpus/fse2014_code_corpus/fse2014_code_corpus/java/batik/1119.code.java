package org.apache.batik.script;
import org.w3c.dom.Document;
public interface ScriptHandler {
    void run(Document doc, Window win);
}
