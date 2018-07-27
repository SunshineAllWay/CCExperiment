package org.apache.tools.zip;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.ZipException;
public class ZipEntry extends java.util.zip.ZipEntry implements Cloneable {
    public static final int PLATFORM_UNIX = 3;
    public static final int PLATFORM_FAT  = 0;
    private static final int SHORT_MASK = 0xFFFF;
    private static final int SHORT_SHIFT = 16;
    private int internalAttributes = 0;
    private int platform = PLATFORM_FAT;
    private long externalAttributes = 0;
    private LinkedHashMap extraFields = null;
    private UnparseableExtraFieldData unparseableExtra = null;
    private String name = null;
    public ZipEntry(String name) {
        super(name);
    }
    public ZipEntry(java.util.zip.ZipEntry entry) throws ZipException {
        super(entry);
        byte[] extra = entry.getExtra();
        if (extra != null) {
            setExtraFields(ExtraFieldUtils.parse(extra, true,
                                                 ExtraFieldUtils
                                                 .UnparseableExtraField.READ));
        } else {
            setExtra();
        }
    }
    public ZipEntry(ZipEntry entry) throws ZipException {
        this((java.util.zip.ZipEntry) entry);
        setInternalAttributes(entry.getInternalAttributes());
        setExternalAttributes(entry.getExternalAttributes());
        setExtraFields(entry.getExtraFields(true));
    }
    protected ZipEntry() {
        super("");
    }
    public Object clone() {
        ZipEntry e = (ZipEntry) super.clone();
        e.setInternalAttributes(getInternalAttributes());
        e.setExternalAttributes(getExternalAttributes());
        e.setExtraFields(getExtraFields(true));
        return e;
    }
    public int getInternalAttributes() {
        return internalAttributes;
    }
    public void setInternalAttributes(int value) {
        internalAttributes = value;
    }
    public long getExternalAttributes() {
        return externalAttributes;
    }
    public void setExternalAttributes(long value) {
        externalAttributes = value;
    }
    public void setUnixMode(int mode) {
        setExternalAttributes((mode << SHORT_SHIFT)
                              | ((mode & 0200) == 0 ? 1 : 0)
                              | (isDirectory() ? 0x10 : 0));
        platform = PLATFORM_UNIX;
    }
    public int getUnixMode() {
        return platform != PLATFORM_UNIX ? 0 :
            (int) ((getExternalAttributes() >> SHORT_SHIFT) & SHORT_MASK);
    }
    public int getPlatform() {
        return platform;
    }
    protected void setPlatform(int platform) {
        this.platform = platform;
    }
    public void setExtraFields(ZipExtraField[] fields) {
        extraFields = new LinkedHashMap();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] instanceof UnparseableExtraFieldData) {
                unparseableExtra = (UnparseableExtraFieldData) fields[i];
            } else {
                extraFields.put(fields[i].getHeaderId(), fields[i]);
            }
        }
        setExtra();
    }
    public ZipExtraField[] getExtraFields() {
        return getExtraFields(false);
    }
    public ZipExtraField[] getExtraFields(boolean includeUnparseable) {
        if (extraFields == null) {
            return !includeUnparseable || unparseableExtra == null
                ? new ZipExtraField[0]
                : new ZipExtraField[] { unparseableExtra };
        }
        List result = new ArrayList(extraFields.values());
        if (includeUnparseable && unparseableExtra != null) {
            result.add(unparseableExtra);
        }
        return (ZipExtraField[]) result.toArray(new ZipExtraField[0]);
    }
    public void addExtraField(ZipExtraField ze) {
        if (ze instanceof UnparseableExtraFieldData) {
            unparseableExtra = (UnparseableExtraFieldData) ze;
        } else {
            if (extraFields == null) {
                extraFields = new LinkedHashMap();
            }
            extraFields.put(ze.getHeaderId(), ze);
        }
        setExtra();
    }
    public void addAsFirstExtraField(ZipExtraField ze) {
        if (ze instanceof UnparseableExtraFieldData) {
            unparseableExtra = (UnparseableExtraFieldData) ze;
        } else {
            LinkedHashMap copy = extraFields;
            extraFields = new LinkedHashMap();
            extraFields.put(ze.getHeaderId(), ze);
            if (copy != null) {
                copy.remove(ze.getHeaderId());
                extraFields.putAll(copy);
            }
        }
        setExtra();
    }
    public void removeExtraField(ZipShort type) {
        if (extraFields == null) {
            throw new java.util.NoSuchElementException();
        }
        if (extraFields.remove(type) == null) {
            throw new java.util.NoSuchElementException();
        }
        setExtra();
    }
    public void removeUnparseableExtraFieldData() {
        if (unparseableExtra == null) {
            throw new java.util.NoSuchElementException();
        }
        unparseableExtra = null;
        setExtra();
    }
    public ZipExtraField getExtraField(ZipShort type) {
        if (extraFields != null) {
            return (ZipExtraField) extraFields.get(type);
        }
        return null;
    }
    public UnparseableExtraFieldData getUnparseableExtraFieldData() {
        return unparseableExtra;
    }
    public void setExtra(byte[] extra) throws RuntimeException {
        try {
            ZipExtraField[] local =
                ExtraFieldUtils.parse(extra, true,
                                      ExtraFieldUtils.UnparseableExtraField.READ);
            mergeExtraFields(local, true);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing extra fields for entry: "
                                       + getName() + " - " + e.getMessage(), e);
        }
    }
    protected void setExtra() {
        super.setExtra(ExtraFieldUtils.mergeLocalFileDataData(getExtraFields(true)));
    }
    public void setCentralDirectoryExtra(byte[] b) {
        try {
            ZipExtraField[] central =
                ExtraFieldUtils.parse(b, false,
                                      ExtraFieldUtils.UnparseableExtraField.READ);
            mergeExtraFields(central, false);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    public byte[] getLocalFileDataExtra() {
        byte[] extra = getExtra();
        return extra != null ? extra : new byte[0];
    }
    public byte[] getCentralDirectoryExtra() {
        return ExtraFieldUtils.mergeCentralDirectoryData(getExtraFields(true));
    }
    public void setComprSize(long size) {
        setCompressedSize(size);
    }
    public String getName() {
        return name == null ? super.getName() : name;
    }
    public boolean isDirectory() {
        return getName().endsWith("/");
    }
    protected void setName(String name) {
        this.name = name;
    }
    public int hashCode() {
        return getName().hashCode();
    }
    public boolean equals(Object o) {
        return (this == o);
    }
    private void mergeExtraFields(ZipExtraField[] f, boolean local)
        throws ZipException {
        if (extraFields == null) {
            setExtraFields(f);
        } else {
            for (int i = 0; i < f.length; i++) {
                ZipExtraField existing;
                if (f[i] instanceof UnparseableExtraFieldData) {
                    existing = unparseableExtra;
                } else {
                    existing = getExtraField(f[i].getHeaderId());
                }
                if (existing == null) {
                    addExtraField(f[i]);
                } else {
                    if (local
                        || !(existing
                             instanceof CentralDirectoryParsingZipExtraField)) {
                        byte[] b = f[i].getLocalFileDataData();
                        existing.parseFromLocalFileData(b, 0, b.length);
                    } else {
                        byte[] b = f[i].getCentralDirectoryData();
                        ((CentralDirectoryParsingZipExtraField) existing)
                            .parseFromCentralDirectoryData(b, 0, b.length);
                    }
                }
            }
            setExtra();
        }
    }
}
