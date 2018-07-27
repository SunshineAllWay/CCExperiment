package org.apache.lucene.store;
import java.io.IOException;
import org.apache.lucene.util.LuceneTestCase;
public class TestLock extends LuceneTestCase {
    public void testObtain() {
        LockMock lock = new LockMock();
        Lock.LOCK_POLL_INTERVAL = 10;
        try {
            lock.obtain(Lock.LOCK_POLL_INTERVAL);
            fail("Should have failed to obtain lock");
        } catch (IOException e) {
            assertEquals("should attempt to lock more than once", lock.lockAttempts, 2);
        }
    }
    private class LockMock extends Lock {
        public int lockAttempts;
        @Override
        public boolean obtain() {
            lockAttempts++;
            return false;
        }
        @Override
        public void release() {
        }
        @Override
        public boolean isLocked() {
            return false;
        }
    }
}
