package org.apache.cassandra.db.marshal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import org.apache.thrift.TBaseHelper;
public final class IntegerType extends AbstractType
{
    public static final IntegerType instance = new IntegerType();
    private static int findMostSignificantByte(ByteBuffer bytes)
    {
        int len = bytes.remaining() - 1;
        int i = 0;
        for (; i < len; i++)
        {
            byte b0 = bytes.get(bytes.position() + i);
            if (b0 != 0 && b0 != -1)
                break;
            byte b1 = bytes.get(bytes.position() + i + 1);
            if (b0 == 0 && b1 != 0)
            {
                if (b1 > 0)
                    i++;
                break;
            }
            if (b0 == -1 && b1 != -1)
            {
                if (b1 < 0)
                    i++;
                break;
            }
        }
        return i;
    }
    IntegerType() {}
    public int compare(ByteBuffer lhs, ByteBuffer rhs)
    {
        int lhsLen = lhs.remaining();
        int rhsLen = rhs.remaining();
        if (lhsLen == 0)
            return rhsLen == 0 ? 0 : -1;
        if (rhsLen == 0)
            return 1;
        int lhsMsbIdx = findMostSignificantByte(lhs);
        int rhsMsbIdx = findMostSignificantByte(rhs);
        int lhsLenDiff = lhsLen - lhsMsbIdx;
        int rhsLenDiff = rhsLen - rhsMsbIdx;
        byte lhsMsb = lhs.get(lhs.position() + lhsMsbIdx);
        byte rhsMsb = rhs.get(rhs.position() + rhsMsbIdx);
        if (lhsLenDiff != rhsLenDiff)
        {
            if (lhsMsb < 0)
                return rhsMsb < 0 ? rhsLenDiff - lhsLenDiff : -1;
            if (rhsMsb < 0)
                return 1;
            return lhsLenDiff - rhsLenDiff;
        }
        if (lhsMsb != rhsMsb)
            return lhsMsb - rhsMsb;
        lhsMsbIdx++;
        rhsMsbIdx++;
        while (lhsMsbIdx < lhsLen)
        {
            lhsMsb = lhs.get(lhs.position() + lhsMsbIdx++);
            rhsMsb = rhs.get(rhs.position() + rhsMsbIdx++);
            if (lhsMsb != rhsMsb)
                return (lhsMsb & 0xFF) - (rhsMsb & 0xFF);
        }
        return 0;
    }
    @Override
    public String getString(ByteBuffer bytes)
    {
        if (bytes == null)
            return "null";
        if (bytes.remaining() == 0)
            return "empty";
        return new java.math.BigInteger(TBaseHelper.byteBufferToByteArray(bytes)).toString(10);
    }
    public ByteBuffer fromString(String source)
    {
        BigInteger integerType;
        try
        {
            integerType = new BigInteger(source);
        }
        catch (Exception e)
        {
            throw new RuntimeException("'" + source + "' could not be translated into an IntegerType.");
        }
        return ByteBuffer.wrap(integerType.toByteArray());
    }
    public void validate(ByteBuffer bytes) throws MarshalException
    {
    }
}
