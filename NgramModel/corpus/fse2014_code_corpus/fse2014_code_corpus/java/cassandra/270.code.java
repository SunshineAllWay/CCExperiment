package org.apache.cassandra.io;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
public interface ICompactSerializer2<T>
{
    public void serialize(T t, DataOutput dos) throws IOException;
    public T deserialize(DataInput dis) throws IOException;    
}
