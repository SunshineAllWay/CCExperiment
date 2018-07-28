package org.apache.batik.apps.svgbrowser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.DocumentFactory;
import org.apache.batik.dom.util.SAXDocumentFactory;
import org.apache.batik.util.PreferenceManager;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
public class XMLPreferenceManager extends PreferenceManager {
    protected String xmlParserClassName;
    public static final String PREFERENCE_ENCODING = "8859_1";
    public XMLPreferenceManager(String prefFileName){
        this(prefFileName, null, 
             XMLResourceDescriptor.getXMLParserClassName());
    }
    public XMLPreferenceManager(String prefFileName,
                                Map defaults){
        this(prefFileName, defaults, 
             XMLResourceDescriptor.getXMLParserClassName());
    }
    public XMLPreferenceManager(String prefFileName, String parser) {
        this(prefFileName, null, parser);
    }
    public XMLPreferenceManager(String prefFileName, Map defaults, String parser) {
        super(prefFileName, defaults);
        internal = new XMLProperties();
        xmlParserClassName = parser;
    }
    protected class XMLProperties extends Properties {
        public synchronized void load(InputStream is) throws IOException {
            BufferedReader r;
            r = new BufferedReader(new InputStreamReader(is, PREFERENCE_ENCODING));
            DocumentFactory df = new SAXDocumentFactory
                (GenericDOMImplementation.getDOMImplementation(),
                 xmlParserClassName);
            Document doc = df.createDocument("http://xml.apache.org/batik/preferences",
                                             "preferences",
                                             null,
                                             r);
            Element elt = doc.getDocumentElement();
            for (Node n = elt.getFirstChild(); n != null; n = n.getNextSibling()) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    if (n.getNodeName().equals("property")) {
                        String name = ((Element)n).getAttributeNS(null, "name");
                        StringBuffer cont = new StringBuffer();
                        for (Node c = n.getFirstChild();
                             c != null;
                             c = c.getNextSibling()) {
                            if (c.getNodeType() == Node.TEXT_NODE) {
                                cont.append(c.getNodeValue());
                            } else {
                                break;
                            }
                        }
                        String val = cont.toString();
                        put(name, val);
                    }
                }
            }
        }
        public synchronized void store(OutputStream os, String header)
            throws IOException {
            BufferedWriter w;
            w = new BufferedWriter(new OutputStreamWriter(os, PREFERENCE_ENCODING));
            Map m = new HashMap();
            enumerate(m);
            w.write("<preferences xmlns=\"http://xml.apache.org/batik/preferences\">\n");
            Iterator it = m.keySet().iterator();
            while (it.hasNext()) {
                String n = (String)it.next();
                String v = (String)m.get(n);
                w.write("<property name=\"" + n + "\">");
                try {
                    w.write(DOMUtilities.contentToString(v, false));
                } catch (IOException ex) {
                }
                w.write("</property>\n");
            }
            w.write("</preferences>\n");
            w.flush();
        }
        private synchronized void enumerate(Map m) {
            if (defaults != null) {
                Iterator it = m.keySet().iterator();
                while (it.hasNext()) {
                    Object k = it.next();
                    m.put(k, defaults.get(k));
                }
            }
            Iterator it = keySet().iterator();
            while (it.hasNext()) {
                Object k = it.next();
                m.put(k, get(k));
            }
        }
    }
}
