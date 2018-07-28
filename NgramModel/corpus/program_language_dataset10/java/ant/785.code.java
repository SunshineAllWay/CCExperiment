package org.apache.tools.ant.util;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
public class LayoutPreservingProperties extends Properties {
    private String LS = StringUtils.LINE_SEP;
    private ArrayList logicalLines = new ArrayList();
    private HashMap keyedPairLines = new HashMap();
    private boolean removeComments;
    public LayoutPreservingProperties() {
        super();
    }
    public LayoutPreservingProperties(Properties defaults) {
        super(defaults);
    }
    public boolean isRemoveComments() {
        return removeComments;
    }
    public void setRemoveComments(boolean val) {
        removeComments = val;
    }
    public void load(InputStream inStream) throws IOException {
        String s = readLines(inStream);
        byte[] ba = s.getBytes(ResourceUtils.ISO_8859_1);
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        super.load(bais);
    }
    public Object put(Object key, Object value) throws NullPointerException {
        Object obj = super.put(key, value);
        innerSetProperty(key.toString(), value.toString());
        return obj;
    }
    public Object setProperty(String key, String value)
        throws NullPointerException {
        Object obj = super.setProperty(key, value);
        innerSetProperty(key, value);
        return obj;
    }
    private void innerSetProperty(String key, String value) {
        value = escapeValue(value);
        if (keyedPairLines.containsKey(key)) {
            Integer i = (Integer) keyedPairLines.get(key);
            Pair p = (Pair) logicalLines.get(i.intValue());
            p.setValue(value);
        } else {
            key = escapeName(key);
            Pair p = new Pair(key, value);
            p.setNew(true);
            keyedPairLines.put(key, new Integer(logicalLines.size()));
            logicalLines.add(p);
        }
    }
    public void clear() {
        super.clear();
        keyedPairLines.clear();
        logicalLines.clear();
    }
    public Object remove(Object key) {
        Object obj = super.remove(key);
        Integer i = (Integer) keyedPairLines.remove(key);
        if (null != i) {
            if (removeComments) {
                removeCommentsEndingAt(i.intValue());
            }
            logicalLines.set(i.intValue(), null);
        }
        return obj;
    }
    public Object clone() {
        LayoutPreservingProperties dolly =
            (LayoutPreservingProperties) super.clone();
        dolly.keyedPairLines = (HashMap) this.keyedPairLines.clone();
        dolly.logicalLines = (ArrayList) this.logicalLines.clone();
        for (int j = 0; j < dolly.logicalLines.size(); j++) {
            LogicalLine line = (LogicalLine) dolly.logicalLines.get(j);
            if (line instanceof Pair) {
                Pair p = (Pair) line;
                dolly.logicalLines.set(j, p.clone());
            }
        }
        return dolly;
    }
    public void listLines(PrintStream out) {
        out.println("-- logical lines --");
        Iterator i = logicalLines.iterator();
        while (i.hasNext()) {
            LogicalLine line = (LogicalLine) i.next();
            if (line instanceof Blank) {
                out.println("blank:   \"" + line + "\"");
            }
            else if (line instanceof Comment) {
                out.println("comment: \"" + line + "\"");
            }
            else if (line instanceof Pair) {
                out.println("pair:    \"" + line + "\"");
            }
        }
    }
    public void saveAs(File dest) throws IOException {
        FileOutputStream fos = new FileOutputStream(dest);
        store(fos, null);
        fos.close();
    }
    public void store(OutputStream out, String header) throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(out, ResourceUtils.ISO_8859_1);
        int skipLines = 0;
        int totalLines = logicalLines.size();
        if (header != null) {
            osw.write("#" + header + LS);
            if (totalLines > 0
                && logicalLines.get(0) instanceof Comment
                && header.equals(logicalLines.get(0).toString().substring(1))) {
                skipLines = 1;
            }
        }
        if (totalLines > skipLines
            && logicalLines.get(skipLines) instanceof Comment) {
            try {
                DateUtils.parseDateFromHeader(logicalLines
                                              .get(skipLines)
                                              .toString().substring(1));
                skipLines++;
            } catch (java.text.ParseException pe) {
            }
        }
        osw.write("#" + DateUtils.getDateForHeader() + LS);
        boolean writtenSep = false;
        for (Iterator i = logicalLines.subList(skipLines, totalLines).iterator();
             i.hasNext(); ) {
            LogicalLine line = (LogicalLine) i.next();
            if (line instanceof Pair) {
                if (((Pair)line).isNew()) {
                    if (!writtenSep) {
                        osw.write(LS);
                        writtenSep = true;
                    }
                }
                osw.write(line.toString() + LS);
            }
            else if (line != null) {
                osw.write(line.toString() + LS);
            }
        }
        osw.close();
    }
    private String readLines(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(is, ResourceUtils.ISO_8859_1);
        PushbackReader pbr = new PushbackReader(isr, 1);
        if (logicalLines.size() > 0) {
            logicalLines.add(new Blank());
        }
        String s = readFirstLine(pbr);
        BufferedReader br = new BufferedReader(pbr);
        boolean continuation = false;
        boolean comment = false;
        StringBuffer fileBuffer = new StringBuffer();
        StringBuffer logicalLineBuffer = new StringBuffer();
        while (s != null) {
            fileBuffer.append(s).append(LS);
            if (continuation) {
                s = "\n" + s;
            } else {
                comment = s.matches("^( |\t|\f)*(#|!).*");
            }
            if (!comment) {
                continuation = requiresContinuation(s);
            }
            logicalLineBuffer.append(s);
            if (!continuation) {
                LogicalLine line = null;
                if (comment) {
                    line = new Comment(logicalLineBuffer.toString());
                } else if (logicalLineBuffer.toString().trim().length() == 0) {
                    line = new Blank();
                } else {
                    line = new Pair(logicalLineBuffer.toString());
                    String key = unescape(((Pair)line).getName());
                    if (keyedPairLines.containsKey(key)) {
                        remove(key);
                    }
                    keyedPairLines.put(key, new Integer(logicalLines.size()));
                }
                logicalLines.add(line);
                logicalLineBuffer.setLength(0);
            }
            s = br.readLine();
        }
        return fileBuffer.toString();
    }
    private String readFirstLine(PushbackReader r) throws IOException {
        StringBuffer sb = new StringBuffer(80);
        int ch = r.read();
        boolean hasCR = false;
        LS = StringUtils.LINE_SEP;
        while (ch >= 0) {
            if (hasCR && ch != '\n') {
                r.unread(ch);
                break;
            }
            if (ch == '\r') {
                LS = "\r";
                hasCR = true;
            } else if (ch == '\n') {
                LS = hasCR ? "\r\n" : "\n";
                break;
            } else {
                sb.append((char) ch);
            }
            ch = r.read();
        }
        return sb.toString();
    }
    private boolean requiresContinuation(String s) {
        char[] ca = s.toCharArray();
        int i = ca.length - 1;
        while (i > 0 && ca[i] == '\\') {
            i--;
        }
        int tb = ca.length - i - 1;
        return tb % 2 == 1;
    }
    private String unescape(String s) {
        char[] ch = new char[s.length() + 1];
        s.getChars(0, s.length(), ch, 0);
        ch[s.length()] = '\n';
        StringBuffer buffy = new StringBuffer(s.length());
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (c == '\n') {
                break;
            }
            else if (c == '\\') {
                c = ch[++i];
                if (c == 'n')
                    buffy.append('\n');
                else if (c == 'r')
                    buffy.append('\r');
                else if (c == 'f')
                    buffy.append('\f');
                else if (c == 't')
                    buffy.append('\t');
                else if (c == 'u') {
                    c = unescapeUnicode(ch, i+1);
                    i += 4;
                    buffy.append(c);
                }
                else
                    buffy.append(c);
            }
            else {
                buffy.append(c);
            }
        }
        return buffy.toString();
    }
    private char unescapeUnicode(char[] ch, int i) {
        String s = new String(ch, i, 4);
        return (char) Integer.parseInt(s, 16);
    }
    private String escapeValue(String s) {
        return escape(s, false);
    }
    private String escapeName(String s) {
        return escape(s, true);
    }
    private String escape(String s, boolean escapeAllSpaces) {
        if (s == null) {
            return null;
        }
        char[] ch = new char[s.length()];
        s.getChars(0, s.length(), ch, 0);
        String forEscaping = "\t\f\r\n\\:=#!";
        String escaped = "tfrn\\:=#!";
        StringBuffer buffy = new StringBuffer(s.length());
        boolean leadingSpace = true;
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (c == ' ') {
                if (escapeAllSpaces || leadingSpace) {
                    buffy.append("\\");
                }
            } else {
                leadingSpace = false;
            }
            int p = forEscaping.indexOf(c);
            if (p != -1) {
                buffy.append("\\").append(escaped.substring(p,p+1));
            } else if (c < 0x0020 || c > 0x007e) {
                buffy.append(escapeUnicode(c));
            } else {
                buffy.append(c);
            }
        }
        return buffy.toString();
    }
    private String escapeUnicode(char ch) {
        return "\\" + UnicodeUtil.EscapeUnicode(ch);
        }
    private void removeCommentsEndingAt(int pos) {
        int end = pos - 1;
        for (pos = end; pos > 0; pos--) {
            if (!(logicalLines.get(pos) instanceof Blank)) {
                break;
            }
        }
        if (!(logicalLines.get(pos) instanceof Comment)) {
            return;
        }
        for (; pos >= 0; pos--) {
            if (!(logicalLines.get(pos) instanceof Comment)) {
                break;
            }
        }
        for (pos++ ;pos <= end; pos++) {
            logicalLines.set(pos, null);
        }
    }
    private static abstract class LogicalLine {
        private String text;
        public LogicalLine(String text) {
            this.text = text;
        }
        public void setText(String text) {
            this.text = text;
        }
        public String toString() {
            return text;
        }
    }
    private static class Blank extends LogicalLine {
        public Blank() {
            super("");
        }
    }
    private class Comment extends LogicalLine {
        public Comment(String text) {
            super(text);
        }
    }
    private static class Pair extends LogicalLine implements Cloneable {
        private String name;
        private String value;
        private boolean added;
        public Pair(String text) {
            super(text);
            parsePair(text);
        }
        public Pair(String name, String value) {
            this(name + "=" + value);
        }
        public String getName() {
            return name;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
            setText(name + "=" + value);
        }
        public boolean isNew() {
            return added;
        }
        public void setNew(boolean val) {
            added = val;
        }
        public Object clone() {
            Object dolly = null;
            try {
                dolly = super.clone();
            }
            catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return dolly;
        }
        private void parsePair(String text) {
            int pos = findFirstSeparator(text);
            if (pos == -1) {
                name = text;
                value = null;
            }
            else {
                name = text.substring(0, pos);
                value = text.substring(pos+1, text.length());
            }
            name = stripStart(name, " \t\f");
        }
        private String stripStart(String s, String chars) {
            if (s == null) {
                return null;
            }
            int i = 0;
            for (;i < s.length(); i++) {
                if (chars.indexOf(s.charAt(i)) == -1) {
                    break;
                }
            }
            if (i == s.length()) {
                return "";
            }
            return s.substring(i);
        }
        private int findFirstSeparator(String s) {
            s = s.replaceAll("\\\\\\\\", "__");
            s = s.replaceAll("\\\\=", "__");
            s = s.replaceAll("\\\\:", "__");
            s = s.replaceAll("\\\\ ", "__");
            s = s.replaceAll("\\\\t", "__");
            return indexOfAny(s, " :=\t");
        }
        private int indexOfAny(String s, String chars) {
            if (s == null || chars == null) {
                return -1;
            }
            int p = s.length() + 1;
            for (int i = 0; i < chars.length(); i++) {
                int x = s.indexOf(chars.charAt(i));
                if (x != -1 && x < p) {
                    p = x;
                }
            }
            if (p == s.length() + 1) {
                return -1;
            }
            return p;
        }
    }
}
