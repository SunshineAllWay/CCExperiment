package org.apache.cassandra.db.marshal;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.apache.cassandra.utils.UUIDGen;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;
public class TimeUUIDTypeTest
{
    TimeUUIDType timeUUIDType = new TimeUUIDType();
    UUIDGenerator generator = UUIDGenerator.getInstance();
    @Test
    public void testEquality()
    {
        UUID a = generator.generateTimeBasedUUID();
        UUID b = new UUID(a.asByteArray());
        timeUUIDType.validate(ByteBuffer.wrap(a.asByteArray()));
        timeUUIDType.validate(ByteBuffer.wrap(b.asByteArray()));
        assertEquals(0, timeUUIDType.compare(ByteBuffer.wrap(a.asByteArray()), ByteBuffer.wrap(b.asByteArray())));
    }
    @Test
    public void testSmaller()
    {
        UUID a = generator.generateTimeBasedUUID();
        UUID b = generator.generateTimeBasedUUID();
        UUID c = generator.generateTimeBasedUUID();
        timeUUIDType.validate(ByteBuffer.wrap(a.asByteArray()));
        timeUUIDType.validate(ByteBuffer.wrap(b.asByteArray()));
        timeUUIDType.validate(ByteBuffer.wrap(c.asByteArray()));
        assert timeUUIDType.compare(ByteBuffer.wrap(a.asByteArray()), ByteBuffer.wrap(b.asByteArray())) < 0;
        assert timeUUIDType.compare(ByteBuffer.wrap(b.asByteArray()), ByteBuffer.wrap(c.asByteArray())) < 0;
        assert timeUUIDType.compare(ByteBuffer.wrap(a.asByteArray()), ByteBuffer.wrap(c.asByteArray())) < 0;
    }
    @Test
    public void testBigger()
    {
        UUID a = generator.generateTimeBasedUUID();
        UUID b = generator.generateTimeBasedUUID();
        UUID c = generator.generateTimeBasedUUID();
        timeUUIDType.validate(ByteBuffer.wrap(a.asByteArray()));
        timeUUIDType.validate(ByteBuffer.wrap(b.asByteArray()));
        timeUUIDType.validate(ByteBuffer.wrap(c.asByteArray()));
        assert timeUUIDType.compare(ByteBuffer.wrap(c.asByteArray()), ByteBuffer.wrap(b.asByteArray())) > 0;
        assert timeUUIDType.compare(ByteBuffer.wrap(b.asByteArray()), ByteBuffer.wrap(a.asByteArray())) > 0;
        assert timeUUIDType.compare(ByteBuffer.wrap(c.asByteArray()), ByteBuffer.wrap(a.asByteArray())) > 0;
    }
    @Test
    public void testTimestampComparison()
    {
        Random rng = new Random();
        ByteBuffer[] uuids = new ByteBuffer[100];
        for (int i = 0; i < uuids.length; i++)
        {
            uuids[i] = ByteBuffer.allocate(16);
            rng.nextBytes(uuids[i].array());
            uuids[i].array()[6] &= 0x0F;
            uuids[i].array()[6] |= 0x10;
        }
        Arrays.sort(uuids, timeUUIDType);
        for (int i = 1; i < uuids.length; i++)
        {
            long i0 = UUIDGen.getUUID(uuids[i - 1]).timestamp();
            long i1 = UUIDGen.getUUID(uuids[i]).timestamp();
            assert i0 <= i1;
        }
    }
    @Test
    public void testValidTimeVersion()
    {
        java.util.UUID uuid1 = java.util.UUID.fromString("00000000-0000-1000-0000-000000000000");
        assert uuid1.version() == 1;
        timeUUIDType.validate(ByteBuffer.wrap(UUIDGen.decompose(uuid1)));
    }
    @Test(expected = MarshalException.class)
    public void testInvalidTimeVersion()
    {
        java.util.UUID uuid2 = java.util.UUID.fromString("00000000-0000-2100-0000-000000000000");
        assert uuid2.version() == 2;
        timeUUIDType.validate(ByteBuffer.wrap(UUIDGen.decompose(uuid2)));
    }
}
