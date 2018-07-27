package org.apache.cassandra.db.commitlog;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.junit.Test;
import org.apache.cassandra.SchemaLoader;
public class CommitLogHeaderTest extends SchemaLoader
{
    @Test
    public void testEmptyHeader()
    {
        CommitLogHeader clh = new CommitLogHeader();
        assert clh.getReplayPosition() < 0;
    }
    @Test
    public void lowestPositionWithZero()
    {
        CommitLogHeader clh = new CommitLogHeader();
        clh.turnOn(2, 34);
        assert clh.getReplayPosition() == 34;
        clh.turnOn(100, 0);
        assert clh.getReplayPosition() == 0;
        clh.turnOn(65, 2);
        assert clh.getReplayPosition() == 0;
    }
}
