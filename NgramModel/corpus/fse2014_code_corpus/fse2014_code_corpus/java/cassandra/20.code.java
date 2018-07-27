package org.apache.cassandra.cql.driver;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Compression;
import org.apache.cassandra.thrift.CqlResult;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Connection
{
    public static Compression defaultCompression = Compression.GZIP;
    public final String hostName;
    public final int portNo;
    private static final Logger logger = LoggerFactory.getLogger(Connection.class);
    protected long timeOfLastFailure = 0;
    protected int numFailures = 0;
    private Cassandra.Client client;
    private TTransport transport;
    public Connection(String hostName, int portNo) throws TTransportException
    {
        this.hostName = hostName;
        this.portNo = portNo;
        TSocket socket = new TSocket(hostName, portNo);
        transport = new TFramedTransport(socket);
        TProtocol protocol = new TBinaryProtocol(transport);
        client = new Cassandra.Client(protocol);
        socket.open();
        logger.info("Connected to {}:{}", hostName, portNo);
    }
    public CqlResult execute(String queryStr)
    throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
        return execute(queryStr, defaultCompression);
    }
    public CqlResult execute(String queryStr, Compression compress)
    throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
        try
        {
            return client.execute_cql_query(Utils.compressQuery(queryStr, compress), compress);
        }
        catch (TException error)
        {
            numFailures++;
            timeOfLastFailure = System.currentTimeMillis();
            throw error;
        }
    }
    public void close()
    {
        transport.close();
    }
    public boolean isOpen()
    {
        return transport.isOpen();
    }
}
