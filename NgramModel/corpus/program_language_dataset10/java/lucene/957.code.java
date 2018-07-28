package org.apache.lucene.store.db;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.IndexInput;
import com.sleepycat.db.internal.Db;
import com.sleepycat.db.internal.DbConstants;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.internal.Dbc;
import com.sleepycat.db.internal.DbTxn;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.Database;
import com.sleepycat.db.Transaction;
import com.sleepycat.db.DbHandleExtractor;
public class DbDirectory extends Directory {
    protected Set<DbIndexOutput> openFiles = Collections.synchronizedSet(new HashSet<DbIndexOutput>());
    protected Db files, blocks;
    protected DbTxn txn;
    protected int flags;
    public DbDirectory(DbTxn txn, Db files, Db blocks, int flags)
    {
        super();
        this.txn = txn;
        this.files = files;
        this.blocks = blocks;
        this.flags = flags;
    }
    public DbDirectory(Transaction txn, Database files, Database blocks,
                       int flags)
    {
        super();
        this.txn = txn != null ? DbHandleExtractor.getDbTxn(txn) : null;
        this.files = DbHandleExtractor.getDb(files);
        this.blocks = DbHandleExtractor.getDb(blocks);
        this.flags = flags;
    }
    public DbDirectory(Transaction txn, Database files, Database blocks)
    {
        this(txn, files, blocks, 0);
    }
    @Override
    public void close()
        throws IOException
    {
        flush();
    }
    public void flush()
        throws IOException
    {
        Iterator<DbIndexOutput> iterator = openFiles.iterator();
        while (iterator.hasNext())
            iterator.next().flush();
    }
    @Override
    public IndexOutput createOutput(String name)
        throws IOException
    {
        return new DbIndexOutput(this, name, true);
    }
    @Override
    public void deleteFile(String name)
        throws IOException
    {
        new File(name).delete(this);
    }
    @Override
    public boolean fileExists(String name)
        throws IOException
    {
        return new File(name).exists(this);
    }
    @Override
    public long fileLength(String name)
        throws IOException
    {
        File file = new File(name);
        if (file.exists(this))
            return file.getLength();
        throw new IOException("File does not exist: " + name);
    }
    @Override
    public long fileModified(String name)
        throws IOException
    {
        File file = new File(name);
        if (file.exists(this))
            return file.getTimeModified();
        throw new IOException("File does not exist: " + name);
    }
    @Override
    public String[] listAll()
        throws IOException
    {
        Dbc cursor = null;
        List<String> list = new ArrayList<String>();
        try {
            try {
                DatabaseEntry key = new DatabaseEntry(new byte[0]);
                DatabaseEntry data = new DatabaseEntry((byte[]) null);
                data.setPartial(true);
                cursor = files.cursor(txn, flags);
                if (cursor.get(key, data,
                               DbConstants.DB_SET_RANGE | flags) != DbConstants.DB_NOTFOUND)
                {
                    ByteArrayInputStream buffer =
                        new ByteArrayInputStream(key.getData());
                    DataInputStream in = new DataInputStream(buffer);
                    String name = in.readUTF();
                    in.close();
                    list.add(name);
                    while (cursor.get(key, data,
                                      DbConstants.DB_NEXT | flags) != DbConstants.DB_NOTFOUND) {
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
    public IndexInput openInput(String name)
        throws IOException
    {
        return new DbIndexInput(this, name);
    }
    @Override
    public Lock makeLock(String name)
    {
        return new DbLock();
    }
    @Override
    public void touchFile(String name)
        throws IOException
    {
        File file = new File(name);
        long length = 0L;
        if (file.exists(this))
            length = file.getLength();
        file.modify(this, length, System.currentTimeMillis());
    }
    public void setTransaction(Transaction txn)
    {
        setTransaction(txn != null ? DbHandleExtractor.getDbTxn(txn) : null);
    }
    public void setTransaction(DbTxn txn)
    {
        this.txn = txn;
    }
}
