package org.apache.xerces.util;
import java.util.Map;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;
public class ExperimentalTaglet implements Taglet {
    private static final String NAME = "xerces.experimental";
    private static final String HEADER = "EXPERIMENTAL:";
    public boolean inConstructor() {
        return false;
    }
    public boolean inField() {
        return false;
    }
    public boolean inMethod() {
        return true;
    }
    public boolean inOverview() {
        return true;
    }
    public boolean inPackage() {
        return false;
    }
    public boolean inType() {
        return true;
    }
    public boolean isInlineTag() {
        return false;
    }
    public String getName() {
        return NAME;
    }
    public String toString(Tag arg0) {
        return "<DT><H1 style=\"font-size:150%\">" + HEADER + "</H1><DD>"
        + "This class should not be considered stable. It is likely to be altered or replaced in the future.<br/>"
        + "<I>" + arg0.text() + "</I></DD>\n";
    }
    public String toString(Tag[] tags) {
        if (tags.length == 0) {
            return null;
        }
        String result = "\n<DT><H1 style=\"font-size:150%\">" + HEADER + "</H1><DD>";
        result += "This class should not be considered stable. It is likely to be altered or replaced in the future.";
        result += "<I>";
        for (int i = 0; i < tags.length; i++) {
            result += "<br/>";
            result += tags[i].text();
        }
        return result + "</I></DD>\n";
    }
    public static void register(Map tagletMap) {
        ExperimentalTaglet tag = new ExperimentalTaglet();
        Taglet t = (Taglet) tagletMap.get(tag.getName());
        if (t != null) {
            tagletMap.remove(tag.getName());
        }
        tagletMap.put(tag.getName(), tag);
    }
}
