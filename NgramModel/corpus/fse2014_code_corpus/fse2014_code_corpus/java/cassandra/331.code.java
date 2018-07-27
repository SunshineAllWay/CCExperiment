package org.apache.cassandra.net;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import org.apache.cassandra.concurrent.Stage;
import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
public class Message
{
    private static MessageSerializer serializer_;
    static
    {
        serializer_ = new MessageSerializer();        
    }
    public static MessageSerializer serializer()
    {
        return serializer_;
    }
    final Header header_;
    private final byte[] body_;
    Message(Header header, byte[] body)
    {
        assert header != null;
        assert body != null;
        header_ = header;
        body_ = body;
    }
    public Message(InetAddress from, StorageService.Verb verb, byte[] body)
    {
        this(new Header(from, verb), body);
    }    
    public byte[] getHeader(Object key)
    {
        return header_.getDetail(key);
    }
    public void setHeader(String key, byte[] value)
    {
        header_.setDetail(key, value);
    }
    public byte[] getMessageBody()
    {
        return body_;
    }
    public InetAddress getFrom()
    {
        return header_.getFrom();
    }
    public Stage getMessageType()
    {
        return StorageService.verbStages.get(getVerb());
    }
    public StorageService.Verb getVerb()
    {
        return header_.getVerb();
    }
    public String getMessageId()
    {
        return header_.getMessageId();
    }
    void setMessageId(String id)
    {
        header_.setMessageId(id);
    }    
    public Message getReply(InetAddress from, byte[] args)
    {
        Header header = new Header(getMessageId(), from, StorageService.Verb.REQUEST_RESPONSE);
        return new Message(header, args);
    }
    public Message getInternalReply(byte[] body)
    {
        Header header = new Header(getMessageId(), FBUtilities.getLocalAddress(), StorageService.Verb.INTERNAL_RESPONSE);
        return new Message(header, body);
    }
    public String toString()
    {
        StringBuilder sbuf = new StringBuilder("");
        String separator = System.getProperty("line.separator");
        sbuf.append("ID:" + getMessageId())
        	.append(separator)
        	.append("FROM:" + getFrom())
        	.append(separator)
        	.append("TYPE:" + getMessageType())
        	.append(separator)
        	.append("VERB:" + getVerb())
        	.append(separator);
        return sbuf.toString();
    }
}
class MessageSerializer implements ICompactSerializer<Message>
{
    public void serialize(Message t, DataOutputStream dos) throws IOException
    {
        Header.serializer().serialize( t.header_, dos);
        byte[] bytes = t.getMessageBody();
        dos.writeInt(bytes.length);
        dos.write(bytes);
    }
    public Message deserialize(DataInputStream dis) throws IOException
    {
        Header header = Header.serializer().deserialize(dis);
        int size = dis.readInt();
        byte[] bytes = new byte[size];
        dis.readFully(bytes);
        return new Message(header, bytes);
    }
}
