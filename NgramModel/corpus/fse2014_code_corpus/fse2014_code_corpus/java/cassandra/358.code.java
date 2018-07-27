package org.apache.cassandra.service;
import java.io.IOException;
import org.apache.cassandra.thrift.CassandraDaemon;
import org.apache.thrift.transport.TTransportException;
public class EmbeddedCassandraService implements Runnable
{
    CassandraDaemon cassandraDaemon;
    public void init() throws TTransportException, IOException
    {
        cassandraDaemon = new CassandraDaemon();
        cassandraDaemon.init(null);
    }
    public void run()
    {
        cassandraDaemon.start();
    }
}
