package org.apache.xerces.xni;
public class QName 
    implements Cloneable {
    public String prefix;
    public String localpart;
    public String rawname;
    public String uri;
    public QName() {
        clear();
    } 
    public QName(String prefix, String localpart, String rawname, String uri) {
        setValues(prefix, localpart, rawname, uri);
    } 
    public QName(QName qname) {
        setValues(qname);
    } 
    public void setValues(QName qname) {
        prefix = qname.prefix;
        localpart = qname.localpart;
        rawname = qname.rawname;
        uri = qname.uri;
    } 
    public void setValues(String prefix, String localpart, String rawname, 
                          String uri) {
        this.prefix = prefix;
        this.localpart = localpart;
        this.rawname = rawname;
        this.uri = uri;
    } 
    public void clear() {
        prefix = null;
        localpart = null;
        rawname = null;
        uri = null;
    } 
    public Object clone() {
        return new QName(this);
    } 
    public int hashCode() {
        if (uri != null) {
            return uri.hashCode() + 
                ((localpart != null) ? localpart.hashCode() : 0);
        }
        return (rawname != null) ? rawname.hashCode() : 0;
    } 
    public boolean equals(Object object) {
        if (object instanceof QName) {
            QName qname = (QName)object;
            if (qname.uri != null) {
                return uri == qname.uri && localpart == qname.localpart;
            }
            else if (uri == null) {
                return rawname == qname.rawname;
            }
        }
        return false;
    } 
    public String toString() {
        StringBuffer str = new StringBuffer();
        boolean comma = false;
        if (prefix != null) {
            str.append("prefix=\"").append(prefix).append('"');
            comma = true;
        }
        if (localpart != null) {
            if (comma) {
                str.append(',');
            }
            str.append("localpart=\"").append(localpart).append('"');
            comma = true;
        }
        if (rawname != null) {
            if (comma) {
                str.append(',');
            }
            str.append("rawname=\"").append(rawname).append('"');
            comma = true;
        }
        if (uri != null) {
            if (comma) {
                str.append(',');
            }
            str.append("uri=\"").append(uri).append('"');
        }
        return str.toString();
    } 
} 
