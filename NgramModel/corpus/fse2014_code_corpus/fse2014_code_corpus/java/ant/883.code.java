package org.apache.tools.zip;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipException;
public class ZipOutputStream extends FilterOutputStream {
    private static final int BYTE_MASK = 0xFF;
    private static final int SHORT = 2;
    private static final int WORD = 4;
    private static final int BUFFER_SIZE = 512;
    private static final int DEFLATER_BLOCK_SIZE = 8192;
    public static final int DEFLATED = java.util.zip.ZipEntry.DEFLATED;
    public static final int DEFAULT_COMPRESSION = Deflater.DEFAULT_COMPRESSION;
    public static final int STORED = java.util.zip.ZipEntry.STORED;
    static final String DEFAULT_ENCODING = null;
    public static final int UFT8_NAMES_FLAG = 1 << 11;
    public static final int EFS_FLAG = UFT8_NAMES_FLAG;
    private ZipEntry entry;
    private String comment = "";
    private int level = DEFAULT_COMPRESSION;
    private boolean hasCompressionLevelChanged = false;
    private int method = java.util.zip.ZipEntry.DEFLATED;
    private final List entries = new LinkedList();
    private final CRC32 crc = new CRC32();
    private long written = 0;
    private long dataStart = 0;
    private long localDataStart = 0;
    private long cdOffset = 0;
    private long cdLength = 0;
    private static final byte[] ZERO = {0, 0};
    private static final byte[] LZERO = {0, 0, 0, 0};
    private final Map offsets = new HashMap();
    private String encoding = null;
    private ZipEncoding zipEncoding =
        ZipEncodingHelper.getZipEncoding(DEFAULT_ENCODING);
    protected Deflater def = new Deflater(level, true);
    protected byte[] buf = new byte[BUFFER_SIZE];
    private RandomAccessFile raf = null;
    private boolean useUTF8Flag = true; 
    private boolean fallbackToUTF8 = false;
    private UnicodeExtraFieldPolicy createUnicodeExtraFields =
        UnicodeExtraFieldPolicy.NEVER;
    public ZipOutputStream(OutputStream out) {
        super(out);
    }
    public ZipOutputStream(File file) throws IOException {
        super(null);
        try {
            raf = new RandomAccessFile(file, "rw");
            raf.setLength(0);
        } catch (IOException e) {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException inner) {
                }
                raf = null;
            }
            out = new FileOutputStream(file);
        }
    }
    public boolean isSeekable() {
        return raf != null;
    }
    public void setEncoding(final String encoding) {
        this.encoding = encoding;
        this.zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
        useUTF8Flag &= ZipEncodingHelper.isUTF8(encoding);
    }
    public String getEncoding() {
        return encoding;
    }
    public void setUseLanguageEncodingFlag(boolean b) {
        useUTF8Flag = b && ZipEncodingHelper.isUTF8(encoding);
    }
    public void setCreateUnicodeExtraFields(UnicodeExtraFieldPolicy b) {
        createUnicodeExtraFields = b;
    }
    public void setFallbackToUTF8(boolean b) {
        fallbackToUTF8 = b;
    }
    public void finish() throws IOException {
        closeEntry();
        cdOffset = written;
        for (Iterator i = entries.iterator(); i.hasNext(); ) {
            writeCentralFileHeader((ZipEntry) i.next());
        }
        cdLength = written - cdOffset;
        writeCentralDirectoryEnd();
        offsets.clear();
        entries.clear();
    }
    public void closeEntry() throws IOException {
        if (entry == null) {
            return;
        }
        long realCrc = crc.getValue();
        crc.reset();
        if (entry.getMethod() == DEFLATED) {
            def.finish();
            while (!def.finished()) {
                deflate();
            }
            entry.setSize(adjustToLong(def.getTotalIn()));
            entry.setCompressedSize(adjustToLong(def.getTotalOut()));
            entry.setCrc(realCrc);
            def.reset();
            written += entry.getCompressedSize();
        } else if (raf == null) {
            if (entry.getCrc() != realCrc) {
                throw new ZipException("bad CRC checksum for entry "
                                       + entry.getName() + ": "
                                       + Long.toHexString(entry.getCrc())
                                       + " instead of "
                                       + Long.toHexString(realCrc));
            }
            if (entry.getSize() != written - dataStart) {
                throw new ZipException("bad size for entry "
                                       + entry.getName() + ": "
                                       + entry.getSize()
                                       + " instead of "
                                       + (written - dataStart));
            }
        } else { 
            long size = written - dataStart;
            entry.setSize(size);
            entry.setCompressedSize(size);
            entry.setCrc(realCrc);
        }
        if (raf != null) {
            long save = raf.getFilePointer();
            raf.seek(localDataStart);
            writeOut(ZipLong.getBytes(entry.getCrc()));
            writeOut(ZipLong.getBytes(entry.getCompressedSize()));
            writeOut(ZipLong.getBytes(entry.getSize()));
            raf.seek(save);
        }
        writeDataDescriptor(entry);
        entry = null;
    }
    public void putNextEntry(ZipEntry ze) throws IOException {
        closeEntry();
        entry = ze;
        entries.add(entry);
        if (entry.getMethod() == -1) { 
            entry.setMethod(method);
        }
        if (entry.getTime() == -1) { 
            entry.setTime(System.currentTimeMillis());
        }
        if (entry.getMethod() == STORED && raf == null) {
            if (entry.getSize() == -1) {
                throw new ZipException("uncompressed size is required for"
                                       + " STORED method when not writing to a"
                                       + " file");
            }
            if (entry.getCrc() == -1) {
                throw new ZipException("crc checksum is required for STORED"
                                       + " method when not writing to a file");
            }
            entry.setCompressedSize(entry.getSize());
        }
        if (entry.getMethod() == DEFLATED && hasCompressionLevelChanged) {
            def.setLevel(level);
            hasCompressionLevelChanged = false;
        }
        writeLocalFileHeader(entry);
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public void setLevel(int level) {
        if (level < Deflater.DEFAULT_COMPRESSION
            || level > Deflater.BEST_COMPRESSION) {
            throw new IllegalArgumentException("Invalid compression level: "
                                               + level);
        }
        hasCompressionLevelChanged = (this.level != level);
        this.level = level;
    }
    public void setMethod(int method) {
        this.method = method;
    }
    public void write(byte[] b, int offset, int length) throws IOException {
        if (entry.getMethod() == DEFLATED) {
            if (length > 0) {
                if (!def.finished()) {
                    if (length <= DEFLATER_BLOCK_SIZE) {
                        def.setInput(b, offset, length);
                        deflateUntilInputIsNeeded();
                    } else {
                        final int fullblocks = length / DEFLATER_BLOCK_SIZE;
                        for (int i = 0; i < fullblocks; i++) {
                            def.setInput(b, offset + i * DEFLATER_BLOCK_SIZE,
                                         DEFLATER_BLOCK_SIZE);
                            deflateUntilInputIsNeeded();
                        }
                        final int done = fullblocks * DEFLATER_BLOCK_SIZE;
                        if (done < length) {
                            def.setInput(b, offset + done, length - done);
                            deflateUntilInputIsNeeded();
                        }
                    }
                }
            }
        } else {
            writeOut(b, offset, length);
            written += length;
        }
        crc.update(b, offset, length);
    }
    public void write(int b) throws IOException {
        byte[] buff = new byte[1];
        buff[0] = (byte) (b & BYTE_MASK);
        write(buff, 0, 1);
    }
    public void close() throws IOException {
        finish();
        if (raf != null) {
            raf.close();
        }
        if (out != null) {
            out.close();
        }
    }
    public void flush() throws IOException {
        if (out != null) {
            out.flush();
        }
    }
    protected static final byte[] LFH_SIG = ZipLong.getBytes(0X04034B50L);
    protected static final byte[] DD_SIG = ZipLong.getBytes(0X08074B50L);
    protected static final byte[] CFH_SIG = ZipLong.getBytes(0X02014B50L);
    protected static final byte[] EOCD_SIG = ZipLong.getBytes(0X06054B50L);
    protected final void deflate() throws IOException {
        int len = def.deflate(buf, 0, buf.length);
        if (len > 0) {
            writeOut(buf, 0, len);
        }
    }
    protected void writeLocalFileHeader(ZipEntry ze) throws IOException {
        boolean encodable = zipEncoding.canEncode(ze.getName());
        final ZipEncoding entryEncoding;
        if (!encodable && fallbackToUTF8) {
            entryEncoding = ZipEncodingHelper.UTF8_ZIP_ENCODING;
        } else {
            entryEncoding = zipEncoding;
        }
        ByteBuffer name = entryEncoding.encode(ze.getName());        
        if (createUnicodeExtraFields != UnicodeExtraFieldPolicy.NEVER) {
            if (createUnicodeExtraFields == UnicodeExtraFieldPolicy.ALWAYS
                || !encodable) {
                ze.addExtraField(new UnicodePathExtraField(ze.getName(),
                                                           name.array(),
                                                           name.arrayOffset(),
                                                           name.limit()));
            }
            String comm = ze.getComment();
            if (comm != null && !"".equals(comm)) {
                boolean commentEncodable = this.zipEncoding.canEncode(comm);
                if (createUnicodeExtraFields == UnicodeExtraFieldPolicy.ALWAYS
                    || !commentEncodable) {
                    ByteBuffer commentB = entryEncoding.encode(comm);
                    ze.addExtraField(new UnicodeCommentExtraField(comm,
                                                                  commentB.array(),
                                                                  commentB.arrayOffset(),
                                                                  commentB.limit())
                                     );
                }
            }
        }
        offsets.put(ze, ZipLong.getBytes(written));
        writeOut(LFH_SIG);
        written += WORD;
        final int zipMethod = ze.getMethod();
        writeVersionNeededToExtractAndGeneralPurposeBits(zipMethod,
                                                         !encodable
                                                         && fallbackToUTF8);
        written += WORD;
        writeOut(ZipShort.getBytes(zipMethod));
        written += SHORT;
        writeOut(toDosTime(ze.getTime()));
        written += WORD;
        localDataStart = written;
        if (zipMethod == DEFLATED || raf != null) {
            writeOut(LZERO);
            writeOut(LZERO);
            writeOut(LZERO);
        } else {
            writeOut(ZipLong.getBytes(ze.getCrc()));
            writeOut(ZipLong.getBytes(ze.getSize()));
            writeOut(ZipLong.getBytes(ze.getSize()));
        }
        written += 12;
        writeOut(ZipShort.getBytes(name.limit()));
        written += SHORT;
        byte[] extra = ze.getLocalFileDataExtra();
        writeOut(ZipShort.getBytes(extra.length));
        written += SHORT;
        writeOut(name.array(), name.arrayOffset(), name.limit());
        written += name.limit();
        writeOut(extra);
        written += extra.length;
        dataStart = written;
    }
    protected void writeDataDescriptor(ZipEntry ze) throws IOException {
        if (ze.getMethod() != DEFLATED || raf != null) {
            return;
        }
        writeOut(DD_SIG);
        writeOut(ZipLong.getBytes(entry.getCrc()));
        writeOut(ZipLong.getBytes(entry.getCompressedSize()));
        writeOut(ZipLong.getBytes(entry.getSize()));
        written += 16;
    }
    protected void writeCentralFileHeader(ZipEntry ze) throws IOException {
        writeOut(CFH_SIG);
        written += WORD;
        writeOut(ZipShort.getBytes((ze.getPlatform() << 8) | 20));
        written += SHORT;
        final int zipMethod = ze.getMethod();
        final boolean encodable = zipEncoding.canEncode(ze.getName());
        writeVersionNeededToExtractAndGeneralPurposeBits(zipMethod,
                                                         !encodable
                                                         && fallbackToUTF8);
        written += WORD;
        writeOut(ZipShort.getBytes(zipMethod));
        written += SHORT;
        writeOut(toDosTime(ze.getTime()));
        written += WORD;
        writeOut(ZipLong.getBytes(ze.getCrc()));
        writeOut(ZipLong.getBytes(ze.getCompressedSize()));
        writeOut(ZipLong.getBytes(ze.getSize()));
        written += 12;
        final ZipEncoding entryEncoding;
        if (!encodable && fallbackToUTF8) {
            entryEncoding = ZipEncodingHelper.UTF8_ZIP_ENCODING;
        } else {
            entryEncoding = zipEncoding;
        }
        ByteBuffer name = entryEncoding.encode(ze.getName());        
        writeOut(ZipShort.getBytes(name.limit()));
        written += SHORT;
        byte[] extra = ze.getCentralDirectoryExtra();
        writeOut(ZipShort.getBytes(extra.length));
        written += SHORT;
        String comm = ze.getComment();
        if (comm == null) {
            comm = "";
        }
        ByteBuffer commentB = entryEncoding.encode(comm);
        writeOut(ZipShort.getBytes(commentB.limit()));
        written += SHORT;
        writeOut(ZERO);
        written += SHORT;
        writeOut(ZipShort.getBytes(ze.getInternalAttributes()));
        written += SHORT;
        writeOut(ZipLong.getBytes(ze.getExternalAttributes()));
        written += WORD;
        writeOut((byte[]) offsets.get(ze));
        written += WORD;
        writeOut(name.array(), name.arrayOffset(), name.limit());
        written += name.limit();
        writeOut(extra);
        written += extra.length;
        writeOut(commentB.array(), commentB.arrayOffset(), commentB.limit());
        written += commentB.limit();
    }
    protected void writeCentralDirectoryEnd() throws IOException {
        writeOut(EOCD_SIG);
        writeOut(ZERO);
        writeOut(ZERO);
        byte[] num = ZipShort.getBytes(entries.size());
        writeOut(num);
        writeOut(num);
        writeOut(ZipLong.getBytes(cdLength));
        writeOut(ZipLong.getBytes(cdOffset));
        ByteBuffer data = this.zipEncoding.encode(comment);
        writeOut(ZipShort.getBytes(data.limit()));
        writeOut(data.array(), data.arrayOffset(), data.limit());
    }
    private static final byte[] DOS_TIME_MIN = ZipLong.getBytes(0x00002100L);
    protected static ZipLong toDosTime(Date time) {
        return new ZipLong(toDosTime(time.getTime()));
    }
    protected static byte[] toDosTime(long t) {
        Date time = new Date(t);
        int year = time.getYear() + 1900;
        if (year < 1980) {
            return DOS_TIME_MIN;
        }
        int month = time.getMonth() + 1;
        long value =  ((year - 1980) << 25)
            |         (month << 21)
            |         (time.getDate() << 16)
            |         (time.getHours() << 11)
            |         (time.getMinutes() << 5)
            |         (time.getSeconds() >> 1);
        return ZipLong.getBytes(value);
    }
    protected byte[] getBytes(String name) throws ZipException {
        try {
            ByteBuffer b =
                ZipEncodingHelper.getZipEncoding(encoding).encode(name);
            byte[] result = new byte[b.limit()];
            System.arraycopy(b.array(), b.arrayOffset(), result, 0,
                             result.length);
            return result;
        } catch (IOException ex) {
            throw new ZipException("Failed to encode name: " + ex.getMessage());
        }
    }
    protected final void writeOut(byte[] data) throws IOException {
        writeOut(data, 0, data.length);
    }
    protected final void writeOut(byte[] data, int offset, int length)
        throws IOException {
        if (raf != null) {
            raf.write(data, offset, length);
        } else {
            out.write(data, offset, length);
        }
    }
    protected static long adjustToLong(int i) {
        if (i < 0) {
            return 2 * ((long) Integer.MAX_VALUE) + 2 + i;
        } else {
            return i;
        }
    }
    private void deflateUntilInputIsNeeded() throws IOException {
        while (!def.needsInput()) {
            deflate();
        }
    }
    private void writeVersionNeededToExtractAndGeneralPurposeBits(final int
                                                                  zipMethod,
                                                                  final boolean
                                                                  utfFallback)
        throws IOException {
        int versionNeededToExtract = 10;
        int generalPurposeFlag = (useUTF8Flag || utfFallback) ? UFT8_NAMES_FLAG : 0;
        if (zipMethod == DEFLATED && raf == null) {
            versionNeededToExtract =  20;
            generalPurposeFlag |= 8;
        }
        writeOut(ZipShort.getBytes(versionNeededToExtract));
        writeOut(ZipShort.getBytes(generalPurposeFlag));
    }
    public static final class UnicodeExtraFieldPolicy {
        public static final UnicodeExtraFieldPolicy ALWAYS =
            new UnicodeExtraFieldPolicy("always");
        public static final UnicodeExtraFieldPolicy NEVER =
            new UnicodeExtraFieldPolicy("never");
        public static final UnicodeExtraFieldPolicy NOT_ENCODEABLE =
            new UnicodeExtraFieldPolicy("not encodeable");
        private final String name;
        private UnicodeExtraFieldPolicy(String n) {
            name = n;
        }
        public String toString() {
            return name;
        }
    }
}
