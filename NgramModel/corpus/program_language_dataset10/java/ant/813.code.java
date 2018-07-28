package org.apache.tools.ant.util;
import org.apache.tools.ant.BuildException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;
public final class StringUtils {
    private static final long KILOBYTE = 1024;
    private static final long MEGABYTE = KILOBYTE * 1024;
    private static final long GIGABYTE = MEGABYTE * 1024;
    private static final long TERABYTE = GIGABYTE * 1024;
    private static final long PETABYTE = TERABYTE * 1024;
    private StringUtils() {
    }
    public static final String LINE_SEP = System.getProperty("line.separator");
    public static Vector lineSplit(String data) {
        return split(data, '\n');
    }
    public static Vector split(String data, int ch) {
        Vector elems = new Vector();
        int pos = -1;
        int i = 0;
        while ((pos = data.indexOf(ch, i)) != -1) {
            String elem = data.substring(i, pos);
            elems.addElement(elem);
            i = pos + 1;
        }
        elems.addElement(data.substring(i));
        return elems;
    }
    public static String replace(String data, String from, String to) {
        StringBuffer buf = new StringBuffer(data.length());
        int pos = -1;
        int i = 0;
        while ((pos = data.indexOf(from, i)) != -1) {
            buf.append(data.substring(i, pos)).append(to);
            i = pos + from.length();
        }
        buf.append(data.substring(i));
        return buf.toString();
    }
    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.flush();
        pw.close();
        return sw.toString();
    }
    public static boolean endsWith(StringBuffer buffer, String suffix) {
        if (suffix.length() > buffer.length()) {
            return false;
        }
        int endIndex = suffix.length() - 1;
        int bufferIndex = buffer.length() - 1;
        while (endIndex >= 0) {
            if (buffer.charAt(bufferIndex) != suffix.charAt(endIndex)) {
                return false;
            }
            bufferIndex--;
            endIndex--;
        }
        return true;
    }
    public static String resolveBackSlash(String input) {
        StringBuffer b = new StringBuffer();
        boolean backSlashSeen = false;
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            if (!backSlashSeen) {
                if (c == '\\') {
                    backSlashSeen = true;
                } else {
                    b.append(c);
                }
            } else {
                switch (c) {
                    case '\\':
                        b.append((char) '\\');
                        break;
                    case 'n':
                        b.append((char) '\n');
                        break;
                    case 'r':
                        b.append((char) '\r');
                        break;
                    case 't':
                        b.append((char) '\t');
                        break;
                    case 'f':
                        b.append((char) '\f');
                        break;
                    case 's':
                        b.append(" \t\n\r\f");
                        break;
                    default:
                        b.append(c);
                }
                backSlashSeen = false;
            }
        }
        return b.toString();
    }
    public static long parseHumanSizes(String humanSize) throws Exception {
        long factor = 1L;
        char s = humanSize.charAt(0);
        switch (s) {
            case '+':
                humanSize = humanSize.substring(1);
                break;
            case '-':
                factor = -1L;
                humanSize = humanSize.substring(1);
                break;
            default:
                break;
        }
        char c = humanSize.charAt(humanSize.length() - 1);
        if (!Character.isDigit(c)) {
            int trim = 1;
            switch (c) {
                case 'K':
                    factor *= KILOBYTE;
                    break;
                case 'M':
                    factor *= MEGABYTE;
                    break;
                case 'G':
                    factor *= GIGABYTE;
                    break;
                case 'T':
                    factor *= TERABYTE;
                    break;
                case 'P':
                    factor *= PETABYTE;
                    break;
                default:
                    trim = 0;
            }
            humanSize = humanSize.substring(0, humanSize.length() - trim);
        }
        try {
            return factor * Long.parseLong(humanSize);
        } catch (NumberFormatException e) {
            throw new BuildException("Failed to parse \"" + humanSize + "\"", e);
        }
    }
    public static String removeSuffix(String string, String suffix) {
        if (string.endsWith(suffix)) {
            return string.substring(0, string.length() - suffix.length());
        } else {
            return string;
        }
    }
    public static String removePrefix(String string, String prefix) {
        if (string.startsWith(prefix)) {
            return string.substring(prefix.length());
        } else {
            return string;
        }
    }
}
