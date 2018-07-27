package org.apache.batik.util.io;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
public class GenericDecoder implements CharDecoder {
    protected Reader reader;
    public GenericDecoder(InputStream is, String enc) throws IOException {
        reader = new InputStreamReader(is, enc);
        reader = new BufferedReader(reader);
    }
    public GenericDecoder(Reader r) {
        reader = r;
        if (!(r instanceof BufferedReader)) {
            reader = new BufferedReader(reader);
        }
    }
    public int readChar() throws IOException {
        return reader.read();
    }
    public void dispose() throws IOException {
        reader.close();
        reader = null;
    }
}
