package org.apache.batik.util.io;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import org.apache.batik.util.EncodingUtilities;
public class StreamNormalizingReader extends NormalizingReader {
    protected CharDecoder charDecoder;
    protected int nextChar = -1;
    protected int line = 1;
    protected int column;
    public StreamNormalizingReader(InputStream is) throws IOException {
        this(is, null);
    }
    public StreamNormalizingReader(InputStream is, String enc)
        throws IOException {
        if (enc == null) {
            enc = "ISO-8859-1";
        }
        charDecoder = createCharDecoder(is, enc);
    }
    public StreamNormalizingReader(Reader r) throws IOException {
        charDecoder = new GenericDecoder(r);
    }
    protected StreamNormalizingReader() {
    }
    public int read() throws IOException {
        int result = nextChar;
        if (result != -1) {
            nextChar = -1;
            if (result == 13) {
                column = 0;
                line++;
            } else {
                column++;
            }
            return result;
        }
        result = charDecoder.readChar();
        switch (result) {
        case 13:
            column = 0;
            line++;
            int c = charDecoder.readChar();
            if (c == 10) {
                return 10;
            }
            nextChar = c;
            return 10;
        case 10:
            column = 0;
            line++;
        }
        return result;
    }
    public int getLine() {
        return line;
    }
    public int getColumn() {
        return column;
    }
    public void close() throws IOException {
        charDecoder.dispose();
        charDecoder = null;
    }
    protected CharDecoder createCharDecoder(InputStream is, String enc)
        throws IOException {
        CharDecoderFactory cdf =
            (CharDecoderFactory)charDecoderFactories.get(enc.toUpperCase());
        if (cdf != null) {
            return cdf.createCharDecoder(is);
        }
        String e = EncodingUtilities.javaEncoding(enc);
        if (e == null) {
            e = enc;
        }
        return new GenericDecoder(is, e);
    }
    protected static final Map charDecoderFactories = new HashMap(11);
    static {
        CharDecoderFactory cdf = new ASCIIDecoderFactory();
        charDecoderFactories.put("ASCII", cdf);
        charDecoderFactories.put("US-ASCII", cdf);
        charDecoderFactories.put("ISO-8859-1", new ISO_8859_1DecoderFactory());
        charDecoderFactories.put("UTF-8", new UTF8DecoderFactory());
        charDecoderFactories.put("UTF-16", new UTF16DecoderFactory());
    }
    protected interface CharDecoderFactory {
        CharDecoder createCharDecoder(InputStream is) throws IOException;
    }
    protected static class ASCIIDecoderFactory
        implements CharDecoderFactory {
        public CharDecoder createCharDecoder(InputStream is)
            throws IOException {
            return new ASCIIDecoder(is);
        }
    }
    protected static class ISO_8859_1DecoderFactory
        implements CharDecoderFactory {
        public CharDecoder createCharDecoder(InputStream is)
            throws IOException {
            return new ISO_8859_1Decoder(is);
        }
    }
    protected static class UTF8DecoderFactory
        implements CharDecoderFactory {
        public CharDecoder createCharDecoder(InputStream is)
            throws IOException {
            return new UTF8Decoder(is);
        }
    }
    protected static class UTF16DecoderFactory
        implements CharDecoderFactory {
        public CharDecoder createCharDecoder(InputStream is)
            throws IOException {
            return new UTF16Decoder(is);
        }
    }
}
