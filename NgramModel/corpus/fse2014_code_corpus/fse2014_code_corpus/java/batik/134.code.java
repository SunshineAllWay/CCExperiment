package org.apache.batik.bridge;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDocumentFactory;
import org.apache.batik.dom.util.DocumentDescriptor;
import org.apache.batik.util.CleanerThread;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
public class DocumentLoader {
    protected SVGDocumentFactory documentFactory;
    protected HashMap cacheMap = new HashMap();
    protected UserAgent userAgent;
    protected DocumentLoader() { }
    public DocumentLoader(UserAgent userAgent) {
        this.userAgent = userAgent;
        documentFactory = new SAXSVGDocumentFactory
            (userAgent.getXMLParserClassName(), true);
        documentFactory.setValidating(userAgent.isXMLParserValidating());
    }
    public Document checkCache(String uri) {
        int n = uri.lastIndexOf('/');
        if (n == -1) 
            n = 0;
        n = uri.indexOf('#', n);
        if (n != -1) {
            uri = uri.substring(0, n);
        }
        DocumentState state;
        synchronized (cacheMap) {
            state = (DocumentState)cacheMap.get(uri);
        }
        if (state != null)
            return state.getDocument();
        return null;
    }
    public Document loadDocument(String uri) throws IOException {
        Document ret = checkCache(uri);
        if (ret != null)
            return ret;
        SVGDocument document = documentFactory.createSVGDocument(uri);
        DocumentDescriptor desc = documentFactory.getDocumentDescriptor();
        DocumentState state = new DocumentState(uri, document, desc);
        synchronized (cacheMap) {
            cacheMap.put(uri, state);
        }
        return state.getDocument();
    }
    public Document loadDocument(String uri, InputStream is) 
        throws IOException {
        Document ret = checkCache(uri);
        if (ret != null)
            return ret;
        SVGDocument document = documentFactory.createSVGDocument(uri, is);
        DocumentDescriptor desc = documentFactory.getDocumentDescriptor();
        DocumentState state = new DocumentState(uri, document, desc);
        synchronized (cacheMap) {
            cacheMap.put(uri, state);
        }
        return state.getDocument();
    }
    public UserAgent getUserAgent(){
        return userAgent;
    }
    public void dispose() {
        synchronized (cacheMap) {
            cacheMap.clear();
        }
    }
    public int getLineNumber(Element e) {
        String uri = ((SVGDocument)e.getOwnerDocument()).getURL();
        DocumentState state;
        synchronized (cacheMap) {
            state = (DocumentState)cacheMap.get(uri);
        }
        if (state == null) {
            return -1;
        } else {
            return state.desc.getLocationLine(e);
        }
    }
    private class DocumentState extends CleanerThread.SoftReferenceCleared {
        private String uri;
        private DocumentDescriptor desc;
        public DocumentState(String uri,
                             Document document,
                             DocumentDescriptor desc) {
            super(document);
            this.uri = uri;
            this.desc = desc;
        }
        public void cleared() {
            synchronized (cacheMap) {
                cacheMap.remove(uri);
            }
        }
        public DocumentDescriptor getDocumentDescriptor() {
            return desc;
        }
        public String getURI() {
            return uri;
        }
        public Document getDocument() {
            return (Document)get();
        }
    }
}
