package org.apache.batik.gvt.text;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.Map;
import java.util.Set;
public class AttributedCharacterSpanIterator implements
                                   AttributedCharacterIterator {
    private AttributedCharacterIterator aci;
    private int begin;
    private int end;
    public AttributedCharacterSpanIterator(AttributedCharacterIterator aci, 
                                           int start, int stop) {
        this.aci = aci;
        end = Math.min(aci.getEndIndex(), stop);
        begin = Math.max(aci.getBeginIndex(), start);
        this.aci.setIndex(begin);
    }
    public Set getAllAttributeKeys() {
        return aci.getAllAttributeKeys();
    }
    public Object getAttribute(AttributedCharacterIterator.Attribute attribute) {
        return aci.getAttribute(attribute);
    }
    public Map getAttributes() {
        return aci.getAttributes();
    }
    public int getRunLimit() {
        return Math.min(aci.getRunLimit(), end);
    }
    public int getRunLimit(AttributedCharacterIterator.Attribute attribute) {
        return Math.min(aci.getRunLimit(attribute), end);
    }
    public int getRunLimit(Set attributes) {
        return Math.min(aci.getRunLimit(attributes), end);
    }
    public int getRunStart() {
        return Math.max(aci.getRunStart(), begin);
    }
    public int getRunStart(AttributedCharacterIterator.Attribute attribute) {
        return Math.max(aci.getRunStart(attribute), begin);
    }
    public int getRunStart(Set attributes) {
        return Math.max(aci.getRunStart(attributes), begin);
    }
    public Object clone() {
        return new AttributedCharacterSpanIterator(
                      (AttributedCharacterIterator) aci.clone(), begin, end);
    }
    public char current() {
        return aci.current();
    }
    public char first() {
        return aci.setIndex(begin);
    }
    public int getBeginIndex() {
        return begin;
    }
    public int getEndIndex() {
        return end;
    }
    public int getIndex() {
        return aci.getIndex();
    }
    public char last() {
        return setIndex(end-1);
    }
    public char next() {
        if (getIndex() < end-1 ) {
            return aci.next();
        } else {
            return setIndex(end);
        }
    }
    public char previous() {
        if (getIndex() > begin) {
            return aci.previous();
        } else {
            return CharacterIterator.DONE;
        }
    }
    public char setIndex(int position) {
        int ndx = Math.max(position, begin);
        ndx = Math.min(ndx, end);
        char c = aci.setIndex(ndx);
        if (ndx == end) {
            c = CharacterIterator.DONE;
        }
        return c;
    }
}
