package org.apache.xerces.impl.io;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Locale;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.util.MessageFormatter;
public final class UTF16Reader 
    extends Reader {
    public static final int DEFAULT_BUFFER_SIZE = 4096;
    protected final InputStream fInputStream;
    protected final byte[] fBuffer;
    protected final boolean fIsBigEndian;
    private final MessageFormatter fFormatter;
    private final Locale fLocale;
    public UTF16Reader(InputStream inputStream, boolean isBigEndian) {
        this(inputStream, DEFAULT_BUFFER_SIZE, isBigEndian, 
                new XMLMessageFormatter(), Locale.getDefault());
    } 
    public UTF16Reader(InputStream inputStream, boolean isBigEndian, 
            MessageFormatter messageFormatter, Locale locale) {
        this(inputStream, DEFAULT_BUFFER_SIZE, isBigEndian, messageFormatter, locale);
    } 
    public UTF16Reader(InputStream inputStream, int size, boolean isBigEndian, 
            MessageFormatter messageFormatter, Locale locale) {
        this(inputStream, new byte[size], isBigEndian, messageFormatter, locale);
    } 
    public UTF16Reader(InputStream inputStream, byte [] buffer, boolean isBigEndian, 
            MessageFormatter messageFormatter, Locale locale) {
        fInputStream = inputStream;
        fBuffer = buffer;
        fIsBigEndian = isBigEndian;
        fFormatter = messageFormatter;
        fLocale = locale;
    } 
    public int read() throws IOException {
        final int b0 = fInputStream.read();
        if (b0 == -1) {
            return -1;
        }
        final int b1 = fInputStream.read();
        if (b1 == -1) {
            expectedTwoBytes();
        }
        if (fIsBigEndian) {
            return (b0 << 8) | b1;
        }
        return (b1 << 8) | b0;
    } 
    public int read(char ch[], int offset, int length) throws IOException {
        int byteLength = length << 1;
        if (byteLength > fBuffer.length) {
            byteLength = fBuffer.length;
        }
        int byteCount = fInputStream.read(fBuffer, 0, byteLength);
        if (byteCount == -1) {
            return -1;
        }
        if ((byteCount & 1) != 0) {
            int b = fInputStream.read();
            if (b == -1) {
                expectedTwoBytes();
            }
            fBuffer[byteCount++] = (byte) b;
        }
        final int charCount = byteCount >> 1;
        if (fIsBigEndian) {
            processBE(ch, offset, charCount);
        }
        else {
            processLE(ch, offset, charCount);
        }
        return charCount;
    } 
    public long skip(long n) throws IOException {
        long bytesSkipped = fInputStream.skip(n << 1);
        if ((bytesSkipped & 1) != 0) {
            int b = fInputStream.read();
            if (b == -1) {
                expectedTwoBytes();
            }
            ++bytesSkipped;
        }
        return bytesSkipped >> 1;
    } 
    public boolean ready() throws IOException {
        return false;
    } 
    public boolean markSupported() {
        return false;
    } 
    public void mark(int readAheadLimit) throws IOException {
        throw new IOException(fFormatter.formatMessage(fLocale, "OperationNotSupported", new Object[]{"mark()", "UTF-16"}));
    } 
    public void reset() throws IOException {
    } 
     public void close() throws IOException {
         fInputStream.close();
     } 
     private void processBE(final char ch[], int offset, final int count) {
         int curPos = 0;
         for (int i = 0; i < count; ++i) {
             final int b0 = fBuffer[curPos++] & 0xff;
             final int b1 = fBuffer[curPos++] & 0xff;
             ch[offset++] = (char) ((b0 << 8) | b1);
         }
     } 
     private void processLE(final char ch[], int offset, final int count) {
         int curPos = 0;
         for (int i = 0; i < count; ++i) {
             final int b0 = fBuffer[curPos++] & 0xff;
             final int b1 = fBuffer[curPos++] & 0xff;
             ch[offset++] = (char) ((b1 << 8) | b0);
         }
     } 
     private void expectedTwoBytes()
         throws MalformedByteSequenceException {
         throw new MalformedByteSequenceException(fFormatter,
             fLocale,
             XMLMessageFormatter.XML_DOMAIN,
             "ExpectedByte",
             new Object[] {"2", "2"});
     } 
} 
