package org.apache.xalan.xsltc;
public interface DOMCache {
    public DOM retrieveDocument(String baseURI, String href, Translet translet);
}
