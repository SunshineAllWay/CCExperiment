package org.apache.cassandra.dht;
import java.math.BigInteger;
public class BigIntegerToken extends Token<BigInteger>
{
    public BigIntegerToken(BigInteger token)
    {
        super(token);
    }
    public BigIntegerToken(String token) {
        this(new BigInteger(token));
    }
    @Override
    public int compareTo(Token<BigInteger> o)
    {
        return token.compareTo(o.token);
    }
}
