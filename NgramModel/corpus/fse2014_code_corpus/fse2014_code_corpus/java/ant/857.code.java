package org.apache.tools.mail;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
public class SmtpResponseReader {
    protected BufferedReader reader = null;
    private StringBuffer result = new StringBuffer();
    public SmtpResponseReader(InputStream in) {
        reader = new BufferedReader(new InputStreamReader(in));
    }
    public String getResponse() throws IOException {
        result.setLength(0);
        String line = reader.readLine();
        if (line != null && line.length() >= 3) {
            result.append(line.substring(0, 3));
            result.append(" ");
        }
        while (line != null) {
            append(line);
            if (!hasMoreLines(line)) {
                break;
            }
            line = reader.readLine();
        }
        return result.toString().trim();
    }
    public void close() throws IOException {
        reader.close();
    }
    protected boolean hasMoreLines(String line) {
        return line.length() > 3 && line.charAt(3) == '-';
    }
    private void append(String line) {
        if (line.length() > 4) {
            result.append(line.substring(4));
            result.append(" ");
        }
    }
}
