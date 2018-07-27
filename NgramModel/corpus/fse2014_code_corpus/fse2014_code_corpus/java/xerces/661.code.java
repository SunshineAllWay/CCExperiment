package org.apache.xerces.xni;
public class XMLString {
    public char[] ch;
    public int offset;
    public int length;
    public XMLString() {
    } 
    public XMLString(char[] ch, int offset, int length) {
        setValues(ch, offset, length);
    } 
    public XMLString(XMLString string) {
        setValues(string);
    } 
    public void setValues(char[] ch, int offset, int length) {
        this.ch = ch;
        this.offset = offset;
        this.length = length;
    } 
    public void setValues(XMLString s) {
        setValues(s.ch, s.offset, s.length);
    } 
    public void clear() {
        this.ch = null;
        this.offset = 0;
        this.length = -1;
    } 
    public boolean equals(char[] ch, int offset, int length) {
        if (ch == null) {
            return false;
        }
        if (this.length != length) {
            return false;
        }
        for (int i=0; i<length; i++) {
            if (this.ch[this.offset+i] != ch[offset+i] ) {
                return false;
            }
        }
        return true;
    } 
    public boolean equals(String s) {
        if (s == null) {
            return false;
        }
        if ( length != s.length() ) {
            return false;
        }
        for (int i=0; i<length; i++) {
            if (ch[offset+i] != s.charAt(i)) {
                return false;
            }
        }
        return true;
    } 
    public String toString() {
        return length > 0 ? new String(ch, offset, length) : "";
    } 
} 
