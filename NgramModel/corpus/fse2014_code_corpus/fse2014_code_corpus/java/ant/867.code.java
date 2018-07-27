package org.apache.tools.zip;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;
public class ExtraFieldUtils {
    private static final int WORD = 4;
    private static final Map implementations;
    static {
        implementations = new HashMap();
        register(AsiExtraField.class);
        register(JarMarker.class);
        register(UnicodePathExtraField.class);
        register(UnicodeCommentExtraField.class);
    }
    public static void register(Class c) {
        try {
            ZipExtraField ze = (ZipExtraField) c.newInstance();
            implementations.put(ze.getHeaderId(), c);
        } catch (ClassCastException cc) {
            throw new RuntimeException(c + " doesn\'t implement ZipExtraField");
        } catch (InstantiationException ie) {
            throw new RuntimeException(c + " is not a concrete class");
        } catch (IllegalAccessException ie) {
            throw new RuntimeException(c + "\'s no-arg constructor is not public");
        }
    }
    public static ZipExtraField createExtraField(ZipShort headerId)
        throws InstantiationException, IllegalAccessException {
        Class c = (Class) implementations.get(headerId);
        if (c != null) {
            return (ZipExtraField) c.newInstance();
        }
        UnrecognizedExtraField u = new UnrecognizedExtraField();
        u.setHeaderId(headerId);
        return u;
    }
    public static ZipExtraField[] parse(byte[] data) throws ZipException {
        return parse(data, true, UnparseableExtraField.THROW);
    }
    public static ZipExtraField[] parse(byte[] data, boolean local)
        throws ZipException {
        return parse(data, local, UnparseableExtraField.THROW);
    }
    public static ZipExtraField[] parse(byte[] data, boolean local,
                                        UnparseableExtraField onUnparseableData)
        throws ZipException {
        List v = new ArrayList();
        int start = 0;
        LOOP:
        while (start <= data.length - WORD) {
            ZipShort headerId = new ZipShort(data, start);
            int length = (new ZipShort(data, start + 2)).getValue();
            if (start + WORD + length > data.length) {
                switch(onUnparseableData.getKey()) {
                case UnparseableExtraField.THROW_KEY:
                    throw new ZipException("bad extra field starting at "
                                           + start + ".  Block length of "
                                           + length + " bytes exceeds remaining"
                                           + " data of "
                                           + (data.length - start - WORD)
                                           + " bytes.");
                case UnparseableExtraField.READ_KEY:
                    UnparseableExtraFieldData field =
                        new UnparseableExtraFieldData();
                    if (local) {
                        field.parseFromLocalFileData(data, start,
                                                     data.length - start);
                    } else {
                        field.parseFromCentralDirectoryData(data, start,
                                                            data.length - start);
                    }
                    v.add(field);
                case UnparseableExtraField.SKIP_KEY:
                    break LOOP;
                default:
                    throw new ZipException("unknown UnparseableExtraField key: "
                                           + onUnparseableData.getKey());
                }
            }
            try {
                ZipExtraField ze = createExtraField(headerId);
                if (local
                    || !(ze instanceof CentralDirectoryParsingZipExtraField)) {
                    ze.parseFromLocalFileData(data, start + WORD, length);
                } else {
                    ((CentralDirectoryParsingZipExtraField) ze)
                        .parseFromCentralDirectoryData(data, start + WORD,
                                                       length);
                }
                v.add(ze);
            } catch (InstantiationException ie) {
                throw new ZipException(ie.getMessage());
            } catch (IllegalAccessException iae) {
                throw new ZipException(iae.getMessage());
            }
            start += (length + WORD);
        }
        ZipExtraField[] result = new ZipExtraField[v.size()];
        return (ZipExtraField[]) v.toArray(result);
    }
    public static byte[] mergeLocalFileDataData(ZipExtraField[] data) {
        final boolean lastIsUnparseableHolder = data.length > 0
            && data[data.length - 1] instanceof UnparseableExtraFieldData;
        int regularExtraFieldCount =
            lastIsUnparseableHolder ? data.length - 1 : data.length;
        int sum = WORD * regularExtraFieldCount;
        for (int i = 0; i < data.length; i++) {
            sum += data[i].getLocalFileDataLength().getValue();
        }
        byte[] result = new byte[sum];
        int start = 0;
        for (int i = 0; i < regularExtraFieldCount; i++) {
            System.arraycopy(data[i].getHeaderId().getBytes(),
                             0, result, start, 2);
            System.arraycopy(data[i].getLocalFileDataLength().getBytes(),
                             0, result, start + 2, 2);
            byte[] local = data[i].getLocalFileDataData();
            System.arraycopy(local, 0, result, start + WORD, local.length);
            start += (local.length + WORD);
        }
        if (lastIsUnparseableHolder) {
            byte[] local = data[data.length - 1].getLocalFileDataData();
            System.arraycopy(local, 0, result, start, local.length);
        }
        return result;
    }
    public static byte[] mergeCentralDirectoryData(ZipExtraField[] data) {
        final boolean lastIsUnparseableHolder = data.length > 0
            && data[data.length - 1] instanceof UnparseableExtraFieldData;
        int regularExtraFieldCount =
            lastIsUnparseableHolder ? data.length - 1 : data.length;
        int sum = WORD * regularExtraFieldCount;
        for (int i = 0; i < data.length; i++) {
            sum += data[i].getCentralDirectoryLength().getValue();
        }
        byte[] result = new byte[sum];
        int start = 0;
        for (int i = 0; i < regularExtraFieldCount; i++) {
            System.arraycopy(data[i].getHeaderId().getBytes(),
                             0, result, start, 2);
            System.arraycopy(data[i].getCentralDirectoryLength().getBytes(),
                             0, result, start + 2, 2);
            byte[] local = data[i].getCentralDirectoryData();
            System.arraycopy(local, 0, result, start + WORD, local.length);
            start += (local.length + WORD);
        }
        if (lastIsUnparseableHolder) {
            byte[] local = data[data.length - 1].getCentralDirectoryData();
            System.arraycopy(local, 0, result, start, local.length);
        }
        return result;
    }
    public static final class UnparseableExtraField {
        public static final int THROW_KEY = 0;
        public static final int SKIP_KEY = 1;
        public static final int READ_KEY = 2;
        public static final UnparseableExtraField THROW
            = new UnparseableExtraField(THROW_KEY);
        public static final UnparseableExtraField SKIP
            = new UnparseableExtraField(SKIP_KEY);
        public static final UnparseableExtraField READ
            = new UnparseableExtraField(READ_KEY);
        private final int key;
        private UnparseableExtraField(int k) {
            key = k;
        }
        public int getKey() { return key; }
    }
}
