package org.apache.batik.parser;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import org.apache.batik.util.io.NormalizingReader;
import org.apache.batik.util.io.StreamNormalizingReader;
import org.apache.batik.util.io.StringNormalizingReader;
public abstract class AbstractScanner {
    protected NormalizingReader reader;
    protected int current;
    protected char[] buffer = new char[128];
    protected int position;
    protected int type;
    protected int previousType;
    protected int start;
    protected int end;
    protected int blankCharacters;
    public AbstractScanner(Reader r) throws ParseException {
        try {
            reader = new StreamNormalizingReader(r);
            current = nextChar();
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }
    public AbstractScanner(InputStream is, String enc) throws ParseException {
        try {
            reader = new StreamNormalizingReader(is, enc);
            current = nextChar();
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }
    public AbstractScanner(String s) throws ParseException {
        try {
            reader = new StringNormalizingReader(s);
            current = nextChar();
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }
    public int getLine() {
        return reader.getLine();
    }
    public int getColumn() {
        return reader.getColumn();
    }
    public char[] getBuffer() {
        return buffer;
    }
    public int getStart() {
        return start;
    }
    public int getEnd() {
        return end;
    }
    public void clearBuffer() {
        if (position <= 0) {
            position = 0;
        } else {
            buffer[0] = buffer[position-1];
            position = 1;
        }
    }
    public int getType() {
        return type;
    }
    public String getStringValue() {
        return new String(buffer, start, end - start);
    }
    public int next() throws ParseException {
        blankCharacters = 0;
        start = position - 1;
        previousType = type;
        nextToken();
        end = position - endGap();
        return type;
    }
    protected abstract int endGap();
    protected abstract void nextToken() throws ParseException;
    protected static boolean isEqualIgnoreCase(int i, char c) {
        return (i == -1) ? false : Character.toLowerCase((char)i) == c;
    }
    protected int nextChar() throws IOException {
        current = reader.read();
        if (current == -1) {
            return current;
        }
        if (position == buffer.length) {
            char[] t = new char[ 1 + position + position / 2];
            System.arraycopy( buffer, 0, t, 0, position );
            buffer = t;
        }
        return buffer[position++] = (char)current;
    }
}
