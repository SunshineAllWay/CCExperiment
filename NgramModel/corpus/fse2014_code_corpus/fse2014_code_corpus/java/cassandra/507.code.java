package org.apache.cassandra.io.util;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
public class BufferedRandomAccessFileTest
{
    @Test
    public void testLength() throws IOException
    {
        File tmpFile = File.createTempFile("lengthtest", "bin");
        BufferedRandomAccessFile rw = new BufferedRandomAccessFile(tmpFile, "rw");
        assertEquals(0, rw.length());
        byte[] lessThenBuffer = new byte[BufferedRandomAccessFile.DEFAULT_BUFFER_SIZE / 2];
        rw.write(lessThenBuffer);
        assertEquals(lessThenBuffer.length, rw.length());
        rw.sync();
        assertEquals(lessThenBuffer.length, rw.length());
        byte[] biggerThenBuffer = new byte[BufferedRandomAccessFile.DEFAULT_BUFFER_SIZE * 2];
        rw.write(biggerThenBuffer);
        assertEquals(biggerThenBuffer.length + lessThenBuffer.length, rw.length());
        rw.seek(0);
        rw.read();
        assertEquals(biggerThenBuffer.length + lessThenBuffer.length, rw.length());
        rw.close();
        BufferedRandomAccessFile r = new BufferedRandomAccessFile(tmpFile, "r");
        assertEquals(lessThenBuffer.length + biggerThenBuffer.length, r.length());
        r.close();
    }
    @Test
    public void testReadsAndWriteOnCapacity() throws IOException
    {
        File tmpFile = File.createTempFile("readtest", "bin");
        BufferedRandomAccessFile rw = new BufferedRandomAccessFile(tmpFile, "rw");
        byte[] in = new byte[BufferedRandomAccessFile.DEFAULT_BUFFER_SIZE];
        rw.write(in);
        byte[] out = new byte[BufferedRandomAccessFile.DEFAULT_BUFFER_SIZE];
        rw.read(out);
        long rem = rw.bytesRemaining();
        assert rw.isEOF();
        assert rem == 0 : "BytesRemaining should be 0 but it's " + rem;
        int negone = rw.read();
        assert negone == -1 : "We read past the end of the file, should have gotten EOF -1. Instead, " + negone;
        rw.write(new byte[BufferedRandomAccessFile.DEFAULT_BUFFER_SIZE]);
        rw.write(42);
    }
    protected void expectException(int size, int offset, int len, BufferedRandomAccessFile braf)
    {
        boolean threw = false;
        try
        {
            braf.readFully(new byte[size], offset, len);
        }
        catch(Throwable t)
        {
            assert t.getClass().equals(EOFException.class) : t.getClass().getName() + " is not " + EOFException.class.getName();
            threw = true;
        }
        assert threw : EOFException.class.getName() + " not received";
    }
    @Test
    public void testEOF() throws Exception
    {
        for (String mode : Arrays.asList("r", "rw")) 
        {
            for (int buf : Arrays.asList(8, 16, 32, 0))  
            {
                for (int off : Arrays.asList(0, 8))
                {
                    expectException(32, off, 17, new BufferedRandomAccessFile(writeTemporaryFile(new byte[16]), mode, buf));
                }
            }
        }
    }
    protected File writeTemporaryFile(byte[] data) throws Exception
    {
        File f = File.createTempFile("BRAFTestFile", null);
        f.deleteOnExit();
        FileOutputStream fout = new FileOutputStream(f);
        fout.write(data);
        fout.getFD().sync();
        fout.close();
        return f;
    }
    @Test (expected=UnsupportedOperationException.class)
    public void testOverflowMark() throws IOException
    {
        File tmpFile = File.createTempFile("overflowtest", "bin");
        tmpFile.deleteOnExit();
        BufferedRandomAccessFile rw = new BufferedRandomAccessFile(tmpFile.getPath(), "rw");
        assert tmpFile.getPath().equals(rw.getPath());
        FileMark mark = rw.mark();
        rw.reset(mark);
        int bpm = rw.bytesPastMark(mark);
        rw.seek(4L*1024L*1024L*1024L*1024L);
        bpm = rw.bytesPastMark(mark);
    }
}
