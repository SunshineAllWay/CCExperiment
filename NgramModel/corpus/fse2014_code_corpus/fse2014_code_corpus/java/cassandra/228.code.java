package org.apache.cassandra.dht;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.text.Collator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.cassandra.utils.FBUtilities;
public class CollatingOrderPreservingPartitioner extends AbstractByteOrderedPartitioner
{
    static final Collator collator = Collator.getInstance(new Locale("en", "US"));
    public BytesToken getToken(ByteBuffer key)
    {
        if (key.remaining() == 0)
            return MINIMUM;
        String skey;
        try
        {
            skey = FBUtilities.decodeToUTF8(key);
        }
        catch (CharacterCodingException e)
        {
            throw new RuntimeException("The provided key was not UTF8 encoded.", e);
        }
        return new BytesToken(ByteBuffer.wrap(collator.getCollationKey(skey).toByteArray()));
    }
    public Map<Token, Float> describeOwnership(List<Token> sortedTokens){ throw new UnsupportedOperationException(); }
}
