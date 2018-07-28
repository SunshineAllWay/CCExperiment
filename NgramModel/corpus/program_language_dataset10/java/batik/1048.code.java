package org.apache.batik.gvt.text;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
public class GVTACIImpl
                implements GVTAttributedCharacterIterator {
    private String simpleString;
    private Set allAttributes;
    private ArrayList mapList;
    private static int START_RUN = 2;
    private static int END_RUN = 3;
    private static int MID_RUN = 1;
    private static int SINGLETON = 0;
    private int[] charInRun;
    private CharacterIterator iter = null;
    private int currentIndex = -1;
    public GVTACIImpl() {
        simpleString = "";
        buildAttributeTables();
    }
    public GVTACIImpl(AttributedCharacterIterator aci) {
        buildAttributeTables(aci);
    }
    public void setString(String s) {
        simpleString = s;
        iter = new StringCharacterIterator(simpleString);
        buildAttributeTables();
    }
    public void setString(AttributedString s) {
        iter = s.getIterator();
        buildAttributeTables((AttributedCharacterIterator) iter);
    }
    public void setAttributeArray
        (GVTAttributedCharacterIterator.TextAttribute attr,
         Object[] attValues, int beginIndex, int endIndex) {
        beginIndex = Math.max(beginIndex, 0);
        endIndex = Math.min(endIndex, simpleString.length());
        if (charInRun[beginIndex] == END_RUN) {
            if (charInRun[beginIndex - 1] == MID_RUN) {
                charInRun[beginIndex - 1] = END_RUN;
            } else {
                charInRun[beginIndex - 1] = SINGLETON;
            }
        }
        if (charInRun[endIndex + 1] == END_RUN) {
            charInRun[endIndex + 1] = SINGLETON;
        } else if (charInRun[endIndex + 1] == MID_RUN) {
            charInRun[endIndex + 1] = START_RUN;
        }
        for (int i = beginIndex; i <= endIndex; ++i) {
            charInRun[i] = SINGLETON;
            int n = Math.min(i, attValues.length - 1);
            ((Map) mapList.get(i)).put(attr, attValues[n]);
        }
    }
    public Set getAllAttributeKeys() {
        return allAttributes;
    }
    public Object getAttribute(AttributedCharacterIterator.Attribute attribute)
    {
        return getAttributes().get(attribute);
    }
    public Map getAttributes() {
        return (Map) mapList.get(currentIndex);
    }
    public int getRunLimit() {
        int  ndx = currentIndex;
        do {
            ++ndx;
        } while (charInRun[ndx] == MID_RUN);
        return ndx;
    }
    public int getRunLimit(AttributedCharacterIterator.Attribute attribute) {
        int ndx = currentIndex;
        Object  value = getAttributes().get(attribute);
        if (value == null) {
            do {
                 ++ndx;
            } while (((Map) mapList.get(ndx)).get(attribute) == null);
        } else {
            do {
                ++ndx;
            } while (value.equals(((Map) mapList.get(ndx)).get(attribute)));
        }
        return ndx;
    }
    public int getRunLimit(Set attributes) {
        int ndx = currentIndex;
        do {
            ++ndx;
        } while (attributes.equals(mapList.get(ndx)));
        return ndx;
    }
    public int getRunStart() {
        int ndx = currentIndex;
        while (charInRun[ndx] == MID_RUN) --ndx;
        return ndx;
    }
    public int getRunStart(AttributedCharacterIterator.Attribute attribute) {
        int ndx = currentIndex - 1;
        Object value = getAttributes().get(attribute);
        try {
            if (value == null) {
                while (((Map) mapList.get(ndx - 1)).get(attribute) == null)
                    --ndx;
            } else {
                while (value.equals(
                        ((Map) mapList.get(ndx - 1)).get(attribute)) )
                    --ndx;
            }
        } catch(IndexOutOfBoundsException e) {
        }
        return ndx;
    }
    public int getRunStart(Set attributes) {
        int ndx = currentIndex;
        try {
            while (attributes.equals(mapList.get(ndx - 1))) --ndx;
        } catch(IndexOutOfBoundsException e) {
        }
        return ndx;
    }
    public Object clone() {
        GVTAttributedCharacterIterator cloneACI =
                new GVTACIImpl(this);
        return cloneACI;
    }
    public char current() {
        return iter.current();
    }
    public char first() {
        return iter.first();
    }
    public int getBeginIndex() {
        return iter.getBeginIndex();
    }
    public int getEndIndex() {
        return iter.getEndIndex();
    }
    public int getIndex() {
        return iter.getIndex();
    }
    public char last() {
        return iter.last();
    }
    public char next() {
        return iter.next();
    }
    public char previous() {
        return iter.previous();
    }
    public char setIndex(int position) {
        return iter.setIndex(position);
    }
    private void buildAttributeTables() {
        allAttributes = new HashSet();
        mapList = new ArrayList(simpleString.length());
        charInRun = new int[simpleString.length()];
        for (int i = 0; i < charInRun.length; ++i) {
            charInRun[i] = SINGLETON;
            mapList.set(i, new HashMap());
        }
    }
    private void buildAttributeTables(AttributedCharacterIterator aci) {
        allAttributes = aci.getAllAttributeKeys();
        int length = aci.getEndIndex() - aci.getBeginIndex();
        mapList = new ArrayList(length);
        charInRun = new int[length];
        char  c = aci.first();
        char[] chars = new char[length];
        for (int i = 0; i < length; ++i) {
            chars[i] = c;
            charInRun[i] = SINGLETON;
            mapList.set(i, new HashMap(aci.getAttributes()));
            c = aci.next();
        }
        simpleString = new String(chars);
    }
    public class TransformAttributeFilter implements
                     GVTAttributedCharacterIterator.AttributeFilter {
        public AttributedCharacterIterator
                     mutateAttributes(AttributedCharacterIterator aci) {
            return aci;
        }
    }
}
