package org.apache.tools.ant.util;
import java.io.IOException;
import java.io.Reader;
import org.apache.tools.ant.ProjectComponent;
public class StringTokenizer extends ProjectComponent implements Tokenizer {
    private static final int NOT_A_CHAR = -2;
    private String intraString = "";
    private int    pushed = NOT_A_CHAR;
    private char[] delims = null;
    private boolean delimsAreTokens = false;
    private boolean suppressDelims = false;
    private boolean includeDelims = false;
    public void setDelims(String delims) {
        this.delims = StringUtils.resolveBackSlash(delims).toCharArray();
    }
    public void setDelimsAreTokens(boolean delimsAreTokens) {
        this.delimsAreTokens = delimsAreTokens;
    }
    public void setSuppressDelims(boolean suppressDelims) {
        this.suppressDelims = suppressDelims;
    }
    public void setIncludeDelims(boolean includeDelims) {
        this.includeDelims = includeDelims;
    }
    public String getToken(Reader in) throws IOException {
        int ch = -1;
        if (pushed != NOT_A_CHAR) {
            ch = pushed;
            pushed = NOT_A_CHAR;
        } else {
            ch = in.read();
        }
        if (ch == -1) {
            return null;
        }
        boolean inToken = true;
        intraString = "";
        StringBuffer word = new StringBuffer();
        StringBuffer padding = new StringBuffer();
        while (ch != -1) {
            char c = (char) ch;
            boolean isDelim = isDelim(c);
            if (inToken) {
                if (isDelim) {
                    if (delimsAreTokens) {
                        if (word.length() == 0) {
                            word.append(c);
                        } else {
                            pushed = ch;
                        }
                        break;
                    }
                    padding.append(c);
                    inToken = false;
                } else {
                    word.append(c);
                }
            } else {
                if (isDelim) {
                    padding.append(c);
                } else {
                    pushed = ch;
                    break;
                }
            }
            ch = in.read();
        }
        intraString = padding.toString();
        if (includeDelims) {
            word.append(intraString);
        }
        return word.toString();
    }
    public String getPostToken() {
        return suppressDelims || includeDelims ? "" : intraString;
    }
    private boolean isDelim(char ch) {
        if (delims == null) {
            return Character.isWhitespace(ch);
        }
        for (int i = 0; i < delims.length; ++i) {
            if (delims[i] == ch) {
                return true;
            }
        }
        return false;
    }
}
