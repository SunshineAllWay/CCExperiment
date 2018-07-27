package org.apache.tools.ant.util.regexp;
public class RegexpUtil {
    public static boolean hasFlag(int options, int flag) {
        return ((options & flag) > 0);
    }
    public static int removeFlag(int options, int flag) {
        return (options & (0xFFFFFFFF - flag));
    }
    public static int asOptions(String flags) {
        int options = RegexpMatcher.MATCH_DEFAULT;
        if (flags != null) {
            options = asOptions(flags.indexOf('i') == -1,
                                flags.indexOf('m') != -1,
                                flags.indexOf('s') != -1);
            if (flags.indexOf('g') != -1) {
                options |= Regexp.REPLACE_ALL;
            }
        }
        return options;
    }
    public static int asOptions(boolean caseSensitive) {
        return asOptions(caseSensitive, false, false);
    }
    public static int asOptions(boolean caseSensitive, boolean multiLine,
                                boolean singleLine) {
        int options = RegexpMatcher.MATCH_DEFAULT;
        if (!caseSensitive) {
            options = options | RegexpMatcher.MATCH_CASE_INSENSITIVE;
        }
        if (multiLine) {
            options = options | RegexpMatcher.MATCH_MULTILINE;
        }
        if (singleLine) {
            options = options | RegexpMatcher.MATCH_SINGLELINE;
        }
        return options;
    }
}
