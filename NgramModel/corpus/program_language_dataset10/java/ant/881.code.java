package org.apache.tools.zip;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;
public class ZipFile {
    private static final int HASH_SIZE = 509;
    private static final int SHORT     =   2;
    private static final int WORD      =   4;
    private static final int NIBLET_MASK = 0x0f;
    private static final int BYTE_SHIFT = 8;
    private static final int POS_0 = 0;
    private static final int POS_1 = 1;
    private static final int POS_2 = 2;
    private static final int POS_3 = 3;
    private final Map entries = new HashMap(HASH_SIZE);
    private final Map nameMap = new HashMap(HASH_SIZE);
    private static final class OffsetEntry {
        private long headerOffset = -1;
        private long dataOffset = -1;
    }
    private String encoding = null;
    private final ZipEncoding zipEncoding;
    private RandomAccessFile archive;
    private final boolean useUnicodeExtraFields;
    public ZipFile(File f) throws IOException {
        this(f, null);
    }
    public ZipFile(String name) throws IOException {
        this(new File(name), null);
    }
    public ZipFile(String name, String encoding) throws IOException {
        this(new File(name), encoding, true);
    }
    public ZipFile(File f, String encoding) throws IOException {
        this(f, encoding, true);
    }
    public ZipFile(File f, String encoding, boolean useUnicodeExtraFields)
        throws IOException {
        this.encoding = encoding;
        this.zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
        this.useUnicodeExtraFields = useUnicodeExtraFields;
        archive = new RandomAccessFile(f, "r");
        boolean success = false;
        try {
            Map entriesWithoutUTF8Flag = populateFromCentralDirectory();
            resolveLocalFileHeaderData(entriesWithoutUTF8Flag);
            success = true;
        } finally {
            if (!success) {
                try {
                    archive.close();
                } catch (IOException e2) {
                }
            }
        }
    }
    public String getEncoding() {
        return encoding;
    }
    public void close() throws IOException {
        archive.close();
    }
    public static void closeQuietly(ZipFile zipfile) {
        if (zipfile != null) {
            try {
                zipfile.close();
            } catch (IOException e) {
            }
        }
    }
    public Enumeration getEntries() {
        return Collections.enumeration(entries.keySet());
    }
    public ZipEntry getEntry(String name) {
        return (ZipEntry) nameMap.get(name);
    }
    public InputStream getInputStream(ZipEntry ze)
        throws IOException, ZipException {
        OffsetEntry offsetEntry = (OffsetEntry) entries.get(ze);
        if (offsetEntry == null) {
            return null;
        }
        long start = offsetEntry.dataOffset;
        BoundedInputStream bis =
            new BoundedInputStream(start, ze.getCompressedSize());
        switch (ze.getMethod()) {
            case ZipEntry.STORED:
                return bis;
            case ZipEntry.DEFLATED:
                bis.addDummy();
                return new InflaterInputStream(bis, new Inflater(true));
            default:
                throw new ZipException("Found unsupported compression method "
                                       + ze.getMethod());
        }
    }
    private static final int CFH_LEN =
         SHORT
         + SHORT
         + SHORT
         + SHORT
         + SHORT
         + SHORT
         + WORD
         + WORD
         + WORD
         + SHORT
         + SHORT
         + SHORT
         + SHORT
         + SHORT
         + WORD
         + WORD;
    private Map populateFromCentralDirectory()
        throws IOException {
        HashMap noUTF8Flag = new HashMap();
        positionAtCentralDirectory();
        byte[] cfh = new byte[CFH_LEN];
        byte[] signatureBytes = new byte[WORD];
        archive.readFully(signatureBytes);
        long sig = ZipLong.getValue(signatureBytes);
        final long cfhSig = ZipLong.getValue(ZipOutputStream.CFH_SIG);
        if (sig != cfhSig && startsWithLocalFileHeader()) {
            throw new IOException("central directory is empty, can't expand"
                                  + " corrupt archive.");
        }
        while (sig == cfhSig) {
            archive.readFully(cfh);
            int off = 0;
            ZipEntry ze = new ZipEntry();
            int versionMadeBy = ZipShort.getValue(cfh, off);
            off += SHORT;
            ze.setPlatform((versionMadeBy >> BYTE_SHIFT) & NIBLET_MASK);
            off += SHORT; 
            final int generalPurposeFlag = ZipShort.getValue(cfh, off);
            final boolean hasUTF8Flag = 
                (generalPurposeFlag & ZipOutputStream.UFT8_NAMES_FLAG) != 0;
            final ZipEncoding entryEncoding =
                hasUTF8Flag ? ZipEncodingHelper.UTF8_ZIP_ENCODING : zipEncoding;
            off += SHORT;
            ze.setMethod(ZipShort.getValue(cfh, off));
            off += SHORT;
            long time = dosToJavaTime(ZipLong.getValue(cfh, off));
            ze.setTime(time);
            off += WORD;
            ze.setCrc(ZipLong.getValue(cfh, off));
            off += WORD;
            ze.setCompressedSize(ZipLong.getValue(cfh, off));
            off += WORD;
            ze.setSize(ZipLong.getValue(cfh, off));
            off += WORD;
            int fileNameLen = ZipShort.getValue(cfh, off);
            off += SHORT;
            int extraLen = ZipShort.getValue(cfh, off);
            off += SHORT;
            int commentLen = ZipShort.getValue(cfh, off);
            off += SHORT;
            off += SHORT; 
            ze.setInternalAttributes(ZipShort.getValue(cfh, off));
            off += SHORT;
            ze.setExternalAttributes(ZipLong.getValue(cfh, off));
            off += WORD;
            byte[] fileName = new byte[fileNameLen];
            archive.readFully(fileName);
            ze.setName(entryEncoding.decode(fileName));
            OffsetEntry offset = new OffsetEntry();
            offset.headerOffset = ZipLong.getValue(cfh, off);
            entries.put(ze, offset);
            nameMap.put(ze.getName(), ze);
            byte[] cdExtraData = new byte[extraLen];
            archive.readFully(cdExtraData);
            ze.setCentralDirectoryExtra(cdExtraData);
            byte[] comment = new byte[commentLen];
            archive.readFully(comment);
            ze.setComment(entryEncoding.decode(comment));
            archive.readFully(signatureBytes);
            sig = ZipLong.getValue(signatureBytes);
            if (!hasUTF8Flag && useUnicodeExtraFields) {
                noUTF8Flag.put(ze, new NameAndComment(fileName, comment));
            }
        }
        return noUTF8Flag;
    }
    private static final int MIN_EOCD_SIZE =
         WORD
         + SHORT
         + SHORT
         + SHORT
         + SHORT
         + WORD
         + WORD
         + SHORT;
    private static final int MAX_EOCD_SIZE = MIN_EOCD_SIZE
         + 0xFFFF;
    private static final int CFD_LOCATOR_OFFSET =
         WORD
         + SHORT
         + SHORT
         + SHORT
         + SHORT
         + WORD;
    private void positionAtCentralDirectory()
        throws IOException {
        boolean found = false;
        long off = archive.length() - MIN_EOCD_SIZE;
        long stopSearching = Math.max(0L, archive.length() - MAX_EOCD_SIZE);
        if (off >= 0) {
            archive.seek(off);
            byte[] sig = ZipOutputStream.EOCD_SIG;
            int curr = archive.read();
            while (off >= stopSearching && curr != -1) {
                if (curr == sig[POS_0]) {
                    curr = archive.read();
                    if (curr == sig[POS_1]) {
                        curr = archive.read();
                        if (curr == sig[POS_2]) {
                            curr = archive.read();
                            if (curr == sig[POS_3]) {
                                found = true;
                                break;
                            }
                        }
                    }
                }
                archive.seek(--off);
                curr = archive.read();
            }
        }
        if (!found) {
            throw new ZipException("archive is not a ZIP archive");
        }
        archive.seek(off + CFD_LOCATOR_OFFSET);
        byte[] cfdOffset = new byte[WORD];
        archive.readFully(cfdOffset);
        archive.seek(ZipLong.getValue(cfdOffset));
    }
    private static final long LFH_OFFSET_FOR_FILENAME_LENGTH =
         WORD
         + SHORT
         + SHORT
         + SHORT
         + SHORT
         + SHORT
         + WORD
         + WORD
         + WORD;
    private void resolveLocalFileHeaderData(Map entriesWithoutUTF8Flag)
        throws IOException {
        Enumeration e = getEntries();
        while (e.hasMoreElements()) {
            ZipEntry ze = (ZipEntry) e.nextElement();
            OffsetEntry offsetEntry = (OffsetEntry) entries.get(ze);
            long offset = offsetEntry.headerOffset;
            archive.seek(offset + LFH_OFFSET_FOR_FILENAME_LENGTH);
            byte[] b = new byte[SHORT];
            archive.readFully(b);
            int fileNameLen = ZipShort.getValue(b);
            archive.readFully(b);
            int extraFieldLen = ZipShort.getValue(b);
            int lenToSkip = fileNameLen;
            while (lenToSkip > 0) {
                int skipped = archive.skipBytes(lenToSkip);
                if (skipped <= 0) {
                    throw new RuntimeException("failed to skip file name in"
                                               + " local file header");
                }
                lenToSkip -= skipped;
            }            
            byte[] localExtraData = new byte[extraFieldLen];
            archive.readFully(localExtraData);
            ze.setExtra(localExtraData);
            offsetEntry.dataOffset = offset + LFH_OFFSET_FOR_FILENAME_LENGTH
                + SHORT + SHORT + fileNameLen + extraFieldLen;
            if (entriesWithoutUTF8Flag.containsKey(ze)) {
                setNameAndCommentFromExtraFields(ze,
                                                 (NameAndComment)
                                                 entriesWithoutUTF8Flag.get(ze));
            }
        }
    }
    protected static Date fromDosTime(ZipLong zipDosTime) {
        long dosTime = zipDosTime.getValue();
        return new Date(dosToJavaTime(dosTime));
    }
    private static long dosToJavaTime(long dosTime) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, (int) ((dosTime >> 25) & 0x7f) + 1980);
        cal.set(Calendar.MONTH, (int) ((dosTime >> 21) & 0x0f) - 1);
        cal.set(Calendar.DATE, (int) (dosTime >> 16) & 0x1f);
        cal.set(Calendar.HOUR_OF_DAY, (int) (dosTime >> 11) & 0x1f);
        cal.set(Calendar.MINUTE, (int) (dosTime >> 5) & 0x3f);
        cal.set(Calendar.SECOND, (int) (dosTime << 1) & 0x3e);
        return cal.getTime().getTime();
    }
    protected String getString(byte[] bytes) throws ZipException {
        try {
            return ZipEncodingHelper.getZipEncoding(encoding).decode(bytes);
        } catch (IOException ex) {
            throw new ZipException("Failed to decode name: " + ex.getMessage());
        }
    }
    private boolean startsWithLocalFileHeader() throws IOException {
        archive.seek(0);
        final byte[] start = new byte[WORD];
        archive.readFully(start);
        for (int i = 0; i < start.length; i++) {
            if (start[i] != ZipOutputStream.LFH_SIG[i]) {
                return false;
            }
        }
        return true;
    }
    private void setNameAndCommentFromExtraFields(ZipEntry ze,
                                                  NameAndComment nc) {
        UnicodePathExtraField name = (UnicodePathExtraField)
            ze.getExtraField(UnicodePathExtraField.UPATH_ID);
        String originalName = ze.getName();
        String newName = getUnicodeStringIfOriginalMatches(name, nc.name);
        if (newName != null && !originalName.equals(newName)) {
            ze.setName(newName);
            nameMap.remove(originalName);
            nameMap.put(newName, ze);
        }
        if (nc.comment != null && nc.comment.length > 0) {
            UnicodeCommentExtraField cmt = (UnicodeCommentExtraField)
                ze.getExtraField(UnicodeCommentExtraField.UCOM_ID);
            String newComment =
                getUnicodeStringIfOriginalMatches(cmt, nc.comment);
            if (newComment != null) {
                ze.setComment(newComment);
            }
        }
    }
    private String getUnicodeStringIfOriginalMatches(AbstractUnicodeExtraField f,
                                                     byte[] orig) {
        if (f != null) {
            CRC32 crc32 = new CRC32();
            crc32.update(orig);
            long origCRC32 = crc32.getValue();
            if (origCRC32 == f.getNameCRC32()) {
                try {
                    return ZipEncodingHelper
                        .UTF8_ZIP_ENCODING.decode(f.getUnicodeName());
                } catch (IOException ex) {
                    return null;
                }
            }
        }
        return null;
    }
    private class BoundedInputStream extends InputStream {
        private long remaining;
        private long loc;
        private boolean addDummyByte = false;
        BoundedInputStream(long start, long remaining) {
            this.remaining = remaining;
            loc = start;
        }
        public int read() throws IOException {
            if (remaining-- <= 0) {
                if (addDummyByte) {
                    addDummyByte = false;
                    return 0;
                }
                return -1;
            }
            synchronized (archive) {
                archive.seek(loc++);
                return archive.read();
            }
        }
        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) {
                if (addDummyByte) {
                    addDummyByte = false;
                    b[off] = 0;
                    return 1;
                }
                return -1;
            }
            if (len <= 0) {
                return 0;
            }
            if (len > remaining) {
                len = (int) remaining;
            }
            int ret = -1;
            synchronized (archive) {
                archive.seek(loc);
                ret = archive.read(b, off, len);
            }
            if (ret > 0) {
                loc += ret;
                remaining -= ret;
            }
            return ret;
        }
        void addDummy() {
            addDummyByte = true;
        }
    }
    private static final class NameAndComment {
        private final byte[] name;
        private final byte[] comment;
        private NameAndComment(byte[] name, byte[] comment) {
            this.name = name;
            this.comment = comment;
        }
    }
}
