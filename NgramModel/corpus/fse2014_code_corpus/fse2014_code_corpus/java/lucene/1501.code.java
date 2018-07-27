package org.apache.lucene.index;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.IndexInput;
import java.util.LinkedList;
import java.util.HashSet;
import java.io.IOException;
final class CompoundFileWriter {
    private static final class FileEntry {
        String file;
        long directoryOffset;
        long dataOffset;
    }
    private Directory directory;
    private String fileName;
    private HashSet<String> ids;
    private LinkedList<FileEntry> entries;
    private boolean merged = false;
    private SegmentMerger.CheckAbort checkAbort;
    public CompoundFileWriter(Directory dir, String name) {
      this(dir, name, null);
    }
    CompoundFileWriter(Directory dir, String name, SegmentMerger.CheckAbort checkAbort) {
        if (dir == null)
            throw new NullPointerException("directory cannot be null");
        if (name == null)
            throw new NullPointerException("name cannot be null");
        this.checkAbort = checkAbort;
        directory = dir;
        fileName = name;
        ids = new HashSet<String>();
        entries = new LinkedList<FileEntry>();
    }
    public Directory getDirectory() {
        return directory;
    }
    public String getName() {
        return fileName;
    }
    public void addFile(String file) {
        if (merged)
            throw new IllegalStateException(
                "Can't add extensions after merge has been called");
        if (file == null)
            throw new NullPointerException(
                "file cannot be null");
        if (! ids.add(file))
            throw new IllegalArgumentException(
                "File " + file + " already added");
        FileEntry entry = new FileEntry();
        entry.file = file;
        entries.add(entry);
    }
    public void close() throws IOException {
        if (merged)
            throw new IllegalStateException(
                "Merge already performed");
        if (entries.isEmpty())
            throw new IllegalStateException(
                "No entries to merge have been defined");
        merged = true;
        IndexOutput os = null;
        try {
            os = directory.createOutput(fileName);
            os.writeVInt(entries.size());
            long totalSize = 0;
            for (FileEntry fe : entries) {
                fe.directoryOffset = os.getFilePointer();
                os.writeLong(0);    
                os.writeString(fe.file);
                totalSize += directory.fileLength(fe.file);
            }
            final long finalLength = totalSize+os.getFilePointer();
            os.setLength(finalLength);
            byte buffer[] = new byte[16384];
            for (FileEntry fe : entries) {
                fe.dataOffset = os.getFilePointer();
                copyFile(fe, os, buffer);
            }
            for (FileEntry fe : entries) {
                os.seek(fe.directoryOffset);
                os.writeLong(fe.dataOffset);
            }
            assert finalLength == os.length();
            IndexOutput tmp = os;
            os = null;
            tmp.close();
        } finally {
            if (os != null) try { os.close(); } catch (IOException e) { }
        }
    }
    private void copyFile(FileEntry source, IndexOutput os, byte buffer[])
    throws IOException
    {
        IndexInput is = null;
        try {
            long startPtr = os.getFilePointer();
            is = directory.openInput(source.file);
            long length = is.length();
            long remainder = length;
            int chunk = buffer.length;
            while(remainder > 0) {
                int len = (int) Math.min(chunk, remainder);
                is.readBytes(buffer, 0, len, false);
                os.writeBytes(buffer, len);
                remainder -= len;
                if (checkAbort != null)
                  checkAbort.work(80);
            }
            if (remainder != 0)
                throw new IOException(
                    "Non-zero remainder length after copying: " + remainder
                    + " (id: " + source.file + ", length: " + length
                    + ", buffer size: " + chunk + ")");
            long endPtr = os.getFilePointer();
            long diff = endPtr - startPtr;
            if (diff != length)
                throw new IOException(
                    "Difference in the output file offsets " + diff
                    + " does not match the original file length " + length);
        } finally {
            if (is != null) is.close();
        }
    }
}
