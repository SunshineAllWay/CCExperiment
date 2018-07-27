package org.apache.batik.ext.awt.image.spi;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
public abstract class MagicNumberRegistryEntry
    extends AbstractRegistryEntry
    implements StreamRegistryEntry {
    public static final float PRIORITY = 1000;
    public static class MagicNumber {
        int offset;
        byte [] magicNumber;
        byte [] buffer;
        public MagicNumber(int offset, byte[]magicNumber) {
            this.offset = offset;
            this.magicNumber = magicNumber.clone();
            buffer = new byte[magicNumber.length];
        }
        int getReadlimit() {
            return offset+magicNumber.length;
        }
        boolean isMatch(InputStream is)
            throws StreamCorruptedException {
            int idx = 0;
            is.mark(getReadlimit());
            try {
                while (idx < offset) {
                    int rn = (int)is.skip(offset-idx);
                    if (rn == -1) {
                        return false;
                    }
                    idx += rn;
                }
                idx = 0;
                while (idx < buffer.length) {
                    int rn = is.read(buffer, idx, buffer.length-idx);
                    if (rn == -1) {
                        return false;
                    }
                    idx += rn;
                }
                for (int i=0; i<magicNumber.length; i++) {
                    if (magicNumber[i] != buffer[i]) {
                        return false;
                    }
                }
            } catch (IOException ioe) {
                return false;
            } finally {
                try {
                    is.reset();
                } catch (IOException ioe) {
                    throw new StreamCorruptedException(ioe.getMessage());
                }
            }
            return true;
        }
    }
    MagicNumber [] magicNumbers;
    public MagicNumberRegistryEntry(String name,
                                    float priority,
                                    String ext,
                                    String mimeType,
                                    int offset, byte[]magicNumber) {
        super(name, priority, ext, mimeType);
        magicNumbers    = new MagicNumber[1];
        magicNumbers[0] = new MagicNumber(offset, magicNumber);
    }
    public MagicNumberRegistryEntry(String name,
                                    String ext,
                                    String mimeType,
                                    int offset, byte[] magicNumber) {
        this(name, PRIORITY, ext, mimeType, offset, magicNumber);
    }
    public MagicNumberRegistryEntry(String name,
                                    float priority,
                                    String ext,
                                    String mimeType,
                                    MagicNumber[] magicNumbers) {
        super(name, priority, ext, mimeType);
        this.magicNumbers = magicNumbers;
    }
    public MagicNumberRegistryEntry(String name,
                                    String ext,
                                    String mimeType,
                                    MagicNumber[] magicNumbers) {
        this(name, PRIORITY, ext, mimeType, magicNumbers);
    }
    public MagicNumberRegistryEntry(String    name,
                                    float     priority,
                                    String [] exts,
                                    String [] mimeTypes,
                                    int offset, byte[]magicNumber) {
        super(name, priority, exts, mimeTypes);
        magicNumbers    = new MagicNumber[1];
        magicNumbers[0] = new MagicNumber(offset, magicNumber);
    }
    public MagicNumberRegistryEntry(String    name,
                                    String [] exts,
                                    String [] mimeTypes,
                                    int offset, byte[] magicNumbers) {
        this(name, PRIORITY, exts, mimeTypes, offset, magicNumbers);
    }
    public MagicNumberRegistryEntry(String    name,
                                    float     priority,
                                    String [] exts,
                                    String [] mimeTypes,
                                    MagicNumber [] magicNumbers) {
        super(name, priority, exts, mimeTypes);
        this.magicNumbers = magicNumbers;
    }
    public MagicNumberRegistryEntry(String    name,
                                    String [] exts,
                                    String [] mimeTypes,
                                    MagicNumber [] magicNumbers) {
        this(name, PRIORITY, exts, mimeTypes, magicNumbers);
    }
    public MagicNumberRegistryEntry(String         name,
                                    String []      exts,
                                    String []      mimeTypes,
                                    MagicNumber [] magicNumbers,
                                    float          priority) {
        super(name, priority, exts, mimeTypes);
        this.magicNumbers = magicNumbers;
    }
    public int getReadlimit() {
        int maxbuf = 0;
        for (int i=0; i<magicNumbers.length; i++) {
            int req = magicNumbers[i].getReadlimit();
            if (req > maxbuf) {
                maxbuf = req;
            }
        }
        return maxbuf;
    }
    public boolean isCompatibleStream(InputStream is)
        throws StreamCorruptedException {
        for (int i=0; i<magicNumbers.length; i++) {
            if (magicNumbers[i].isMatch(is)) {
                return true;
            }
        }
        return false;
    }
}
