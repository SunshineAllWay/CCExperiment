package org.w3c.dom.html;
import org.w3c.dom.DOMImplementation;
public interface HTMLDOMImplementation extends DOMImplementation {
    public HTMLDocument createHTMLDocument(String title);
}
