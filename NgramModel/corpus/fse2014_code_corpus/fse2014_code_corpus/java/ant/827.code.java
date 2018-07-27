package org.apache.tools.ant.util;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.apache.tools.ant.DynamicElementNS;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.DynamicConfiguratorNS;
public class XMLFragment extends ProjectComponent implements DynamicElementNS {
    private Document doc;
    private DocumentFragment fragment;
    public XMLFragment() {
        doc = JAXPUtils.getDocumentBuilder().newDocument();
        fragment = doc.createDocumentFragment();
    }
    public DocumentFragment getFragment() {
        return fragment;
    }
    public void addText(String s) {
        addText(fragment, s);
    }
    public Object createDynamicElement(String uri, String name, String qName) {
        Element e = null;
        if (uri.equals("")) {
            e = doc.createElement(name);
        } else {
            e = doc.createElementNS(uri, qName);
        }
        fragment.appendChild(e);
        return new Child(e);
    }
    private void addText(Node n, String s) {
        s = getProject().replaceProperties(s);
        if (s != null && !s.trim().equals("")) {
            Text t = doc.createTextNode(s.trim());
            n.appendChild(t);
        }
    }
    public class Child implements DynamicConfiguratorNS {
        private Element e;
        Child(Element e) {
            this.e = e;
        }
        public void addText(String s) {
            XMLFragment.this.addText(e, s);
        }
        public void setDynamicAttribute(
            String uri, String name, String qName, String value) {
            if (uri.equals("")) {
                e.setAttribute(name, value);
            } else {
                e.setAttributeNS(uri, qName, value);
            }
        }
        public Object createDynamicElement(String uri, String name, String qName) {
            Element e2 = null;
            if (uri.equals("")) {
                e2 = doc.createElement(name);
            } else {
                e2 = doc.createElementNS(uri, qName);
            }
            e.appendChild(e2);
            return new Child(e2);
        }
    }
}
