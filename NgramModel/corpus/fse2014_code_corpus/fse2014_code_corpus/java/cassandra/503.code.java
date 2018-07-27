package org.apache.cassandra.io.sstable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import org.junit.Test;
import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.io.util.BufferedRandomAccessFile;
import org.apache.cassandra.utils.ByteBufferUtil;
public class SSTableTest extends CleanupHelper
{
    @Test
    public void testSingleWrite() throws IOException {
        ByteBuffer key = ByteBuffer.wrap(Integer.toString(1).getBytes());
        ByteBuffer bytes = ByteBuffer.wrap(new byte[1024]);
        new Random().nextBytes(bytes.array());
        Map<ByteBuffer, ByteBuffer> map = new HashMap<ByteBuffer,ByteBuffer>();
        map.put(key, bytes);
        SSTableReader ssTable = SSTableUtils.prepare().cf("Standard1").writeRaw(map);
        verifySingle(ssTable, bytes, key);
        ssTable = SSTableReader.open(ssTable.descriptor); 
        verifySingle(ssTable, bytes, key);
    }
    private void verifySingle(SSTableReader sstable, ByteBuffer bytes, ByteBuffer key) throws IOException
    {
        BufferedRandomAccessFile file = new BufferedRandomAccessFile(sstable.getFilename(), "r");
        file.seek(sstable.getPosition(sstable.partitioner.decorateKey(key), SSTableReader.Operator.EQ));
        assert key.equals(ByteBufferUtil.readWithShortLength(file));
        int size = (int)SSTableReader.readRowSize(file, sstable.descriptor);
        byte[] bytes2 = new byte[size];
        file.readFully(bytes2);
        assert ByteBuffer.wrap(bytes2).equals(bytes);
    }
    @Test
    public void testManyWrites() throws IOException {
        Map<ByteBuffer, ByteBuffer> map = new HashMap<ByteBuffer,ByteBuffer>();
        for (int i = 100; i < 1000; ++i)
        {
            map.put(ByteBuffer.wrap(Integer.toString(i).getBytes()), ByteBuffer.wrap(("Avinash Lakshman is a good man: " + i).getBytes()));
        }
        SSTableReader ssTable = SSTableUtils.prepare().cf("Standard2").writeRaw(map);
        verifyMany(ssTable, map);
        ssTable = SSTableReader.open(ssTable.descriptor); 
        verifyMany(ssTable, map);
    }
    private void verifyMany(SSTableReader sstable, Map<ByteBuffer, ByteBuffer> map) throws IOException
    {
        List<ByteBuffer> keys = new ArrayList<ByteBuffer>(map.keySet());
        Collections.shuffle(keys);
        BufferedRandomAccessFile file = new BufferedRandomAccessFile(sstable.getFilename(), "r");
        for (ByteBuffer key : keys)
        {
            file.seek(sstable.getPosition(sstable.partitioner.decorateKey(key), SSTableReader.Operator.EQ));
            assert key.equals( ByteBufferUtil.readWithShortLength(file));
            int size = (int)SSTableReader.readRowSize(file, sstable.descriptor);
            byte[] bytes2 = new byte[size];
            file.readFully(bytes2);
            assert Arrays.equals(bytes2, map.get(key).array());
        }
    }
}
