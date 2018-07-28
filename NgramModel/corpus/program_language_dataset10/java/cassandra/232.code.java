package org.apache.cassandra.dht;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.*;
import com.google.common.base.Charsets;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.Pair;
public class OrderPreservingPartitioner implements IPartitioner<StringToken>
{
    public static final StringToken MINIMUM = new StringToken("");
    public static final BigInteger CHAR_MASK = new BigInteger("65535");
    public DecoratedKey<StringToken> decorateKey(ByteBuffer key)
    {
        return new DecoratedKey<StringToken>(getToken(key), key);
    }
    public DecoratedKey<StringToken> convertFromDiskFormat(ByteBuffer key)
    {
        return new DecoratedKey<StringToken>(getToken(key), key);
    }
    public StringToken midpoint(Token ltoken, Token rtoken)
    {
        int sigchars = Math.max(((StringToken)ltoken).token.length(), ((StringToken)rtoken).token.length());
        BigInteger left = bigForString(((StringToken)ltoken).token, sigchars);
        BigInteger right = bigForString(((StringToken)rtoken).token, sigchars);
        Pair<BigInteger,Boolean> midpair = FBUtilities.midpoint(left, right, 16*sigchars);
        return new StringToken(stringForBig(midpair.left, sigchars, midpair.right));
    }
    private static BigInteger bigForString(String str, int sigchars)
    {
        assert str.length() <= sigchars;
        BigInteger big = BigInteger.ZERO;
        for (int i = 0; i < str.length(); i++)
        {
            int charpos = 16 * (sigchars - (i + 1));
            BigInteger charbig = BigInteger.valueOf(str.charAt(i) & 0xFFFF);
            big = big.or(charbig.shiftLeft(charpos));
        }
        return big;
    }
    private String stringForBig(BigInteger big, int sigchars, boolean remainder)
    {
        char[] chars = new char[sigchars + (remainder ? 1 : 0)];
        if (remainder)
            chars[sigchars] |= 0x8000;
        for (int i = 0; i < sigchars; i++)
        {
            int maskpos = 16 * (sigchars - (i + 1));
            chars[i] = (char)(big.and(CHAR_MASK.shiftLeft(maskpos)).shiftRight(maskpos).intValue() & 0xFFFF);
        }
        return new String(chars);
    }
    public StringToken getMinimumToken()
    {
        return MINIMUM;
    }
    public StringToken getRandomToken()
    {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random r = new Random();
        StringBuilder buffer = new StringBuilder();
        for (int j = 0; j < 16; j++) {
            buffer.append(chars.charAt(r.nextInt(chars.length())));
        }
        return new StringToken(buffer.toString());
    }
    private final Token.TokenFactory<String> tokenFactory = new Token.TokenFactory<String>()
    {
        public ByteBuffer toByteArray(Token<String> stringToken)
        {
            return ByteBuffer.wrap(stringToken.token.getBytes(Charsets.UTF_8));
        }
        public Token<String> fromByteArray(ByteBuffer bytes)
        {
            return new StringToken(ByteBufferUtil.string(bytes, Charsets.UTF_8));
        }
        public String toString(Token<String> stringToken)
        {
            return stringToken.token;
        }
        public Token<String> fromString(String string)
        {
            return new StringToken(string);
        }
    };
    public Token.TokenFactory<String> getTokenFactory()
    {
        return tokenFactory;
    }
    public boolean preservesOrder()
    {
        return true;
    }
    public StringToken getToken(ByteBuffer key)
    {
        String skey;
        try
        {
            skey = FBUtilities.decodeToUTF8(key);
        }
        catch (CharacterCodingException e)
        {
            throw new RuntimeException("The provided key was not UTF8 encoded.", e);
        }
        return new StringToken(skey);
    }
    public Map<Token, Float> describeOwnership(List<Token> sortedTokens)
    {
        Map<Token, Float> allTokens = new HashMap<Token, Float>();
        List<Range> sortedRanges = new ArrayList<Range>();
        Token lastToken = sortedTokens.get(sortedTokens.size() - 1);
        for (Token node : sortedTokens)
        {
            allTokens.put(node, new Float(0.0));
            sortedRanges.add(new Range(lastToken, node));
            lastToken = node;
        }
        for (String ks : DatabaseDescriptor.getTables())
        {
            for (CFMetaData cfmd : DatabaseDescriptor.getKSMetaData(ks).cfMetaData().values())
            {
                for (Range r : sortedRanges)
                {
                    allTokens.put(r.right, allTokens.get(r.right) + StorageService.instance.getSplits(ks, cfmd.cfName, r, 1).size());
                }
            }
        }
        Float total = new Float(0.0);
        for (Float f : allTokens.values())
            total += f;
        for (Map.Entry<Token, Float> row : allTokens.entrySet())
            allTokens.put(row.getKey(), row.getValue() / total);
        return allTokens;
    }
}
