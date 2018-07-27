package org.apache.lucene.store.je;
import org.apache.lucene.store.Lock;
public class JELock extends Lock {
    boolean isLocked = false;
    public JELock()
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
