package org.apache.lucene.store.je;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
public class JEDirectory extends Directory {
    protected Set<JEIndexOutput> openFiles = Collections.synchronizedSet(new HashSet<JEIndexOutput>());
    protected Database files, blocks;
    protected Transaction txn;
    protected int flags;
    public JEDirectory(Transaction txn, Database files, Database blocks,
                       int flags) {
        super();
        this.txn = txn;
        this.files = files;
        this.blocks = blocks;
        this.flags = flags;
    }
    public JEDirectory(Transaction txn, Database files, Database blocks) {
        this(txn, files, blocks, 0);
    }
    @Override
    public void close() throws IOException {
        flush();
    }
    public void flush() throws IOException {
        Iterator<JEIndexOutput> iterator = openFiles.iterator();
        while (iterator.hasNext()) {
            System.out
                    .println(iterator.next().file.getName());
        }
    }
    @Override
    public IndexOutput createOutput(String name) throws IOException {
        return new JEIndexOutput(this, name, true);
    }
    @Override
    public void deleteFile(String name) throws IOException {
        new File(name).delete(this);
    }
    @Override
    public boolean fileExists(String name) throws IOException {
        return new File(name).exists(this);
    }
    @Override
    public long fileLength(String name) throws IOException {
        File file = new File(name);
        if (file.exists(this))
            return file.getLength();
        throw new IOException("File does not exist: " + name);
    }
    @Override
    public long fileModified(String name) throws IOException {
        File file = new File(name);
        if (file.exists(this))
            return file.getTimeModified();
        throw new IOException("File does not exist: " + name);
    }
    @Override
    public String[] listAll() throws IOException {
        Cursor cursor = null;
        List<String> list = new ArrayList<String>();
        try {
            try {
                DatabaseEntry key = new DatabaseEntry(new byte[0]);
                DatabaseEntry data = new DatabaseEntry(null);
                data.setPartial(true);
                cursor = files.openCursor(txn, null);
                if (cursor.getNext(key, data, null) != OperationStatus.NOTFOUND) {
                    ByteArrayInputStream buffer = new ByteArrayInputStream(key
                            .getData());
                    DataInputStream in = new DataInputStream(buffer);
                    String name = in.readUTF();
                    in.close();
                    list.add(name);
                    while (cursor.getNext(key, data, null) != OperationStatus.NOTFOUND) {
                        buffer = new ByteArrayInputStream(key.getData());
                        in = new DataInputStream(buffer);
                        name = in.readUTF();
                        in.close();
                        list.add(name);
                    }
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        } catch (DatabaseException e) {
            throw new IOException(e.getMessage());
        }
        return list.toArray(new String[list.size()]);
    }
    @Override
    public IndexInput openInput(String name) throws IOException {
        return new JEIndexInput(this, name);
    }
    @Override
    public Lock makeLock(String name) {
        return new JELock();
    }
    @Override
    public void touchFile(String name) throws IOException {
        File file = new File(name);
        long length = 0L;
        if (file.exists(this))
            length = file.getLength();
        file.modify(this, length, System.currentTimeMillis());
    }
    public void setTransaction(Transaction txn) {
        this.txn = txn;
    }
}
