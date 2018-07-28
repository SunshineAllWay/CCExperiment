package org.apache.cassandra.db;
import java.io.IOException;
public class UnserializableColumnFamilyException extends IOException
{
    public final int cfId;
    public UnserializableColumnFamilyException(String msg, int cfId)
    {
        super(msg);
        this.cfId = cfId;
    }
}
