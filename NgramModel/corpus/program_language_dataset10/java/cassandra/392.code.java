package org.apache.cassandra.thrift;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.thrift.TBinaryProtocol;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TTransportFactory;
public class CassandraDaemon extends org.apache.cassandra.service.AbstractCassandraDaemon
{
    private static Logger logger = LoggerFactory.getLogger(CassandraDaemon.class);
    private TServer serverEngine;
    protected void setup() throws IOException
    {
        super.setup();
        final CassandraServer cassandraServer = new CassandraServer();
        Cassandra.Processor processor = new Cassandra.Processor(cassandraServer);
        TServerSocket tServerSocket = null;
        try
        {
            tServerSocket = new TCustomServerSocket(new InetSocketAddress(listenAddr, listenPort),
                                                    DatabaseDescriptor.getRpcKeepAlive(),
                                                    DatabaseDescriptor.getRpcSendBufferSize(),
                                                    DatabaseDescriptor.getRpcRecvBufferSize());
        }
        catch (TTransportException e)
        {
            throw new IOException(String.format("Unable to create thrift socket to %s:%s",
                                                listenAddr, listenPort), e);
        }
        logger.info(String.format("Binding thrift service to %s:%s", listenAddr, listenPort));
        TProtocolFactory tProtocolFactory = new TBinaryProtocol.Factory(true, 
                                                                        true, 
                                                                        DatabaseDescriptor.getThriftMaxMessageLength());
        TTransportFactory inTransportFactory, outTransportFactory;
        if (DatabaseDescriptor.isThriftFramed())
        {
            int tFramedTransportSize = DatabaseDescriptor.getThriftFramedTransportSize();
            inTransportFactory  = new TFastFramedTransport.Factory(64 * 1024, tFramedTransportSize);
            outTransportFactory = new TFastFramedTransport.Factory(64 * 1024, tFramedTransportSize);
            logger.info("Using TFastFramedTransport with a max frame size of {} bytes.", tFramedTransportSize);
        }
        else
        {
            inTransportFactory = new TTransportFactory();
            outTransportFactory = new TTransportFactory();
        }
        CustomTThreadPoolServer.Options options = new CustomTThreadPoolServer.Options();
        options.minWorkerThreads = MIN_WORKER_THREADS;
        ExecutorService executorService = new CleaningThreadPool(cassandraServer.clientState,
                                                                 options.minWorkerThreads,
                                                                 options.maxWorkerThreads);
        serverEngine = new CustomTThreadPoolServer(new TProcessorFactory(processor),
                                             tServerSocket,
                                             inTransportFactory,
                                             outTransportFactory,
                                             tProtocolFactory,
                                             tProtocolFactory,
                                             options,
                                             executorService);
    }
    public void start()
    {
        logger.info("Listening for thrift clients...");
        serverEngine.serve();
    }
    public void stop()
    {
        logger.info("Cassandra shutting down...");
        serverEngine.stop();
    }
    public static void main(String[] args)
    {
        new CassandraDaemon().activate();
    }
}
