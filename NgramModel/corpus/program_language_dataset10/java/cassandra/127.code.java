package org.apache.cassandra.db;
import org.apache.cassandra.thrift.InvalidRequestException;
public class ColumnFamilyNotDefinedException extends InvalidRequestException
{
    public ColumnFamilyNotDefinedException(String message)
    {
        super(message);
    }
}
