package org.apache.xerces.impl;
public class XML11NamespaceBinder extends XMLNamespaceBinder {
    public XML11NamespaceBinder() {
    } 
    protected boolean prefixBoundToNullURI(String uri, String localpart) {
        return false;
    } 
} 
