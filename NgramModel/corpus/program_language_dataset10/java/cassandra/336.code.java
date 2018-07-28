package org.apache.cassandra.net;
import java.net.InetAddress;
import org.apache.cassandra.concurrent.Stage;
class OutboundTcpConnectionPool
{
    public final OutboundTcpConnection cmdCon;
    public final OutboundTcpConnection ackCon;
    OutboundTcpConnectionPool(InetAddress remoteEp)
    {
        cmdCon = new OutboundTcpConnection(remoteEp);
        cmdCon.start();
        ackCon = new OutboundTcpConnection(remoteEp);
        ackCon.start();
    }
    OutboundTcpConnection getConnection(Message msg)
    {
        Stage stage = msg.getMessageType();
        return stage == Stage.REQUEST_RESPONSE || stage == Stage.INTERNAL_RESPONSE || stage == Stage.GOSSIP
               ? ackCon
               : cmdCon;
    }
    synchronized void reset()
    {
        for (OutboundTcpConnection con : new OutboundTcpConnection[] { cmdCon, ackCon })
            con.closeSocket();
    }
}
