package org.apache.batik.transcoder.svg2svg;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.xml.XMLUtilities;
public class OutputManager {
    protected PrettyPrinter prettyPrinter;
    protected Writer writer;
    protected int level;
    protected StringBuffer margin = new StringBuffer();
    protected int line = 1;
    protected int column;
    protected List xmlSpace = new LinkedList();
    {
        xmlSpace.add(Boolean.FALSE);
    }
    protected boolean canIndent = true;
    protected List startingLines = new LinkedList();
    protected boolean lineAttributes = false;
    public OutputManager(PrettyPrinter pp, Writer w) {
        prettyPrinter = pp;
        writer = w;
    }
    public void printCharacter(char c) throws IOException {
        if (c == 10) {
            printNewline();
        } else {
            column++;
            writer.write(c);
        }
    }
    public void printNewline() throws IOException {
        String nl = prettyPrinter.getNewline();
        for (int i = 0; i < nl.length(); i++) {
            writer.write(nl.charAt(i));
        }
        column = 0;
        line++;
     }
    public void printString(String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            printCharacter(s.charAt(i));
        }
    }
    public void printCharacters(char[] ca) throws IOException {
        for (int i = 0; i < ca.length; i++) {
            printCharacter(ca[i]);
        }
    }
    public void printSpaces(char[] text, boolean opt) throws IOException {
        if (prettyPrinter.getFormat()) {
            if (!opt) {
                printCharacter(' ');
            }
        } else {
            printCharacters(text);
        }
    }
    public void printTopSpaces(char[] text) throws IOException {
        if (prettyPrinter.getFormat()) {
            int nl = newlines(text);
            for (int i = 0; i < nl; i++) {
                printNewline();
            }
        } else {
            printCharacters(text);
        }
    }
    public void printComment(char[] text) throws IOException {
        if (prettyPrinter.getFormat()) {
            if (canIndent) {
                printNewline();
                printString(margin.toString());
            }
            printString("<!--");
            if (column + text.length + 3 < prettyPrinter.getDocumentWidth()) {
                printCharacters(text);
            } else {
                formatText(text, margin.toString(), false);
                printCharacter(' ');
            }
            if (column + 3 > prettyPrinter.getDocumentWidth()) {
                printNewline();
                printString(margin.toString());
            }
            printString("-->");
        } else {
            printString("<!--");
            printCharacters(text);
            printString("-->");
        }
    }
    public void printXMLDecl(char[] space1,
                             char[] space2,
                             char[] space3,
                             char[] version, char versionDelim,
                             char[] space4,
                             char[] space5,
                             char[] space6,
                             char[] encoding, char encodingDelim,
                             char[] space7,
                             char[] space8,
                             char[] space9,
                             char[] standalone, char standaloneDelim,
                             char[] space10)
        throws IOException {
        printString("<?xml");
        printSpaces(space1, false);
        printString("version");
        if (space2 != null) {
            printSpaces(space2, true);
        }
        printCharacter('=');
        if (space3 != null) {
            printSpaces(space3, true);
        }
        printCharacter(versionDelim);
        printCharacters(version);
        printCharacter(versionDelim);
        if (space4 != null) {
            printSpaces(space4, false);
            if (encoding != null) {
                printString("encoding");
                if (space5 != null) {
                    printSpaces(space5, true);
                }
                printCharacter('=');
                if (space6 != null) {
                    printSpaces(space6, true);
                }
                printCharacter(encodingDelim);
                printCharacters(encoding);
                printCharacter(encodingDelim);
                if (space7 != null) {
                    printSpaces(space7, standalone == null);
                }
            }
            if (standalone != null) {
                printString("standalone");
                if (space8 != null) {
                    printSpaces(space8, true);
                }
                printCharacter('=');
                if (space9 != null) {
                    printSpaces(space9, true);
                }
                printCharacter(standaloneDelim);
                printCharacters(standalone);
                printCharacter(standaloneDelim);
                if (space10 != null) {
                    printSpaces(space10, true);
                }
            }
        }
        printString("?>");
    }
    public void printPI(char[] target, char[] space, char[] data) throws IOException {
        if (prettyPrinter.getFormat()) {
            if (canIndent) {
                printNewline();
                printString(margin.toString());
            }
        }
        printString("<?");
        printCharacters(target);
        printSpaces(space, false);
        printCharacters(data);
        printString("?>");
    }
    public void printDoctypeStart(char[] space1,
                                  char[] root,
                                  char[] space2,
                                  String externalId,
                                  char[] space3,
                                  char[] string1, char string1Delim,
                                  char[] space4,
                                  char[] string2, char string2Delim,
                                  char[] space5) throws IOException {
        if (prettyPrinter.getFormat()) {
            printString("<!DOCTYPE");
            printCharacter(' ');
            printCharacters(root);
            if (space2 != null) {
                printCharacter(' ');
                printString(externalId);
                printCharacter(' ');
                printCharacter(string1Delim);
                printCharacters(string1);
                printCharacter(string1Delim);
                if (space4 != null) {
                    if (string2 != null) {
                        if (column + string2.length + 3 >
                            prettyPrinter.getDocumentWidth()) {
                            printNewline();
                            for (int i = 0;
                                 i < prettyPrinter.getTabulationWidth();
                                 i++) {
                                printCharacter(' ');
                            }
                        } else {
                            printCharacter(' ');
                        }
                        printCharacter(string2Delim);
                        printCharacters(string2);
                        printCharacter(string2Delim);
                        printCharacter(' ');
                    }
                }
            }
        } else {
            printString("<!DOCTYPE");
            printSpaces(space1, false);
            printCharacters(root);
            if (space2 != null) {
                printSpaces(space2, false);
                printString(externalId);
                printSpaces(space3, false);
                printCharacter(string1Delim);
                printCharacters(string1);
                printCharacter(string1Delim);
                if (space4 != null) {
                    printSpaces(space4, string2 == null);
                    if (string2 != null) {
                        printCharacter(string2Delim);
                        printCharacters(string2);
                        printCharacter(string2Delim);
                        if (space5 != null) {
                            printSpaces(space5, true);
                        }
                    }
                }
            }
        }
    }
    public void printDoctypeEnd(char[] space) throws IOException {
        if (space != null) {
            printSpaces(space, true);
        }
        printCharacter('>');
    }
    public void printParameterEntityReference(char[] name) throws IOException {
        printCharacter('%');
        printCharacters(name);
        printCharacter(';');
    }
    public void printEntityReference(char[] name, 
                                     boolean first) throws IOException {
        if ((prettyPrinter.getFormat()) &&
            (xmlSpace.get(0) != Boolean.TRUE) &&
            first) {
            printNewline();
            printString(margin.toString());
        }
        printCharacter('&');
        printCharacters(name);
        printCharacter(';');
    }
    public void printCharacterEntityReference
        (char[] code, boolean first, boolean preceedingSpace) 
        throws IOException {
        if ((prettyPrinter.getFormat()) &&
            (xmlSpace.get(0) != Boolean.TRUE)) {
            if (first) {
                printNewline();
                printString(margin.toString());
            } else if (preceedingSpace) {
                int endCol = column + code.length + 3;
                if (endCol > prettyPrinter.getDocumentWidth()){
                    printNewline();
                    printString(margin.toString());
                } else {
                    printCharacter(' ');
                }
            }
        }
        printString("&#");
        printCharacters(code);
        printCharacter(';');
    }
    public void printElementStart(char[] name, List attributes, char[] space)
        throws IOException {
        xmlSpace.add(0, xmlSpace.get(0));
        startingLines.add(0, new Integer(line));
        if (prettyPrinter.getFormat()) {
            if (canIndent) {
                printNewline();
                printString(margin.toString());
            }
        }
        printCharacter('<');
        printCharacters(name);
        if (prettyPrinter.getFormat()) {
            Iterator it = attributes.iterator();
            if (it.hasNext()) {
                AttributeInfo ai = (AttributeInfo)it.next();
                if (ai.isAttribute("xml:space")) {
                    xmlSpace.set(0, (ai.value.equals("preserve")
                                     ? Boolean.TRUE
                                     : Boolean.FALSE));
                }
                printCharacter(' ');
                printCharacters(ai.name);
                printCharacter('=');
                printCharacter(ai.delimiter);
                printString(ai.value);
                printCharacter(ai.delimiter);
            }
            while (it.hasNext()) {
                AttributeInfo ai = (AttributeInfo)it.next();
                if (ai.isAttribute("xml:space")) {
                    xmlSpace.set(0, (ai.value.equals("preserve")
                                     ? Boolean.TRUE
                                     : Boolean.FALSE));
                }
                int len = ai.name.length + ai.value.length() + 4;
                if (lineAttributes ||
                    len + column > prettyPrinter.getDocumentWidth()) {
                    printNewline();
                    printString(margin.toString());
                    for (int i = 0; i < name.length + 2; i++) {
                        printCharacter(' ');
                    }
                } else {
                    printCharacter(' ');
                }
                printCharacters(ai.name);
                printCharacter('=');
                printCharacter(ai.delimiter);
                printString(ai.value);
                printCharacter(ai.delimiter);
            }
        } else {
            Iterator it = attributes.iterator();
            while (it.hasNext()) {
                AttributeInfo ai = (AttributeInfo)it.next();
                if (ai.isAttribute("xml:space")) {
                    xmlSpace.set(0, (ai.value.equals("preserve")
                                     ? Boolean.TRUE
                                     : Boolean.FALSE));
                }
                printSpaces(ai.space, false);
                printCharacters(ai.name);
                if (ai.space1 != null) {
                    printSpaces(ai.space1, true);
                }
                printCharacter('=');
                if (ai.space2 != null) {
                    printSpaces(ai.space2, true);
                }
                printCharacter(ai.delimiter);
                printString(ai.value);
                printCharacter(ai.delimiter);
            }
        }
        if (space != null) {
            printSpaces(space, true);
        }
        level++;
        for (int i = 0; i < prettyPrinter.getTabulationWidth(); i++) {
            margin.append(' ');
        }
        canIndent = true;
    }
    public void printElementEnd(char[] name, char[] space) throws IOException {
        for (int i = 0; i < prettyPrinter.getTabulationWidth(); i++) {
            margin.deleteCharAt(0);
        }
        level--;
        if (name != null) {
            if (prettyPrinter.getFormat()) {
                if (xmlSpace.get(0) != Boolean.TRUE &&
                    (line != ((Integer)startingLines.get(0)).intValue() ||
                     column + name.length + 3 >= prettyPrinter.getDocumentWidth())) {
                    printNewline();
                    printString(margin.toString());
                }
            }
            printString("</");
            printCharacters(name);
            if (space != null) {
                printSpaces(space, true);
            }
            printCharacter('>');
        } else {
            printString("/>");
        }
        startingLines.remove(0);
        xmlSpace.remove(0);
    }
    public boolean printCharacterData(char[] data, 
                                      boolean first,
                                      boolean preceedingSpace) 
        throws IOException {
        if (!prettyPrinter.getFormat()) {
            printCharacters(data);
            return false;
        }
        canIndent = true;
        if (isWhiteSpace(data)) {
            int nl = newlines(data);
            for (int i = 0; i < nl - 1; i++) {
                printNewline();
            }
            return true;
        }
        if (xmlSpace.get(0) == Boolean.TRUE) {
            printCharacters(data);
            canIndent = false;
            return false;
        }
        if (first) {
            printNewline();
            printString(margin.toString());
        }
        return formatText(data, margin.toString(), preceedingSpace);
    }
    public void printCDATASection(char[] data) throws IOException {
        printString("<![CDATA[");
        printCharacters(data);
        printString("]]>");
    }
    public void printNotation(char[] space1,
                              char[] name,
                              char[] space2,
                              String externalId,
                              char[] space3,
                              char[] string1, char string1Delim,
                              char[] space4,
                              char[] string2, char string2Delim,
                              char[] space5)
        throws IOException {
        writer.write("<!NOTATION");
        printSpaces(space1, false);
        writer.write(name);
        printSpaces(space2, false);
        writer.write(externalId);
        printSpaces(space3, false);
        writer.write(string1Delim);
        writer.write(string1);
        writer.write(string1Delim);
        if (space4 != null) {
            printSpaces(space4, false);
            if (string2 != null) {
                writer.write(string2Delim);
                writer.write(string2);
                writer.write(string2Delim);
            }
        }
        if (space5 != null) {
            printSpaces(space5, true);
        }
        writer.write('>');
    }
    public void printAttlistStart(char[] space, char[] name) throws IOException {
        writer.write("<!ATTLIST");
        printSpaces(space, false);
        writer.write(name);
    }
    public void printAttlistEnd(char[] space) throws IOException {
        if (space != null) {
            printSpaces(space, false);
        }
        writer.write('>');
    }
    public void printAttName(char[] space1, char[] name, char[] space2)
        throws IOException {
        printSpaces(space1, false);
        writer.write(name);
        printSpaces(space2, false);
    }
    public void printEnumeration(List names) throws IOException {
        writer.write('(');
        Iterator it = names.iterator();
        NameInfo ni = (NameInfo)it.next();
        if (ni.space1 != null) {
            printSpaces(ni.space1, true);
        }
        writer.write(ni.name);
        if (ni.space2 != null) {
            printSpaces(ni.space2, true);
        }
        while (it.hasNext()) {
            writer.write('|');
            ni = (NameInfo)it.next();
            if (ni.space1 != null) {
                printSpaces(ni.space1, true);
            }
            writer.write(ni.name);
            if (ni.space2 != null) {
                printSpaces(ni.space2, true);
            }
        }
        writer.write(')');
    }
    protected int newlines(char[] text) {
        int result = 0;
        for (int i = 0; i < text.length; i++) {
            if (text[i] == 10) {
                result++;
            }
        }
        return result;
    }
    protected boolean isWhiteSpace(char[] text) {
        for (int i = 0; i < text.length; i++) {
            if (!XMLUtilities.isXMLSpace(text[i])) {
                return false;
            }
        }
        return true;
    }
    protected boolean formatText(char[] text, String margin,
                                 boolean preceedingSpace) throws IOException {
        int i = 0;
        boolean startsWithSpace = preceedingSpace;
        loop: while (i < text.length) {
            for (;;) {
                if (i >= text.length) {
                    break loop;
                }
                if (!XMLUtilities.isXMLSpace(text[i])) {
                    break;
                }
                startsWithSpace = true;
                i++;
            }
            StringBuffer sb = new StringBuffer();
            for (;;) {
                if (i >= text.length || XMLUtilities.isXMLSpace(text[i])) {
                    break;
                }
                sb.append(text[i++]);
            }
            if (sb.length() == 0) {
                return startsWithSpace;
            }
            if (startsWithSpace) {
                int endCol = column + sb.length();
                if ((endCol >= prettyPrinter.getDocumentWidth() - 1) &&
                    ((margin.length() + sb.length() <
                      prettyPrinter.getDocumentWidth() - 1) ||
                     (margin.length() < column))) {
                    printNewline();
                    printString(margin);
                } else if (column > margin.length()) {
                    printCharacter(' ');
                }
            }
            printString(sb.toString());
            startsWithSpace = false;
        }
        return startsWithSpace;
    }
    public static class NameInfo {
        public char[] space1;
        public char[] name;
        public char[] space2;
        public NameInfo(char[] sp1, char[] nm, char[] sp2) {
            space1 = sp1;
            name = nm;
            space2 = sp2;
        }
    }
    public static class AttributeInfo {
        public char[] space;
        public char[] name;
        public char[] space1;
        public char[] space2;
        public String value;
        public char delimiter;
        public boolean entityReferences;
        public AttributeInfo(char[] sp, char[] n, char[] sp1, char[] sp2,
                             String val, char delim, boolean entity) {
            space = sp;
            name = n;
            space1 = sp1;
            space2 = sp2;
            value = val;
            delimiter = delim;
            entityReferences = entity;
        }
        public boolean isAttribute(String s) {
            if (name.length == s.length()) {
                for (int i = 0; i < name.length; i++) {
                    if (name[i] != s.charAt(i)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    }
}
