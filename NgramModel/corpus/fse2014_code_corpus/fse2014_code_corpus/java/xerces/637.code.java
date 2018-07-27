package org.apache.xerces.util;
final class XMLErrorCode {
    private String fDomain;
    private String fKey;
    public XMLErrorCode(String domain, String key) {
        fDomain = domain;
        fKey = key;
    }
    public void setValues(String domain, String key) {
        fDomain = domain;
        fKey = key;
    }
    public boolean equals(Object obj) {
        if (!(obj instanceof XMLErrorCode))
            return false;
        XMLErrorCode err = (XMLErrorCode) obj;
        return (fDomain.equals(err.fDomain) && fKey.equals(err.fKey));
    }
    public int hashCode() {
        return fDomain.hashCode() + fKey.hashCode();
    }
}