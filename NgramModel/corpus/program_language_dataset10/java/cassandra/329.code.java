package org.apache.cassandra.net;
import java.io.*;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.EncryptionOptions;
import org.apache.cassandra.security.streaming.SSLIncomingStreamReader;
import org.apache.cassandra.streaming.IncomingStreamReader;
import org.apache.cassandra.streaming.StreamHeader;
public class IncomingTcpConnection extends Thread
{
    private static Logger logger = LoggerFactory.getLogger(IncomingTcpConnection.class);
    private Socket socket;
    public IncomingTcpConnection(Socket socket)
    {
        assert socket != null;
        this.socket = socket;
    }
    @Override
    public void run()
    {
        DataInputStream input;
        boolean isStream;
        try
        {
            input = new DataInputStream(socket.getInputStream());
            MessagingService.validateMagic(input.readInt());
            int header = input.readInt();
            isStream = MessagingService.getBits(header, 3, 1) == 1;
            if (!isStream)
                input = new DataInputStream(new BufferedInputStream(socket.getInputStream(), 4096));
        }
        catch (IOException e)
        {
            close();
            throw new IOError(e);
        }
        while (true)
        {
            try
            {
                if (isStream)
                {
                    int size = input.readInt();
                    byte[] headerBytes = new byte[size];
                    input.readFully(headerBytes);
                    stream(StreamHeader.serializer().deserialize(new DataInputStream(new ByteArrayInputStream(headerBytes))), input);
                    break;
                }
                else
                {
                    int size = input.readInt();
                    byte[] contentBytes = new byte[size];
                    input.readFully(contentBytes);
                    Message message = Message.serializer().deserialize(new DataInputStream(new ByteArrayInputStream(contentBytes)));
                    MessagingService.instance().receive(message);
                }
                MessagingService.validateMagic(input.readInt());
                int header = input.readInt();
                assert isStream == (MessagingService.getBits(header, 3, 1) == 1) : "Connections cannot change type: " + isStream;
            }
            catch (EOFException e)
            {
                if (logger.isTraceEnabled())
                    logger.trace("eof reading from socket; closing", e);
                break;
            }
            catch (IOException e) 
            {
                if (logger.isDebugEnabled())
                    logger.debug("error reading from socket; closing", e);
                break;
            }
        }
        close();
    }
    private void close()
    {
        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            if (logger.isDebugEnabled())
                logger.debug("error closing socket", e);
        }
    }
    private void stream(StreamHeader streamHeader, DataInputStream input) throws IOException
    {
        if (DatabaseDescriptor.getEncryptionOptions().internode_encryption == EncryptionOptions.InternodeEncryption.all)
            new SSLIncomingStreamReader(streamHeader, socket, input).read();
        else
            new IncomingStreamReader(streamHeader, socket).read();
    }
}
