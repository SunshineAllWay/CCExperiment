package org.apache.cassandra.dht;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.GuidGenerator;
import org.apache.cassandra.utils.Pair;
import static com.google.common.base.Charsets.UTF_8;
public class RandomPartitioner implements IPartitioner<BigIntegerToken>
{
    public static final BigInteger ZERO = new BigInteger("0");
    public static final BigIntegerToken MINIMUM = new BigIntegerToken("-1");
    private static final byte DELIMITER_BYTE = ":".getBytes()[0];
    public DecoratedKey<BigIntegerToken> decorateKey(ByteBuffer key)
    {
        return new DecoratedKey<BigIntegerToken>(getToken(key), key);
    }
    public DecoratedKey<BigIntegerToken> convertFromDiskFormat(ByteBuffer fromdisk)
    {
        int splitPoint = -1;
        for (int i = fromdisk.position(); i < fromdisk.limit(); i++)
        {
            if (fromdisk.get(i) == DELIMITER_BYTE)
            {
                splitPoint = i;
                break;
            }
        }
        assert splitPoint != -1;
        String token = ByteBufferUtil.string(fromdisk, fromdisk.position(), splitPoint - fromdisk.position(), UTF_8);
        ByteBuffer key = fromdisk.duplicate();
        key.position(splitPoint + 1);
        return new DecoratedKey<BigIntegerToken>(new BigIntegerToken(token), key);
    }
    public Token midpoint(Token ltoken, Token rtoken)
    {
        BigInteger left = ltoken.equals(MINIMUM) ? ZERO : ((BigIntegerToken)ltoken).token;
        BigInteger right = rtoken.equals(MINIMUM) ? ZERO : ((BigIntegerToken)rtoken).token;
        Pair<BigInteger,Boolean> midpair = FBUtilities.midpoint(left, right, 127);
        return new BigIntegerToken(midpair.left);
    }
	public BigIntegerToken getMinimumToken()
    {
        return MINIMUM;
    }
    public BigIntegerToken getRandomToken()
    {
        BigInteger token = FBUtilities.md5hash(GuidGenerator.guidAsBytes());
        if ( token.signum() == -1 )
            token = token.multiply(BigInteger.valueOf(-1L));
        return new BigIntegerToken(token);
    }
    private final Token.TokenFactory<BigInteger> tokenFactory = new Token.TokenFactory<BigInteger>() {
        public ByteBuffer toByteArray(Token<BigInteger> bigIntegerToken)
        {
            return ByteBuffer.wrap(bigIntegerToken.token.toByteArray());
        }
        public Token<BigInteger> fromByteArray(ByteBuffer bytes)
        {
            byte[] b = new byte[bytes.remaining()];
            bytes.get(b);
            bytes.rewind();
            return new BigIntegerToken(new BigInteger(b));
        }
        public String toString(Token<BigInteger> bigIntegerToken)
        {
            return bigIntegerToken.token.toString();
        }
        public Token<BigInteger> fromString(String string)
        {
            return new BigIntegerToken(new BigInteger(string));
        }
    };
    public Token.TokenFactory<BigInteger> getTokenFactory()
    {
        return tokenFactory;
    }
    public boolean preservesOrder()
    {
        return false;
    }
    public BigIntegerToken getToken(ByteBuffer key)
    {
        if (key.remaining() == 0)
            return MINIMUM;
        return new BigIntegerToken(FBUtilities.md5hash(key));
    }
    public Map<Token, Float> describeOwnership(List<Token> sortedTokens)
    {
        Map<Token, Float> ownerships = new HashMap<Token, Float>();
        Iterator i = sortedTokens.iterator();
        if (!i.hasNext()) { throw new RuntimeException("No nodes present in the cluster. How did you call this?"); }
        if (sortedTokens.size() == 1) {
            ownerships.put((Token)i.next(), new Float(1.0));
        }
        else {
            final BigInteger ri = new BigInteger("2").pow(127);                             
            final BigDecimal r  = new BigDecimal(ri);                                       
            Token start = (Token)i.next(); BigInteger ti = ((BigIntegerToken)start).token;  
            Token t; BigInteger tim1 = ti;                                                  
            while (i.hasNext()) {
                t = (Token)i.next(); ti = ((BigIntegerToken)t).token;                       
                float x = new BigDecimal(ti.subtract(tim1)).divide(r).floatValue();         
                ownerships.put(t, x);                                                       
                tim1 = ti;                                                                  
            }
            ownerships.put(start, new BigDecimal(((BigIntegerToken)start).token.subtract(ti).add(ri).mod(ri)).divide(r).floatValue());
        }
        return ownerships;
    }
}
