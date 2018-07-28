package org.apache.cassandra.dht;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import org.apache.cassandra.db.DecoratedKey;
public interface IPartitioner<T extends Token>
{
    public DecoratedKey<T> convertFromDiskFormat(ByteBuffer key);
    public DecoratedKey<T> decorateKey(ByteBuffer key);
    public Token midpoint(Token left, Token right);
	public T getMinimumToken();
    public T getToken(ByteBuffer key);
    public T getRandomToken();
    public Token.TokenFactory getTokenFactory();
    public boolean preservesOrder();
    public Map<Token, Float> describeOwnership(List<Token> sortedTokens);
}
