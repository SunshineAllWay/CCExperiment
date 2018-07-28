package org.apache.batik.script;
import org.apache.batik.bridge.BridgeContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
public interface Window extends org.w3c.dom.Window {
    Object setInterval(String script, long interval);
    Object setInterval(Runnable r, long interval);
    void clearInterval(Object interval);
    Object setTimeout(String script, long timeout);
    Object setTimeout(Runnable r, long timeout);
    void clearTimeout(Object timeout);
    Node parseXML(String text, Document doc);
    String printNode(Node n);
    void getURL(String uri, URLResponseHandler h);
    void getURL(String uri, URLResponseHandler h, String enc);
    void postURL(String uri, String content, URLResponseHandler h);
    void postURL(String uri, String content, URLResponseHandler h,
                 String mimeType);
    void postURL(String uri, String content, URLResponseHandler h,
                 String mimeType, String enc);
    interface URLResponseHandler {
        void getURLDone(boolean success, String mime, String content);
    }
    void alert(String message);
    boolean confirm(String message);
    String prompt(String message);
    String prompt(String message, String defVal);
    BridgeContext getBridgeContext();
    Interpreter getInterpreter();
}
