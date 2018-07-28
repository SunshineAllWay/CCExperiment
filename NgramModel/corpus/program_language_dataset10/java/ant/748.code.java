package org.apache.tools.ant.types.selectors;
import java.io.File;
public class TokenizedPattern {
    public static final TokenizedPattern EMPTY_PATTERN =
        new TokenizedPattern("", new String[0]);
    private final String pattern;
    private final String tokenizedPattern[];
    public TokenizedPattern(String pattern) {
        this(pattern, SelectorUtils.tokenizePathAsArray(pattern));
    }
    TokenizedPattern(String pattern, String[] tokens) {
        this.pattern = pattern;
        this.tokenizedPattern = tokens;
    }
    public boolean matchPath(TokenizedPath path, boolean isCaseSensitive) {
        return SelectorUtils.matchPath(tokenizedPattern, path.getTokens(),
                                       isCaseSensitive);
    }
    public boolean matchStartOf(TokenizedPath path,
                                boolean caseSensitive) {
        return SelectorUtils.matchPatternStart(tokenizedPattern,
                                               path.getTokens(), caseSensitive);
    }
    public String toString() {
        return pattern;
    }
    public String getPattern() {
        return pattern;
    }
    public boolean equals(Object o) {
        return o instanceof TokenizedPattern
            && pattern.equals(((TokenizedPattern) o).pattern);
    }
    public int hashCode() {
        return pattern.hashCode();
    }
    public int depth() {
        return tokenizedPattern.length;
    }
    public boolean containsPattern(String pat) {
        for (int i = 0; i < tokenizedPattern.length; i++) {
            if (tokenizedPattern[i].equals(pat)) {
                return true;
            }
        }
        return false;
    }
    public TokenizedPath rtrimWildcardTokens() {
        StringBuffer sb = new StringBuffer();
        int newLen = 0;
        for (; newLen < tokenizedPattern.length; newLen++) {
            if (SelectorUtils.hasWildcards(tokenizedPattern[newLen])) {
                break;
            }
            if (newLen > 0
                && sb.charAt(sb.length() - 1) != File.separatorChar) {
                sb.append(File.separator);
            }
            sb.append(tokenizedPattern[newLen]);
        }
        if (newLen == 0) {
            return TokenizedPath.EMPTY_PATH;
        }
        String[] newPats = new String[newLen];
        System.arraycopy(tokenizedPattern, 0, newPats, 0, newLen);
        return new TokenizedPath(sb.toString(), newPats);
    }
    public boolean endsWith(String s) {
        return tokenizedPattern.length > 0
            && tokenizedPattern[tokenizedPattern.length - 1].equals(s);
    }
    public TokenizedPattern withoutLastToken() {
        if (tokenizedPattern.length == 0) {
            throw new IllegalStateException("cant strip a token from nothing");
        } else if (tokenizedPattern.length == 1) {
            return EMPTY_PATTERN;
        } else {
            String toStrip = tokenizedPattern[tokenizedPattern.length - 1];
            int index = pattern.lastIndexOf(toStrip);
            String[] tokens = new String[tokenizedPattern.length - 1];
            System.arraycopy(tokenizedPattern, 0, tokens, 0,
                             tokenizedPattern.length - 1);
            return new TokenizedPattern(pattern.substring(0, index), tokens);
        }
    }
}
