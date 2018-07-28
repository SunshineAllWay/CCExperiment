package org.apache.cassandra.db;
import java.nio.ByteBuffer;
import java.io.IOException;
import org.apache.cassandra.net.Message;
public interface IMutation
{
    public String getTable();
    public ByteBuffer key();
    public String toString(boolean shallow);
}
