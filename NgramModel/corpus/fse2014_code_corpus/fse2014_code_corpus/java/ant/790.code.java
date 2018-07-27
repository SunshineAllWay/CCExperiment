package org.apache.tools.ant.util;
import java.io.Reader;
import java.io.IOException;
import org.apache.tools.ant.ProjectComponent;
public class LineTokenizer extends ProjectComponent
    implements Tokenizer {
    private static final int NOT_A_CHAR = -2;
    private String  lineEnd = "";
    private int     pushed = NOT_A_CHAR;
    private boolean includeDelims = false;
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
        lineEnd = "";
        StringBuffer line = new StringBuffer();
        int state = 0;
        while (ch != -1) {
            if (state == 0) {
                if (ch == '\r') {
                    state = 1;
                } else if (ch == '\n') {
                    lineEnd = "\n";
                    break;
                } else {
                    line.append((char) ch);
                }
            } else {
                state = 0;
                if (ch == '\n') {
                    lineEnd = "\r\n";
                } else {
                    pushed = ch;
                    lineEnd = "\r";
                }
                break;
            }
            ch = in.read();
        }
        if (ch == -1 && state == 1) {
            lineEnd = "\r";
        }
        if (includeDelims) {
            line.append(lineEnd);
        }
        return line.toString();
    }
    public String getPostToken() {
        if (includeDelims) {
            return "";
        }
        return lineEnd;
    }
}
