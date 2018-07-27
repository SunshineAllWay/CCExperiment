package org.apache.lucene.store.db;
import org.apache.lucene.store.Lock;
public class DbLock extends Lock {
    boolean isLocked = false;
    public DbLock()
    {
    }
    @Override
    public boolean obtain()
    {
        return (isLocked = true);
    }
    @Override
    public void release()
    {
        isLocked = false;
    }
    @Override
    public boolean isLocked()
    {
        return isLocked;
    }
}
