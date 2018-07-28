package org.apache.cassandra.db.marshal;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.Arrays;
import com.google.common.base.Charsets;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
public class UTF8Type extends BytesType
{
    public static final UTF8Type instance = new UTF8Type();
    UTF8Type() {} 
    public String getString(ByteBuffer bytes)
    {
        try
        {
            return FBUtilities.decodeToUTF8(bytes);
        }
        catch (CharacterCodingException e)
        {
            throw new MarshalException("invalid UTF8 bytes " + ByteBufferUtil.string(bytes));
        }
    }
    public ByteBuffer fromString(String source)
    {
        return ByteBuffer.wrap(source.getBytes(Charsets.UTF_8));
    }
    public void validate(ByteBuffer bytes) throws MarshalException
    {
        if (!UTF8Validator.validate(bytes.slice()))
            throw new MarshalException("String didn't validate.");
    }
    static class UTF8Validator
    {
        enum State {
            START,
            TWO,
            TWO_80,
            THREE_a0bf,
            THREE_80bf_1,
            THREE_80bf_2,
            FOUR_90bf,
            FOUR_80bf_3,
        };    
        static boolean validate(ByteBuffer buf) 
        {
            int b = 0;
            State state = State.START;
            while (buf.remaining() > 0)
            {
                b = buf.get();
                switch (state)
                {
                    case START:
                        if (b >= 0)
                        {
                            if (b > 127)
                                return false;
                        }
                        else if ((b >> 5) == -2)
                        {
                            if (b == (byte) 0xc0)
                                state = State.TWO_80;
                            else if ((b & 0x1e) == 0)
                                return false;
                            state = State.TWO;
                        }
                        else if ((b >> 4) == -2)
                        {
                            if (b == (byte)0xe0)
                                state = State.THREE_a0bf;
                            else
                                state = State.THREE_80bf_2;
                            break;            
                        }
                        else if ((b >> 3) == -2)
                        {
                            if (b == (byte)0xf0)
                                state = State.FOUR_90bf;
                            else if (b == (byte)0xf4)
                                state = State.FOUR_80bf_3;
                            else
                                state = State.FOUR_80bf_3;
                            break;
                        }
                        else
                            return false; 
                        break;
                    case TWO:
                        if ((b & 0xc0) != 0x80)
                            return false;
                        state = State.START;
                        break;
                    case TWO_80:
                        if (b != (byte)0x80)
                            return false;
                        state = State.START;
                        break;
                    case THREE_a0bf:
                        if ((b & 0xe0) == 0x80)
                            return false;
                        state = State.THREE_80bf_1;
                        break;
                    case THREE_80bf_1:
                        if ((b & 0xc0) != 0x80)
                            return false;
                        state = State.START;
                        break;
                    case THREE_80bf_2:
                        if ((b & 0xc0) != 0x80)
                            return false;
                        state = State.THREE_80bf_1;
                        break;
                    case FOUR_90bf:
                        if ((b & 0x30) == 0)
                            return false;
                        state = State.THREE_80bf_2;
                        break;
                    case FOUR_80bf_3:
                        if ((b & 0xc0) != 0x80)
                            return false;
                        state = State.THREE_80bf_2;
                        break;
                    default:
                        return false; 
                }
            }
            return state == State.START;
        }
    }
}
