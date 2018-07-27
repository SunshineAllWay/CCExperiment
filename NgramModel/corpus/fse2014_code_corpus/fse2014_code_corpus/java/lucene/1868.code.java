package org.apache.lucene.index;
import java.io.IOException;
import java.io.File;
import org.apache.lucene.util.LuceneTestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.store._TestHelper;
import org.apache.lucene.util._TestUtil;
public class TestCompoundFile extends LuceneTestCase
{
    public static void main(String args[]) {
        TestRunner.run (new TestSuite(TestCompoundFile.class));
    }
    private Directory dir;
    @Override
    protected void setUp() throws Exception {
       super.setUp();
       File file = new File(TEMP_DIR, "testIndex");
       _TestUtil.rmDir(file);
       dir = new SimpleFSDirectory(file,null);
    }
    @Override
    protected void tearDown() throws Exception {
       dir.close();
       _TestUtil.rmDir(new File(TEMP_DIR, "testIndex"));
       super.tearDown();
    }
    private void createRandomFile(Directory dir, String name, int size)
    throws IOException
    {
        IndexOutput os = dir.createOutput(name);
        for (int i=0; i<size; i++) {
            byte b = (byte) (Math.random() * 256);
            os.writeByte(b);
        }
        os.close();
    }
    private void createSequenceFile(Directory dir,
                                    String name,
                                    byte start,
                                    int size)
    throws IOException
    {
        IndexOutput os = dir.createOutput(name);
        for (int i=0; i < size; i++) {
            os.writeByte(start);
            start ++;
        }
        os.close();
    }
    private void assertSameStreams(String msg,
                                   IndexInput expected,
                                   IndexInput test)
    throws IOException
    {
        assertNotNull(msg + " null expected", expected);
        assertNotNull(msg + " null test", test);
        assertEquals(msg + " length", expected.length(), test.length());
        assertEquals(msg + " position", expected.getFilePointer(),
                                        test.getFilePointer());
        byte expectedBuffer[] = new byte[512];
        byte testBuffer[] = new byte[expectedBuffer.length];
        long remainder = expected.length() - expected.getFilePointer();
        while(remainder > 0) {
            int readLen = (int) Math.min(remainder, expectedBuffer.length);
            expected.readBytes(expectedBuffer, 0, readLen);
            test.readBytes(testBuffer, 0, readLen);
            assertEqualArrays(msg + ", remainder " + remainder, expectedBuffer,
                testBuffer, 0, readLen);
            remainder -= readLen;
        }
    }
    private void assertSameStreams(String msg,
                                   IndexInput expected,
                                   IndexInput actual,
                                   long seekTo)
    throws IOException
    {
        if(seekTo >= 0 && seekTo < expected.length())
        {
            expected.seek(seekTo);
            actual.seek(seekTo);
            assertSameStreams(msg + ", seek(mid)", expected, actual);
        }
    }
    private void assertSameSeekBehavior(String msg,
                                        IndexInput expected,
                                        IndexInput actual)
    throws IOException
    {
        long point = 0;
        assertSameStreams(msg + ", seek(0)", expected, actual, point);
        point = expected.length() / 2l;
        assertSameStreams(msg + ", seek(mid)", expected, actual, point);
        point = expected.length() - 2;
        assertSameStreams(msg + ", seek(end-2)", expected, actual, point);
        point = expected.length() - 1;
        assertSameStreams(msg + ", seek(end-1)", expected, actual, point);
        point = expected.length();
        assertSameStreams(msg + ", seek(end)", expected, actual, point);
        point = expected.length() + 1;
        assertSameStreams(msg + ", seek(end+1)", expected, actual, point);
    }
    private void assertEqualArrays(String msg,
                                   byte[] expected,
                                   byte[] test,
                                   int start,
                                   int len)
    {
        assertNotNull(msg + " null expected", expected);
        assertNotNull(msg + " null test", test);
        for (int i=start; i<len; i++) {
            assertEquals(msg + " " + i, expected[i], test[i]);
        }
    }
    public void testSingleFile() throws IOException {
        int data[] = new int[] { 0, 1, 10, 100 };
        for (int i=0; i<data.length; i++) {
            String name = "t" + data[i];
            createSequenceFile(dir, name, (byte) 0, data[i]);
            CompoundFileWriter csw = new CompoundFileWriter(dir, name + ".cfs");
            csw.addFile(name);
            csw.close();
            CompoundFileReader csr = new CompoundFileReader(dir, name + ".cfs");
            IndexInput expected = dir.openInput(name);
            IndexInput actual = csr.openInput(name);
            assertSameStreams(name, expected, actual);
            assertSameSeekBehavior(name, expected, actual);
            expected.close();
            actual.close();
            csr.close();
        }
    }
    public void testTwoFiles() throws IOException {
        createSequenceFile(dir, "d1", (byte) 0, 15);
        createSequenceFile(dir, "d2", (byte) 0, 114);
        CompoundFileWriter csw = new CompoundFileWriter(dir, "d.csf");
        csw.addFile("d1");
        csw.addFile("d2");
        csw.close();
        CompoundFileReader csr = new CompoundFileReader(dir, "d.csf");
        IndexInput expected = dir.openInput("d1");
        IndexInput actual = csr.openInput("d1");
        assertSameStreams("d1", expected, actual);
        assertSameSeekBehavior("d1", expected, actual);
        expected.close();
        actual.close();
        expected = dir.openInput("d2");
        actual = csr.openInput("d2");
        assertSameStreams("d2", expected, actual);
        assertSameSeekBehavior("d2", expected, actual);
        expected.close();
        actual.close();
        csr.close();
    }
    public void testRandomFiles() throws IOException {
        String segment = "test";
        int chunk = 1024; 
        createRandomFile(dir, segment + ".zero", 0);
        createRandomFile(dir, segment + ".one", 1);
        createRandomFile(dir, segment + ".ten", 10);
        createRandomFile(dir, segment + ".hundred", 100);
        createRandomFile(dir, segment + ".big1", chunk);
        createRandomFile(dir, segment + ".big2", chunk - 1);
        createRandomFile(dir, segment + ".big3", chunk + 1);
        createRandomFile(dir, segment + ".big4", 3 * chunk);
        createRandomFile(dir, segment + ".big5", 3 * chunk - 1);
        createRandomFile(dir, segment + ".big6", 3 * chunk + 1);
        createRandomFile(dir, segment + ".big7", 1000 * chunk);
        createRandomFile(dir, "onetwothree", 100);
        createRandomFile(dir, segment + ".notIn", 50);
        createRandomFile(dir, segment + ".notIn2", 51);
        CompoundFileWriter csw = new CompoundFileWriter(dir, "test.cfs");
        final String data[] = new String[] {
            ".zero", ".one", ".ten", ".hundred", ".big1", ".big2", ".big3",
            ".big4", ".big5", ".big6", ".big7"
        };
        for (int i=0; i<data.length; i++) {
            csw.addFile(segment + data[i]);
        }
        csw.close();
        CompoundFileReader csr = new CompoundFileReader(dir, "test.cfs");
        for (int i=0; i<data.length; i++) {
            IndexInput check = dir.openInput(segment + data[i]);
            IndexInput test = csr.openInput(segment + data[i]);
            assertSameStreams(data[i], check, test);
            assertSameSeekBehavior(data[i], check, test);
            test.close();
            check.close();
        }
        csr.close();
    }
    private void setUp_2() throws IOException {
        CompoundFileWriter cw = new CompoundFileWriter(dir, "f.comp");
        for (int i=0; i<20; i++) {
            createSequenceFile(dir, "f" + i, (byte) 0, 2000);
            cw.addFile("f" + i);
        }
        cw.close();
    }
    public void testReadAfterClose() throws IOException {
        demo_FSIndexInputBug(dir, "test");
    }
    private void demo_FSIndexInputBug(Directory fsdir, String file)
    throws IOException
    {
        IndexOutput os = fsdir.createOutput(file);
        for(int i=0; i<2000; i++) {
            os.writeByte((byte) i);
        }
        os.close();
        IndexInput in = fsdir.openInput(file);
        in.readByte();
        in.close();
        in.readByte();
        in.seek(1099);
        try {
            in.readByte();
            fail("expected readByte() to throw exception");
        } catch (IOException e) {
        }
    }
    static boolean isCSIndexInput(IndexInput is) {
        return is instanceof CompoundFileReader.CSIndexInput;
    }
    static boolean isCSIndexInputOpen(IndexInput is) throws IOException {
        if (isCSIndexInput(is)) {
            CompoundFileReader.CSIndexInput cis =
            (CompoundFileReader.CSIndexInput) is;
            return _TestHelper.isSimpleFSIndexInputOpen(cis.base);
        } else {
            return false;
        }
    }
    public void testClonedStreamsClosing() throws IOException {
        setUp_2();
        CompoundFileReader cr = new CompoundFileReader(dir, "f.comp");
        IndexInput expected = dir.openInput("f11");
        assertTrue(_TestHelper.isSimpleFSIndexInput(expected));
        assertTrue(_TestHelper.isSimpleFSIndexInputOpen(expected));
        IndexInput one = cr.openInput("f11");
        assertTrue(isCSIndexInputOpen(one));
        IndexInput two = (IndexInput) one.clone();
        assertTrue(isCSIndexInputOpen(two));
        assertSameStreams("basic clone one", expected, one);
        expected.seek(0);
        assertSameStreams("basic clone two", expected, two);
        one.close();
        assertTrue("Only close when cr is closed", isCSIndexInputOpen(one));
        expected.seek(0);
        two.seek(0);
        assertSameStreams("basic clone two/2", expected, two);
        cr.close();
        assertFalse("Now closed one", isCSIndexInputOpen(one));
        assertFalse("Now closed two", isCSIndexInputOpen(two));
        expected.seek(0);
        two.seek(0);
        two.close();
        expected.seek(0);
        two.seek(0);
        expected.close();
    }
    public void testRandomAccess() throws IOException {
        setUp_2();
        CompoundFileReader cr = new CompoundFileReader(dir, "f.comp");
        IndexInput e1 = dir.openInput("f11");
        IndexInput e2 = dir.openInput("f3");
        IndexInput a1 = cr.openInput("f11");
        IndexInput a2 = dir.openInput("f3");
        e1.seek(100);
        a1.seek(100);
        assertEquals(100, e1.getFilePointer());
        assertEquals(100, a1.getFilePointer());
        byte be1 = e1.readByte();
        byte ba1 = a1.readByte();
        assertEquals(be1, ba1);
        e2.seek(1027);
        a2.seek(1027);
        assertEquals(1027, e2.getFilePointer());
        assertEquals(1027, a2.getFilePointer());
        byte be2 = e2.readByte();
        byte ba2 = a2.readByte();
        assertEquals(be2, ba2);
        assertEquals(101, e1.getFilePointer());
        assertEquals(101, a1.getFilePointer());
        be1 = e1.readByte();
        ba1 = a1.readByte();
        assertEquals(be1, ba1);
        e1.seek(1910);
        a1.seek(1910);
        assertEquals(1910, e1.getFilePointer());
        assertEquals(1910, a1.getFilePointer());
        be1 = e1.readByte();
        ba1 = a1.readByte();
        assertEquals(be1, ba1);
        assertEquals(1028, e2.getFilePointer());
        assertEquals(1028, a2.getFilePointer());
        be2 = e2.readByte();
        ba2 = a2.readByte();
        assertEquals(be2, ba2);
        e2.seek(17);
        a2.seek(17);
        assertEquals(17, e2.getFilePointer());
        assertEquals(17, a2.getFilePointer());
        be2 = e2.readByte();
        ba2 = a2.readByte();
        assertEquals(be2, ba2);
        assertEquals(1911, e1.getFilePointer());
        assertEquals(1911, a1.getFilePointer());
        be1 = e1.readByte();
        ba1 = a1.readByte();
        assertEquals(be1, ba1);
        e1.close();
        e2.close();
        a1.close();
        a2.close();
        cr.close();
    }
    public void testRandomAccessClones() throws IOException {
        setUp_2();
        CompoundFileReader cr = new CompoundFileReader(dir, "f.comp");
        IndexInput e1 = cr.openInput("f11");
        IndexInput e2 = cr.openInput("f3");
        IndexInput a1 = (IndexInput) e1.clone();
        IndexInput a2 = (IndexInput) e2.clone();
        e1.seek(100);
        a1.seek(100);
        assertEquals(100, e1.getFilePointer());
        assertEquals(100, a1.getFilePointer());
        byte be1 = e1.readByte();
        byte ba1 = a1.readByte();
        assertEquals(be1, ba1);
        e2.seek(1027);
        a2.seek(1027);
        assertEquals(1027, e2.getFilePointer());
        assertEquals(1027, a2.getFilePointer());
        byte be2 = e2.readByte();
        byte ba2 = a2.readByte();
        assertEquals(be2, ba2);
        assertEquals(101, e1.getFilePointer());
        assertEquals(101, a1.getFilePointer());
        be1 = e1.readByte();
        ba1 = a1.readByte();
        assertEquals(be1, ba1);
        e1.seek(1910);
        a1.seek(1910);
        assertEquals(1910, e1.getFilePointer());
        assertEquals(1910, a1.getFilePointer());
        be1 = e1.readByte();
        ba1 = a1.readByte();
        assertEquals(be1, ba1);
        assertEquals(1028, e2.getFilePointer());
        assertEquals(1028, a2.getFilePointer());
        be2 = e2.readByte();
        ba2 = a2.readByte();
        assertEquals(be2, ba2);
        e2.seek(17);
        a2.seek(17);
        assertEquals(17, e2.getFilePointer());
        assertEquals(17, a2.getFilePointer());
        be2 = e2.readByte();
        ba2 = a2.readByte();
        assertEquals(be2, ba2);
        assertEquals(1911, e1.getFilePointer());
        assertEquals(1911, a1.getFilePointer());
        be1 = e1.readByte();
        ba1 = a1.readByte();
        assertEquals(be1, ba1);
        e1.close();
        e2.close();
        a1.close();
        a2.close();
        cr.close();
    }
    public void testFileNotFound() throws IOException {
        setUp_2();
        CompoundFileReader cr = new CompoundFileReader(dir, "f.comp");
        try {
            cr.openInput("bogus");
            fail("File not found");
        } catch (IOException e) {
        }
        cr.close();
    }
    public void testReadPastEOF() throws IOException {
        setUp_2();
        CompoundFileReader cr = new CompoundFileReader(dir, "f.comp");
        IndexInput is = cr.openInput("f2");
        is.seek(is.length() - 10);
        byte b[] = new byte[100];
        is.readBytes(b, 0, 10);
        try {
            is.readByte();
            fail("Single byte read past end of file");
        } catch (IOException e) {
        }
        is.seek(is.length() - 10);
        try {
            is.readBytes(b, 0, 50);
            fail("Block read past end of file");
        } catch (IOException e) {
        }
        is.close();
        cr.close();
    }
    public void testLargeWrites() throws IOException {
        IndexOutput os = dir.createOutput("testBufferStart.txt");
        byte[] largeBuf = new byte[2048];
        for (int i=0; i<largeBuf.length; i++) {
            largeBuf[i] = (byte) (Math.random() * 256);
        }
        long currentPos = os.getFilePointer();
        os.writeBytes(largeBuf, largeBuf.length);
        try {
            assertEquals(currentPos + largeBuf.length, os.getFilePointer());
        } finally {
            os.close();
        }
    }
}
