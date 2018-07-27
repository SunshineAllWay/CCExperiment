package org.apache.cassandra.dht;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import org.apache.cassandra.io.ICompactSerializer2;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.ByteBufferUtil;
public abstract class Token<T> implements Comparable<Token<T>>, Serializable
{
    private static final long serialVersionUID = 1L;
    private static final TokenSerializer serializer = new TokenSerializer();
    public static TokenSerializer serializer()
    {
        return serializer;
    }
    public final T token;
    protected Token(T token)
    {
        this.token = token;
    }
    abstract public int compareTo(Token<T> o);
    public String toString()
    {
        return token.toString();
    }
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Token)) {
            return false;
        }
        return token.equals(((Token)obj).token);
    }
    public int hashCode()
    {
        return token.hashCode();
    }
    public static abstract class TokenFactory<T>
    {
        public abstract ByteBuffer toByteArray(Token<T> token);
        public abstract Token<T> fromByteArray(ByteBuffer bytes);
        public abstract String toString(Token<T> token); 
        public abstract Token<T> fromString(String string); 
    }
    public static class TokenSerializer implements ICompactSerializer2<Token>
    {
        public void serialize(Token token, DataOutput dos) throws IOException
        {
            IPartitioner p = StorageService.getPartitioner();
            ByteBuffer b = p.getTokenFactory().toByteArray(token);
            ByteBufferUtil.writeWithLength(b, dos);
        }
        public Token deserialize(DataInput dis) throws IOException
        {
            IPartitioner p = StorageService.getPartitioner();
            int size = dis.readInt();
            byte[] bytes = new byte[size];
            dis.readFully(bytes);
            return p.getTokenFactory().fromByteArray(ByteBuffer.wrap(bytes));
        }
    }
}
